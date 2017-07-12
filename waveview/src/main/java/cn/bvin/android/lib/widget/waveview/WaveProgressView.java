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
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.text.DecimalFormat;

/**
 * Created by bvin on 2017/7/12.
 */

public class WaveProgressView extends View {


    public WaveProgressView(Context context) {
        super(context);
    }

    public WaveProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        attrs(context, attrs);
        init();
    }

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


    private int radius = dp2px(55);
    private int textColor;
    private int textSize;
    private int progressColor;
    private int radiusColor;
    private Paint textPaint;
    private Paint circlePaint;
    private Paint pathPaint;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private int width, height;
    private int minPadding;
    private float progress;
    private float maxProgress;
    private Path path = new Path();
    private DecimalFormat df = new DecimalFormat("0.0");

    private void attrs(Context context, @Nullable AttributeSet attrs){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaveView);
        mRadius = a.getDimension(R.styleable.WaveView_radius, 0);
        Log.d("attrs: ", mRadius + "");
        a.recycle();
    }

    private void init(){
        mRingPaint = new Paint();
        Shader shader = new LinearGradient(mRadius,mRadius*2,mRadius,0, Color.parseColor("#FF8362"), Color.parseColor("#FF4258"), Shader.TileMode.CLAMP);
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

        mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
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

        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
            bitmapCanvas = new Canvas(bitmap);
        }
        bitmapCanvas.save();
        //移动坐标系
        bitmapCanvas.translate(minPadding, minPadding);
        //绘制圆
        bitmapCanvas.drawCircle(radius, radius, radius, circlePaint);

        //绘制PATH
        //重置绘制路线
        path.reset();
        float percent=progress * 1.0f / maxProgress;
        float y = (1 - percent) * radius * 2;
        //移动到右上边
        path.moveTo(radius * 2, y);
        //移动到最右下方
        path.lineTo(radius * 2, radius * 2);
        //移动到最左下边
        path.lineTo(0, radius * 2);
        //移动到左上边
        // path.lineTo(0, y);
        //实现左右波动,根据progress来平移
        path.lineTo(-(1 -percent) * radius*2, y);
        if (progress != 0.0f) {
            //根据直径计算绘制贝赛尔曲线的次数
            int count = radius * 4 / 60;
            //控制-控制点y的坐标
            float point = (1 - percent) * 15;
            for (int i = 0; i < count; i++) {
                path.rQuadTo(15, -point, 30, 0);
                path.rQuadTo(15, point, 30, 0);
            }
        }
        //闭合
        path.close();
        bitmapCanvas.drawPath(path, pathPaint);

        //绘制文字
        String text = progress + "%";
        float textW = textPaint.measureText(text);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float baseLine = radius - (fontMetrics.ascent + fontMetrics.descent) / 2;
        bitmapCanvas.drawText(text, radius - textW / 2, baseLine, textPaint);

        bitmapCanvas.restore();

        canvas.drawBitmap(bitmap, 0, 0, null);
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

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
