package cn.bvin.android.lib.widget.waveview.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import cn.bvin.android.lib.widget.waveview.WaveView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WaveView waveView = (WaveView) findViewById(R.id.wv);

        new Thread(){
            @Override
            public void run() {
                super.run();
                for (int i = 1; i <= 100; i++) {
                    final float person = i / 100f;
                    Log.d("onCreate: ",""+person);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            waveView.setPercent(person);
                        }
                    });
                }
            }
        }.start();
    }
}
