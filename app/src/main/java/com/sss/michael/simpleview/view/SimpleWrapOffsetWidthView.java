package com.sss.michael.simpleview.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.sss.michael.simpleview.utils.DensityUtil;

public class SimpleWrapOffsetWidthView extends FrameLayout {

    /**
     * 动画
     */
    private ValueAnimator valueAnimator;
    /**
     * 宽度
     */
    private int width;

    /**
     * 阈值
     */
    private int threshold = DensityUtil.dp2px(200);
    private OnSimpleWrapOffsetWidthViewCallBack onSimpleWrapOffsetWidthViewCallBack;

    public void setOnSimpleWrapOffsetWidthViewCallBack(OnSimpleWrapOffsetWidthViewCallBack onSimpleWrapOffsetWidthViewCallBack) {
        this.onSimpleWrapOffsetWidthViewCallBack = onSimpleWrapOffsetWidthViewCallBack;
    }

    public SimpleWrapOffsetWidthView(Context context) {
        this(context, null);
    }

    public SimpleWrapOffsetWidthView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleWrapOffsetWidthView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SimpleWrapOffsetWidthView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingStart() - getPaddingEnd();
        if (getChildCount() != 1) {
            throw new RuntimeException("you must add one child view!");
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clear();
    }

    int left;

    public void start(final boolean isClose) {
        start(isClose, threshold);
    }

    public void start(final boolean isClose, int threshold) {
        this.threshold = threshold;
        clear();
        valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                left = getChildAt(0).getLeft();
                if (onSimpleWrapOffsetWidthViewCallBack != null) {
                    onSimpleWrapOffsetWidthViewCallBack.onBeforeStatusChange(isClose);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (onSimpleWrapOffsetWidthViewCallBack != null) {
                    onSimpleWrapOffsetWidthViewCallBack.onAfterStatusChanged(isClose);
                }
            }
        });
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                int prepareLeft;
                if (isClose) {
                    prepareLeft = getChildAt(0).getLeft() + (int) (value * width);
                    left = Math.min(prepareLeft, width - SimpleWrapOffsetWidthView.this.threshold);
                } else {
                    prepareLeft = getChildAt(0).getLeft() - (int) (value * width);
                    left = Math.max(prepareLeft, 0);
                }
                getChildAt(0).layout(left, getChildAt(0).getTop(), getChildAt(0).getRight(), getChildAt(0).getBottom());

            }
        });
        valueAnimator.setDuration(300);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.start();
    }

    private void clear() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.removeAllUpdateListeners();
            valueAnimator.removeAllListeners();
        }
    }

    public interface OnSimpleWrapOffsetWidthViewCallBack {
        /**
         * 状态更改前回调
         *
         * @param isClose 是否是关闭状态
         */
        void onBeforeStatusChange(boolean isClose);

        /**
         * 状态更改后回调
         *
         * @param isClose 是否是关闭状态
         */
        void onAfterStatusChanged(boolean isClose);
    }
}
