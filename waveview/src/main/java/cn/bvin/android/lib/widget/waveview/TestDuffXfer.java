package cn.bvin.android.lib.widget.waveview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by bvin on 2017/7/12.
 */

public class TestDuffXfer extends View {

    private  Paint mDstPaint;
    private  Paint mSrcPaint;

    public TestDuffXfer(Context context) {
        super(context);
    }

    public TestDuffXfer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mDstPaint = new Paint();
        mSrcPaint = new Paint();
        mDstPaint.setColor(Color.YELLOW);
        mSrcPaint.setColor(Color.BLUE);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //dst
        canvas.drawRect(0,0,200,200,mDstPaint);
        mSrcPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //src
        canvas.drawCircle(100,100,100,mSrcPaint);
    }
}
