package com.ynu.diary.entries.diary;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ynu.diary.R;
import com.ynu.diary.shared.ThemeManager;
import com.ynu.diary.shared.gui.MyDiaryButton;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

@SuppressLint("ValidFragment")
public class SoundRecordingDialogFragment extends DialogFragment implements View.OnClickListener {
    public SoundRecordingDialogFragment(soundRecordingCallBack callback) {
        this.callBack = callback;
    }

    // 录音文件存放目录
    final String audioSaveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Diary/";
    private soundRecordingCallBack callBack;
    private LinearLayout LL_sound_recording;
    private Button btn_start, btn_stop;
    private TextView text_time;
    private MyDiaryButton But_cancel, But_ok;

    public interface soundRecordingCallBack {
        void addSoundRecording(String filePath);
    }


    // 录音功能相关
    MediaRecorder mMediaRecorder; // MediaRecorder 实例
    boolean isRecording; // 录音状态
    String fileName = ""; // 录音文件的名称
    String filePath = ""; // 录音文件存储路径
    Thread timeThread; // 记录录音时长的线程
    int timeCount; // 录音时长 计数
    final int TIME_COUNT = 0x101;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_fragment_sound_recording, container);
        LL_sound_recording = (LinearLayout) rootView.findViewById(R.id.LL_sound_recording);
        LL_sound_recording.setBackgroundColor(ThemeManager.getInstance().getThemeMainColor(getActivity()));
        text_time = (TextView) rootView.findViewById(R.id.text_time);
        btn_start = (Button) rootView.findViewById(R.id.btn_start);
        btn_start.setOnClickListener(this);
        btn_stop = (Button) rootView.findViewById(R.id.btn_stop);
        btn_stop.setOnClickListener(this);
        But_ok = (MyDiaryButton) rootView.findViewById(R.id.But_ok);
        But_ok.setOnClickListener(this);
        But_cancel = (MyDiaryButton) rootView.findViewById(R.id.But_cancel);
        But_cancel.setOnClickListener(this);
//        callBack = soundRecordingCallBack;
        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                // 开始录音
                btn_start.setEnabled(false);
                btn_stop.setEnabled(true);
                But_ok.setEnabled(false);
                But_cancel.setEnabled(false);
                startRecord();
                isRecording = true;
                // 初始化录音时长记录
                timeThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        countTime();
                    }
                });
                timeThread.start();
                break;
            case R.id.btn_stop:
                // 停止录音
                btn_start.setEnabled(false);
                btn_stop.setEnabled(false);
                But_ok.setEnabled(true);
                But_cancel.setEnabled(true);

                stopRecord();
                isRecording = false;
                break;
            case R.id.But_ok:
                callBack.addSoundRecording(filePath);
                dismiss();
                break;
            case R.id.But_cancel:
                File file = new File(filePath);
                if (file.exists())
                    file.delete();
                dismiss();
                break;
        }
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        //有一些网友反应在5.0以上在调用stop的时候会报错，翻阅了一下谷歌文档发现上面确实写的有可能会报错的情况，捕获异常清理一下就行了，感谢大家反馈！
        try {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
//            filePath = "";

        } catch (RuntimeException e) {
            Log.i("Recording", e.toString());
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;

            File file = new File(filePath);
            if (file.exists())
                file.delete();

//            filePath = "";
        }
    }

    // 记录录音时长
    private void countTime() {
        while (isRecording) {
            Log.d("Recording", "正在录音");
            timeCount++;
            Message msg = Message.obtain();
            msg.what = TIME_COUNT;
            msg.obj = timeCount;
            myHandler.sendMessage(msg);
            try {
                timeThread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d("Recording", "结束录音");
        timeCount = 0;
        Message msg = Message.obtain();
        msg.what = TIME_COUNT;
        msg.obj = timeCount;
        myHandler.sendMessage(msg);
    }


    /**
     * 开始录音 使用amr格式
     * 录音文件
     *
     * @return
     */
    public void startRecord() {
        // 开始录音
        /* ①Initial：实例化MediaRecorder对象 */
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        try {
            /* ②setAudioSource/setVedioSource */
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            fileName = DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA)) + ".m4a";
            File file = new File(audioSaveDir);
            if (!file.exists()) {// 目录存在返回false
                file.mkdirs();// 创建一个目录
            }
            filePath = audioSaveDir + fileName;
            /* ③准备 */
            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.prepare();
            /* ④开始 */
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            Log.i("Recording", "call startAmr(File mRecAudioFile) failed!" + e.getMessage());
        } catch (IOException e) {
            Log.i("Recording", "call startAmr(File mRecAudioFile) failed!" + e.getMessage());
        }
    }


    // 格式化 录音时长为 时:分:秒
    public static String FormatMiss(int miss) {
        String hh = miss / 3600 > 9 ? miss / 3600 + "" : "0" + miss / 3600;
        String mm = (miss % 3600) / 60 > 9 ? (miss % 3600) / 60 + "" : "0" + (miss % 3600) / 60;
        String ss = (miss % 3600) % 60 > 9 ? (miss % 3600) % 60 + "" : "0" + (miss % 3600) % 60;
        return hh + ":" + mm + ":" + ss;
    }


    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TIME_COUNT) {
                int count = (int) msg.obj;
                Log.d("Recording", "count == " + count);
                text_time.setText(FormatMiss(count));
            }
        }
    };
}
