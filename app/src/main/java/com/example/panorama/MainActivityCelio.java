package com.example.panorama;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;

import android.media.Image;
import android.media.ImageReader;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;

import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.nio.ByteBuffer;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class MainActivityCelio extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "MainActivity";

    /**
     * Vue de activity_main.xml
     */
    private TextView commonMessageTextView;
    private Button startButton;
    private Button stitchingButton;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    /**
     * COnfiguration d'orientation
     */
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Load Library
     */
    static {
        //If you use OpenCV 2.*, use "opencv_java"
        System.loadLibrary("opencv_java3");
        System.loadLibrary("MyLib");
    }

    /**
     * Android Hardware
     */
    private String cameraId;
    CameraDevice cameraDevice;
    CameraManager cameraManager;
    CameraCaptureSession cameraCaptureSession;
    CaptureRequest captureRequest;
    CaptureRequest.Builder captureRequestBuilder;

    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    Handler mBackgroundHandler;
    HandlerThread mBackgroundThread;

    /**
     * Les variables pour le Sensor
     */

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor magneticSensor;
    private SensorEventListener accelerometerEventListener;
    private float[] accelerometerReading = new float[3];
    private float[] magnetickReading = new float[3];

    // Gravity rotational data
    private float gravity[];
    // Magnetic rotational data
    private float magnetic[]; //for magnetic rotational data
    private float accels[] = new float[3];
    private float mags[] = new float[3];
    private float[] values = new float[3];

    // time calendar
    private long ti;

    // azimuth, pitch and roll
    private float azimuth;
    private float pitch;
    private float roll;

    /** Variable to use */
    private int angle = 0;
    private int nombre_photo = 0;
    private int quinze = 15;
    private int clicked_once = 0;
    public int x_init;
    public int y_init;

    private boolean isMultipleOf15 = false;
    private boolean takeone = true;
    private boolean startClicked = false;
    public boolean pitchIsOK = false;

    private static int pic_number_to_take = 24;
    private static int final_angle = 360;
    public static int x, y;
    public static int width;
    public static int height;
    private static int x_final = 0, y_final = 0, dx = 0;
    private static final int FROM_RADS_TO_DEGS = -57;

    /**
     * Acitivty_main variable
     */
    private TextView txtlist, xValue, yValue, zValue, notif, notif2;
    private ImageView spot_left, spot_right;

    /**
     * OpenCV
     */

    private List<Mat> listImage = new ArrayList<>();
    List<Bitmap> bmList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE); //hide title bar
        //set app to full screen and keep screen on
        getWindow().setFlags(0xFFFFFFFF, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_celio);
        MainActivityCelio activity = this;

        final RelativeLayout mainView = (android.widget.RelativeLayout)findViewById(R.id.main_layout);

        Log.d(TAG, "onCreate: Initializing Sensor service");

        /**
         * Obtenir les éléments de l'interface
         */
        startButton = (Button) findViewById(R.id.startButton);
        stitchingButton = (Button) findViewById(R.id.stitchingButton);
        textureView = (TextureView) findViewById(R.id.texture);
        txtlist = (TextView) findViewById(R.id.sensorslist);
        xValue = (TextView) findViewById(R.id.xValue);
        yValue = (TextView) findViewById(R.id.yValue);
        zValue = (TextView) findViewById(R.id.zValue);
        notif = (TextView) findViewById(R.id.notif);
        notif2 = (TextView) findViewById(R.id.notif2);
        spot_left = (ImageView) findViewById(R.id.spot_left);
        spot_right = (ImageView) findViewById(R.id.spot_right);

        Display mDisplay = activity.getWindowManager().getDefaultDisplay();
        width = mDisplay.getWidth();
        height = mDisplay.getHeight();

        /**
         * Sensor data
         */
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Log.d(TAG, "onCreate: Registered Sensor service");

        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        StringBuilder stringBuilder = new StringBuilder();
        for (Sensor s : sensorList) {
            stringBuilder.append(s.getName() + "\n");
        }
        //txtlist.setVisibility(View.VISIBLE);
        //txtlist.setText(stringBuilder);

        // Test de Présence du Sensor
        if (accelerometerSensor == null) {
            Toast.makeText(this, "Device has no Accelerometer Sensor", Toast.LENGTH_LONG).show();
        }
        if (magneticSensor == null) {
            Toast.makeText(this, "Device has no Magnetic-Field Sensor", Toast.LENGTH_LONG).show();
            //spot_left.setY(y_init);
            //spot_left.setX(x_init);
        }

        notif.setText("Captuer la première image avec le bouton START");

        // Function
        /**
         * Start button function
         */
        /*
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clicked_once += 1;
                if (takeone == true) {
                    Toast.makeText(MainActivity.this, "Capture du premier image", Toast.LENGTH_SHORT).show();
                    try {
                        takePicture();
                        Toast.makeText(MainActivity.this, "Capture du premier image faite.", Toast.LENGTH_SHORT).show();
                        nombre_photo += 1;
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    takeone = false;
                }
                if (v.getId() == R.id.startButton) {
                    v.setBackgroundResource(R.drawable.start_green);
                    notif.setText("Utiliser la Bille verte pour la suite!");
                }
            }
        });*/

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clicked_once += 1;
                if (takeone == true && pitchIsOK == true) {
                    //Toast.makeText(MainActivity.this, "Capture du premier image", Toast.LENGTH_SHORT).show();
                    try {
                        takePicture();
                        Toast.makeText(MainActivityCelio.this, "Capture du premier image faite", Toast.LENGTH_SHORT).show();
                        nombre_photo = 1;
                        angle = 15;

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    takeone = false;
                    startClicked = true;

                    //if (v.getId() == R.id.startButton) {
                    //v.setBackgroundResource(R.drawable.start_green);
                    //
                    //}
                    notif.setText("Utiliser la Bille verte pour la suite!");
                }

                if (clicked_once > 1) {
                    Toast.makeText(MainActivityCelio.this, "Première capture déjà faite, n'appuyez plus ce bouton. Utilisez la jauge du bille vers vers la bille blanche", Toast.LENGTH_LONG).show();
                }
            }
        });

        /**
         * Save and Sticthing button
         */
        stitchingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //showImg(file, bmList);
                    // Create a long array to store all image address

                    int elems = listImage.size();
                    if (elems <= 1 || elems < pic_number_to_take) {
                        Toast.makeText(MainActivityCelio.this, "Images taken: 0 or 1 (Min: " + pic_number_to_take + ")", Toast.LENGTH_SHORT).show();
                        //finish();
                    } else {
                        long[] tempobjadr = new long[elems];

                        for (int i = 0; i < elems; i++) {
                            tempobjadr[i] = listImage.get(i).getNativeObjAddr();
                            //Log.d(TAG, "Res addr[" + i + "] = " + tempobjadr[i]);
                            //Toast.makeText(MainActivity.this, "Res addr[" + i + "] = " + tempobjadr[i], Toast.LENGTH_SHORT).show();
                        }
                        // Create a Mat to store the final panorama image
                        Mat result = new Mat();

                        // Call the OpenCV C++ Code to perform stitching process
//                        CvUtil.processMat(tempobjadr, result.getNativeObjAddr());
                        // Save the image to external storage

                        File sdcard = Environment.getExternalStorageDirectory();
                        final String fileName = sdcard.getAbsolutePath() + "/opencv_" + System.currentTimeMillis() +
                                ".bmp";
                        Imgcodecs.imwrite(fileName, result);

                        FileInputStream streamIn = new FileInputStream(fileName);
                        Bitmap bm = BitmapFactory.decodeStream(streamIn);

                        ImageView imageView = (ImageView) findViewById(R.id.img_view);
                        imageView.setImageBitmap(bm);

                        listImage.clear();
                        Toast.makeText(MainActivityCelio.this, "Stitching ok", Toast.LENGTH_SHORT).show();
                        notif.setText("Panorama sauvé sous sdCard/opencv_......bmp");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Mat img = readImageFromResources();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                mags = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accels = event.values.clone();
                break;
        }

        /*notif.setText("xR: " + (int)spot_right.getX() + " <--> yR: " + (int)spot_right.getY() +
                " et x_i: " + (width-(int)spot_right.getX()) + ", y_i: " + (height-(int)spot_right.getX()));*/
        //notif.setText("x_init: " + x_init + ", y_init: " + y_init);

        if (mags != null && accels != null) {
            gravity = new float[9];
            magnetic = new float[9];
            SensorManager.getRotationMatrix(gravity, magnetic, accels, mags);
            float[] outGravity = new float[9];
            SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_X,SensorManager.AXIS_Z, outGravity);
            SensorManager.getOrientation(outGravity, values);
            //SensorManager.getOrientation(gravity, values);

            //azimuth = values[0] * 57.2957795f;
            azimuth = values[0];
            float axisX = values[1]; /** PITCH  value*/
            float axisY = values[2]; /** ROLL value */

            //notif2.setText("(axisX, axisY, axisZ) = (" + axisX + ", " + axisY + ", " + azimuth + ")");

            mags = null;
            accels = null;

            pitch = axisX;
            roll = axisY;

            xValue.setText("Pitch: " + (int)Math.toDegrees(pitch));

            yValue.setText("Roll: " +  (-1 * (int)Math.toDegrees(roll)));

            zValue.setText("Azimuth: " + Math.toDegrees(azimuth));
            
            /** x = x_init + pas à avoir 15° */
            x = x_init + (int) Math.toDegrees(roll)*(-dx);
            y = y_init + (int) Math.toDegrees(pitch);

            //spot_left.setContentDescription((-1 * (int)Math.toDegrees(roll)) + "°");
            spot_left.setY(y);
            spot_left.setX(x);

            //notif.setText("xLeft, yLeft = " + spot_left.getX() + ", " + spot_left.getY());
        }
    }

    private Mat readImageFromResources() {
        Mat img = null;
        try {
            img = Utils.loadResource(this, R.drawable.start_green, Imgcodecs.CV_LOAD_IMAGE_COLOR);
            Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2BGRA);
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return img;
    }

    private void showImg(File file, List<Bitmap> bmList) throws IOException {
        //Bitmap bm = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
        //Utils.matToBitmap(img, bm);
        FileInputStream streamIn = new FileInputStream(file);
        Bitmap bm = BitmapFactory.decodeStream(streamIn);

        ImageView imageView = (ImageView) findViewById(R.id.img_view);
        for (Bitmap bml : bmList) {
            imageView.setImageBitmap(bml);
        }
        listImage = prepareData(bmList);
        Toast.makeText(getApplicationContext(), "Nombre de photo prise: " + listImage.size(), Toast.LENGTH_SHORT).show();
        streamIn.close();
    }

    private List<Mat> prepareData(List<Bitmap> bitmap) {
        // convert the captured Bitmap into an OpenCV Mat and store in this listImage list
        Mat mat = new Mat();
        for (Bitmap bm : bitmap) {
            Utils.bitmapToMat(bm, mat);
            listImage.add(mat);
        }
        return listImage;
    }

    private void addFileToBitmapList(byte[] data) throws IOException {
        // Decode byte array to Bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        // Rotate the picture to fit portrait mode
        Matrix matrix = new Matrix();
        //matrix.postRotate(90);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

        // OpenCV
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        listImage.add(mat);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(getApplicationContext(), "Permission de la Camera Nécessaire", Toast.LENGTH_LONG).show();
            }
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            try {
                createCameraPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int i) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreview() throws CameraAccessException {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());

        Surface surface = new Surface(texture);
        captureRequestBuilder = cameraDevice.createCaptureRequest(cameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(surface);

        cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(CameraCaptureSession session) {
                if (cameraDevice == null) {
                    return;
                }
                cameraCaptureSession = session;
                try {
                    updatePreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(CameraCaptureSession session) {
                Toast.makeText(getApplicationContext(), "Configuration changed", Toast.LENGTH_LONG).show();
            }
        }, null);
    }

    private void updatePreview() throws CameraAccessException {
        if (cameraDevice == null) return;
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
    }

    private void openCamera() throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        // 0 for rear camera
        cameraId = cameraManager.getCameraIdList()[0];
        CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivityCelio.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            return;
        }
        cameraManager.openCamera(cameraId, stateCallback, null);
    }

    private void takePicture() throws CameraAccessException, IOException {
        if (cameraDevice == null) {
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
        Size[] jpegSizes = null;

        jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

        int width = 640;
        int height = 480;

        if (jpegSizes != null && jpegSizes.length > 0) {
            width = jpegSizes[0].getWidth();
            height = jpegSizes[0].getHeight();
        }

        ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
        List<Surface> outputSurfaces = new ArrayList<>(2);
        outputSurfaces.add(reader.getSurface());

        outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

        final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(reader.getSurface());
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

        // timestamp
        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();

        // Fichier et dossier
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/Pictures");
        file = new File(Environment.getExternalStorageDirectory() + "/Pictures/pano_" + ts + ".jpg");


        ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = null;
                image = reader.acquireLatestImage();

                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                try {
                    save(bytes);
                    addFileToBitmapList(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (image != null) {
                        image.close();
                    }
                }
            }
        };
        reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

        final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                //Toast.makeText(getApplicationContext(), "Image " + nombre_photo + "/" + pic_number_to_take + " Sauvé", Toast.LENGTH_SHORT).show();
                try {
                    createCameraPreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        };

        cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(CameraCaptureSession session) {
                try {
                    session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(CameraCaptureSession session) {

            }
        }, mBackgroundHandler);

    }

    private void save(byte[] bytes) throws IOException {
        OutputStream outputStream = null;
        outputStream = new FileOutputStream(file);
        outputStream.write(bytes);
        outputStream.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);

        if (textureView.isAvailable()) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);

        try {
            stopBackgroundThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    protected void stopBackgroundThread() throws InterruptedException {
        mBackgroundThread.quitSafely();
        mBackgroundThread.join();
        mBackgroundThread = null;
    }
}