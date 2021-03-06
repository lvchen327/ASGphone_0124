package com.handict.superapp_mobile.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

//语音波形视图
public class VoiceWaveView extends View {

    private final static int MODE_RECORDING = 0;
    private final static int MODE_PLAYING = 1;

    private long duration = 0;
    private long playSleepTime = 0;

    private int current_position = 1;
    private int maxLines = 0;
    private int mode = -1;

    private boolean record_flag = false;  //整体运行标识
    private boolean play_flag = false;  //整体运行标识
    private boolean isRecordPause = false;  //停止画图线程标识
    private boolean isPlayPause = false;   //是否需要重绘标识

    private final String COLOR_BACKGROUND = "#99181818";
    private final String COLOR_LINE = "#ffffff";
    private final String COLOR_LINE_UNPLAYED = "#99ffffff";

    private final float X_DIVIDER_WIDTH = 55; //线条间隔
    private final float LINE_WIDTH = 6; //线宽
    private final int MAX_TIME = 60; //最大录音时间

    private Context context;
    private VoiceDrawTask voiceDrawTask;
    private VoicePlayDrawTask voicePlayDrawTask;
    private Paint paint;
    private Timer timer;
    private Random random = new Random();
    private int indicatorColor = 0xFF389C65;
    private LinkedList<WaveBean> linkedList;
    private LinkedList<WaveBean> allLinkedList;
    private LinkedList<WaveBean> compressLinkedList;

    public VoiceWaveView(Context context) {
        this(context, null);
    }

    public VoiceWaveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public VoiceWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        // TODO Auto-generated constructor stub
        super(context, attrs, defStyleAttr);
        this.context = context;
        timer = new Timer();
        paint = new Paint();
        paint.setFakeBoldText(true);  //设置粗体
        paint.setStrokeWidth(LINE_WIDTH); //设置线宽
        allLinkedList = new LinkedList<WaveBean>();
        linkedList = new LinkedList<WaveBean>();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        maxLines = (int) ((getWidth()-10) / (X_DIVIDER_WIDTH));  //根据控件宽度计算可以容纳单个波纹的最大个数
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        if(mode == MODE_RECORDING) {
            paint.setColor(indicatorColor);
//            paint.setColor(Color.WHITE);
            //录制最大时间
            if (duration/1000 == MAX_TIME || isRecordPause) {
                //获得压缩后的波形并画出
                compressLinkedList = getCompressLinkedList();
                drawWave(canvas, compressLinkedList);
                record_flag = false;
            } else {
                WaveBean wave = new WaveBean(random.nextInt(30)+1);
                //链表长度超过能显示的最大数
                if (linkedList.size() > maxLines) {
                    allLinkedList.add(linkedList.getFirst());
                    linkedList.removeFirst(); //移除链表头
                }
                linkedList.add(wave); //加入表尾
                drawWave(canvas, linkedList);
            }
        } else if (mode == MODE_PLAYING) {
            if(!isPlayPause) {
                if (current_position == compressLinkedList.size()) {
                    drawPlayWave(canvas, current_position);
                    play_flag = false;
                } else {
                    drawPlayWave(canvas, current_position);
                    current_position++;
                }
            } else {
                drawPlayWave(canvas, current_position);
            }
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if(visibility != VISIBLE)
            releaseAll();
    }

    private void drawWave(Canvas canvas, LinkedList<WaveBean> linkedList) {
        int i = linkedList.size();
        for (WaveBean bean : linkedList) {
            //从表头开始画
            canvas.drawLine(bean.WIDTH - i * X_DIVIDER_WIDTH, bean.HEIGHT_HALF - bean.getYoffset(), bean.WIDTH - i * X_DIVIDER_WIDTH, bean.HEIGHT_HALF + bean.getYoffset(), paint);
            i--;
        }
    }

    private void drawPlayWave(Canvas canvas, int current_position) {
        int i = compressLinkedList.size();
        for (WaveBean bean : compressLinkedList) {
            //从表头开始画
            if (compressLinkedList.size() - i <= current_position)
                paint.setColor(Color.parseColor(COLOR_LINE));
            else
                paint.setColor(Color.parseColor(COLOR_LINE_UNPLAYED));
            canvas.drawLine(bean.WIDTH - i * X_DIVIDER_WIDTH, bean.HEIGHT_HALF - bean.getYoffset(), bean.WIDTH - i * X_DIVIDER_WIDTH, bean.HEIGHT_HALF + bean.getYoffset(), paint);
            i--;
        }
    }

    //清屏画背景
    private void drawBackground(Canvas canvas) {
        if(canvas != null) {
//            canvas.drawColor(Color.parseColor(COLOR_BACKGROUND));
        }
    }

    //压缩波形生成MAX_LINES个的波形图
    private LinkedList<WaveBean> getCompressLinkedList() {
        if(allLinkedList.size() == 0) {
            int remain_size = maxLines - linkedList.size();
            LinkedList<WaveBean> compressList = new LinkedList<WaveBean>();
            if(remain_size >= linkedList.size()) {
                int compress_ratio = remain_size / linkedList.size();
                for(WaveBean waveBean : linkedList) {
                    compressList.add(waveBean);
                    for(int i = 0; i < compress_ratio; i++)
                        compressList.add(new WaveBean(waveBean.getYoffset()));
                }
            } else {
                int compress_ratio = linkedList.size() / remain_size;
                int current_postion = 1;
                for(WaveBean waveBean : linkedList) {
                    compressList.add(waveBean);
                    if(current_postion % compress_ratio == 0)
                        compressList.add(new WaveBean(waveBean.getYoffset()));
                    current_postion++;
                }
            }
            return compressList;
        } else {
            allLinkedList.addAll(linkedList);
            LinkedList<WaveBean> compressList = new LinkedList<WaveBean>();
            int compress_ratio = allLinkedList.size()/maxLines;
            float average = 0;
            for (int i = 1; i <= allLinkedList.size(); i++) {
                if(i % compress_ratio == 0) {
                    if(compressList.size() < maxLines) {
                        average = average == 0 ? allLinkedList.get(i-1).getYoffset(): average;
                        compressList.add(new WaveBean(average / compress_ratio));
                        average = 0;
                    } else {
                        return compressList;
                    }
                } else {
                    average += allLinkedList.get(i-1).getYoffset();
                }
            }
            return compressList;
        }
    }

    public void startRecord() {
        releaseAll();
        mode = MODE_RECORDING;
        isRecordPause = false;
        duration = 0;
        voiceDrawTask = new VoiceDrawTask();  //波纹画图线程初始化
        timer.schedule(voiceDrawTask, 200);   //延迟200ms执行
    }

    public void pauseRecord() {
        isRecordPause = true;
    }

    public void stopRecord() {
        releaseAll();
    }

    //启动语音播放波纹，必须在stopRecord后或者达到最长录音时间后调用
    public void startPlay() {
        if(compressLinkedList != null) {
            releaseThread();
            mode = MODE_PLAYING;
            isPlayPause = false;
            current_position = 1;
            playSleepTime = duration / compressLinkedList.size();  //根据语音时长计算单个波纹间隔
            voicePlayDrawTask = new VoicePlayDrawTask();  //语音播放画图线程初始化
            timer.schedule(voicePlayDrawTask, 200);  //延迟200ms执行
        }
    }

    public void pausePlay() {
        if (isPlayPause)
            isPlayPause = false;
        else
            isPlayPause = true;
    }

    private void releaseAll() {
        releaseThread();
        if(allLinkedList != null) {
            allLinkedList.clear();
        }
        if(linkedList != null) {
            linkedList.clear();
        }
        if(compressLinkedList != null)
            compressLinkedList.clear();

    }

    private void releaseThread() {
        if(voiceDrawTask != null && record_flag)
            voiceDrawTask.cancel();
        if(voicePlayDrawTask != null && play_flag)
            voicePlayDrawTask.cancel();
    }

    //绘制波形线程
    private class VoiceDrawTask extends TimerTask {

        public VoiceDrawTask() {
            record_flag = true;
        }

        @Override
        public boolean cancel() {
            record_flag = false;
            return true;
        }

        @Override
        public void run() {
            while (record_flag) {
                try {
                    postInvalidate();
                    Thread.sleep(100);
                    duration += 100;
                } catch (Exception ex) {
                }
            }
        }
    }

    //    //绘图播放波形线程
    private class VoicePlayDrawTask extends TimerTask {

        public VoicePlayDrawTask() {
            play_flag = true;
        }

        @Override
        public boolean cancel() {
            // TODO Auto-generated method stub\
            play_flag = false;
            return true;
        }

        @Override
        public void run() {
            while(play_flag) {
                try {
                    postInvalidate();
                    Thread.sleep(playSleepTime);
                } catch (Exception ex) {

                }
            }
        }
    }

    private class WaveBean {
        private final float HEIGHT_HALF = getHeight()/2;
        private final float WIDTH = getWidth();
        private float y_offset;

        public WaveBean(float y_offset) {
            super();
            this.y_offset = y_offset*3;
        }

        public float getYoffset() {
            return y_offset;
        }
    }

}

