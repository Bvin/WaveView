package cn.bvin.android.lib.widget.waveview.demo;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.bvin.android.lib.widget.waveview.WaveView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView iv = (ImageView) findViewById(R.id.pp);
        iv.setImageResource(R.drawable.pp);
        final WaveView waveView = (WaveView) findViewById(R.id.wv);
        Rect rect = new Rect();
        iv.getDrawingRect(rect);
        Log.d("onCreate: ",rect.toString());
        Bitmap bitmap = iv.getDrawingCache();
        if (bitmap != null) {
            Log.d( "onCreate: ","getDrawingCache");
            waveView.setBackground(bitmap);
        }

        final TextView tv = (TextView) findViewById(R.id.tv);

        new Thread(){
            @Override
            public void run() {
                super.run();
                for (int i = 1; i <= 100; i++) {
                    final float person = i / 100f;
                    Log.d("onCreate: ",""+person);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            waveView.setPercent(person);
                            SpannableStringBuilder ssb = new SpannableStringBuilder(finalI +"%");
                            ssb.setSpan(new RelativeSizeSpan(0.5f),ssb.length()-1,ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            tv.setText(ssb);
                        }
                    });
                }
            }
        }.start();
    }
}
