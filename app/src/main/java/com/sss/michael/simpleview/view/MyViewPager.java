package com.sss.michael.simpleview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sss.michael.simpleview.R;

public class MyViewPager extends ViewGroup {
    private ImageView bg1, bg2;
    private boolean bg1Front = true;

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
        addView(bg1);
        addView(bg2);
        bg1.setImageResource(R.mipmap.xhr1);
        bg2.setImageResource(R.mipmap.xhr2);
    }

    int originLeft, originTop, originRight, originBottom;
    int parentLeft, parentTop, parentRight, parentBottom;
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (parentRight == 0 || parentBottom == 0) {
            parentLeft = l;
            parentTop = t;
            parentRight = r;
            parentBottom = b;
        }
        if (originRight == 0 || originBottom == 0) {
            originLeft = 0;
            originTop = 0;
//            originRight = w;
//            originBottom = h;
        }
//        Log.e("SSSSS", changed + "---" + l + "---" + t + "---" + r + "---" + b + "---" + touchLock + "---" + bg1Front);

    }


    private void layoutChild() {
        Log.e("SSSSS", bg1.getRight()- bg1.getHeight()+"---"+bg1.getWidth());
        if (touchLock) {
            if (bg1Front) {
                int pre1Left = (int) (bg1.getLeft() + offsetByLastX);
                int pre1Right = (int) (pre1Left + bg1.getWidth());

                int pre2Left = pre1Right;
                int pre2Right = (int) (pre2Left + bg2.getWidth());
                if (!checkMatchScope()) {
                    bg1Front = false;
                    pre1Right = 0;
                    pre1Left = pre1Right - bg1.getWidth();

                    pre2Left = pre1Right;
                    pre2Right = pre2Left + bg2.getWidth();
                }
                bg1.layout(pre1Left, 0, pre1Right, bg1.getHeight());
                bg2.layout(pre2Left, 0, pre2Right, bg2.getHeight());
            } else {
                int pre2Left = (int) (bg2.getLeft() + offsetByLastX);
                int pre2Right = (int) (pre2Left + bg2.getWidth());

                int pre1Left = pre2Right;
                int pre1Right = (int) (pre2Right + bg1.getWidth());

                if (!checkMatchScope()) {

                }
//                Log.e("SSSSS", direction+"---"+checkMatchScope() + "---" + bg1.getLeft() + "---" + bg1.getTop() + "---" + bg1.getRight() + "---" + bg1.getBottom());
                bg1.layout(pre1Left, 0, pre1Right,  bg1.getHeight());
                bg2.layout(pre2Left, 0, pre2Right,  bg2.getHeight());
            }
        } else {
            bg1.layout(0, 0, originRight,  bg1.getHeight());
            bg2.layout(0, 0, originRight,  bg2.getHeight());
        }
    }

    public boolean checkMatchScope() {
        if (direction == Direction.RIGHT_TO_LEFT) {
            if (bg1Front) {
                if (bg1.getRight() < originLeft) {
                    return true;
                }
            } else {
                if (bg2.getRight() < originLeft) {
                    return true;
                }
            }
        } else if (direction == Direction.LEFT_TO_RIGHT) {
            if (bg1Front) {
                if (bg2.getLeft() < originLeft) {
                    return true;
                }
            } else {
                if (bg1.getLeft() < originLeft) {
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * 触摸方向意图
     */
    private Direction direction = Direction.NORMAL;
    /**
     * 上一次X轴Y轴的触摸位置
     */
    private float downX;
    /**
     * X轴偏移量
     */
    private float offsetX;
    /**
     * X轴相较于上一个点的偏移量
     */
    private float offsetByLastX;
    /**
     * 上一个点的X轴位置
     */
    private float lastX;
    private boolean touchLock;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                touchLock = true;
                downX = event.getX();
                lastX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                offsetX = event.getX() - downX;
                if (offsetX < 0) {
                    direction = Direction.RIGHT_TO_LEFT;
                } else {
                    direction = Direction.LEFT_TO_RIGHT;
                }
                offsetByLastX = event.getX() - lastX;
                lastX = event.getX();
                layoutChild();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touchLock = false;
                layoutChild();
                break;
        }

        return touchLock;
    }


    public enum Direction {
        RIGHT_TO_LEFT,
        LEFT_TO_RIGHT,
        NORMAL,
    }

}