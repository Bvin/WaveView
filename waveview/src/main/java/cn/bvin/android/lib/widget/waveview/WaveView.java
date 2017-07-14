package cn.bvin.android.lib.widget.waveview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
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

    private Paint mWavePaintTransparent;
    private Paint mWavePaint2;

    // 波纹颜色
    private static final int WAVE_PAINT_COLOR = 0x880000aa;
    // y = Asin(wx+b)+h
    private static final float STRETCH_FACTOR_A = 20;
    private static final int OFFSET_Y = 0;
    // 第一条水波移动速度
    private static final int TRANSLATE_X_SPEED_ONE = 9;
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
    private float mMaxPosition;

    private float mPercent;

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
        mPercent = mRadius * 2;
    }

    private void init(){
        mRingPaint = new Paint();
        Shader shader = new LinearGradient(mRadius,mRadius*2,mRadius,0,Color.parseColor("#FF8362"), Color.parseColor("#FF4258"), Shader.TileMode.CLAMP);
        mRingPaint.setShader(shader);
        mRingPaint.setAntiAlias(true);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mStrockWidth = 3;
        mRingPaint.setStrokeWidth(mStrockWidth);


        mWavePaintTransparent = new Paint();
        mWavePaintTransparent.setAntiAlias(true);
        mWavePaintTransparent.setStyle(Paint.Style.FILL);
        mWavePaintTransparent.setShader(shader);
        mWavePaintTransparent.setAlpha(128);

        mWavePaint2 = new Paint();
        mWavePaint2.setAntiAlias(true);
        mWavePaint2.setStyle(Paint.Style.FILL);
        mWavePaint2.setShader(shader);

        mXOffsetSpeedOne = TRANSLATE_X_SPEED_ONE*5;
        mXOffsetSpeedTwo = TRANSLATE_X_SPEED_TWO*5;

        mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        mWavePaintTransparent.setXfermode(mPorterDuffXfermode);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) (mRadius * 2);
        width = resolveSize(width, widthMeasureSpec);
        int height = width;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("draw2: ","start");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }

        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            bitmapCanvas = new Canvas(bitmap);
        }
        bitmapCanvas.save();

        //绘制圆形背景
        drawBitmapInCenter(bitmapCanvas, getCircleInRect());

        Path path1 = new Path();
        path1.moveTo(mRadius, getHeight());

        Path path2 = new Path();
        path2.moveTo(0, getHeight());

        resetPositonY();

        Log.d("onDraw: ","percent is "+mPercent);

        if (mPercent < mRadius) {
            // 上半部分
            for (int i = 0; i < getWidth(); i++) {
                float waveY = mResetOneYPositions[i] + mPercent;
                float circleTopY = (float) getYOnCircle(i);
                if (circleTopY > 0) {
                    path1.lineTo(i, Math.max(circleTopY, waveY));
                }
            }
            path1.close();
            bitmapCanvas.drawPath(path1, mWavePaintTransparent);
        } else {
            // 下半部分
            Path pathBottom = new Path();
            for (int i = 0; i < getWidth(); i++){
                float circleY = (float) getYOnCircleBottom(i);
                // 连接波浪线
                float waveY = mResetOneYPositions[i] + mPercent;
                if (i == 0) {
                    pathBottom.moveTo(i, Math.min(waveY, circleY));
                }
                pathBottom.lineTo(i, Math.min(waveY, circleY));
            }
            for (int i = getWidth(); i > 0; i--){
                //连接底部的圆弧
                float circleY = (float) getYOnCircleBottom(i);
                pathBottom.lineTo(i, circleY);
            }
            pathBottom.close();
            bitmapCanvas.drawPath(pathBottom, mWavePaintTransparent);
        }

        //绘制第二层波浪
        for (int i = 0; i < getWidth(); i++) {
            path2.lineTo(i, mResetTwoYPositions[i]+mPercent);
        }

        path2.lineTo(getWidth(), getHeight());
        path2.close();
        mWavePaint2.setXfermode(mPorterDuffXfermode);
        bitmapCanvas.drawPath(path2, mWavePaint2);

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
        mWavePaintTransparent.setXfermode(null);
        mWavePaint2.setXfermode(null);

        // 绘制外环
        canvas.drawCircle(mRadius, mRadius, mRadius - mStrockWidth / 2, mRingPaint);

        //mPercent--;
        if (mPercent > 0)
            postInvalidate();
        Log.d("draw2: ","end");
    }

    public void setPercent(float percent) {
        float height = mRadius*2;
        float actrue = height * percent;
        mPercent = height - actrue;
        Log.d( "setPercent: ",percent+","+mPercent+","+actrue);
    }

    private void drawBitmapInCenter(Canvas canvas, Bitmap bitmap){
        Bitmap emptyBitmap =Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvasOfBitmap = new Canvas(emptyBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);
        canvasOfBitmap.drawCircle(mRadius, mRadius, mRadius, paint);
        Rect rectSrc = new Rect(bitmap.getWidth()*1/4, bitmap.getHeight()*1/4, bitmap.getWidth()*3/4, bitmap.getHeight()*3/4);
        Rect rect = new Rect(0, 0, getWidth(), getHeight());
        paint.setXfermode(mPorterDuffXfermode);
        canvasOfBitmap.drawBitmap(bitmap, rectSrc, rect, paint);
        canvas.drawBitmap(emptyBitmap, 0, 0, null);
    }

    private Bitmap getCircleInRect(){
        return BitmapFactory.decodeResource(getResources(),R.drawable.pp);
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
                    mWavePaintTransparent);*/

            //mWavePaintTransparent.setXfermode(mPorterDuffXfermode);

            // 绘制第二条水波纹
            /*canvas.drawLine(i, mTotalHeight - mResetTwoYPositions[i] - 400, i,
                    mTotalHeight,
                    mWavePaintTransparent);*/
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
        mWavePaintTransparent.setXfermode(mPorterDuffXfermode);
        bitmapCanvas.drawRect(0,190,getWidth(),getBottom(), mWavePaintTransparent);

        canvas.drawBitmap(bitmap, 0, 0, null);
        mWavePaintTransparent.setXfermode(null);

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

        mMaxPosition = Float.MIN_VALUE;

        // 根据view总宽度得出所有对应的y值
        for (int i = 0; i < mTotalWidth; i++) {
            mYPositions[i] = (float) (STRETCH_FACTOR_A * Math.sin(mCycleFactorW * i) + OFFSET_Y);
            if (mMaxPosition < mYPositions[i]) {
                mMaxPosition = mYPositions[i];
            }
        }

    }

    private double getYOnCircle(float x) {
        x = x - mRadius;
        double number = Math.pow(mRadius, 2) - Math.pow(x, 2);
        double result = Math.sqrt(number);
        return mRadius - result ;
    }

    private double getYOnCircleBottom(float x) {
        x = x - mRadius;
        double number = Math.pow(mRadius, 2) - Math.pow(x, 2);
        double result = Math.sqrt(number);
        return result  + mRadius;
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
