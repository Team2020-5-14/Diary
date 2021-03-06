package com.ynu.diary.entries.diary.item;

import android.app.Activity;
import android.net.Uri;
import android.view.View;

import com.ynu.diary.shared.gui.DiarySoundLayout;

public class DiarySound implements IDairyRow {
    private DiarySoundLayout diarySoundLayout;
    private String soundFileName;
    private Uri soundUri;
    private int position;

    public DiarySound(Activity activity) {
        diarySoundLayout = new DiarySoundLayout(activity);
        //Default is editable
        setEditMode(true);
    }

    public void setPlayBtnClickListener(View.OnClickListener clickListener) {
        diarySoundLayout.setPlayBtnOnClick(clickListener);
    }

    public void setPlayBtnPositionTag(int playBtnPositionTag) {
        diarySoundLayout.setPlayBtnPositionTag(playBtnPositionTag);
    }

    public void setDeleteClickListener(View.OnClickListener clickListener) {
        diarySoundLayout.setDeleteOnClick(clickListener);
    }

    public void setSound(Uri soundUri, String soundFileName) {
        this.soundUri = soundUri;
        this.soundFileName = soundFileName;
    }

    public void setSoundFileName(String soundFileName) {
        this.soundFileName = soundFileName;
    }

    @Override
    public String getContent() {
        return soundFileName;
    }

    @Override
    public void setContent(String content) {
    }

    @Override
    public int getType() {
        return TYPE_SOUND;
    }

    @Override
    public View getView() {
        return diarySoundLayout;
    }

    @Override
    public void setEditMode(boolean isEditMode) {
        diarySoundLayout.setVisibleViewByMode(isEditMode);
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
        //When  content is modified(e.g.insert or delete) , update setDeletePositionTag
        diarySoundLayout.setDeletePositionTag(position);
    }
}
