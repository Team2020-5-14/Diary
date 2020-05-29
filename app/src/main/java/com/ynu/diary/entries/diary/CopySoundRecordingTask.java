package com.ynu.diary.entries.diary;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ynu.diary.R;
import com.ynu.diary.shared.FileManager;

import java.io.File;

public class CopySoundRecordingTask extends AsyncTask<Void, Void, String> {
    private CopySoundRecordingTask.CopySoundRecordingCallBack callBack;
    private String srcFilePath;
    @SuppressLint("StaticFieldLeak")
    private ProgressDialog progressDialog;
    private FileManager fileManager;

    public CopySoundRecordingTask(Context context, String srcFilePath,
                                  FileManager fileManager, CopySoundRecordingCallBack callBack) {
        this.srcFilePath = srcFilePath;
        this.fileManager = fileManager;
        this.callBack = callBack;
        this.progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.process_dialog_loading));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar);
        progressDialog.show();
    }

    @Override
    protected String doInBackground(Void... voids) {
        return saveSoundRecordingToTemp();
    }

    @Override
    protected void onPostExecute(String fileName) {
        super.onPostExecute(fileName);
        progressDialog.dismiss();
        callBack.onCopySoundRecordingCompiled(fileName);
    }

    private String saveSoundRecordingToTemp() {
        String fileName = FileManager.createRandomFileName();
        try {
            File file = new File(srcFilePath); //源文件
            if (file.renameTo(new File(fileManager.getDirAbsolutePath() + "/" + fileName))) //源文件移动至目标文件目录
            {
                Log.i("saveSoundRecToTemp", "Success!");
            } else {
                Log.e("saveSoundRecToTemp", "Failed!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    public interface CopySoundRecordingCallBack {
        void onCopySoundRecordingCompiled(String fileName);
    }
}
