package com.sss.michael.simpleview.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;
import com.sss.michael.simpleview.utils.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE;

/**
 * @author Michael by 61642
 * @date 2024/2/22 13:31
 * @Description 一个简单的圆角tab(支持四边圆角)
 */
public class SimpleRoundTabViewV2 extends View {
    private boolean DEBUG;
    /**
     * 四个顶点圆角半径
     */
    private float topLeftRadius = DensityUtil.dp2px(8);
    private float topRightRadius = DensityUtil.dp2px(8);
    private float bottomLeftRadius = 0f;
    private float bottomRightRadius = 0f;
    /**
     * 每一个矩阵的宽度
     */
    private int eachRectWidth;
    /**
     * 背景矩阵
     */
    private RectF backgroundRectF = new RectF();
    /**
     * 背景色
     */
    private int backgroundColor = Color.parseColor("#f9f9f9");
    /**
     * 遮罩色
     */
    private int maskColor = Color.parseColor("#ffffff");
    /**
     * 遮罩矩阵
     */
    private RectF maskRectF = new RectF();
    /**
     * 底部指示线矩阵
     */
    private RectF lineRectF = new RectF();
    /**
     * 内部矩阵与背景之间的间距
     */
    private int distance = DensityUtil.dp2px(0);
    /**
     * 底部指示横线的高度
     */
    private int indicatorHeight = DensityUtil.dp2px(3);
    /**
     * 底部指示横线的宽度
     */
    private int indicatorWidth = DensityUtil.dp2px(20);
    /**
     * 底部指示横线的颜色
     */
    private int indicatorColor = Color.parseColor("#e9302d");

    /**
     * 底部指示横线背景矩阵
     */
    private RectF indicatorBackgroundRectF = new RectF();
    /**
     * 底部指示横线背景色
     */
    private int indicatorBackgroundColor = maskColor;
    /**
     * 画笔
     */
    private Paint paint = new Paint();

    {
        paint.setAntiAlias(true);
    }

    /**
     * 宽高
     */
    private int width, height;
    /**
     * tab列表
     */
    private List<SimpleRoundTabBean> list = new ArrayList();
    Path path = new Path();
    private OnSimpleRoundTabViewCallBack onSimpleRoundTabViewCallBack;

    public void setOnSimpleRoundTabViewV2CallBack(OnSimpleRoundTabViewCallBack onSimpleRoundTabViewCallBack) {
        this.onSimpleRoundTabViewCallBack = onSimpleRoundTabViewCallBack;
    }

    public SimpleRoundTabViewV2(Context context) {
        this(context, null);
    }

    public SimpleRoundTabViewV2(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleRoundTabViewV2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (DEBUG) {
            for (int i = 0; i < 3; i++) {
                SimpleRoundTabBean simpleRoundTabBean = new SimpleRoundTabBean();
                simpleRoundTabBean.text = "标签No." + (i + 1);
                simpleRoundTabBean.checked = i == 0;
                list.add(simpleRoundTabBean);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int maxTextHeight = 0;
        for (int i = 0; i < list.size(); i++) {
            maxTextHeight = Math.max(maxTextHeight, list.get(i).getSize()[1]);
        }
        switch (widthMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                width = widthSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                width = DensityUtil.dp2px(100);
                break;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                height = maxTextHeight + getPaddingTop() + getPaddingBottom();
                break;
        }


        backgroundRectF.left = 0;
        backgroundRectF.top = 0;
        backgroundRectF.right = width;
        backgroundRectF.bottom = height;

        indicatorBackgroundRectF.left = backgroundRectF.left;
        indicatorBackgroundRectF.right = backgroundRectF.right;
        indicatorBackgroundRectF.top = backgroundRectF.bottom - indicatorHeight;
        indicatorBackgroundRectF.bottom = backgroundRectF.bottom;

        if (list.size() > 0) {
            //等分每个tab的宽度
            eachRectWidth = (width - distance * 2) / list.size();
            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {
                    list.get(i).rectF.left = backgroundRectF.left + distance;
                } else {
                    list.get(i).rectF.left = list.get(i - 1).rectF.right;
                }
                list.get(i).rectF.top = distance;
                list.get(i).rectF.bottom = indicatorBackgroundRectF.bottom - indicatorHeight;


                if (i == 0) {
                    list.get(i).rectF.right = list.get(i).rectF.left + eachRectWidth;
                } else {
                    list.get(i).rectF.right = list.get(i - 1).rectF.right + eachRectWidth;
                }
            }
        }

        setMeasuredDimension(width, height);
        changeMask(0);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制背景
        paint.setColor(backgroundColor);
        float[] radii = getCornerRadii();

        path.reset();
        path.addRoundRect(backgroundRectF, radii, Path.Direction.CW);
        canvas.drawPath(path, paint);
        paint.setColor(indicatorBackgroundColor);
        canvas.drawRect(indicatorBackgroundRectF, paint);

        // 绘制遮罩
        for (SimpleRoundTabBean bean : list) {
            if (bean.checked) {
                if (!animationIsRunning) {
                    maskRectF.top = bean.rectF.top;
                    maskRectF.bottom = bean.rectF.bottom;
                }
                paint.setColor(maskColor);
                path.reset();
                path.addRoundRect(maskRectF, radii, Path.Direction.CW);
                canvas.drawPath(path, paint);
                break;
            }
        }
        lineRectF.top = maskRectF.bottom;
        lineRectF.bottom = height - distance;
        lineRectF.left = maskRectF.left + (maskRectF.width() / 2) - (indicatorWidth >> 1);
        lineRectF.right = maskRectF.left + (maskRectF.width() / 2) + (indicatorWidth >> 1);
        paint.setColor(indicatorColor);
        canvas.drawRoundRect(lineRectF, indicatorHeight >> 1, indicatorHeight >> 1, paint);

        //绘制文字
        for (SimpleRoundTabBean bean : list) {
            bean.getPaint().setColor(bean.getTextColor());
            canvas.drawText(bean.text, bean.rectF.left + bean.rectF.width() / 2, bean.rectF.top + bean.rectF.height() / 2 + (bean.getSize()[1] >> 1) - DensityUtil.dp2px(2), bean.getPaint());
        }
    }


    private float clickX = 0, clickY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                clickX = event.getX();
                clickY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).rectF.contains(clickX, clickY)) {
                        int fromPosition = 0;
                        //获取起始下标
                        for (int ii = 0; ii < list.size(); ii++) {
                            if (list.get(ii).checked) {
                                fromPosition = ii;
                                break;
                            }
                        }
                        //重置所有选中状态
                        for (int iii = 0; iii < list.size(); iii++) {
                            list.get(iii).checked = false;
                        }
                        list.get(i).checked = true;
                        change(fromPosition, i);
                        break;
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 设置tab
     */
    public void setTab(List<SimpleRoundTabBean> list) {
        this.list = list;
        requestLayout();
    }

    /**
     * 设置圆角
     */
    public void setRadius(float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
        this.topLeftRadius = topLeftRadius;
        this.topRightRadius = topRightRadius;
        this.bottomLeftRadius = bottomLeftRadius;
        this.bottomRightRadius = bottomRightRadius;
        invalidate();
    }


    ViewPager viewPager;
    ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (changeByClickTab) {
                return;
            }
            for (SimpleRoundTabBean bean : list) {
                bean.checked = false;
            }
            list.get(position).checked = true;
            changeMask(eachRectWidth * position + positionOffset * eachRectWidth);
            invalidate();

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == SCROLL_STATE_IDLE) {
                changeByClickTab = false;
            }
        }
    };


    /**
     * 设置遮罩矩阵左右位置
     */
    private void changeMask(float left) {
        maskRectF.left = left;
        maskRectF.right = maskRectF.left + eachRectWidth;
    }

    /**
     * 依附到viewpager
     */
    public void attachToViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        this.viewPager.addOnPageChangeListener(listener);
    }

    private ValueAnimator valueAnimator;
    private boolean animationIsRunning;
    /**
     * 改变意图由点击tab触发
     */
    private boolean changeByClickTab;

    /**
     * 选中tab被改变
     *
     * @param fromPosition 开始索引
     * @param toPosition   结束索引
     */
    private void change(final int fromPosition, final int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        changeByClickTab = true;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.removeAllListeners();
            valueAnimator.removeAllUpdateListeners();
        }
        int dPosition = toPosition - fromPosition;
        valueAnimator = ValueAnimator.ofFloat(maskRectF.left, maskRectF.left + dPosition * eachRectWidth);

        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(150);
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                animationIsRunning = true;
                maskRectF.set(list.get(fromPosition).rectF);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animationIsRunning = false;
                if (onSimpleRoundTabViewCallBack != null) {
                    onSimpleRoundTabViewCallBack.onTabChecked(fromPosition, toPosition);
                }
                invalidate();
                if (viewPager != null) {
                    viewPager.setCurrentItem(toPosition);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                changeMask((float) animation.getAnimatedValue());
                invalidate();
            }
        });
        valueAnimator.start();

    }


    /**
     * 获取四个顶点圆角
     */
    private float[] getCornerRadii() {
        return new float[]{
                topLeftRadius, topLeftRadius,
                topRightRadius, topRightRadius,
                bottomRightRadius, bottomRightRadius,
                bottomLeftRadius, bottomLeftRadius
        };
    }

    public static class SimpleRoundTabBean {
        private Paint paint = new Paint();

        {
            paint.setAntiAlias(true);
        }

        /**
         * 矩阵
         */
        private RectF rectF = new RectF();

        /**
         * 是否选中
         */
        public boolean checked;
        /**
         * 文字
         */
        public String text = "";
        /**
         * 未选中时文字颜色
         */
        public int normalTextColor = Color.parseColor("#666666");
        /**
         * 选中时文字颜色
         */
        public int checkedTextColor = Color.parseColor("#e9302d");
        /**
         * 背景色
         */
        public int backgroundColor = Color.parseColor("#f2f2f2");
        /**
         * 文字大小
         */
        public int textSize = DensityUtil.sp2px(14);

        int getTextColor() {
            if (checked) {
                return checkedTextColor;
            } else {
                return normalTextColor;
            }
        }

        Paint getPaint() {
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(textSize);
            if (checked) {
                paint.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                paint.setTypeface(Typeface.DEFAULT);
            }
            return paint;
        }

        int[] getSize() {
            return DrawViewUtils.getTextWH(getPaint(), text);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (viewPager != null) {
            viewPager.removeOnPageChangeListener(listener);
        }
    }

    public interface OnSimpleRoundTabViewCallBack {
        void onTabChecked(int fromPosition, int toPosition);
    }
}

