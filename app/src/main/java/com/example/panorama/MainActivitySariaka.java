//package com.example.panorama;
//
//import android.annotation.TargetApi;
//import android.content.Context;
//import android.content.pm.ActivityInfo;
//import android.content.res.Resources;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Camera;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//import android.graphics.PixelFormat;
//import android.graphics.Rect;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.os.Build;
//import android.os.Environment;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.util.SparseIntArray;
//import android.view.Display;
//import android.view.MotionEvent;
//import android.view.Surface;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
//
//public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback,SensorEventListener {
//
//
//    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
//    /**
//     * COnfiguration d'orientation
//     */
//    static {
//        ORIENTATIONS.append(Surface.ROTATION_0, 90);
//        ORIENTATIONS.append(Surface.ROTATION_90, 0);
//        ORIENTATIONS.append(Surface.ROTATION_180, 270);
//        ORIENTATIONS.append(Surface.ROTATION_270, 180);
//    }
//    SurfaceView cameraView,transparentView;
//    SurfaceHolder holder,holderTransparent;
//    RelativeLayout layout;
//    android.hardware.Camera camera;
//    private float RectLeft, RectTop,RectRight,RectBottom ;
//    private float x, y, z, rayon;
//    int  deviceHeight,deviceWidth;
//    // private android.hardware.Camera.PictureCallback picture;
//    private Button capture;
//
//    // TODO les sensors
//    private SensorManager mSensorManager;
//
//    // Accelerometer and magnetometer sensors, as retrieved from the
//    // sensor manager.
//    private Sensor mSensorAccelerometer;
//    private Sensor mSensorMagnetometer;
//    private float[] mAccelerometerData = new float[3];
//    private float[] mMagnetometerData = new float[3];
//
//    float rollDeg;
//    float angle;
//    float roll;
//
//    private CustomCircle customCircle;
//
//    // TextViews to display current sensor values.
//    private TextView mTextSensorAzimuth;
//    private TextView mTextSensorPitch;
//    private TextView mTextSensorRoll;
//
//    private float gravity[];
//
//    private float magnetic[];
//    private float[] values = new float[3];
//
//    private boolean initialized;
//    private final float max = 360;
//    private boolean maxAtteint;
//    private int compteur = 0;
//    private ImageView spot_left, spot_right;
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        //getting the device heigth and width
//
//
//        deviceWidth=getScreenWidth();
//        deviceHeight=getScreenHeight();
//
//        mTextSensorAzimuth = (TextView) findViewById(R.id.value_azimuth);
//        mTextSensorPitch = (TextView) findViewById(R.id.value_pitch);
//        mTextSensorRoll = (TextView) findViewById(R.id.value_roll);
//
//        layout = findViewById(R.id.main_layout);
//
//        customCircle = new CustomCircle(this,(deviceWidth/10) +50,100,0,40);
//        /*layout.addView(customCircle);
//        layout.addView(mTextSensorAzimuth);
//        layout.addView(mTextSensorPitch);
//        layout.addView(mTextSensorRoll);*/
//        /*layout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                customCircle.setPoint(motionEvent.getX(), motionEvent.getY());
//                return false;
//            }
//        });*/
//
//
//        // transparentView.addView(customCircle);
//        cameraView = (SurfaceView)findViewById(R.id.cameraView);
//        capture = findViewById(R.id.capture);
//        capture.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                captureImage();
//                //refreshCamera();
//            }
//        });
//
//        holder = cameraView.getHolder();
//        holder.addCallback((SurfaceHolder.Callback) this);
//        //holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        cameraView.setSecure(true);
//
//        // Create second surface with another holder (holderTransparent)
//        transparentView = (SurfaceView)findViewById(R.id.transparentView);
//
//        holderTransparent = transparentView.getHolder();
//        holderTransparent.addCallback((SurfaceHolder.Callback) this);
//        holderTransparent.setFormat(PixelFormat.TRANSLUCENT);
//        transparentView.setZOrderMediaOverlay(true);
//
//        x = 200;
//        y = 200;
//        rayon = 50;
//        /*picture = new android.hardware.Camera.PictureCallback() {
//
//            @Override
//            public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
//
//                Log.d("MainActivity", "Azo tsara ny sary");
//                Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data.length);
//                if(bitmap!=null){
//                    Log.d("MainActivity","Atao anaty fichier ny sary azo");
//                    FileOutputStream outStream;
//                    String temps = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//                    String fileName = "image"+temps+".jpg";
//                    Log.d("MainActivity",fileName);
//                    try{
//                        // Get a public path on the device storage for saving the file. Note that the word external does not mean the file is saved in the SD card. It is still saved in the internal storage.
//                        // File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//
//                        // Creates a directory for saving the image.
//                        // File saveDir =  new File(path + "/saved_images/");
//
//                        String root = Environment.getExternalStorageDirectory().toString();
//                        File myDir = new File(root + "/saved_images/");
//
//                        // If the directory is not created, create it.
//                        if(!myDir.exists())
//                            myDir.mkdirs();
//
//                        // Create the image file within the directory.
//                        File fileDir =  new File(myDir, fileName); // Creates the file.
//
//                        // Write into the image file by the BitMap content.
//                        outStream = new FileOutputStream(fileDir);
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
//
//                        // Close the output stream.
//                        outStream.close();
//                        Log.d("MainActivity","Image saved");
//                    }catch(Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//        };*/
//        mSensorManager = (SensorManager) getSystemService(
//                Context.SENSOR_SERVICE);
//        mSensorAccelerometer = mSensorManager.getDefaultSensor(
////                Sensor.TYPE_LINEAR_ACCELERATION);
//                Sensor.TYPE_ACCELEROMETER);
//        mSensorMagnetometer = mSensorManager.getDefaultSensor(
//                Sensor.TYPE_MAGNETIC_FIELD);
//
//        spot_left = (ImageView) findViewById(R.id.spot_left);
//        spot_right = (ImageView) findViewById(R.id.spot_right);
//
//        initialized = false;
//        maxAtteint = false;
//    }
//
//    private void captureImage() {
//        camera.takePicture(null, null, new android.hardware.Camera.PictureCallback() {
//            @Override
//            public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
//
//                Log.d("MainActivity", "Azo tsara ny sary");
//                Matrix matrix = new Matrix();
//                matrix.postRotate(90F);
//
//                Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data.length);
//                Bitmap newbitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
//                if(newbitmap!=null) {
//                    Log.d("MainActivity", "Atao anaty fichier ny sary azo");
//                    FileOutputStream outStream;
//                    String temps = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//                    String fileName = "image" + temps + ".jpg";
//                    Log.d("MainActivity", fileName);
//                    try {
//                        // Get a public path on the device storage for saving the file. Note that the word external does not mean the file is saved in the SD card. It is still saved in the internal storage.
//                        // File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//
//                        // Creates a directory for saving the image.
//                        // File saveDir =  new File(path + "/saved_images/");
//
//                        String root = Environment.getExternalStorageDirectory().toString();
//                        File myDir = new File(root + "/saved_images/");
//
//                        // If the directory is not created, create it.
//                        if (!myDir.exists())
//                            myDir.mkdirs();
//
//                        // Create the image file within the directory.
//                        File fileDir = new File(myDir, fileName); // Creates the file.
//
//                        // Write into the image file by the BitMap content.
//                        outStream = new FileOutputStream(fileDir);
//                        newbitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
//
//                        // Close the output stream.
//                        outStream.flush();
//                        outStream.close();
//                        Log.d("MainActivity", "Image saved");
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    refreshCamera();
//                }
//            }
//        });
//        //refreshCamera();
//    }
//
//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        try {
//            synchronized (holder)
//            {
//
//                Draw();
//            }   //call a draw method
//            camera = android.hardware.Camera.open(); //open a camera
//        }
//        catch (Exception e) {
//            Log.i("Exception", e.toString());
//            return;
//        }
//        android.hardware.Camera.Parameters param;
//        param = camera.getParameters();
//
//        //param.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
//        if(param.isAutoExposureLockSupported()){
//            Log.d("CustomCamera","Exposure actuel= " + param.getExposureCompensation());
//
//            param.setAutoExposureLock(false);
//            param.setExposureCompensation(6);
//            //param.setAutoExposureLock(true);
//        }
//
//        Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
//        if(display.getRotation() == Surface.ROTATION_0) {
//            camera.setDisplayOrientation(90);
//        }
//
//        camera.setParameters(param);
//
//        try {
//            camera.setPreviewDisplay(holder);
//            camera.startPreview();
//            //camera.takePicture(null, null, picture);
//        }
//        catch (Exception e) {
//            return;
//        }
//    }
//
//    @Override
//
//    protected void onDestroy() {
//        super.onDestroy();
//    }
//
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        refreshCamera();
//    }
//
//    public void refreshCamera() {
//        if (holder.getSurface() == null) {
//            return;
//        }
//
//        try {
//            camera.stopPreview();
//        }
//
//        catch (Exception e) {
//        }
//
//        try {
//            camera.setPreviewDisplay(holder);
//            camera.startPreview();
//            //camera.takePicture(null,null,picture);
//        }
//        catch (Exception e) {
//        }
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        camera.release();
//    }
//
//    public int getScreenWidth() {
//        return Resources.getSystem().getDisplayMetrics().widthPixels;
//    }
//
//    public int getScreenHeight() {
//        return Resources.getSystem().getDisplayMetrics().heightPixels;
//    }
//
//    private void Draw() {
//        Canvas canvas = holderTransparent.lockCanvas(null);
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint.setStyle(Paint.Style.FILL_AND_STROKE);
//        paint.setColor(Color.GREEN);
//        paint.setStrokeWidth(3);
//
//        Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint1.setStyle(Paint.Style.STROKE);
//        paint1.setColor(Color.WHITE);
//        paint1.setStrokeWidth(2);
//        /*RectLeft = 1;
//        RectTop = 200 ;
//        RectRight = RectLeft+ deviceWidth-100;
//        RectBottom =RectTop+ 200;
//        Rect rec=new Rect((int) RectLeft,(int)RectTop,(int)RectRight,(int)RectBottom);
//        canvas.drawRect(rec,paint);*/
//
//        //x = 200;
//        //y = 200;
//        //canvas.drawCircle(x,y,rayon,paint);
//        canvas.drawLine(deviceWidth/10,50,(9*deviceWidth)/10,50,paint1);
//        canvas.drawLine(deviceWidth/10,150,(9*deviceWidth)/10,150,paint1);
//        canvas.drawCircle(((9*deviceWidth)/10)-50,100,50,paint1);
//        holderTransparent.unlockCanvasAndPost(canvas);
//    }
//
//    public void Draw2(int col){
//        Canvas canvas = holderTransparent.lockCanvas(null);
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint.setStyle(Paint.Style.FILL_AND_STROKE);
//        paint.setColor(col);
//        paint.setStrokeWidth(3);
//
//        /*Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint1.setStyle(Paint.Style.STROKE);
//        paint1.setColor(Color.WHITE);
//        paint1.setStrokeWidth(2);*/
//        /*RectLeft = 1;
//        RectTop = 200 ;
//        RectRight = RectLeft+ deviceWidth-100;
//        RectBottom =RectTop+ 200;
//        Rect rec=new Rect((int) RectLeft,(int)RectTop,(int)RectRight,(int)RectBottom);
//        canvas.drawRect(rec,paint);*/
//
//        //x = 200;
//        //y = 200;
//        canvas.drawCircle(x,y,rayon,paint);
//        /*canvas.drawLine(deviceWidth/10,50,(9*deviceWidth)/10,50,paint1);
//        canvas.drawLine(deviceWidth/10,150,(9*deviceWidth)/10,150,paint1);
//        canvas.drawCircle(((9*deviceWidth)/10)-50,100,50,paint1);*/
//        holderTransparent.unlockCanvasAndPost(canvas);
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        // Listeners for the sensors are registered in this callback and
//        // can be unregistered in onStop().
//        //
//        // Check to ensure sensors are available before registering listeners.
//        // Both listeners are registered with a "normal" amount of delay
//        // (SENSOR_DELAY_NORMAL).
//        if (mSensorAccelerometer != null) {
//            mSensorManager.registerListener(this, mSensorAccelerometer,
//                    SensorManager.SENSOR_DELAY_NORMAL);
//        }
//        if (mSensorMagnetometer != null) {
//            mSensorManager.registerListener(this, mSensorMagnetometer,
//                    SensorManager.SENSOR_DELAY_NORMAL);
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        // Unregister all sensor listeners in this callback so they don't
//        // continue to use resources when the app is stopped.
//        mSensorManager.unregisterListener(this);
//    }
//    @Override
//    public void onSensorChanged(SensorEvent sensorEvent) {
//
//        // TODO anah
//
//
//        switch (sensorEvent.sensor.getType()) {
//            case Sensor.TYPE_MAGNETIC_FIELD:
//                mMagnetometerData = sensorEvent.values.clone();
//                break;
//            //case Sensor.TYPE_LINEAR_ACCELERATION:
//            case Sensor.TYPE_ACCELEROMETER:
//                mAccelerometerData = sensorEvent.values.clone();
//                break;
//        }
//      /*  float[] rotationMatrix = new float[9];
//        boolean rotationOK = SensorManager.getRotationMatrix(rotationMatrix,
//                null, mAccelerometerData, mMagnetometerData);
//        float orientationValues[] = new float[3];
//        if (rotationOK) {
//            SensorManager.getOrientation(rotationMatrix, orientationValues);
//        }*/
//
//        /// initializing de azimuth, pitch and roll sensor data
//
//        // TODO anah
//        /*float azimuth = orientationValues[0];
//        float pitch = orientationValues[1];
//        float roll=0;
//        if(pitch>=-0.9 && pitch<=-0.65){
//            roll = orientationValues[2];
//        }
//
//        rollDeg = Math.abs((float) Math.toDegrees(roll)) - Math.abs(angle);*/
//
//
//        /*x = Math.abs(rollDeg)*((8*deviceWidth)/10-100)/15 + (deviceWidth/10) +50;
//        //x = Math.abs(rollDeg)*((8*deviceWidth)/10-100)/15;
//        y = 100;
//        //customCircle.setPoint(x,y);
//        customCircle.X = x;
//*/
//
//       /* x += orientationValues[0];
//        y += orientationValues[1];
//        z += orientationValues[2];
//
//        customCircle.X = x;
//        customCircle.Y = y;
//        customCircle.Z = z;*/
//        // refreshCamera();
//        // Draw2(Color.GREEN);
//        /*if(Math.abs(rollDeg)==15){
//            prendrePhoto();
//        }*/
//
//       /* if (Math.abs(pitch) < VALUE_DRIFT) {
//            pitch = 0;
//        }
//        if (Math.abs(roll) < VALUE_DRIFT) {
//            roll = 0;
//        }*/
//        /*mTextSensorAzimuth.setText(getResources().getString(
//                R.string.value_format, azimuth));
//        mTextSensorPitch.setText(getResources().getString(
//                R.string.value_format, pitch));
//        mTextSensorRoll.setText(getResources().getString(
//                R.string.value_format, rollDeg));*/
//
//        /// Reset the spot's alpha values
//        //mSpotTop.setAlpha(0f);
//        /*mSpotBottom.setAlpha(0f);
//        mSpotLeft.setAlpha(0f);
//        mSpotRight.setAlpha(0f);*/
//        // TODO anah
//        /*
//        if(rollDeg>=15){
//            angle += rollDeg;
//            //prendrePhoto();
//            //captureImage();
//            rollDeg = 0;
//        }
//        mTextSensorAzimuth.setText(getResources().getString(
//                R.string.value_format, azimuth));
//        mTextSensorPitch.setText(getResources().getString(
//                R.string.value_format, pitch));
//        mTextSensorRoll.setText(getResources().getString(
//                R.string.value_format, rollDeg));
//*/
//
//        // CELIO
//        /*switch (sensorEvent.sensor.getType()) {
//            case Sensor.TYPE_MAGNETIC_FIELD:
//                mMagnetometerData = sensorEvent.values.clone();
//                break;
//            //case Sensor.TYPE_LINEAR_ACCELERATION:
//            case Sensor.TYPE_ACCELEROMETER:
//                mAccelerometerData = sensorEvent.values.clone();
//                break;
//        }*/
//
//            /*notif.setText("xR: " + (int)spot_right.getX() + " <--> yR: " + (int)spot_right.getY() +
//                    " et x_i: " + (width-(int)spot_right.getX()) + ", y_i: " + (height-(int)spot_right.getX()));*/
//        //notif.setText("x_init: " + x_init + ", y_init: " + y_init);
//
//       /* if (mMagnetometerData != null && mAccelerometerData != null) {
//            gravity = new float[9];
//            magnetic = new float[9];*/
//
//                /*SensorManager.getRotationMatrix(gravity, magnetic, accels, mags);
//                float[] outGravity = new float[9];
//                SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_X,SensorManager.AXIS_Z, outGravity);
//                SensorManager.getOrientation(outGravity, values);*/
//        //SensorManager.getOrientation(gravity, values);
//        //azimuth = values[0] * 57.2957795f;
//
//            /*boolean rotationOk = SensorManager.getRotationMatrix(gravity, null, mAccelerometerData, mMagnetometerData);
//            if (rotationOk) SensorManager.getOrientation(gravity, values);*/
//         /*   SensorManager.getRotationMatrix(gravity, magnetic, mAccelerometerData, mMagnetometerData);
//            float[] outGravity = new float[9];
//            SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_Y,SensorManager.AXIS_Z, outGravity);
//            SensorManager.getOrientation(outGravity, values);*/
//
//        /**
//         * Result values from sensor
//         */
//
//        //    float azimuth = values[0]; / azimuth value /
//        //   float pitch = values[1]; /** PITCH  value*/
//
//        // roll = values[2];
//            /*if(Math.toDegrees(pitch)<=-75 && Math.toDegrees(pitch)>=-85){
//                roll = values[2];
//            }*/
//
///*
//            mAccelerometerData = null;
//            mMagnetometerData = null;
//*/
////            if (pitch >= -2 && pitch <= 2) {
//            /*if (Math.toDegrees(pitch) >= -2 && Math.toDegrees(pitch) <= 2) {
//                roll = values[0];
//            }
//            else{
//                roll = 0;
//            }*/
//
//
//            /*if (Math.abs(Math.toDegrees(roll)) == 15) {
//                roll = 0;
//            }*/
//
//      /*      mTextSensorAzimuth.setText(getResources().getString(
//                    R.string.value_format, Math.toDegrees(azimuth)));
//            mTextSensorPitch.setText(getResources().getString(
//                    R.string.value_format, Math.toDegrees(pitch)));
//            mTextSensorRoll.setText(getResources().getString(
//                    R.string.value_format, Math.toDegrees(roll)));
//        }*/
//
//        //mSpotTop.setX((float) (rollDeg+20));
//
//
//
//
//        // TODO tena ahy ary mety
//        if (mMagnetometerData != null && mAccelerometerData != null) {
//            gravity = new float[9];
//            magnetic = new float[9];
//
//                /*SensorManager.getRotationMatrix(gravity, magnetic, accels, mags);
//                float[] outGravity = new float[9];
//                SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_X,SensorManager.AXIS_Z, outGravity);
//                SensorManager.getOrientation(outGravity, values);*/
//            //SensorManager.getOrientation(gravity, values);
//            //azimuth = values[0] * 57.2957795f;
//
//            /*boolean rotationOk = SensorManager.getRotationMatrix(gravity, null, mAccelerometerData, mMagnetometerData);
//            if (rotationOk) SensorManager.getOrientation(gravity, values);*/
//            SensorManager.getRotationMatrix(gravity, magnetic, mAccelerometerData, mMagnetometerData);
//            float[] outGravity = new float[9];
//            SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_Y,SensorManager.AXIS_Z, outGravity);
//            SensorManager.getOrientation(outGravity, values);
//
//            /**
//             * Result values from sensor
//             */
//
//            float azimuth = values[1]; / azimuth value /
//            float pitch = values[2]; /* PITCH  value*/
//
//            roll = (float) Math.toDegrees(values[0]);
//            if (roll<0){
//                roll +=360;
//            }
//            /*if(Math.toDegrees(pitch)<=-75 && Math.toDegrees(pitch)>=-85){
//                roll = values[2];
//            }*/
//
//
//            mAccelerometerData = null;
//            mMagnetometerData = null;
//            float rollDeg = 0;
//            if(!initialized){
//                if(Math.toDegrees(pitch) <= -20) {
//                    //if(Math.toDegrees(pitch) >= -20 && Math.toDegrees(pitch) <= 0){
//                    //angle = (float) Math.toDegrees(values[0]);
//
//                    angle = roll;
//                    if (angle<0){
//                        angle +=360;
//                    }
//                    Log.d("MainActivity","angle initial = "+angle);
//                    initialized = true;
//                    Log.d("MainActivity","angle bien initialisé ");
//                }
//
//            }
//            else{
//                /*if (Math.toDegrees(pitch) >= -20 && Math.toDegrees(pitch) <= 0) {
//                    roll = (float) Math.toDegrees(values[0]);
//                    if (roll<0){
//                        roll +=360;
//                    }
//                }
//                else{
//                    roll = 0;
//                }*/
//
//                if (Math.toDegrees(pitch) >= -3 && Math.toDegrees(pitch) <= 3) {
//                    spot_left.setImageResource(R.drawable.green_circle);
//
//                    if (!maxAtteint) {
//                        rollDeg = roll - angle;
//                        if (angle >= 345) {
//                            maxAtteint = true;
//                        }
//                    } else {
//                        if (roll >= 345) {
//                            rollDeg = roll - angle;
//                        } else {
//                            rollDeg = max - angle + roll;
//                        }
//                    }
//
//
//                    if (rollDeg >= 15) {
//                        Log.d("MainActivity", "rollDeg = " + rollDeg);
//                        angle += 15;
//                        compteur += 1;
//                        captureImage();
//                        rollDeg = 0;
//                    }
//                    if(compteur == 25){
//                        try {
//                            camera.stopPreview();
//                        }
//
//                        catch (Exception e) {
//                        }
//                    }
//                }
//                else {
//                    spot_left.setImageResource(R.drawable.red_circle);
//                }
//                spot_left.setY(pitch + 50);
//                spot_left.setX(rollDeg*10);
//            }
//
////            if (pitch >= -2 && pitch <= 2) {
//
//
//            //Log.d("MainActivity","rollDeg = "+Math.toDegrees(rollDeg));
//
//            /*if (Math.abs(Math.toDegrees(roll)) == 15) {
//                roll = 0;
//            }*/
//
//
//
//            mTextSensorAzimuth.setText(getResources().getString(
//                    R.string.value_format, angle));
//            /*mTextSensorPitch.setText(getResources().getString(
//                    R.string.value_format, Math.toDegrees(pitch)));*/
//            mTextSensorPitch.setText(getResources().getString(
//                    R.string.value_format, rollDeg));
//            mTextSensorRoll.setText(getResources().getString(
//                    R.string.value_format, roll));
//
//            if(compteur == 24){
//                Log.d("MainActivity"," Les 360 degrés sont atteints");
//            }
//        }
//
//
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//    }
//}