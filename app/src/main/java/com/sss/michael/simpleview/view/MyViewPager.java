package com.sss.michael.simpleview.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.sss.michael.simpleview.R;
import com.sss.michael.simpleview.utils.DensityUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyViewPager extends ViewGroup {
    private List<Integer> list = new ArrayList<>();
    private ImageView bg1, bg2;

    public MyViewPager(Context context) {
        this(context, null);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MyViewPager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (bg1 == null) {
            bg1 = new ImageView(context);
            bg1.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        if (bg2 == null) {
            bg2 = new ImageView(context);
            bg2.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        bg1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, DensityUtil.dp2px(200)));
        bg2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, DensityUtil.dp2px(200)));
        addView(bg1);
        addView(bg2);
        bg1.setImageResource(R.mipmap.ic_launcher);
        bg2.setImageResource(R.mipmap.pic);
    }

    public void setData(){

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        int maxWidth = 0, maxHeight = 0;
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
            if (maxWidth < getChildAt(i).getMeasuredWidth()) {
                maxWidth = getChildAt(i).getMeasuredWidth();
            }
            if (maxHeight < getChildAt(i).getMeasuredHeight()) {
                maxHeight = getChildAt(i).getMeasuredHeight();
            }
        }
        Log.e("SSSSS",maxWidth+"---"+maxHeight);
        setMeasuredDimension(maxWidth, maxHeight);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.e("SSSSS",l+"---"+t+"---"+r+"---"+b);
        bg1.layout(l,t,r,b);
        bg2.layout(l,t,r,b);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:


                return false;
            case MotionEvent.ACTION_MOVE:

                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:

                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        return true;
    }

}