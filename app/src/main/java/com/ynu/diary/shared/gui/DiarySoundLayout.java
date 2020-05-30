package com.ynu.diary.shared.gui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ynu.diary.R;

public class DiarySoundLayout extends LinearLayout {
    private ImageView IV_diary_sound_delete;
    private ImageView IV_diary_sound_play;

    public DiarySoundLayout(Activity activity) {
        super(activity);
        View v = LayoutInflater.from(activity).inflate(R.layout.layout_diarysound, this, true);
        IV_diary_sound_delete = (ImageView) v.findViewById(R.id.IV_diary_sound_delete);
        IV_diary_sound_play = (ImageView) v.findViewById(R.id.IV_diary_sound_play);
    }

    public void setPlayBtnOnClick(OnClickListener listener) {
        IV_diary_sound_play.setOnClickListener(listener);
    }

    public void setDeleteOnClick(OnClickListener listener) {
        IV_diary_sound_delete.setOnClickListener(listener);
    }

    public void setPlayBtnPositionTag(int position) {
        IV_diary_sound_play.setTag(position);
    }

    public void setDeletePositionTag(int position) {
        IV_diary_sound_delete.setTag(position);
    }

    public void setVisibleViewByMode(boolean isEditMode) {
        if (isEditMode) {
            IV_diary_sound_delete.setVisibility(VISIBLE);
        } else {
            IV_diary_sound_delete.setVisibility(GONE);
        }
    }
}
