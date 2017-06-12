package wuziqi.example.asus_pc.yaoyiyao;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    boolean isShake;
    private TextView txt;
    private static final int START_SHAKE = 0x1;
    private static final int AGAIN_SHAKE = 0x2;
    private static final int END_SHAKE = 0x3;

    private Vibrator mVibrator;//手机震动
    private SoundPool mSoundPool;//摇一摇音效
    private int mWeiChatAudio;

    private ImageView mTopLine;
    private ImageView mBottomLine;
    private LinearLayout mTopLayout;
    private LinearLayout mBottomLayout;

    private Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e("AAA", "1");
            switch (msg.what) {
                case START_SHAKE:
                    Log.e("AAA", "1");
                    //This method requires the caller to hold the permission VIBRATE.
                    mVibrator.vibrate(300);
                    //发出提示音
                    mSoundPool.play(mWeiChatAudio, 1, 1, 0, 0, 1);
                    mTopLine.setVisibility(View.VISIBLE);
                    mBottomLine.setVisibility(View.VISIBLE);
                    startAnimation(false);//参数含义: (不是回来) 也就是说两张图片分散开的动画
                    break;
                case AGAIN_SHAKE:
                    Log.e("AAA", "2");
                    mVibrator.vibrate(300);
                    break;
                case END_SHAKE:
                    Log.e("AAA", "3");
                    //整体效果结束, 将震动设置为false
                    isShake = false;
                    // 展示上下两种图片回来的效果
                    startAnimation(true);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt = (TextView) findViewById(R.id.txt);
        mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);

        mWeiChatAudio = mSoundPool.load(this, R.raw.weichat_audio, 1);

//获取Vibrator震动服务
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);


        mTopLayout = (LinearLayout) findViewById(R.id.main_linear_top);
        mBottomLayout = ((LinearLayout) findViewById(R.id.main_linear_bottom));
        mTopLine = (ImageView) findViewById(R.id.main_shake_top_line);
        mBottomLine = (ImageView) findViewById(R.id.main_shake_bottom_line);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager != null) {
            mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Log.e("AAA", "chenggong");
            if (mAccelerometerSensor != null) {
                mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        if (type == Sensor.TYPE_ACCELEROMETER) {
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];


            if (Math.abs(x) > 17 || Math.abs(y) > 17 || Math.abs(z) > 17 && !isShake) {
                isShake = true;
                txt.setText(x + ":" + y + ":" + z);
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
//                            myHandler.obtainMessage(START_SHAKE).sendToTarget();

                            myHandler.sendEmptyMessage(START_SHAKE);
                            Thread.sleep(500);

                            //再来一次震动提示
//                            myHandler.obtainMessage(AGAIN_SHAKE).sendToTarget();
                            myHandler.sendEmptyMessage(AGAIN_SHAKE);
                            Thread.sleep(500);
//                            myHandler.obtainMessage(END_SHAKE).sendToTarget();
                            myHandler.sendEmptyMessage(END_SHAKE);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                    }
                };
                thread.start();

            }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void startAnimation(boolean isBack) {
        //动画坐标移动的位置的类型是相对自己的
        int type = Animation.RELATIVE_TO_SELF;

        float topFromY;
        float topToY;
        float bottomFromY;
        float bottomToY;
        if (isBack) {
            topFromY = -0.5f;
            topToY = 0;
            bottomFromY = 0.5f;
            bottomToY = 0;
        } else {
            topFromY = 0;
            topToY = -0.5f;
            bottomFromY = 0;
            bottomToY = 0.5f;
        }

        //上面图片的动画效果
        TranslateAnimation topAnim = new TranslateAnimation(
                type, 0, type, 0, type, topFromY, type, topToY
        );
        topAnim.setDuration(200);
        //动画终止时停留在最后一帧~不然会回到没有执行之前的状态
        topAnim.setFillAfter(true);

        //底部的动画效果
        TranslateAnimation bottomAnim = new TranslateAnimation(
                type, 0, type, 0, type, bottomFromY, type, bottomToY
        );
        bottomAnim.setDuration(200);
        bottomAnim.setFillAfter(true);

        //大家一定不要忘记, 当要回来时, 我们中间的两根线需要GONE掉
        if (isBack) {
            bottomAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationRepeat(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    //当动画结束后 , 将中间两条线GONE掉, 不让其占位
                    mTopLine.setVisibility(View.GONE);
                    mBottomLine.setVisibility(View.GONE);
                }
            });
        }
        //设置动画
        mTopLayout.startAnimation(topAnim);
        mBottomLayout.startAnimation(bottomAnim);

    }
}
