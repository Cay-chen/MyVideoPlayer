package com.code.first.myvideoplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class VideoPlayActivity extends AppCompatActivity implements Runnable{
    private VideoView mVideoView;
    private MediaController mMediaController;
    private MyMediaController myMediaController;
    private static final String TAG = "Cay";

    private static final int TIME = 0;
    private static final int BATTERY = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME:
                    myMediaController.setTime(msg.obj.toString());

                    break;
                case BATTERY:
                    myMediaController.setBattery(msg.obj.toString());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window = VideoPlayActivity.this.getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
        //设置视频解码监听
        if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
            return;
        setContentView(R.layout.activity_video_play);
        String path = getIntent().getStringExtra("urlpath");
        Log.i(TAG, "path: "+path);
        playFunction(path);
    }

    private void playFunction(String path) {
        if (path.isEmpty()) {
            Toast.makeText(this, "输入的地址不正确", Toast.LENGTH_LONG).show();
            return;
        } else {

            mVideoView = (VideoView) findViewById(R.id.surface_view);
            mVideoView.setVideoPath(path);
            mMediaController = new MediaController(this);
            myMediaController = new MyMediaController(this,mVideoView,this);
            mVideoView.setMediaController(myMediaController);
            mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);//高画质
            mMediaController.show(5000);
            mVideoView.requestFocus();
            registerBoradcastReceiver();
            myMediaController.setFileName("天启");
            new Thread(this).start();

        }
    }

    public static void actionStart(Context context, String path) {
        Intent intent = new Intent(context, VideoPlayActivity.class);
        intent.putExtra("urlpath", path);
        context.startActivity(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(TAG, "onConfigurationChanged: ");

        if (mVideoView != null) {
            mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(batteryBroadcastReceiver);
        } catch (IllegalArgumentException ex) {

        }
    }

    private BroadcastReceiver batteryBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                //获取当前电量
                int level = intent.getIntExtra("level", 0);
                //电量的总刻度
                int scale = intent.getIntExtra("scale", 100);
                //把它转成百分比
                //tv.setText("电池电量为"+((level*100)/scale)+"%");
                Message msg = new Message();
                msg.obj = (level * 100) / scale + "";
                msg.what = BATTERY;
                mHandler.sendMessage(msg);
            }
        }
    };

    public void registerBoradcastReceiver() {
        //注册电量广播监听电池电量改变
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryBroadcastReceiver, intentFilter);

    }

    @Override
    public void run() {
        while (true) {
            //读取线程
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String str = sdf.format(new Date());
            Message msg = new Message();
            msg.obj = str;
            msg.what = TIME;
            mHandler.sendMessage(msg);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
