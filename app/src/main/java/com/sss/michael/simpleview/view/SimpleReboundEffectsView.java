package com.sss.michael.simpleview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
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
    private static final int ANIMATION_TIME = 300;
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
     * 反方向滑动是否拦截
     */
    private boolean interceptByNegativeOrientation;

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
        ta.recycle();
        this.setClickable(true);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (childView != null) {
            this.top = childView.getTop();
            this.bottom = childView.getBottom();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return intercept || super.onInterceptTouchEvent(ev);
    }
    private boolean intercept;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (null != childView && !isReleasing) {
            boolean isTop = !childView.canScrollVertically(-1);
            boolean isBottom = !childView.canScrollVertically(1);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //记录每次手指的位置
                    y = event.getY();
                    direction = SlideDirection.SLIDE_NORMAL;
                    intercept = false;
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
                            if (interceptByNegativeOrientation) {
                                intercept = true;
                            }
                            release();
                            return super.dispatchTouchEvent(event);
                        }

                        if (isTop) {
                            childView.layout(childView.getLeft(), childView.getTop() + (int) diffY, childView.getRight(), childView.getBottom() + (int) diffY);
                        }

                    } else if ((portraitSwitch == 0 || portraitSwitch == 1) && direction == SlideDirection.SLIDE_UP) {

                        if (getRealTimeSlideDirection(event) == SlideDirection.SLIDE_DOWN) {
                            if (interceptByNegativeOrientation) {
                                intercept = true;
                            }
                            release();
                            return super.dispatchTouchEvent(event);
                        }
                        if (isBottom) {
                            childView.layout(childView.getLeft(), childView.getTop() + (int) diffY, childView.getRight(), childView.getBottom() + (int) diffY);
                        }

                    }
                    y = nowY;
                    isSliding = true;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    direction = SlideDirection.SLIDE_NORMAL;
                    release();
                    if (onSimpleReboundEffectsViewCallBack != null) {
                        onSimpleReboundEffectsViewCallBack.onSingleTouchY(event.getY() - this.y, false);
                    }
                    break;
                default:
                    direction = SlideDirection.SLIDE_NORMAL;
                    break;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 释放布局
     */
    private void release() {
        if (isSliding) {
            TranslateAnimation ta = new TranslateAnimation(0, 0, childView.getTop() - top, 0);
            ta.setDuration(ANIMATION_TIME);
            ta.setInterpolator(new OvershootInterpolator());
            ta.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    isReleasing = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    isReleasing = false;
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
            onSimpleReboundEffectsViewCallBack.onSingleTouchY(result, isTouch);
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
        }
    }


    enum SlideDirection {
        SLIDE_UP,
        SLIDE_NORMAL,
        SLIDE_DOWN
    }


    public interface OnSimpleReboundEffectsViewCallBack {
        void onSingleTouchY(float singleDy, boolean isTouch);
    }


}