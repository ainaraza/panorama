package com.example.panorama;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Point;
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
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

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
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.core.Mat;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

import org.opencv.android.Utils;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.w3c.dom.Text;

import me.aflak.ezcam.EZCam;
import me.aflak.ezcam.EZCamCallback;
import pl.pawelkleczkowski.customgauge.CustomGauge;

public class MainActivityIntegration extends Activity implements EZCamCallback, View.OnLongClickListener{
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
    public int x_init=70;
    public int y_init=60;
    private static int x_final=0, y_final=0, dx=0, dy=0;
    private TextView txtlist, xValue, yValue, zValue, notif, pitch_value_inside;

    private ImageView spot_left, spot_right;
    private Button startButton, stitchingButton;
    private int direction = 1;
    private double starting_pitch;
    private boolean started_pitch;

    // Status
    private boolean can_start = false, started = false;
    private TextView can_start_view;
    private boolean can_take_picture = false;
    private boolean taking_picture = false;

    /*** Debut Variables Integration design ***/
    private int counter = 1;
    private boolean isOk = false;

    private Handler mHandler = new Handler();

    private static final int REQUEST_IMAGE_CAPTURE = 101;

    BallView mBallView = null;
    DegreeView mDegreeView = null;

    android.graphics.PointF mBallPos, mBallSpd;
    int mScrWidth, mScrHeight;
    private float xMax, yMax, mBallPosZ, mBallSpdZ;
    Timer mTmr = null;
    TimerTask mTsk = null;
    Handler RedrawHandler = new Handler();

    private float rollV, pitchV;

    Mat mRGBA, mRGBAT;

    private CustomGauge gauge;// Declare this variable in your activity
    private int circleProgess = 4;
    private ImageView patern_image;

    private float initial_y = 100 + 82/2 - 5;
    private float initial_x = 60;
    private float final_y = 100 + 82/2 - 5;
    private float final_x = 60 + 435;
    /*** Fin Variables integration design ***/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_integration);

        textureView = (TextureView) findViewById(R.id.textureView);
        loading = findViewById(R.id.loading);
        dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());

        /**
         * Obtenir les éléments de l'interface
         */
        textureView = (TextureView)findViewById(R.id.textureView);
//        startButton = (Button)findViewById(R.id.startButton);
//        stitchingButton = (Button)findViewById(R.id.stitchingButton);
//        txtlist = (TextView)findViewById(R.id.sensorslist);
//        xValue = (TextView)findViewById(R.id.xValue);
//        yValue = (TextView)findViewById(R.id.yValue);
//        zValue = (TextView)findViewById(R.id.zValue);
//        notif = (TextView)findViewById(R.id.notif);
//        spot_left = (ImageView)findViewById(R.id.spot_left);
//        spot_right = (ImageView)findViewById(R.id.spot_right);
//        can_start_view = (TextView) findViewById(R.id.canstart);
//        pitch_value_inside = (TextView) findViewById(R.id.pitch_value_inside);


        cam = new EZCam(this);
        cam.setCameraCallback(this);

        String id = cam.getCamerasList().get(CameraCharacteristics.LENS_FACING_BACK);
        cam.selectCamera(id);

        Dexter.withActivity(MainActivityIntegration.this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                cam.open(CameraDevice.TEMPLATE_PREVIEW, textureView);
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

        /*** Integration ***/
        final FrameLayout mainView = (android.widget.FrameLayout) findViewById(R.id.niveau_view);

        Point size = new Point();
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(size);

        mScrWidth = size.x;
        mScrHeight = size.y;
        mBallPos = new android.graphics.PointF();
        mBallSpd = new android.graphics.PointF();

        xMax = (float) mScrWidth - 155;
        yMax = (float) 180;

        mBallPos.x = 60;
        mBallPos.y = mScrHeight/2;
        mBallSpd.x = 0;
        mBallSpd.y = 0;

        mBallView = new BallView(this,mBallPos.x,mBallPos.y, mBallPosZ,5);
        mDegreeView = new DegreeView(this, Float.toString(mBallPosZ), mBallPos.x,mBallPos.y, mBallPosZ,5);

        mainView.addView(mBallView);
        mainView.addView(mDegreeView);


        gauge = findViewById(R.id.gauge2);
        patern_image = findViewById(R.id.patern_image);


        mBallView.mY = initial_y;
        mBallView.mX = initial_x;
        mDegreeView.mY = initial_y;
        mDegreeView.mX = initial_x;

        /*** Fin integration ***/

        // Gyroscope listener
        gyroscopeEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float axisX = event.values[0]; // Pitch value
                float axisY = event.values[1]; // roll acceleration value in rad/s
                float axisZ = event.values[2];

                long diff = Calendar.getInstance().getTimeInMillis() - ti;

                if(started){
                    roll += (axisY * diff) / 1000;
                }
                pitch += (axisX * diff) / 1000;

                ti = Calendar.getInstance().getTimeInMillis(); // get the initial time

                // Doing the calculations
                dx = (int) ((final_x - initial_x)/15);
                Log.i("roll", String.valueOf(Math.toDegrees(roll)));
                if(! taking_picture){
                    if(started){
                        mBallView.mX = initial_x + (float) Math.toDegrees(roll) * (-dx);
                        mDegreeView.mX = initial_x + (float) Math.toDegrees(roll) * (-dx);

                    }

                    if(Math.abs(Math.abs(Math.toDegrees(roll)) - 15) < 0.3 && Math.abs(Math.toDegrees(pitch)) < 3){
                        taking_picture = true;
                        cam.takePicture();
                    }

                    mBallView.mY = initial_y - (float) Math.toDegrees(pitch) * 5;
                    mDegreeView.mY = initial_y - (float) Math.toDegrees(pitch) * 5;
                    mDegreeView.mText = Integer.toString((int) Math.toDegrees(pitch));
                }

                if(!started && can_start && (int) Math.toDegrees(pitch) == 0){
                    showProcessingDialog("Capturing...");
                    started = true;
                    taking_picture = true;
                    cam.takePicture();
                }

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

                if(!started_pitch){
                    pitch = (float) (-Math.PI/2 - result[1]);
                    started_pitch = true;
                }

//                mBallView.mY = patern_image.getY() + 82/2 - 5; // center the ball
//                mBallView.mX = 60 + 435;
                Log.i("POSY", String.valueOf(patern_image.getY()));
                Log.i("POSX", String.valueOf(patern_image.getX()));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

    }

    @Override
    public void onStart(){
        super.onStart();

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityIntegration.this);
        builder.setTitle("Prise de vue")
                .setMessage("Allez dans la première pièce et placez-vous au centre de la pièce.")
                .setCancelable(false)
                .setPositiveButton("J'ai compris", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] testItem = {"test1", "Test2", "Test3", "Test4"};
                        AlertDialog.Builder items = new AlertDialog.Builder(MainActivityIntegration.this);
                        items.setTitle("Test")
                                .setItems(testItem, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // The 'which' argument contains the index position
                                        // of the selected item
                                    }
                                });
                        AlertDialog showItem = items.create();
                        showItem.show();

                        can_start = true;
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent mainIntent = new Intent(MainActivityIntegration.this, MainActivity.class);
                        startActivity(mainIntent);
                    }
                });
        //Creating dialog box
        AlertDialog dialog  = builder.create();
        dialog.show();
    }

    @Override
    public void onResume() {

        super.onResume();
//        View bar2 = findViewById(R.id.bar2);
//        int[] positions_bar1 = new int[2];
//        bar2.getLocationInWindow(positions_bar1);


        //x_init = positions_bar1[0];
        //y_init = positions_bar1[1];
//        Log.i("positionbar", Arrays.toString(positions_bar1));
//        Log.i("positionleft", Float.toString(spot_right.getX()));
//        Log.i("positionleft", Float.toString(spot_right.getY()));
        sensorManager.registerListener(rotationVectorEventListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(gyroscopeEventListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);

        mTmr = new Timer();
        mTsk = new TimerTask() {
            public void run() {

                //redraw ball. Must run in background thread to prevent thread lock.
                RedrawHandler.post(new Runnable() {
                    public void run() {
                        mBallView.invalidate();
                        mDegreeView.invalidate();
                    }});
            }}; // TimerTask

        mTmr.schedule(mTsk,10,10); //start timer

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

    public void takePicture(View v){
        showProcessingDialog("Capturing...");
        if(!started){
            started = true;
            cam.takePicture();
        }else{
            cam.takePicture();
            taking_picture = true;
        }
    }

    public void stitch(View v){
        showProcessingDialog("Stitching en cours...");
        Context context = this.getBaseContext();
        String p = context.getExternalFilesDir(null).getAbsolutePath();
//        p = "/storage/sdcard0/a_stitching";
        // OpenCV
        try {
            // Create a long array to store all image address
            int elems = listImage.size();
            long[] tempobjadr = new long[elems];
            Log.i("LEN", Integer.toString(tempobjadr.length));
//
//
            for (int i = 0; i < elems; i++) {
                tempobjadr[i] = listImage.get(i).getNativeObjAddr();
            }
            // Create a Mat to store the final panorama image
            Mat result = new Mat();
            // Call the OpenCV C++ Code to perform stitching process
            NativePanorama.processPanorama(tempobjadr, result.getNativeObjAddr());

            // Second approach
//            int elems = listImage.size();
//            long[] tempobjadr = new long[elems];
//            Log.i("LEN", Integer.toString(tempobjadr.length));
//
//            long[] toBeStitched = new long[2];
//            toBeStitched[0] = listImage.get(0).getNativeObjAddr();
//            toBeStitched[1] = listImage.get(1).getNativeObjAddr();
//
//            Mat result = new Mat();
//            NativePanorama.processPanorama(toBeStitched, result.getNativeObjAddr());
//
//            for (int i = 2; i < elems; i++) {
//                toBeStitched[0] = result.getNativeObjAddr();
//                toBeStitched[1] = listImage.get(i).getNativeObjAddr();
//                NativePanorama.processPanorama(toBeStitched, result.getNativeObjAddr());
//            }

//            // Trying with 6 by 6
//            // Here we have a group of 2 by 2
//            // If we have 24 images, we must set it to be n_groups = 4 and n_in_group = 6
//            int n_groups = 2;
//            int n_in_group = 6;
//
//            long[][] allTempObjAdr = new long[n_groups][n_in_group];
//            Mat[] results = new Mat[n_groups];
//            long[] resultsAdr = new long[n_groups];
//
//            Log.i("ADR", Arrays.toString(tempobjadr));
//            for (int i = 0; i < n_groups; i++) {
//
//                for (int j = 0; j < n_in_group; j++) {
//                    Log.i("ADR", "a loop");
//                    allTempObjAdr[i][j] = tempobjadr[n_in_group*i + j];
//                }
//
//            }
//
//            // Initializing results
//            for (int i = 0; i < results.length; i++) {
//                results[i] = new Mat();
//            }
//
//            Log.i("ADR", "outside");
//            for(int k=0; k < n_groups; k++){
//                Log.i("ADR", "iteration");
//                NativePanorama.processPanorama(allTempObjAdr[k], results[k].getNativeObjAddr());
//            }
//
//            // Stitching all groups
//            for (int i = 0; i < n_groups; i++) {
//                resultsAdr[i] = results[i].getNativeObjAddr();
//            }
//
//            Mat result = new Mat();
//            NativePanorama.processPanorama(resultsAdr, result.getNativeObjAddr());

            // Not changing
            final String fileName = p+ "/stitch.png";

//            final String fileName = "";
            Imgcodecs.imwrite(fileName, result);

            listImage.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeProcessingDialog();
    }

    @Override
    public void onPicture(Image image) {
        closeProcessingDialog();
        Toast.makeText(getApplicationContext(), "Taken picture " + Integer.toString(listImage.size() + 1), Toast.LENGTH_SHORT).show();

        cam.stopPreview();


        // Testing JavaCV
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        MatOfByte inputframe = new MatOfByte(bytes);
        Mat result = Imgcodecs.imdecode(inputframe, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        listImage.add(result);
//        TextView nbImages = findViewById(R.id.nbImages);
//        xValue.setText(Integer.toString(listImage.size()));
        image.close();

        Log.i("IMAGESIZE", Integer.toString(listImage.size()));
        cam.restartPreview();

        taking_picture = false; // finished
        roll = 0;
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

    private void showProcessingDialog(String m) {
        cam.stopPreview();
        ringProgressDialog = ProgressDialog.show(MainActivityIntegration.this, "", m, true);
        ringProgressDialog.setCancelable(false);
    }

    private void closeProcessingDialog() {
        cam.startPreview();
        ringProgressDialog.dismiss();
    }
}
