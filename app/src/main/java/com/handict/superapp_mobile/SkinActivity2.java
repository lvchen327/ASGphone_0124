package com.handict.superapp_mobile;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.aliyun.vodplayer.media.AliyunLocalSource;
import com.aliyun.vodplayer.media.AliyunPlayAuth;
import com.aliyun.vodplayer.media.IAliyunVodPlayer;
import com.aliyun.vodplayerview.widget.AliyunScreenMode;
import com.aliyun.vodplayerview.widget.AliyunVodPlayerView;
import com.baidu.speech.VoiceRecognitionService;
import com.handict.superapp_mobile.utils.ScreenStatusController;
import com.handict.superapp_mobile.view.VoiceWaveView;
import com.unity3d.player.UnityPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkinActivity2 extends Activity implements Handler.Callback, RecognitionListener {

    private AliyunVodPlayerView mAliyunVodPlayerView = null;

    private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SS");
    private List<String> logStrs = new ArrayList<>();

    private String mVid = null;
    private String mAuthinfo = null;

    private AliyunPlayAuth mPlayAuth = null;
    private AliyunLocalSource mLocalSource = null;
    private ProgressBar mBar;
    private ScreenStatusController mScreenStatusController = null;
    private ImageView mScanBtn, mBackBtn;
    private String videoUrl;
    private int showsys, showback;
    public static final int UPDATE_PROGRESS = 100;
    public static final int UPDATE_NEXT = 101;
    public static final int UPDATE_YY = 102;
    private int currentPosition;
    private Handler mHandler;
    private boolean isYY = true;
    private boolean isPlay = true;
    VoiceWaveView wvw_main;
    public static final int STATUS_None = 0;
    public static final int STATUS_WaitingReady = 2;
    public static final int STATUS_Ready = 3;
    public static final int STATUS_Speaking = 4;
    public static final int STATUS_Recognition = 5;
    private int status = STATUS_None;
    private long speechEndTime = -1;
    private static final int EVENT_ERROR = 11;
    private String s = "蛋蛋没听清楚你说什么请再来一遍";
    //2次读错直接过
    private int isDouble = 0;
    private static final int REQUEST_UI = 1;
    private Runnable mRunnable;


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_log, menu);
        return super.onCreateOptionsMenu(menu);
    }*/
    private ImageView mPlay, mPlay3,iv_frame;
    private int yy = 0;
    private boolean isPause = false;
    private SpeechRecognizer speechRecognizer;
    private LinearLayout mBj_linear;

//    private int[] yydanciTime = {64000, 384000, 892000};
    private int[] yydanciTime = {150680,
        316000,
        535240
};
//    private int[] yyxpyTime = {46080, 366000, 875080};
    private int[] yyxpyTime = {84680,
        168440,
        198600,
        219000,
        247160


};

//    private String[] yydanciS = {"star", "大大", "大"};
    private String[] yydanciS = {"cookie", "酷", "库克"};

    private String xpyName = "昵 称";
    private MediaPlayer mpp;
    private LottieAnimationView animationView;
    ;
    private ComponentName SkinActivity2;
    private AnimationDrawable frameAnim;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.log) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.view_log, null);

            TextView textview = (TextView) view.findViewById(R.id.log);
            if (mAliyunVodPlayerView != null) {
                for (String log : logStrs) {
                    textview.append("     " + log + "\n");
                }
            }
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("播放器日志：");
            alertDialog.setView(view);
            alertDialog.setPositiveButton("OK", null);
            AlertDialog alert = alertDialog.create();
            alert.show();
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main22);
        initView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //应用运行时,保持屏幕高亮,不锁屏
        //横屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, new ComponentName(this, VoiceRecognitionService.class));

        speechRecognizer.setRecognitionListener(this);

//        mPlay3 = (ImageView) findViewById(R.id.play_play3);


        iv_frame= (ImageView) findViewById(R.id.play_play3);


        animationView = (LottieAnimationView) findViewById(R.id.animation_view);


        mHandler = new Handler(this);
        wvw_main = (VoiceWaveView) findViewById(R.id.wvw_main);

        setting();
//        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                mAliyunVodPlayerView.start();
//            }
//        });
//        findViewById(R.id.pause).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mAliyunVodPlayerView.pause();
//            }
//        });
//
//        findViewById(R.id.replay).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mAliyunVodPlayerView.rePlay();
//            }
//        });

        mAliyunVodPlayerView = (AliyunVodPlayerView) findViewById(R.id.video_view);
        String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test_save_cache";
        mAliyunVodPlayerView.setPlayingCache(true, sdDir, 60 * 60 /*时长, s */, 300 /*大小,MB*/);
        mAliyunVodPlayerView.setOnPreparedListener(new IAliyunVodPlayer.OnPreparedListener() {
            @Override
            public void onPrepared() {
//                logStrs.add(format.format(new Date()) + " 准备成功");
//                Toast.makeText(SkinActivity.this, "准备成功", Toast.LENGTH_SHORT).show();
                mAliyunVodPlayerView.start();
                isPause = false;
                mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                mHandler.sendEmptyMessage(UPDATE_YY);
            }
        });

        mAliyunVodPlayerView.setOnCompletionListener(new IAliyunVodPlayer.OnCompletionListener() {
            @Override
            public void onCompletion() {
//                logStrs.add(format.format(new Date()) + " 播放结束");
//                Toast.makeText(SkinActivity.this, "播放结束", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        mAliyunVodPlayerView.setOnFirstFrameStartListener(new IAliyunVodPlayer.OnFirstFrameStartListener() {
            @Override
            public void onFirstFrameStart() {
                Map<String, String> debugInfo = mAliyunVodPlayerView.getAllDebugInfo();
                long createPts = 0;
                if (debugInfo.get("create_player") != null) {
                    String time = debugInfo.get("create_player");
                    createPts = (long) Double.parseDouble(time);
                    logStrs.add(format.format(new Date(createPts)) + " 播放创建成功");
                }
                if (debugInfo.get("open-url") != null) {
                    String time = debugInfo.get("open-url");
                    long openPts = (long) Double.parseDouble(time) + createPts;
                    logStrs.add(format.format(new Date(openPts)) + " url请求成功");
                }
                if (debugInfo.get("find-stream") != null) {
                    String time = debugInfo.get("find-stream");
                    long findPts = (long) Double.parseDouble(time) + createPts;
                    logStrs.add(format.format(new Date(findPts)) + " 请求流成功");
                }
                if (debugInfo.get("open-stream") != null) {
                    String time = debugInfo.get("open-stream");
                    long openPts = (long) Double.parseDouble(time) + createPts;
                    logStrs.add(format.format(new Date(openPts)) + " 开始传输码流");
                }
                logStrs.add(format.format(new Date()) + " 第一帧播放完成");
                logStrs.add(format.format(new Date()) + " 第一帧播放完成");
                mBar.setVisibility(View.GONE);
            }
        });

        mAliyunVodPlayerView.setOnChangeQualityListener(new IAliyunVodPlayer.OnChangeQualityListener() {
            @Override
            public void onChangeQualitySuccess(String finalQuality) {
                logStrs.add(format.format(new Date()) + " 切换分辨率 " + finalQuality + " 成功");
//                Toast.makeText(SkinActivity.this, "切换分辨率 " + finalQuality + " 成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChangeQualityFail(int code, String msg) {
                logStrs.add(format.format(new Date()) + " 切换分辨率失败：" + msg);
//                Toast.makeText(SkinActivity.this, "切换分辨率失败: " + msg, Toast.LENGTH_SHORT).show();
            }
        });

        mAliyunVodPlayerView.setOnStoppedListner(new IAliyunVodPlayer.OnStoppedListener() {
            @Override
            public void onStopped() {
//                Toast.makeText(SkinActivity.this, "播放器停止成功", Toast.LENGTH_SHORT).show();
            }
        });


        mAliyunVodPlayerView.enableNativeLog();

        setPlaySource();
        xpyName = getIntent().getStringExtra("name");
        mScreenStatusController = new ScreenStatusController(this);
        mScreenStatusController.setScreenStatusListener(new ScreenStatusController.ScreenStatusListener() {
            @Override
            public void onScreenOn() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            @Override
            public void onScreenOff() {

            }
        });

        mScreenStatusController.startListen();
        mScreenStatusController.startListen();
        mScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UnityPlayer.UnitySendMessage("MenuePrefab", "LoadGameScene", "");
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mAliyunVodPlayerView.stop();
                finish();
            }
        });
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAliyunVodPlayerView.stop();
                finish();
            }
        });
        // TODO: 2018/1/22 帧动画
        frameAnim =new AnimationDrawable();
        // 为AnimationDrawable添加动画帧
        addFra();


        // 设置为循环播放
        frameAnim.setOneShot(true);

        // 设置ImageView的背景为AnimationDrawable
        iv_frame.setBackgroundDrawable(frameAnim);

    }

    private void initView() {
        mScanBtn = (ImageView) findViewById(R.id.scanBtn);
        mBackBtn = (ImageView) findViewById(R.id.backBtn);
        mBar = (ProgressBar) findViewById(R.id.play_bar);
        mBj_linear= (LinearLayout) findViewById(R.id.bj_linear);
    }

    private void setPlaySource() {
//        String type = getIntent().getStringExtra("type");
//        if ("authInfo".equals(type)) {
        //auth方式
        //NOTE： 注意过期时间。特别是重播的时候,可能已经过期。所以重播的时候最好重新请求一次服务器。
//            mVid = getIntent().getStringExtra("vid");
//        } else if ("localSource".equals(type)) {
         /*   //本地播放
//            String url = getIntent().getStringExtra("url");

            String str=this.getIntent().getStringExtra("url");
//        int indext=this.getIntent().getStringExtra("videourl").indexOf("*objname*");
            int indext=str.indexOf("*showscanbtn*");
            videoUrl=str.substring(4,indext);
//        unityObjName=this.getIntent().getStringExtra("videourl").substring(indext+9);
            showsys= Integer.parseInt(str.substring(indext+13,indext+14));
            showback= Integer.parseInt(str.substring(indext+27,indext+28));
            if(showsys==0){
                mScanBtn.setVisibility(View.GONE);
            }*/

//            String url="http://handict-supperapp-course.oss-cn-hangzhou.aliyuncs.com/ASGVIDEO/SG_dazuiyu.mp4";
        AliyunLocalSource.AliyunLocalSourceBuilder asb = new AliyunLocalSource.AliyunLocalSourceBuilder();
        asb.setSource(  "http://vd.handict.net/ANT_VIDEO/ANT_cook_2B.mp4");
//        String s = "android.resource://" + getPackageName() + "/" + R.raw.ant_cook;

        Log.i("lclc", "s======>>: " + s);
//        asb.setSource(s);
//        asb.setCoverPath(s);
        mLocalSource = asb.build();
        mAliyunVodPlayerView.setLocalSource(mLocalSource);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updatePlayerViewMode();
        if (mAliyunVodPlayerView != null) {
            mAliyunVodPlayerView.resume();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (mAliyunVodPlayerView != null) {
            mAliyunVodPlayerView.stop();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updatePlayerViewMode();
    }


    private void updatePlayerViewMode() {
        if (mAliyunVodPlayerView != null) {
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {                //转为竖屏了。
                //显示状态栏
//                if (Build.DEVICE.equalsIgnoreCase("mx5")
//                        || Build.DEVICE.equalsIgnoreCase("Redmi Note2")
//                        || Build.DEVICE.equalsIgnoreCase("Z00A_1")) {
////                    getSupportActionBar().show();
//                }
                this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mAliyunVodPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                //设置view的布局,宽高之类
//                ViewGroup.LayoutParams aliVcVideoViewLayoutParams = mAliyunVodPlayerView.getLayoutParams();
//                aliVcVideoViewLayoutParams.height = (int) (ScreenUtils.getWight(this) * 9.0f / 16);
//                aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;

                //设置为小屏状态
                mAliyunVodPlayerView.changeScreenMode(AliyunScreenMode.Small);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {                //转到横屏了。
                //隐藏状态栏
                if (Build.DEVICE.equalsIgnoreCase("mx5")
                        || Build.DEVICE.equalsIgnoreCase("Redmi Note2")
                        || Build.DEVICE.equalsIgnoreCase("Z00A_1")) {
//                    getSupportActionBar().hide();
                } else if (!(Build.DEVICE.equalsIgnoreCase("V4") && Build.MANUFACTURER.equalsIgnoreCase("Meitu"))) {
                    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    mAliyunVodPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }

                //设置view的布局,宽高
                ViewGroup.LayoutParams aliVcVideoViewLayoutParams = mAliyunVodPlayerView.getLayoutParams();
                aliVcVideoViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;

                //设置为全屏状态
                mAliyunVodPlayerView.changeScreenMode(AliyunScreenMode.Full);
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAliyunVodPlayerView != null) {
            mAliyunVodPlayerView.destroy();
            mAliyunVodPlayerView = null;
        }

        mScreenStatusController.stopListen();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mAliyunVodPlayerView != null) {
            boolean handler = mAliyunVodPlayerView.onKeyDown(keyCode, event);
            if (!handler) {
                return false;
            }
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                mAliyunVodPlayerView.start();
//                Toast.makeText(this, "你按下中间键", Toast.LENGTH_SHORT).show();
                break;
            case KeyEvent.KEYCODE_BACK:
                finish();
                break;

        }
        return super.onKeyDown(keyCode, event);
    }


    //________________________________________________________________
    public boolean handleMessage(Message msg) {

        //处理消息
        switch (msg.what) {
            case UPDATE_PROGRESS:
//  加载动画

                //更新时间
                currentPosition = mAliyunVodPlayerView.getCurrentPosition();//当前时间

                // TODO: 2018/1/8
                for (int i = 0; i < yydanciTime.length; i++) {
                    if (currentPosition < yydanciTime[i] + 2000 && currentPosition > yydanciTime[i] - 2000) {
                        YYdanci(yydanciTime[i]);
                    }
                }
              /*  if (currentPosition < yydanciTime[1] + 2000 && currentPosition > yydanciTime[1] - 2000) {
                    YYdanci(yydanciTime[1]);
                }
                if (currentPosition < yydanciTime[2] + 2000 && currentPosition > yydanciTime[2] - 2000) {
                    YYdanci(yydanciTime[2]);
                }*/
                Log.i("lclc", "handleMessage: ");
                mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000); //1秒发送一次

                break;

            case UPDATE_YY:
                // TODO: 2018/1/9  ttxpy
                Log.d("111", "handleMessage: 444444");
                currentPosition = mAliyunVodPlayerView.getCurrentPosition();//当前时间
//                XXXPY(currentPosition,yyxpyTime[0]);

                // TODO: 2018/1/19 循环
                for (int i = 0; i < yyxpyTime.length; i++) {
                    if (currentPosition < yyxpyTime[i] + 1000 && currentPosition > yyxpyTime[i] - 1000) {
                        XXXPY(currentPosition, yyxpyTime[i]);
                    }
                }

              /*  if (currentPosition < yyxpyTime[1] + 1000 && currentPosition > yyxpyTime[1] - 1000) {
                    XXXPY(currentPosition, yyxpyTime[1]);
                }
                if (currentPosition < yyxpyTime[2] + 1000 && currentPosition > yyxpyTime[2] - 1000) {
                    XXXPY(currentPosition, yyxpyTime[2]);
                }*/
                mHandler.sendEmptyMessageDelayed(UPDATE_YY, 200); //200毫秒发送一次
                break;
        }
        //返回 false 消息没处理完 其他人可继续处理
        //返回 true 消息处理完了

        return true;
    }

    /**
     * 语音单词
     */
    private void YYdanci(int time) {
        if (currentPosition < time) {
            isYY = true;
        }

        if (currentPosition >= time && currentPosition < (time + 1000) && isYY) {
            mAliyunVodPlayerView.pause();
            mBar.setVisibility(View.GONE);
            wvw_main.setVisibility(View.VISIBLE);
            wvw_main.startRecord();
            sss();
            isPlay = false;
            mHandler.removeMessages(UPDATE_PROGRESS);
            mHandler.removeCallbacks(mRunnable);

            isYY = false;
        }
        if (!isPlay) {
            mBar.setVisibility(View.GONE);
        }
    }

    /**
     * 某某小朋友
     *
     * @param currentPosition
     */
    private void XXXPY(int currentPosition, int time) {
        if (currentPosition >= time && currentPosition < (time + 200)) {
            //    http://handict-supperapp-course.oss-cn-hangzhou.aliyuncs.com/lcdemo/ttxpy.wav
//            MediaPlayer mp = MediaPlayer.create(this, Uri.parse("http://handict-supperapp-course.oss-cn-hangzhou.aliyuncs.com/lcdemo/ttxpy.wav"));
            // TODO: 2018/1/16
            if (xpyName.equals("tt")) {
                mpp = MediaPlayer.create(this, R.raw.ttxpy);
            }
            if (xpyName.equals("lc")) {
                mpp = MediaPlayer.create(this, R.raw.lcxpy);
            }
            if (xpyName.equals("lx")) {
                mpp = MediaPlayer.create(this, R.raw.lxxpy);
            }
            if (xpyName.equals("cyd")) {
                mpp = MediaPlayer.create(this, R.raw.cydxpy);
            }
            if (xpyName.equals("cc")) {
                mpp = MediaPlayer.create(this, R.raw.ccxpy);
            }

            mpp.start();
        }
    }

    private void setting() {
        Intent intent = new Intent(this, Setting.class);
        startActivity(intent);
    }


    private String sss() {


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SkinActivity2.this);
        boolean api = sp.getBoolean("api", false);
        if (api) {
            switch (status) {
                case STATUS_None:
                    start();
//                    btn.setText("取消");
                    status = STATUS_WaitingReady;
                    break;
                case STATUS_WaitingReady:
                    cancel();
                    status = STATUS_None;
//                    btn.setText("开始");
                    break;
                case STATUS_Ready:
                    cancel();
                    status = STATUS_None;
//                    btn.setText("开始");
                    break;
                case STATUS_Speaking:
                    stop();
                    status = STATUS_Recognition;
//                    btn.setText("识别中");
                    break;
                case STATUS_Recognition:
                    cancel();
                    status = STATUS_None;
//                    btn.setText("开始");
                    break;
            }
        } else {
            start();

        }

        return s;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            onResults(data.getExtras());
        }
    }

    public void bindParams(Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean("tips_sound", true)) {
            intent.putExtra(Constant.EXTRA_SOUND_START, R.raw.bdspeech_recognition_start);
            intent.putExtra(Constant.EXTRA_SOUND_END, R.raw.bdspeech_speech_end);
            intent.putExtra(Constant.EXTRA_SOUND_SUCCESS, R.raw.bdspeech_recognition_success);
            intent.putExtra(Constant.EXTRA_SOUND_ERROR, R.raw.bdspeech_recognition_error);
            intent.putExtra(Constant.EXTRA_SOUND_CANCEL, R.raw.bdspeech_recognition_cancel);
        }
        if (sp.contains(Constant.EXTRA_INFILE)) {
            String tmp = sp.getString(Constant.EXTRA_INFILE, "").replaceAll(",.*", "").trim();
            intent.putExtra(Constant.EXTRA_INFILE, tmp);
        }
        if (sp.getBoolean(Constant.EXTRA_OUTFILE, false)) {
            intent.putExtra(Constant.EXTRA_OUTFILE, "sdcard/outfile.pcm");
        }
        if (sp.getBoolean(Constant.EXTRA_GRAMMAR, false)) {
            intent.putExtra(Constant.EXTRA_GRAMMAR, "assets:///baidu_speech_grammar.bsg");
        }
        if (sp.contains(Constant.EXTRA_SAMPLE)) {
            String tmp = sp.getString(Constant.EXTRA_SAMPLE, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_SAMPLE, Integer.parseInt(tmp));
            }
        }
        if (sp.contains(Constant.EXTRA_LANGUAGE)) {
            String tmp = sp.getString(Constant.EXTRA_LANGUAGE, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_LANGUAGE, tmp);
            }
        }
        if (sp.contains(Constant.EXTRA_NLU)) {
            String tmp = sp.getString(Constant.EXTRA_NLU, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_NLU, tmp);
            }
        }

        if (sp.contains(Constant.EXTRA_VAD)) {
            String tmp = sp.getString(Constant.EXTRA_VAD, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_VAD, tmp);
            }
        }
        String prop = null;
        if (sp.contains(Constant.EXTRA_PROP)) {
            String tmp = sp.getString(Constant.EXTRA_PROP, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_PROP, Integer.parseInt(tmp));
                prop = tmp;
            }
        }

        // offline asr
        {
            intent.putExtra(Constant.EXTRA_OFFLINE_ASR_BASE_FILE_PATH, "/sdcard/easr/s_1");
            if (null != prop) {
                int propInt = Integer.parseInt(prop);
                if (propInt == 10060) {
                    intent.putExtra(Constant.EXTRA_OFFLINE_LM_RES_FILE_PATH, "/sdcard/easr/s_2_Navi");
                } else if (propInt == 20000) {
                    intent.putExtra(Constant.EXTRA_OFFLINE_LM_RES_FILE_PATH, "/sdcard/easr/s_2_InputMethod");
                }
            }
            intent.putExtra(Constant.EXTRA_OFFLINE_SLOT_DATA, buildTestSlotData());
        }
    }

    private String buildTestSlotData() {
        JSONObject slotData = new JSONObject();
        JSONArray name = new JSONArray().put("李涌泉").put("郭下纶");
        JSONArray song = new JSONArray().put("七里香").put("发如雪");
        JSONArray artist = new JSONArray().put("周杰伦").put("李世龙");
        JSONArray app = new JSONArray().put("手机百度").put("百度地图");
        JSONArray usercommand = new JSONArray().put("关灯").put("开门");
        try {
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_NAME, name);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_SONG, song);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_ARTIST, artist);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_APP, app);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_USERCOMMAND, usercommand);
        } catch (JSONException e) {

        }
        return slotData.toString();
    }

    private void start() {
//        txtLog.setText("");
//        print("点击了“开始”");
        Intent intent = new Intent();
        bindParams(intent);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        {

            String args = sp.getString("args", "");
            if (null != args) {
//                print("参数集：" + args);
                intent.putExtra("args", args);
            }
        }
        boolean api = sp.getBoolean("api", false);
        if (api) {
            speechEndTime = -1;
            speechRecognizer.startListening(intent);
        } else {
            intent.setAction("com.baidu.action.RECOGNIZE_SPEECH");
            startActivityForResult(intent, REQUEST_UI);
        }

//        txtResult.setText("");
    }

    private void stop() {
        speechRecognizer.stopListening();
//        print("点击了“说完了”");
    }

    private void cancel() {
        speechRecognizer.cancel();
        status = STATUS_None;
//        print("点击了“取消”");
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        status = STATUS_Ready;
//        print("准备就绪,可以开始说话");
    }

    @Override
    public void onBeginningOfSpeech() {
        time = System.currentTimeMillis();
        status = STATUS_Speaking;
//        btn.setText("说完了");
//        print("检测到用户的已经开始说话");
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        speechEndTime = System.currentTimeMillis();
        status = STATUS_Recognition;
//        print("检测到用户的已经停止说话");
//        btn.setText("识别中");
    }

    @Override
    public void onError(int error) {
        MediaPlayer mp = null;
        time = 0;
        status = STATUS_None;
        StringBuilder sb = new StringBuilder();
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                sb.append("音频问题");
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                sb.append("没有语音输入");
//                Log.i(TAG, "onError: " + "2222222222222222");
                if (isDouble != 1 || yy < 2) {
                    for (int i = 0; i < yydanciTime.length; i++) {
                        if (currentPosition < yydanciTime[i] + 2000 && currentPosition > yydanciTime[i] - 2000) {
                            YYmypp(yydanciTime[i] - 7000);
                        }
                    }
                   /* if (currentPosition < yydanciTime[1] + 2000 && currentPosition > yydanciTime[1] - 2000) {
                        YYmypp(yydanciTime[1] - 7000);
                    }
                    if (currentPosition < yydanciTime[2] + 2000 && currentPosition > yydanciTime[2] - 2000) {
                        YYmypp(yydanciTime[2] - 7000);
                    }*/
                } else {
                    mAliyunVodPlayerView.start();
                    isDouble = 0;
                    wvw_main.setVisibility(View.GONE);
                }
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                sb.append("其它客户端错误");
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                sb.append("权限不足");
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                sb.append("网络问题");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                sb.append("没有匹配的识别结果");
//                Log.i(TAG, "onError: " + "1111111111111111");
                if (isDouble != 1 || yy < 2) {
                    for (int i = 0; i < yydanciTime.length; i++) {
                        if (currentPosition < yydanciTime[i] + 2000 && currentPosition > yydanciTime[i] - 2000) {
                            YYmypp(yydanciTime[i] - 7000);
                        }
                    }
                   /* if (currentPosition < yydanciTime[1] + 2000 && currentPosition > yydanciTime[1] - 2000) {
                        YYmypp(yydanciTime[1] - 7000);
                    }
                    if (currentPosition < yydanciTime[2] + 2000 && currentPosition > yydanciTime[2] - 2000) {
                        YYmypp(yydanciTime[2] - 7000);
                    }*/
                } else {
                    mAliyunVodPlayerView.start();
                    isDouble = 0;
                    wvw_main.setVisibility(View.GONE);

                }
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                sb.append("引擎忙");
                break;
            case SpeechRecognizer.ERROR_SERVER:
                sb.append("服务端错误");
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                sb.append("连接超时");
                break;
        }
        sb.append(":" + error);
//        print("识别失败：" + sb.toString());
//        btn.setText("开始");
//		UnityPlayer.UnitySendMessage("Canvas", "messgae", s);
//		finish();
    }

    /**
     * 语音没有匹配
     * int time  回答错误前跳时间节点
     */
    private void YYmypp(final int time) {
//        wvw_main.setVisibility(View.GONE);
        MediaPlayer mp;
        if (yy < 2) {
            yy++;
//            Log.i(TAG, "y=====" + yy);
            sss();
        } else if (isDouble < 1) {
            wvw_main.setVisibility(View.GONE);
            mp = MediaPlayer.create(this, R.raw.qnzsyb);
            mp.start();
            yy = 0;
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mAliyunVodPlayerView.seekTo(time);
                    isDouble++;
                    if (!isPlay) {
                        isPlay = true;
                        mAliyunVodPlayerView.start();
                        isPause = false;
                        mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                    }
                }
            });
        }
    }

    @Override
    public void onResults(Bundle results) {
//        Log.i(TAG, "onResults: ");
        long end2finish = System.currentTimeMillis() - speechEndTime;
        status = STATUS_None;
        ArrayList<String> nbest = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//        print("识别成功：" + Arrays.toString(nbest.toArray(new String[nbest.size()])));
        String regEx = "[\\[\\]]";
        Pattern p = Pattern.compile(regEx);
        final Matcher m = p.matcher(Arrays.toString(nbest.toArray(new String[nbest.size()])));
        String trim1 = m.replaceAll("").trim();
//        Log.i(TAG, "trim1: " + trim1);
        String[] split1 = null;
        split1 = trim1.split(", ");
//        Log.i(TAG, "split: " + split1);
        boolean isZLYB = true;
        // TODO: 2018/1/9 111111111111111111111
        for (int i = 0; i < yydanciTime.length; i++) {
            if (currentPosition < yydanciTime[i] + 2000 && currentPosition > yydanciTime[i] - 2000) {
                YYppda(split1, isZLYB, yydanciTime[i] - 7000, yydanciS);
            }
        }
       /* if (currentPosition < yydanciTime[1] + 2000 && currentPosition > yydanciTime[1] - 2000) {
            YYppda(split1, isZLYB, yydanciTime[1] - 7000, yydanciS);
        }
        if (currentPosition < yydanciTime[2] + 2000 && currentPosition > yydanciTime[2] - 2000) {
            YYppda(split1, isZLYB, yydanciTime[2] - 7000, yydanciS);
        }*/
        String json_res = results.getString("origin_result");
        json_res = json_res.replaceAll("\\\\n\"", "");
        json_res = json_res.replaceAll("\\\\", "");
        json_res = json_res.replace("\"{", "{");
//		json_res= json_res.replace("\","");
//        Log.i(TAG, "onResults: " + json_res);
        parseJsonMulti(json_res);
        try {
//            print("s=" + s);

//            print("origin_result=\n" + new JSONObject(json_res).toString(4));
        } catch (Exception e) {
//            print("origin_result=[warning: bad json]\n" + json_res);
        }
//        btn.setText("开始");
        String strEnd2Finish = "";
        if (end2finish < 60 * 1000) {
            strEnd2Finish = "(waited " + end2finish + "ms)";
        }
//        txtResult.setText(nbest.get(0) + strEnd2Finish);

        time = 0;

//		finish();
    }

    /**
     * 语音匹配答案
     *
     * @param split1 识别结果
     * @param isZLYB (忘了)
     */
    private void YYppda(String[] split1, boolean isZLYB, final int time, String[] yydanciS) {
        yy++;
        for (int i = 0; i < split1.length; i++) {
//            if (split1[i].equals(answer)) {
            if ((split1[i].indexOf(yydanciS[0]) != -1 || split1[i].indexOf(yydanciS[1]) != -1 || split1[i].indexOf(yydanciS[2]) != -1) && isZLYB) {
//                Log.i(TAG, "YES");
                wvw_main.setVisibility(View.GONE);
                MediaPlayer mp = MediaPlayer.create(this, R.raw.nsdzb);
                yy = 0;
                isDouble = 0;
                mp.start();
                //小朋友真棒动画
//                XPYZBdh();
//                XPYZBdh2();
                mmswoon();
//
//                mPlay.setBackgroundResource(R.mipmap.play_button);

                isZLYB = false;
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mAliyunVodPlayerView.start();
                    }
                });
            } else {
//                Log.i(TAG, "XDADADAS" + split1[i]);
            }
        }
        if (isZLYB && yy < 3) {

//            Log.i(TAG, "y=====" + yy);
            sss();
        } else if (isZLYB && isDouble < 1) {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.qnzsyb);
            wvw_main.setVisibility(View.GONE);
            mp.start();
            yy = 0;
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                    mAliyunVodPlayerView.seekTo(time);
                    isDouble++;
                    if (!isPlay) {
                        isPlay = true;
                        mAliyunVodPlayerView.start();
                        isPause = false;
                        mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                    }
                }
            });
        }
//        Log.i(TAG, "yy==: " + yy);
        if (isDouble == 1 && yy == 3) {
            mAliyunVodPlayerView.start();
            isDouble = 0;
            yy = 0;
            wvw_main.setVisibility(View.GONE);
        }

    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> nbest = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (nbest.size() > 0) {
//            print("~临时识别结果：" + Arrays.toString(nbest.toArray(new String[0])));
            /*String regEx = "[\\[\\]]";
            Pattern p = Pattern.compile(regEx);
            final Matcher m = p.matcher(Arrays.toString(nbest.toArray(new String[0])));
            String trim1 = m.replaceAll("").trim();
            Log.i(TAG, "trim1: " + trim1);
            String[] split1 = null;
            split1 = trim1.split(", ");
            Log.i(TAG, "split: " + split1);
            boolean isZLYB = true;
            if( currentPosition<yydanciTime[0]+2000&&currentPosition>yydanciTime[0]-2000  ){
                YYppda(split1, isZLYB,yydanciTime[0]-7000,"star");
            }
            if( currentPosition<yydanciTime[1]+2000&&currentPosition>yydanciTime[1]-2000  ){
                YYppda(split1, isZLYB,yydanciTime[1]-7000,"star");
            }
            if( currentPosition<yydanciTime[2]+2000&&currentPosition>yydanciTime[2]-2000  ){
                YYppda(split1, isZLYB,yydanciTime[2]-7000,"star");
            }*/
//            txtResult.setText(nbest.get(0));
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        switch (eventType) {
            case EVENT_ERROR:
                String reason = params.get("reason") + "";
//                print("EVENT_ERROR, " + reason);
                break;
            case VoiceRecognitionService.EVENT_ENGINE_SWITCH:
                int type = params.getInt("engine_type");
//                print("*引擎切换至" + (type == 0 ? "在线" : "离线"));
                break;
        }
    }

    long time;


    private void parseJsonMulti(String strResult) {
//        Log.i(TAG, "parseJsonMulti: ");
        try {
            JSONObject jsonObjs = new JSONObject(strResult).getJSONObject("content");
            JSONObject json_res = jsonObjs.getJSONObject("json_res");
            JSONArray results = json_res.getJSONArray("results");
//            Log.i(TAG, "results: " + results);
            JSONObject o = (JSONObject) results.get(0);
            JSONObject object = o.getJSONObject("object");
            String answer = object.getString("answer");
//			print("JSON=====" + answer);
            JSONObject item = jsonObjs.getJSONObject("item");
//			print("item===="+item);
            //
            Toast.makeText(this, answer, Toast.LENGTH_SHORT).show();
            s = answer;
            if (s.equals("")) {
//				UnityPlayer.UnitySendMessage("Canvas", "messgae", "蛋蛋没听清楚你说什么请再来一遍");
            } else {
//				UnityPlayer.UnitySendMessage("Canvas", "messgae", s);
            }
//			UnityPlayer.UnitySendMessage("Canvas", "t", s);
            if (answer.equals("yes")) {
//                mCb.setChecked(true);
//                txtResult.setText(answer);
            } else if (answer.equals("no")) {
//                mCb.setChecked(false);
//                txtResult.setText(answer);
            } else {
//                txtResult.setText("请重试一遍");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 小朋友真棒 动画1
     */
    private void XPYZBdh() {

        Animation animation11 = AnimationUtils.loadAnimation(SkinActivity2.this, R.anim.xpyzb0);
        mPlay3.setVisibility(View.VISIBLE);
        mPlay3.startAnimation(animation11);
        animation11.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation animation1 = AnimationUtils.loadAnimation(SkinActivity2.this, R.anim.xpyzb1);
                mPlay3.startAnimation(animation1);
                animation1.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Animation animation1 = AnimationUtils.loadAnimation(SkinActivity2.this, R.anim.xpyzb_1);
                        mPlay3.startAnimation(animation1);
                        animation1.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                Animation animation1 = AnimationUtils.loadAnimation(SkinActivity2.this, R.anim.xpyzb2);
                                mPlay3.startAnimation(animation1);
                                animation1.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        Animation animation1 = AnimationUtils.loadAnimation(SkinActivity2.this, R.anim.xpyzb_2);
                                        mPlay3.startAnimation(animation1);
                                        animation1.setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                Animation animation1 = AnimationUtils.loadAnimation(SkinActivity2.this, R.anim.xpyzb3);
                                                mPlay3.startAnimation(animation1);
                                                animation1.setAnimationListener(new Animation.AnimationListener() {
                                                    @Override
                                                    public void onAnimationStart(Animation animation) {

                                                    }

                                                    @Override
                                                    public void onAnimationEnd(Animation animation) {
                                                        Animation animation1 = AnimationUtils.loadAnimation(SkinActivity2.this, R.anim.xpyzb_3);
                                                        mPlay3.startAnimation(animation1);
                                                        animation1.setAnimationListener(new Animation.AnimationListener() {
                                                            @Override
                                                            public void onAnimationStart(Animation animation) {

                                                            }

                                                            @Override
                                                            public void onAnimationEnd(Animation animation) {
                                                                mPlay3.setVisibility(View.GONE);
                                                                isHide(100);
                                                            }

                                                            @Override
                                                            public void onAnimationRepeat(Animation animation) {

                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onAnimationRepeat(Animation animation) {

                                                    }
                                                });
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //消失
    private void isHide(int i) {

        if (isPlay) {

            mRunnable = new Runnable() {
                @Override
                public void run() {

                }
            };
            mHandler.postDelayed(mRunnable, i);
        }


    }


    //显示


    public void onClick(View v) {
//        sss();
//        XPYZBdh2();
//        startaa();
//        mmswoon();
        start();
    }

    private void XPYZBdh2() {
        animationView.setImageAssetsFolder("images/");
        animationView.setAnimation("like.json");
        animationView.loop(false);
        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation();


        animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animationView.setVisibility(View.GONE);
            }

//此方法未进行回调,playAnimator,cancelAnimator,pauseAnimator,回调的都是onAnimationEnd方法

            @Override
            public void onAnimationCancel(Animator animation) {
            }

//当loop=true的时候才会回调此方法//

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }


    public ComponentName getActivity() {
        return SkinActivity2;
    }
    // TODO: 2018/1/22  帧动画
    private void addFra() {
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10001), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10002), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10003), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10004), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10005), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10006), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10007), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10008), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10009), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10010), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10011), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10012), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10013), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10014), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10015), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10016), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10017), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10018), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10019), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10020), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10021), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10022), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10023), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10024), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10025), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10026), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10027), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10028), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10029), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10030), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10031), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10032), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10033), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10034), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10035), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10036), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10037), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10038), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10039), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10040), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10041), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10042), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10043), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10044), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10045), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10046), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10047), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10048), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10049), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10050), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10051), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10052), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10053), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10054), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10055), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10056), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10057), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10058), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10059), 33);
        frameAnim.addFrame(getResources().getDrawable(R.mipmap.zndmz10060), 33);
    }
    protected void startaa() {
        iv_frame.setVisibility(View.VISIBLE);
        if (frameAnim != null && !frameAnim.isRunning()) {
            frameAnim.start();
            Toast.makeText(SkinActivity2.this, "开始播放",  Toast.LENGTH_LONG).show();
        }
    }
    protected void stopaa() {
        if (frameAnim != null && frameAnim.isRunning()) {
            frameAnim.stop();
            Toast.makeText(SkinActivity2.this, "停止播放", Toast.LENGTH_LONG).show();
        }
    }


    public void mmswoon(){
        mBj_linear.setVisibility(View.VISIBLE);
        iv_frame.setBackgroundDrawable(frameAnim);
        frameAnim.start();

        int duration = 0;
        for(int i=0;i<frameAnim.getNumberOfFrames();i++){
            duration += frameAnim.getDuration(i);
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                //此处调用第二个动画播放方法
                Log.i("zxzxzxzxzx", "run: ");
                mBj_linear.setVisibility(View.GONE);
                isHide(100);
            }
        }, duration);

    }






}
