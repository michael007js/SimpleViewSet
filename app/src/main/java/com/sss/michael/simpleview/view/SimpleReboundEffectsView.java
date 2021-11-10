package com.sss.michael.simpleview.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import com.sss.michael.simpleview.R;


/**
 * @author Michael by Administrator
 * @date 2021/10/27 9:31
 * @Description 仿IOS滑动阻尼回弹View
 */
public class SimpleReboundEffectsView extends FrameLayout {
    /**
     * 回弹动画时间
     */
    private final int ANIMATION_TIME = 300;
    /**
     * 滑动阈值
     */
    private final int SLIDE_VALUE = 500;
    /**
     * 子View
     */
    private View childView;
    /**
     * 子View初始时上下坐标位置
     */
    private int top, bottom;
    /**
     * 是否正在滑动
     */
    private boolean isSliding;
    /**
     * 手指上下滑动Y坐标变化前的Y坐标值
     */
    private float y;
    /**
     * 布局是否释放中
     */
    private boolean isReleasing;
    /**
     * 子布局重滑偏移量
     */
    private int childViewOffset;
    /**
     * 滑动意图
     */
    private SlideDirection direction = SlideDirection.SLIDE_NORMAL;
    /**
     * 开关 0全部 1手指上滑有效 2手指下滑有效
     */
    private int portraitSwitch;
    /**
     * 衰减度 越大滑动越困难
     */
    private int slideAttenuation = 5;
    /**
     * 滑动的速度如果大于此值，将拦截该次滑动事件,如果未负数，则关闭该功能
     */
    private int interceptSlideScope = 20;
    /**
     * 优化反方向重滑
     */
    private boolean optimizationReverseSlide;
    /**
     * 开启惯性滑动
     */
    private boolean inertialSlide = true;

    private OnSimpleReboundEffectsViewCallBack onSimpleReboundEffectsViewCallBack;

    public void setOnSimpleReboundEffectsViewCallBack(OnSimpleReboundEffectsViewCallBack onSimpleReboundEffectsViewCallBack) {
        this.onSimpleReboundEffectsViewCallBack = onSimpleReboundEffectsViewCallBack;
    }

    public SimpleReboundEffectsView(Context context) {
        this(context, null);
    }

    public SimpleReboundEffectsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleReboundEffectsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SimpleReboundEffectsView);
        portraitSwitch = ta.getInt(R.styleable.SimpleReboundEffectsView_sre_orientation, 0);
        interceptSlideScope = ta.getDimensionPixelSize(R.styleable.SimpleReboundEffectsView_sre_interceptSlideScope, 20);
        slideAttenuation = ta.getInt(R.styleable.SimpleReboundEffectsView_sre_attenuation, 5);
        optimizationReverseSlide = ta.getBoolean(R.styleable.SimpleReboundEffectsView_sre_optimization_reverse_slide, true);
        inertialSlide = ta.getBoolean(R.styleable.SimpleReboundEffectsView_sre_inertial_slide, true);

        ta.recycle();
        this.setClickable(true);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (null != childView && !isReleasing || !isInertialSliding) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (bottom == 0) {
                        this.top = childView.getTop();
                        this.bottom = childView.getBottom();
                    }
                    y = event.getY();
                    direction = SlideDirection.SLIDE_NORMAL;
                    childViewOffset = 0;
                    isTouchUp = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float nowY = event.getY();
                    float diffY = (nowY - y) / slideAttenuation;


                    if (interceptSlideScope > 0 && Math.abs(diffY) > interceptSlideScope) {
                        return super.dispatchTouchEvent(event);
                    }

                    if (direction == SlideDirection.SLIDE_NORMAL) {
                        if (nowY > y) {
                            direction = SlideDirection.SLIDE_DOWN;
                        }
                        if (nowY < y) {
                            direction = SlideDirection.SLIDE_UP;
                        }
                    }

                    if ((portraitSwitch == 0 || portraitSwitch == 2) && direction == SlideDirection.SLIDE_DOWN) {
                        if (getRealTimeSlideDirection(event) == SlideDirection.SLIDE_UP) {
                            if (optimizationReverseSlide) {
                                if (childView.getTop() > top) {
//                                    Log.e("SSSSSS", "xxxxxxxxxxxx" + diffY);
                                    childView.layout(childView.getLeft(), childView.getTop() + (int) diffY, childView.getRight(), childView.getBottom() + (int) diffY);
                                    onCallBack(event);
                                    y = nowY;
                                    isSliding = true;
                                    return true;
                                } else {
                                    if (childView.getBottom() < bottom) {
                                        childViewOffset = childView.getScrollY();
//                                        Log.e("SSSSSS", "" + childViewOffset);
                                        //防止突然重滑导致底部留白
                                        childView.layout(childView.getLeft(), childView.getTop() + (int) diffY, childView.getRight(), bottom);
                                        onCallBack(event);
                                    }
                                    y = nowY;
                                    isSliding = true;
                                    return super.dispatchTouchEvent(event);
                                }
                            }
                            direction = SlideDirection.SLIDE_NORMAL;
                            release(childView.getTop() - top);
                            return super.dispatchTouchEvent(event);

                        }
                        if (isTop() && direction != SlideDirection.SLIDE_NORMAL) {
                            childView.layout(childView.getLeft(), childView.getTop() + (int) diffY, childView.getRight(), childView.getBottom() + (int) diffY);
                            onCallBack(event);
                        }

                    } else if ((portraitSwitch == 0 || portraitSwitch == 1) && direction == SlideDirection.SLIDE_UP) {

                        if (getRealTimeSlideDirection(event) == SlideDirection.SLIDE_DOWN) {
                            if (optimizationReverseSlide) {
                                if (childView.getBottom() < bottom) {
                                    childView.layout(childView.getLeft(), childView.getTop() + (int) diffY, childView.getRight(), childView.getBottom() + (int) diffY);
                                    onCallBack(event);
                                    y = nowY;
                                    isSliding = true;
                                    return true;
                                } else {
                                    if (childView.getTop() > top) {
                                        childViewOffset = childView.getScrollY();
//                                        Log.e("SSSSSS", "" + childViewOffset);
                                        //防止突然重滑导致底部留白
                                        childView.layout(childView.getLeft(), top, childView.getRight(), childView.getBottom() + (int) diffY);
                                        onCallBack(event);
                                    }
                                    y = nowY;
                                    isSliding = true;
                                    return super.dispatchTouchEvent(event);
                                }
                            }
                            direction = SlideDirection.SLIDE_NORMAL;
                            release(childView.getTop() - top);
                            return super.dispatchTouchEvent(event);
                        }
                        if (isBottom() && direction != SlideDirection.SLIDE_NORMAL) {
                            childView.layout(childView.getLeft(), childView.getTop() + (int) diffY, childView.getRight(), childView.getBottom() + (int) diffY);
                            onCallBack(event);
                        }

                    }
                    y = nowY;
                    isSliding = true;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:

                    if (inertialSlide) {
                        if (isTop() || isBottom()) {
                            release(childView.getTop() - top);
                            onCallBack(event);
                        }
                    } else {
                        release(childView.getTop() - top);
                        onCallBack(event);
                    }

                    isTouchUp = true;
                    break;
                default:
                    direction = SlideDirection.SLIDE_NORMAL;
                    break;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void onCallBack(MotionEvent event) {
        if (onSimpleReboundEffectsViewCallBack != null) {
            boolean isTouch = (event.getAction() != MotionEvent.ACTION_UP && event.getAction() != MotionEvent.ACTION_CANCEL);
            onSimpleReboundEffectsViewCallBack.onSingleTouchY(event.getY() - this.y, isTouch, getRealTimeSlideDirection(event) != direction);
        }
    }

    /**
     * 是否到边界
     */
    private boolean isBoundary(int value, SlideDirection direction) {
        if (direction == SlideDirection.SLIDE_DOWN) {
            int prepare = childView.getTop() + value;
            return prepare <= SLIDE_VALUE;
        }
        return false;
    }

    /**
     * 子布局是否滑动到顶部
     */
    private boolean isTop() {
        if (childView != null) {
            return !childView.canScrollVertically(-1);
        }
        return false;
    }

    /**
     * 子布局是否滑动到底部
     */
    private boolean isBottom() {
        if (childView != null) {
            return !childView.canScrollVertically(1);
        }
        return false;
    }

    /**
     * 释放布局
     */
    private void release(int from) {
//        Log.e("SSSSSS", "release");
        if (isSliding) {
            isReleasing = true;
            TranslateAnimation ta = new TranslateAnimation(0, 0, from, 0);
            ta.setDuration(ANIMATION_TIME);
            ta.setInterpolator(new DecelerateInterpolator());
            ta.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (childViewOffset < 0) {
                        //偏移发生在顶部
                        childView.scrollTo(0, childView.getScrollY() - childViewOffset);
                    } else if (childViewOffset > 0) {
                        //偏移发生在底部
                        childView.scrollTo(0, childViewOffset);
                    }

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    isReleasing = false;
                    isInertialSliding = false;
                    childViewOffset = 0;
                    direction = SlideDirection.SLIDE_NORMAL;
                    childView.layout(childView.getLeft(), top, childView.getRight(), bottom);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            childView.startAnimation(ta);
            childView.layout(childView.getLeft(), top, childView.getRight(), bottom);
            isSliding = false;
        }
    }


    /**
     * 获取实时滑动方向
     *
     * @return 小于0手指向上滑动  大于0手指向下滑动
     */
    private SlideDirection getRealTimeSlideDirection(MotionEvent event) {
        float y = event.getY();
        float result = y - this.y;

        if (onSimpleReboundEffectsViewCallBack != null) {
            boolean isTouch = (event.getAction() != MotionEvent.ACTION_UP && event.getAction() != MotionEvent.ACTION_CANCEL);
            onSimpleReboundEffectsViewCallBack.onSingleTouchY(result, isTouch, getRealTimeSlideDirection(event) != direction);
        }
        if (result < 0) {
            return SlideDirection.SLIDE_UP;
        } else if (result == 0) {
            return SlideDirection.SLIDE_NORMAL;
        } else {
            return SlideDirection.SLIDE_DOWN;
        }
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0) {
            childView = getChildAt(0);
            if (inertialSlide && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                childView.setOnScrollChangeListener(new OnScrollChangeListener() {
                    @Override
                    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                        int offset = (scrollY - oldScrollY) * 2 / slideAttenuation;//原始数据过小，惯性滑动不明显，此处的x2纯粹是启放大效果，
                        if (isTop()) {
//                            Log.e("SSSSSS", offset + "__________________" + direction.name());
                            inertialSlide(0, offset);
                        } else if (isBottom()) {
//                            Log.e("SSSSSS", offset + "__________________" + direction.name());
                            inertialSlide(0, offset);
                        }
                    }
                });
            }
        }
    }


    /**
     * 是否正在惯性滑动中
     */
    private boolean isInertialSliding;
    /**
     * 手指是否离开屏幕
     */
    private boolean isTouchUp = true;

    /**
     * 惯性滑动
     */
    private void inertialSlide(final int from, final int to) {
        if (!isInertialSliding && isTouchUp && direction != SlideDirection.SLIDE_NORMAL) {
            isInertialSliding = true;

            ValueAnimator valueAnimator = ValueAnimator.ofInt(from, to);
            valueAnimator.setInterpolator(new OvershootInterpolator());
            valueAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
//                release(childView.getTop() - top);
                    if (to < 0) {
                        //顶部
                        release(childView.getTop() - top);
                    } else {
                        //底部
                        release(childView.getTop() - top);
                    }
//                    childView.scrollTo(0,  0);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);

                }
            });
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
//                    Log.e("SSSSSS", value + "");
                    childView.layout(childView.getLeft(), top - value, childView.getRight(), bottom - value);
                }
            });
            valueAnimator.setDuration(ANIMATION_TIME);
            valueAnimator.start();
        }
    }


    enum SlideDirection {
        SLIDE_UP,
        SLIDE_NORMAL,
        SLIDE_DOWN
    }


    public interface OnSimpleReboundEffectsViewCallBack {
        void onSingleTouchY(float singleDy, boolean isTouch, boolean reverseDirection);
    }


}