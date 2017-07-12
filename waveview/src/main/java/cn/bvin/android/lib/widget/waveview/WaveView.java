package cn.bvin.android.lib.widget.waveview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by bvin on 2017/7/12.
 */

public class WaveView extends View {

    private float mRadius;
    private Paint mRingPaint;
    private float mStrockWidth;

    public WaveView(Context context) {
        super(context);
        init();
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        attrs(context, attrs);
        init();
    }

    private void attrs(Context context, @Nullable AttributeSet attrs){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaveView);
        mRadius = a.getDimension(R.styleable.WaveView_radius, 0);
        Log.d("attrs: ", mRadius + "");
        a.recycle();
    }

    private void init(){
        mRingPaint = new Paint();
        Shader shader = new LinearGradient(mRadius,mRadius*2,mRadius,0,Color.parseColor("#FF8362"), Color.parseColor("#FF4258"), Shader.TileMode.CLAMP);
        mRingPaint.setShader(shader);
        mRingPaint.setAntiAlias(true);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mStrockWidth = 3;
        mRingPaint.setStrokeWidth(mStrockWidth);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) (mRadius * 2);
        width = resolveSize(width, widthMeasureSpec);
        int height = width;
        Log.d("onMeasure-pre: ",width+","+width);
        setMeasuredDimension(width, height);
        Log.d("onMeasure: ",getMeasuredWidth()+","+getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mRadius, mRadius, mRadius - mStrockWidth, mRingPaint);
    }
}
