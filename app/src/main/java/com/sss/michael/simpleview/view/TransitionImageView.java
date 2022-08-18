package com.sss.michael.simpleview.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * @author Michael by Administrator
 * @date 2022/8/17 17:45
 * @Description 转场ImageView
 */
public class TransitionImageView extends AppCompatImageView {


    public TransitionImageView(@NonNull Context context) {
        this(context, null);
    }

    public TransitionImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransitionImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
    }

    /**
     * 复用drawable,防止内存泄漏
     */
    private TransitionDrawable createDrawable(Drawable drawable) {
        Drawable oldDrawable = getDrawable();
        Drawable oldTd = null;
        if (oldDrawable == null) {
            oldTd = new ColorDrawable(Color.TRANSPARENT);
        } else if (oldDrawable instanceof TransitionDrawable) {
            oldTd = ((TransitionDrawable) oldDrawable).getDrawable(1);
        } else {
            oldTd = oldDrawable;
        }
        TransitionDrawable td = new TransitionDrawable(new Drawable[]{
                oldTd,
                drawable
        });
        td.startTransition(150);
        return td;
    }

}