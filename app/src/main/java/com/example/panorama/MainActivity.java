package com.example.panorama;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
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

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;

import android.view.Display;
import android.view.Surface;
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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import me.aflak.ezcam.EZCam;
import pl.pawelkleczkowski.customgauge.CustomGauge;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {
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

    private static final int ANGLE = 90;

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("MyLib");
    }

    private EZCam cam;
    private SimpleDateFormat dateFormat;


    private List<Image> images = new ArrayList<Image>();

    private Sensor gyroscopeSensor;
    private Sensor rotationVectorSensor;
    private SensorEventListener gyroscopeEventListener;
    private SensorEventListener rotationVectorEventListener;
    private final float[] rotationMatrix = new float[9];
    //    private MatVector imgs = new MatVector();
    private List<Bitmap> bitmapImgs = new ArrayList<Bitmap>();
    private long somme = 0;
    private ProgressDialog ringProgressDialog;
    ProgressBar loading;

    // Variables
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
    private int progression = -15;
    private boolean can_stitch = false;

    // Variables for the async stitching
    private int imageDispo = -1; // index in list of the image that is available (using onPicture)
    private int imageWaitedFor = 1; // image waited for the Async Stitching Task
    private Mat finalResult; // the final result that will store the stitched panorama
    private int start_pitch_count = 5;

    /*** Fin Variables integration design ***/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(0xFFFFFFFF, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity activity = this;

        /**
         * Obtenir les éléments de l'interface
         */

        textureView = (TextureView) findViewById(R.id.textureView);
        loading = findViewById(R.id.loading);

        Display mDisplay = activity.getWindowManager().getDefaultDisplay();
        width = mDisplay.getWidth();
        height = mDisplay.getHeight();

        dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());

        listImage = new ArrayList<>();

        gauge = findViewById(R.id.gauge2);
        patern_image = findViewById(R.id.patern_image);

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

        float patern_image_y = patern_image.getY();
        float patern_image_x = patern_image.getX();

//        initial_y = patern_image_y + 82/2 - 5;
//        initial_x = -patern_image_x;
//        final_y = patern_image_y + 82/2 - 5;
//        final_x = -patern_image_x + 435;

        Log.i("POSY", String.valueOf(patern_image_y));
        Log.i("POSX", String.valueOf(patern_image_x));
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
                        if(progression == ANGLE - 15){
                            showProcessingDialog("Capturing and stitching...");
                        }else{
                            showProcessingDialog("Capturing...");
                        }
                        taking_picture = true;

                        // Changing with Async
//                        cam.takePicture();
                        try {
                            takePictureEssai();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                    mBallView.mY = initial_y - (float) Math.toDegrees(pitch) * 5;
                    mDegreeView.mY = initial_y - (float) Math.toDegrees(pitch) * 5;
                    mDegreeView.mText = Integer.toString((int) Math.toDegrees(pitch));
                }

                if(!started && can_start && (int) Math.toDegrees(pitch) == 0){
                    showProcessingDialog("Capturing...");
                    started = true;
                    // Changing with Async
//                     cam.takePicture();
//                    new TakePictureAsyncTask().execute();
                    try {
                        takePictureEssai();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
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
                    if(start_pitch_count == 0){
                        started_pitch = true;
                    }
                    start_pitch_count--;
                }

//                mBallView.mY = patern_image.getY() + 82/2 - 5; // center the ball
//                mBallView.mX = 60 + 435;

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

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

        captureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true);
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

        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
    }

    private void openCamera() throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        // 0 for rear camera
        cameraId = cameraManager.getCameraIdList()[0];
        CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        // Request some capabilities
        Integer supported_hardware_level = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        Log.i("Characteristics", "Hardware level: " + Integer.valueOf(supported_hardware_level));

//        Range exposure_time_range = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
//        Log.i("Characteristics", "Exposure time range: " + exposure_time_range.toString());

        imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            return;
        }
        cameraManager.openCamera(cameraId, stateCallback, null);
    }

    private void takePicture() throws CameraAccessException, IOException {
        if (cameraDevice == null) {
            return;
        }
        Log.i("MyApp", "Picture: taken");
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
//        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF);
//        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
//        captureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,Long.valueOf("22000000"));
//        captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,200);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_CANCEL);

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

        // timestamp
        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();

        // Fichier et dossier
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/Pictures");
        file = new File(Environment.getExternalStorageDirectory() + "/Pictures/pano_" + ts + ".jpg");

        Log.i("MyApp", sdCard.getAbsolutePath());
        ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {

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
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_CANCEL);
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

        if (textureView.isAvailable()) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }

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

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            stopBackgroundThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStart(){
        super.onStart();

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Prise de vue")
                .setMessage("Allez dans la première pièce et placez-vous au centre de la pièce.")
                .setCancelable(false)
                .setPositiveButton("J'ai compris", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] testItem = {"test1", "Test2", "Test3", "Test4"};
                        AlertDialog.Builder items = new AlertDialog.Builder(MainActivity.this);
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
                        try {
                            takePicture();
                        } catch (CameraAccessException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Intent mainIntent = new Intent(MainActivityIntegration.this, MainActivity.class);
//                        startActivity(mainIntent);
                    }
                });
        //Creating dialog box
        AlertDialog dialog  = builder.create();
        dialog.show();
    }

    protected void stopBackgroundThread() throws InterruptedException {
        mBackgroundThread.quitSafely();
        mBackgroundThread.join();
        mBackgroundThread = null;
    }



    // Fonctions helpers
    public void convertToDegrees(float[] vector){
        for (int i=0; i<vector.length; i++) vector[i] = Math.round(Math.toDegrees(vector[i]));
    }

    private void showProcessingDialog(String m) {
//        cam.stopPreview();
//        Context c = getApplicationContext();
        ringProgressDialog = ProgressDialog.show(MainActivity.this, "", m, true);
        ringProgressDialog.setCancelable(false);
    }

    private void closeProcessingDialog() {
        ringProgressDialog.dismiss();
    }


    // Asynchronous task

    class MyAsyncTask extends AsyncTask<Void, Integer, String> {

        Integer count = 0;
        @Override
        protected String doInBackground(Void... voids) {
            finalResult = new Mat();
            while(imageWaitedFor < (int)(ANGLE/15) + 1){
                if(imageDispo >= imageWaitedFor){ // if the image waited for is already available
                    if(imageWaitedFor == 1){
                        long[] temp = new long[2];
                        temp[0] = listImage.get(0).getNativeObjAddr();
                        temp[1] = listImage.get(1).getNativeObjAddr();
                        Log.i("AsyncStitch", "Stitching image 0 with image 1");
                        NativePanorama.processPanorama(temp, finalResult.getNativeObjAddr());
                        Log.i("AsyncStitch", "Completed");
                    }else{
                        long[] temp = new long[2];
                        temp[0] = finalResult.getNativeObjAddr();
                        temp[1] = listImage.get(imageWaitedFor).getNativeObjAddr();
                        Log.i("AsyncStitch", "Stitching result with image " + Integer.valueOf(imageWaitedFor));
                        NativePanorama.processPanorama(temp, finalResult.getNativeObjAddr());
                        Log.i("AsyncStitch", "Completed");
                    }
                    imageWaitedFor++;
                }
            }

            Context context = getApplicationContext();
            String p = context.getExternalFilesDir(null).getAbsolutePath();
            Log.i("AsyncStitch", "filepath: " + p);


            final String fileName = p+ "/stitchAsync.jpg";

            Imgcodecs.imwrite(fileName, finalResult);

            listImage.clear();

            return ("Return object");
        }
        @Override
        protected void onProgressUpdate(Integer... diff) {

        }

        // Surcharge de la méthode onPostExecute (s'exécute dans la Thread de l'IHM)
        @Override
        protected void onPostExecute(String message) {
            // Mettre à jour l'IHM
//            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            Log.i("AsyncThread", "Async task finished");
            closeProcessingDialog();
        }
    }

    private void takePictureEssai() throws FileNotFoundException {
//        showProcessingDialog("Capturing...");
        Bitmap image = textureView.getBitmap();
        String filename = this.getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + "/fetsy" + listImage.size() + ".jpg";
        FileOutputStream out = new FileOutputStream((filename));

        Log.i("MyApp", "Took picture");

        Context context = getBaseContext();
        String p = context.getExternalFilesDir(null).getAbsolutePath();

        // Try to get half of the image
        //Bitmap image2 = Bitmap.createBitmap(image, 0,0,10,image.getHeight());
        //image2.compress(Bitmap.CompressFormat.JPEG, 100, out);
        Mat result = new Mat();
        Utils.bitmapToMat(image, result);
        Imgproc.cvtColor(result, result, Imgproc.COLOR_BGR2RGB);

        listImage.add(result);

        imageDispo = listImage.size() - 1;

        Log.i("IMAGESIZE", Integer.toString(listImage.size()));

        taking_picture = false; // finished
        roll = 0;

        progression += 15;
        gauge.setValue(progression);

        if(progression == ANGLE) {
            stitch();
            started = false;
            can_start = false;
        }else{
            closeProcessingDialog();
        }

        if(started){
            Toast.makeText(getApplicationContext(), "Taken picture " + Integer.toString(listImage.size()), Toast.LENGTH_SHORT).show();
        }
    }

    public void stitch(){
        Context context = this.getBaseContext();
        String p = context.getExternalFilesDir(null).getAbsolutePath();
//        p = "/storage/sdcard0/a_stitching";
        // OpenCV
        try {
            // Create a long array to store all image address
            int elems = listImage.size();
            long[] tempobjadr = new long[elems];
            Log.i("LEN", Integer.toString(tempobjadr.length));

            for (int i = 0; i < elems; i++) {
                tempobjadr[i] = listImage.get(i).getNativeObjAddr();
            }
            // Create a Mat to store the final panorama image
            Mat result = new Mat();
            // Call the OpenCV C++ Code to perform stitching process
            NativePanorama.processPanorama(tempobjadr, result.getNativeObjAddr());

            NativePanorama.crop(result.getNativeObjAddr());

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
            final String fileName = p+ "/stitch.jpg";

//            final String fileName = "";
            Imgcodecs.imwrite(fileName, result);

            listImage.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeProcessingDialog();
    }
}