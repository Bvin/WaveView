package cn.bvin.android.lib.widget.waveview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by bvin on 2017/7/12.
 */

public class WaveView extends View {

    private float mRadius;
    private Paint mRingPaint;
    private float mStrockWidth;

    private Paint mWavePaint;

    // 波纹颜色
    private static final int WAVE_PAINT_COLOR = 0x880000aa;
    // y = Asin(wx+b)+h
    private static final float STRETCH_FACTOR_A = 20;
    private static final int OFFSET_Y = 0;
    // 第一条水波移动速度
    private static final int TRANSLATE_X_SPEED_ONE = 7;
    // 第二条水波移动速度
    private static final int TRANSLATE_X_SPEED_TWO = 5;
    private float mCycleFactorW;

    private int mTotalWidth, mTotalHeight;
    private float[] mYPositions;
    private float[] mResetOneYPositions;
    private float[] mResetTwoYPositions;
    private int mXOffsetSpeedOne;
    private int mXOffsetSpeedTwo;
    private int mXOneOffset;
    private int mXTwoOffset;

    private PorterDuffXfermode mPorterDuffXfermode;

    private Bitmap mBitmap;

    private Bitmap bitmap;
    private Canvas bitmapCanvas;

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
        //mRingPaint.setShader(shader);
        mRingPaint.setColor(Color.BLUE);
        mRingPaint.setAntiAlias(true);
        mRingPaint.setStyle(Paint.Style.FILL);
        mStrockWidth = 3;
        mRingPaint.setStrokeWidth(mStrockWidth);


        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setStyle(Paint.Style.FILL);
        mWavePaint.setShader(shader);

        mXOffsetSpeedOne = TRANSLATE_X_SPEED_ONE;
        mXOffsetSpeedTwo = TRANSLATE_X_SPEED_TWO;

        mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        mWavePaint.setXfermode(mPorterDuffXfermode);
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

        //draw1(canvas);
        draw2(canvas);
    }

    private void draw2(Canvas canvas){
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            bitmapCanvas = new Canvas(bitmap);
        }
        bitmapCanvas.save();
        //绘制圆
        bitmapCanvas.drawCircle(mRadius, mRadius, mRadius, mRingPaint);

        //bitmapCanvas.drawLine(mRadius/2, 0, mRadius/2, mTotalHeight, mWavePaint);

        //bitmapCanvas.drawRect(0, mRadius/2, mTotalWidth, mTotalHeight, mWavePaint);

        Path path = new Path();
        path.moveTo(0, getHeight());
        resetPositonY();
        for (int i = 0; i < getWidth(); i++) {
            path.lineTo(i, mResetOneYPositions[i]+mRadius);
        }
        path.lineTo(getWidth(), getHeight());
        path.close();
        bitmapCanvas.drawPath(path, mWavePaint);

        // 改变两条波纹的移动点
        mXOneOffset += mXOffsetSpeedOne;
        mXTwoOffset += mXOffsetSpeedTwo;

        // 如果已经移动到结尾处，则重头记录
        if (mXOneOffset >= mTotalWidth) {
            mXOneOffset = 0;
        }
        if (mXTwoOffset > mTotalWidth) {
            mXTwoOffset = 0;
        }

        bitmapCanvas.restore();

        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    private void draw1(Canvas canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }

        canvas.drawColor(Color.TRANSPARENT);

        canvas.drawCircle(mRadius, mRadius, mRadius - mStrockWidth, mRingPaint);

        resetPositonY();
        for (int i = 0; i < mTotalWidth; i++) {

            // 减400只是为了控制波纹绘制的y的在屏幕的位置，大家可以改成一个变量，然后动态改变这个变量，从而形成波纹上升下降效果
            // 绘制第一条水波纹
            /*canvas.drawLine(i, mTotalHeight - mResetOneYPositions[i] - 400, i,
                    mTotalHeight,
                    mWavePaint);*/

            //mWavePaint.setXfermode(mPorterDuffXfermode);

            // 绘制第二条水波纹
            /*canvas.drawLine(i, mTotalHeight - mResetTwoYPositions[i] - 400, i,
                    mTotalHeight,
                    mWavePaint);*/
        }

        // 改变两条波纹的移动点
        mXOneOffset += mXOffsetSpeedOne;
        mXTwoOffset += mXOffsetSpeedTwo;

        // 如果已经移动到结尾处，则重头记录
        if (mXOneOffset >= mTotalWidth) {
            mXOneOffset = 0;
        }
        if (mXTwoOffset > mTotalWidth) {
            mXTwoOffset = 0;
        }

        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            bitmapCanvas = new Canvas(bitmap);
        }
        bitmapCanvas.save();
        // 引发view重绘，一般可以考虑延迟20-30ms重绘，空出时间片
        //postInvalidate();
        mWavePaint.setXfermode(mPorterDuffXfermode);
        bitmapCanvas.drawRect(0,190,getWidth(),getBottom(),mWavePaint);

        canvas.drawBitmap(bitmap, 0, 0, null);
        mWavePaint.setXfermode(null);
        bitmapCanvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 记录下view的宽高
        mTotalWidth = w;
        mTotalHeight = h;
        // 用于保存原始波纹的y值
        mYPositions = new float[mTotalWidth];
        // 用于保存波纹一的y值
        mResetOneYPositions = new float[mTotalWidth];
        // 用于保存波纹二的y值
        mResetTwoYPositions = new float[mTotalWidth];

        // 将周期定为view总宽度
        mCycleFactorW = (float) (2 * Math.PI / mTotalWidth);

        // 根据view总宽度得出所有对应的y值
        for (int i = 0; i < mTotalWidth; i++) {
            mYPositions[i] = (float) (STRETCH_FACTOR_A * Math.sin(mCycleFactorW * i) + OFFSET_Y);
        }
    }

    private void resetPositonY() {
        // mXOneOffset代表当前第一条水波纹要移动的距离
        int yOneInterval = mYPositions.length - mXOneOffset;
        // 使用System.arraycopy方式重新填充第一条波纹的数据
        System.arraycopy(mYPositions, mXOneOffset, mResetOneYPositions, 0, yOneInterval);
        System.arraycopy(mYPositions, 0, mResetOneYPositions, yOneInterval, mXOneOffset);

        int yTwoInterval = mYPositions.length - mXTwoOffset;
        System.arraycopy(mYPositions, mXTwoOffset, mResetTwoYPositions, 0,
                yTwoInterval);
        System.arraycopy(mYPositions, 0, mResetTwoYPositions, yTwoInterval, mXTwoOffset);
    }
}
