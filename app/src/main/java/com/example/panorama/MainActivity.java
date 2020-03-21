package com.example.panorama;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.panorama.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Native;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.opencv.core.Mat;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

import org.opencv.android.Utils;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import me.aflak.ezcam.EZCam;
import me.aflak.ezcam.EZCamCallback;

public class MainActivity extends Activity implements EZCamCallback, View.OnLongClickListener{
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("MyLib");
    }

    private TextureView textureView;
    private EZCam cam;
    private SimpleDateFormat dateFormat;

    private final String TAG = "CAM";

    private List<Image> images = new ArrayList<Image>();
    private List<Mat> listImage = new ArrayList<>(); // to store the images to be stitched

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private Sensor rotationVectorSensor;
    private SensorEventListener gyroscopeEventListener;
    private SensorEventListener rotationVectorEventListener;
    private final float[] rotationMatrix = new float[9];
//    private MatVector imgs = new MatVector();
    private List<Bitmap> bitmapImgs = new ArrayList<Bitmap>();
    private long ti;
    private float angle = 0;
    private long somme = 0;
    private ProgressDialog ringProgressDialog;
    ProgressBar loading;

    // Variables
    private float azimuth;
    private float pitch;
    private float roll;

    private int nombre_photo = 0;
    private boolean isMultipleOf15 = false;
    private boolean takeone = true;
    private int quinze = 15;
    private static int pic_number_to_take = 4;
    private int clicked_once = 0;

    public static int x, y;
    public int x_init=60;
    public int y_init=80;
    private static int x_final=0, y_final=0, dx=0, dy=0;
    private TextView txtlist, xValue, yValue, zValue, notif;

    private ImageView spot_left, spot_right;
    private Button startButton, stitchingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boule);

        textureView = (TextureView) findViewById(R.id.textureView);
        loading = findViewById(R.id.loading);
        dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());

        /**
         * Obtenir les éléments de l'interface
         */
        startButton = (Button)findViewById(R.id.startButton);
        stitchingButton = (Button)findViewById(R.id.stitchingButton);
        textureView = (TextureView)findViewById(R.id.texture);
        txtlist = (TextView)findViewById(R.id.sensorslist);
        xValue = (TextView)findViewById(R.id.xValue);
        yValue = (TextView)findViewById(R.id.yValue);
        zValue = (TextView)findViewById(R.id.zValue);
        notif = (TextView)findViewById(R.id.notif);
        spot_left = (ImageView)findViewById(R.id.spot_left);
        spot_right = (ImageView)findViewById(R.id.spot_right);

//        cam = new EZCam(this);
//        cam.setCameraCallback(this);

//        String id = cam.getCamerasList().get(CameraCharacteristics.LENS_FACING_BACK);
//        cam.selectCamera(id);

        Dexter.withActivity(MainActivity.this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
//                cam.open(CameraDevice.TEMPLATE_PREVIEW, textureView);
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Log.e(TAG, "permission denied");
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Gyroscope listener
        gyroscopeEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float axisX = event.values[0]; // Pitch value
                float axisY = event.values[1]; // roll acceleration value in rad/s
                float axisZ = event.values[2];

                long diff = Calendar.getInstance().getTimeInMillis() - ti;

                roll += (axisY * diff) / 1000;
                pitch += (axisX * diff) / 1000;

                ti = Calendar.getInstance().getTimeInMillis(); // get the initial time

                if((int)Math.toDegrees(pitch) >= -3 && (int)Math.toDegrees(pitch) <= 3 && takeone == false) {
                    x_final = (int) spot_right.getX();
                    y_final = (int) spot_right.getY();

                    //Toast.makeText(MainActivity.this, "x_final = " + Float.toString(x_final ), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(MainActivity.this, "y_final = " + Float.toString(y_final), Toast.LENGTH_SHORT).show();
                    y = y_init;
                    x = x_init;
                    dx = (int)(x_final/15);
                    //Toast.makeText(MainActivity.this, "dx = " + Float.toString(dx ), Toast.LENGTH_SHORT).show();
                    if ((-1) * (int) (Math.toDegrees(roll)) / quinze == 1 && (int) (Math.toDegrees(roll)) < 0 && nombre_photo < pic_number_to_take) {
                        // do something
                        Toast.makeText(MainActivity.this, "Photo: " + (int)Math.toDegrees(roll) + "° Prise", Toast.LENGTH_SHORT).show();
                        isMultipleOf15 = true;
                        if (isMultipleOf15 == true) {
                            // Take picture

                            angle += 15;
                            roll = 0;
                            isMultipleOf15 = false;
                        }
                        if (angle > 60) {
                            Toast.makeText(MainActivity.this, "Les " + nombre_photo + " photos capturés avec succès.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }
                xValue.setText("Pitch: " + (int) Math.toDegrees(pitch));
                yValue.setText("Roll: " + (int) Math.toDegrees(roll)*(-1));
                //zValue.setText("Z: " + axisZ);

                // x = x_init + (int) Math.toDegrees(roll) * (-10) * 8;
                // y = y_init + (int) Math.toDegrees(pitch) * 10;

                // x = x_init + pas à avoir 15°
                x = x_init + (int) Math.toDegrees(roll) * -dx;
                y = y_init + (int) Math.toDegrees(pitch) * 10;
//
                spot_left.setY(y);
                spot_left.setX(x);

                Log.i("positionspotx", Float.toString(spot_left.getX()));
                Log.i("positionspoty", Float.toString(spot_left.getY()));
//                float value = event.values[1]; // value in radians of roll acceleration
//
//                angle += value * diff / 1000;
//                Log.i("abcd", Float.toString((float) Math.toDegrees(angle)));
//                ti = Calendar.getInstance().getTimeInMillis(); // get the initial time
//
//                ProgressBar progressBar2 = findViewById(R.id.progressBar2);
//                progressBar2.setProgress((int) Math.abs(Math.toDegrees(angle)));
//                // 15 degrees detection
//                /*
//                    When rotating to the right, the angle decreases
//                 */
//                if(Math.abs(Math.toDegrees(angle)) > 15){
//                    loading.setVisibility(View.VISIBLE);
//
//                    cam.takePicture();
//                    progressBar2.getProgressDrawable().setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
//                    progressBar2.setProgress(0);
//                    angle = 0;
//                    progressBar2.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
//                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        // Rotation vector listener
        rotationVectorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                float [] result = new float[9];
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                SensorManager.getOrientation(rotationMatrix, result);

                convertToDegrees(result);

//                ProgressBar progressBar = findViewById(R.id.progressBar);
//                progressBar.setProgress((int) Math.abs(result[1]));
//
//                if(progressBar.getProgress() >= 80){
//                    progressBar.getProgressDrawable().setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
//                }else{
//                    progressBar.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
//                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

    }

    @Override
    public void onResume() {

        super.onResume();
        sensorManager.registerListener(rotationVectorEventListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(gyroscopeEventListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);

        ti = Calendar.getInstance().getTimeInMillis(); // Initialize the time for calculating angle with gyroscope
    }

    @Override
    public boolean onLongClick(View v) {
        cam.takePicture();
        return false;
    }

    @Override
    public void onCameraReady() {
        cam.setCaptureSetting(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY);
        cam.startPreview();

        textureView.setOnLongClickListener(this);
    }

    public void stitch(View v){
        Context context = this.getBaseContext();
        String p = context.getExternalFilesDir(null).getAbsolutePath();
        // OpenCV
        try {
            // Create a long array to store all image address
            int elems = listImage.size();
            long[] tempobjadr = new long[elems];
//
//
            for (int i = 0; i < elems; i++) {
                tempobjadr[i] = listImage.get(i).getNativeObjAddr();
            }
//            // Create a Mat to store the final panorama image
//            Mat result = new Mat();
//            // Call the OpenCV C++ Code to perform stitching process
//            NativePanorama.processPanorama(tempobjadr, result.getNativeObjAddr());

            // Trying with 6 by 6
            int n_groups = 2;
            int n_in_group = 2;
//
            long[][] allTempObjAdr = new long[n_groups][n_in_group];
            Mat[] results = new Mat[n_groups];
            long[] resultsAdr = new long[n_groups];

            Log.i("ADR", Arrays.toString(tempobjadr));
            for (int i = 0; i < n_groups; i++) {

                for (int j = 0; j < n_in_group; j++) {
                    Log.i("ADR", "a loop");
                    allTempObjAdr[i][j] = tempobjadr[n_in_group*i + j];
                }

            }

            // Initializing results
            for (int i = 0; i < results.length; i++) {
                results[i] = new Mat();
            }

            Log.i("ADR", "outside");
            for(int k=0; k < n_groups; k++){
                Log.i("ADR", "iteration");
                NativePanorama.processPanorama(allTempObjAdr[k], results[k].getNativeObjAddr());
            }

            // Stitching all groups
            for (int i = 0; i < n_groups; i++) {
                resultsAdr[i] = results[i].getNativeObjAddr();
            }
            Mat result = new Mat();
            NativePanorama.processPanorama(resultsAdr, result.getNativeObjAddr());
            final String fileName = p+ "/stitch.png";

//            final String fileName = "";
            Imgcodecs.imwrite(fileName, result);

            listImage.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPicture(Image image) {
        cam.stopPreview();
        String filename = "image_"+dateFormat.format(new Date())+".jpg";
        Context context = this.getBaseContext();
        String p = context.getExternalFilesDir(null).getAbsolutePath();
        Log.i("abcd", p);
        File file = new File(p, filename);


        // Testing JavaCV
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        MatOfByte inputframe = new MatOfByte(bytes);
        Mat result = Imgcodecs.imdecode(inputframe, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        listImage.add(result);
        TextView nbImages = findViewById(R.id.nbImages);
        nbImages.setText(Integer.toString(listImage.size()));
        image.close();

        Log.i("IMAGESIZE", Integer.toString(listImage.size()));
        cam.restartPreview();
        loading.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCameraDisconnected() {
        Log.e(TAG, "Camera disconnected");
    }

    @Override
    public void onError(String message) {
        Log.e(TAG, message);
    }

    @Override
    protected void onDestroy() {
        cam.close();
        super.onDestroy();
    }

    public void convertToDegrees(float[] vector){
        for (int i=0; i<vector.length; i++) vector[i] = Math.round(Math.toDegrees(vector[i]));
    }

    private void showProcessingDialog() {
        cam.stopPreview();
        ringProgressDialog = ProgressDialog.show(MainActivity.this, "", "Capturing...", true);
        ringProgressDialog.setCancelable(false);
    }

    private void closeProcessingDialog() {
        cam.startPreview();
        ringProgressDialog.dismiss();
    }
}
