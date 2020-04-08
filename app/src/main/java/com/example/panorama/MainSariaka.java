package com.example.panorama;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainSariaka extends AppCompatActivity implements SurfaceHolder.Callback {


    SurfaceView cameraView;
    SurfaceHolder holder;
    android.hardware.Camera camera;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sariaka);

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        cameraView = (SurfaceView) findViewById(R.id.cameraView);
        holder = cameraView.getHolder();
        holder.addCallback((SurfaceHolder.Callback) this);
        Log.i("MAIN_SARIAKA", "Created");
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("MAIN_SARIAKA", "SurfaceCreated is called");
        try {
            synchronized (holder) {
            }   //call a draw method
            camera = android.hardware.Camera.open(); //open a camera
        } catch (Exception e) {
            Log.i("Exception", e.toString());
            return;
        }
        android.hardware.Camera.Parameters param;
        param = camera.getParameters();

        //param.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
        if (param.isAutoExposureLockSupported()) {
            Log.d("CustomCamera", "Exposure actuel= " + param.getExposureCompensation());

            param.setAutoExposureLock(true);
        }

        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        if (display.getRotation() == Surface.ROTATION_0) {
            camera.setDisplayOrientation(90);
        }

        camera.setParameters(param);

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            //camera.takePicture(null, null, picture);
        } catch (Exception e) {
            return;
        }
    }

    @Override

    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }


    public void capturer(View v){
        this.captureImage();
    }

    private void captureImage() {
        camera.takePicture(new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                Log.i("CameraLog", "Shutter Called");
                android.hardware.Camera.Parameters param;
                param = camera.getParameters();
                param.setAutoExposureLock(true);
                camera.setParameters(param);
            }
        }, null, new android.hardware.Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
                android.hardware.Camera.Parameters param;
                param = camera.getParameters();
                param.setAutoExposureLock(true);
                camera.setParameters(param);
                Log.d("MainActivity", "Azo tsara ny sary");
                Matrix matrix = new Matrix();
                matrix.postRotate(90F);

                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap newbitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                if (newbitmap != null) {
                    Log.d("MainActivity", "Atao anaty fichier ny sary azo");
                    FileOutputStream outStream;
                    String temps = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String fileName = "image" + temps + ".jpg";
                    Log.d("MainActivity", fileName);
                    try {
                        // Get a public path on the device storage for saving the file. Note that the word external does not mean the file is saved in the SD card. It is still saved in the internal storage.
                        // File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                        // Creates a directory for saving the image.
                        // File saveDir =  new File(path + "/saved_images/");

                        String root = Environment.getExternalStorageDirectory().toString();
                        File myDir = new File(root + "/saved_images/");

                        // If the directory is not created, create it.
                        if (!myDir.exists())
                            myDir.mkdirs();

                        // Create the image file within the directory.
                        File fileDir = new File(myDir, fileName); // Creates the file.

                        // Write into the image file by the BitMap content.
                        outStream = new FileOutputStream(fileDir);
                        newbitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);

                        // Close the output stream.
                        outStream.flush();
                        outStream.close();
                        Log.d("MainActivity", "Image saved");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    refreshCamera();
                }
            }
        });
        //refreshCamera();
    }

    public void refreshCamera() {
        if (holder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {
        }

        try {
            camera.setPreviewDisplay(holder);
            android.hardware.Camera.Parameters param;
            param = camera.getParameters();
            param.setAutoExposureLock(true);
            camera.setParameters(param);

            camera.startPreview();
            //camera.takePicture(null,null,picture);
        } catch (Exception e) {
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.release();
    }


}