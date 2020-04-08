package com.example.panorama;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.Image;
import android.view.View;
import android.widget.ImageView;

public class BallView extends View {

    public float mX;
    public float mY;
    public float mZ;
    private final int mR;
    private Bitmap ball;
    private Bitmap redBall;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    //construct new ball object
    public BallView(Context context, float x, float y, float z, int r) {
        super(context);
        //color hex is [transparency][red][green][blue]
        mPaint.setColor(0xFF00FF00); //not transparent. color is green

        this.mX = x;
        this.mY = y;
        this.mR = r; //radius
        this.mZ = z;
        Bitmap ballSrc = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
        ball = Bitmap.createScaledBitmap(ballSrc, 55, 55, true);

        Bitmap redBallSrc = BitmapFactory.decodeResource(getResources(), R.drawable.red_circle);
        redBall = Bitmap.createScaledBitmap(redBallSrc, 50, 50, true);
    }

    //called by invalidate()
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // canvas.drawText("12", mX, mY, null);
//        if (mZ <= 0.69 && mZ >= 70) {
//            mY = 85;
//        }
//        ImageView patern_image = findViewById(R.id.patern_image);
        float initial_y = 100 + 82/2 - 5;
        float initial_x = 60;
        float final_y = 100 + 82/2 - 5;
        float final_x = 60 + 435;

        if(initial_y - 15 <= mY && mY <= initial_y + 15){
            canvas.drawBitmap(ball, mX, mY, null);
        }else{
            canvas.drawBitmap(redBall, mX, mY, null);
        }
//        if (mY <= 95 && mY >= 65) {
//            canvas.drawBitmap(ball, mX, mY, null);
//        } else {
//            canvas.drawBitmap(redBall, mX, mY, null);
//        }
//        canvas.drawCircle(mX, mY, mR, mPaint);
    }
}
