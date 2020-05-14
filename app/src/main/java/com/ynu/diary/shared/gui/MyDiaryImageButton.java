package com.ynu.diary.shared.gui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.ynu.diary.R;
import com.ynu.diary.shared.ColorTools;
import com.ynu.diary.shared.ScreenHelper;
import com.ynu.diary.shared.ThemeManager;

/**
 * Created by daxia on 2016/11/13.
 */

public class MyDiaryImageButton extends ImageButton {


    public MyDiaryImageButton(Context context) {
        super(context);
    }

    public MyDiaryImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyDiaryImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyDiaryImageButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.setBackground(ThemeManager.getInstance().getButtonBgDrawable(getContext()));
        this.setColorFilter(ColorTools.getColor(getContext(), R.color.imagebutton_hint_color));
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            this.setStateListAnimator(null);
        }
        this.setMinimumWidth(ScreenHelper.dpToPixel(getContext().getResources(), 80));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

}
