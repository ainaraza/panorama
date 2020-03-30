package com.example.panorama;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class DegreeView extends View {

    public float mX;
    public float mY;
    public float mZ;
    public String mText;
    Paint paint = new Paint();

    public DegreeView(Context context, String text_content, float x, float y, float z, int r) {
        super(context);

        this.mText = text_content;

        this.mX = x;
        this.mY = y;
        this.mZ = z;
        paint.setColor(Color.WHITE);
        paint.setTextSize(15);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(mText+"°", mX+20, mY+35, paint);
//        canvas.drawCircle(mX, mY, mR, mPaint);
    }
}
