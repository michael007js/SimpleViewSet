package com.sss.michael.simpleview.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;


import com.sss.michael.simpleview.R;



/**
 * @author Michael by Administrator
 * @date 2021/3/25 10:51
 * @Description 一个简单的层叠轮播图
 */
public class SimpleAutoScrollInfiniteLoopView extends ViewGroup {
    /**
     * 宽高
     */
    private int width, height;
    /**
     * 中心点
     */
    private Point center = new Point();
    /**
     * 动画时间
     */
    private static long ANIMATION_DURATION = 500;
    /**
     * 每张图片的宽度百分比
     */
    private float widthPercent = 0.7f;
    /**
     * 每张图片的高度百分比
     */
    private float heightPercent = 1.0f;
    /**
     * 当前展示图片的下标
     */
    private int centerPosition = 1;
    /**
     * 图片
     */
    private ImageView imageView1, imageView2, imageView3;
    /**
     * 图片位置矩阵
     */
    private Rect rect1 = new Rect(), rect2 = new Rect(), rect3 = new Rect();

    private OnAutoScrollInfiniteLoopViewCallBack onAutoScrollInfiniteLoopViewCallBack;

    public void setOnAutoScrollInfiniteLoopViewCallBack(OnAutoScrollInfiniteLoopViewCallBack onAutoScrollInfiniteLoopViewCallBack) {
        this.onAutoScrollInfiniteLoopViewCallBack = onAutoScrollInfiniteLoopViewCallBack;
    }

    public SimpleAutoScrollInfiniteLoopView(Context context) {
        this(context, null);
    }

    public SimpleAutoScrollInfiniteLoopView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleAutoScrollInfiniteLoopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        imageView1 = createImageView(R.mipmap.ic_launcher);
        imageView2 = createImageView(R.mipmap.ic_launcher);
        imageView3 = createImageView(R.mipmap.ic_launcher);
        addView(imageView1);
        addView(imageView2);
        addView(imageView3);


        imageView1.setScaleY(0.7f);
        imageView3.setScaleY(0.7f);
        imageView1.setTranslationZ(10);
        imageView2.setTranslationZ(30);
        imageView3.setTranslationZ(10);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        ViewGroup.LayoutParams imageView1LayoutParams = imageView1.getLayoutParams();
        imageView1LayoutParams.width = (int) (width * widthPercent);
        imageView1LayoutParams.height = (int) (height * heightPercent);

        ViewGroup.LayoutParams imageView2LayoutParams = imageView2.getLayoutParams();
        imageView2LayoutParams.width = (int) (width * widthPercent);
        imageView2LayoutParams.height = (int) (height * heightPercent);

        ViewGroup.LayoutParams imageView3LayoutParams = imageView3.getLayoutParams();
        imageView3LayoutParams.width = (int) (width * widthPercent);
        imageView3LayoutParams.height = (int) (height * heightPercent);
        rect2.set(center.x - imageView2LayoutParams.width / 2, center.y - imageView2LayoutParams.height / 2, center.x + imageView2LayoutParams.width / 2, center.y + imageView2LayoutParams.height / 2);
        rect1.set(0, center.y - imageView1LayoutParams.height / 2, imageView1LayoutParams.width, center.y + imageView1LayoutParams.height / 2);
        rect3.set(width - imageView3LayoutParams.width, center.y - imageView3LayoutParams.height / 2, width, center.y + imageView3LayoutParams.height / 2);
        imageView1.layout(rect1.left, rect1.top, rect1.right, rect1.bottom);
        imageView2.layout(rect2.left, rect2.top, rect2.right, rect2.bottom);
        imageView3.layout(rect3.left, rect3.top, rect3.right, rect3.bottom);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        center.set(width / 2, height / 2);


    }


    private AnimatorSet animatorSet;
    private ValueAnimator centerTranslation, centerScale, centerTranslationZ, centerAlpha;
    private ValueAnimator leftTranslation, leftScale, leftTranslationZ, leftAlpha;
    private ValueAnimator rightTranslation, rightScale, rightTranslationZ, rightAlpha;

    /**
     * 开始动画过渡
     */
    private void start() {
        if (animatorSet != null && animatorSet.isRunning()) {
            return;

        }
        animatorSet = new AnimatorSet();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                int position = centerPosition + 1;
                if (position > 2) {
                    position = 0;
                }
                centerPosition = position;
                if (onAutoScrollInfiniteLoopViewCallBack != null) {
                    onAutoScrollInfiniteLoopViewCallBack.onPositionChanged(centerPosition);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        /************************************************************************/
        centerTranslation = ValueAnimator.ofInt(rect2.left, rect1.left);
        centerTranslation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int a = (Integer) animation.getAnimatedValue();
                getCenterImageViewByPosition().layout(a, rect2.top, a + rect2.width(), rect2.bottom);
            }
        });

        centerScale = ValueAnimator.ofFloat(1.0f, 0.7f);
        centerScale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float a = (float) animation.getAnimatedValue();
                getCenterImageViewByPosition().setScaleY(a);
            }
        });
        centerTranslationZ = ValueAnimator.ofFloat(getCenterImageViewByPosition().getTranslationZ(), getLeftImageViewByPosition().getTranslationZ());
        centerTranslationZ.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float a = (float) animation.getAnimatedValue();
                getCenterImageViewByPosition().setTranslationZ(a);
            }
        });
        centerAlpha = ValueAnimator.ofFloat(1.0f, 0.8f, 1.0f);
        centerAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float a = (float) animation.getAnimatedValue();
                getCenterImageViewByPosition().setAlpha(a);
            }
        });
        /************************************************************************/
        leftTranslation = ValueAnimator.ofInt(rect1.left, rect3.left);
        leftTranslation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int a = (Integer) animation.getAnimatedValue();
                getLeftImageViewByPosition().layout(a, rect1.top, a + rect1.width(), rect1.bottom);
            }
        });

        leftScale = ValueAnimator.ofFloat(0.7f, 0.7f);
        leftScale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float a = (float) animation.getAnimatedValue();
                getLeftImageViewByPosition().setScaleY(a);
            }
        });
        leftTranslationZ = ValueAnimator.ofFloat(getLeftImageViewByPosition().getTranslationZ(), getRightImageViewByPosition().getTranslationZ());
        leftTranslationZ.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float a = (float) animation.getAnimatedValue();
                getLeftImageViewByPosition().setTranslationZ(a);
            }
        });
        leftAlpha = ValueAnimator.ofFloat(1.0f, 0.8f, 1.0f);
        leftAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float a = (float) animation.getAnimatedValue();
                getLeftImageViewByPosition().setAlpha(a);
            }
        });
        /************************************************************************/
        rightTranslation = ValueAnimator.ofInt(rect3.left, rect2.left);
        rightTranslation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int a = (Integer) animation.getAnimatedValue();
                getRightImageViewByPosition().layout(a, rect3.top, a + rect3.width(), rect3.bottom);
            }
        });

        rightScale = ValueAnimator.ofFloat(0.7f, 1.0f);
        rightScale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float a = (float) animation.getAnimatedValue();
                getRightImageViewByPosition().setScaleY(a);
            }
        });
        rightTranslationZ = ValueAnimator.ofFloat(getRightImageViewByPosition().getTranslationZ(), getCenterImageViewByPosition().getTranslationZ());
        rightTranslationZ.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float a = (float) animation.getAnimatedValue();
                getRightImageViewByPosition().setTranslationZ(a);
            }
        });
        rightAlpha = ValueAnimator.ofFloat(1.0f, 0.8f, 1.0f);
        rightAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float a = (float) animation.getAnimatedValue();
                getRightImageViewByPosition().setAlpha(a);
            }
        });
        /************************************************************************/
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(centerTranslation)
                .with(centerScale)
                .with(centerTranslationZ)
                .with(centerAlpha)
                .with(leftTranslation)
                .with(leftScale)
                .with(leftTranslationZ)
                .with(leftAlpha)
                .with(rightTranslation)
                .with(rightScale)
                .with(rightTranslationZ)
                .with(rightAlpha)
        ;

        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.start();
    }

    /**
     * 创建图片
     */
    private ImageView createImageView(int src) {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(src);
        return imageView;
    }


    /**
     * 获取中间图片下标
     */
    private ImageView getCenterImageViewByPosition() {
        switch (centerPosition) {
            case 0:
                return imageView1;
            case 1:
                return imageView2;
            case 2:
                return imageView3;
            default:
                return imageView1;
        }
    }


    /**
     * 获取左侧图片下标
     */
    private ImageView getLeftImageViewByPosition() {
        switch (centerPosition) {
            case 0:
                return imageView3;
            case 1:
                return imageView1;
            case 2:
                return imageView2;
            default:
                return imageView1;
        }
    }

    /**
     * 获取右侧图片下标
     */
    private ImageView getRightImageViewByPosition() {
        switch (centerPosition) {
            case 0:
                return imageView2;
            case 1:
                return imageView3;
            case 2:
                return imageView1;
            default:
                return imageView1;
        }
    }

    /**
     * 打扫卫生
     */
    public void clear() {

        if (animatorSet != null) {
            animatorSet.cancel();
        }
    }


    public interface OnAutoScrollInfiniteLoopViewCallBack {
        void onPositionChanged(int position);
    }

}
