package com.sss.michael.simpleview.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author Michael by Administrator
 * @date 2022/12/22 13:18
 * @Description 一个简单的圆角tab
 */
public class SimpleRoundTabView extends View {
    private boolean DEBUG;
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
    private int backgroundColor = Color.parseColor("#f2f2f2");
    /**
     * 遮罩色
     */
    private int maskColor = Color.parseColor("#00ff00");
    /**
     * 遮罩矩阵
     */
    private RectF maskRectF = new RectF();
    /**
     * 内部矩阵与背景之间的间距
     */
    private int distance = DensityUtil.dp2px(2);
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

    private OnSimpleRoundTabViewCallBack onSimpleRoundTabViewCallBack;

    public void setOnSimpleRoundTabViewCallBack(OnSimpleRoundTabViewCallBack onSimpleRoundTabViewCallBack) {
        this.onSimpleRoundTabViewCallBack = onSimpleRoundTabViewCallBack;
    }

    public SimpleRoundTabView(Context context) {
        this(context, null);
    }

    public SimpleRoundTabView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleRoundTabView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
                list.get(i).rectF.bottom = height - distance;
                if (i == 0) {
                    list.get(i).rectF.right = list.get(i).rectF.left + eachRectWidth;
                } else {
                    list.get(i).rectF.right = list.get(i - 1).rectF.right + eachRectWidth;
                }
            }
        }
        setMeasuredDimension(width, height);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制背景
        paint.setColor(backgroundColor);
        canvas.drawRoundRect(backgroundRectF, getRadius(false), getRadius(false), paint);

        //绘制前景tab矩阵
        for (SimpleRoundTabBean bean : list) {
            bean.getPaint().setColor(bean.backgroundColor);
            canvas.drawRoundRect(bean.rectF, getRadius(false), getRadius(false), bean.getPaint());
        }
        //绘制遮罩
        for (SimpleRoundTabBean bean : list) {
            if (bean.checked) {
                if (!animationIsRunning) {
                    maskRectF.set(bean.rectF);
                }
                paint.setColor(maskColor);
                canvas.drawRoundRect(maskRectF, getRadius(true), getRadius(true), paint);
                break;
            }
        }
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

    public void setList(List<SimpleRoundTabBean> list) {
        this.list = list;
        invalidate();
    }

    private ValueAnimator valueAnimator;
    private boolean animationIsRunning;

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
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.removeAllListeners();
            valueAnimator.removeAllUpdateListeners();
        }
        int dPosition = toPosition - fromPosition;
        valueAnimator = ValueAnimator.ofFloat(maskRectF.left, maskRectF.left + dPosition * eachRectWidth);

        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(300);
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
                float maskOffset = (float) animation.getAnimatedValue();
                maskRectF.left = maskOffset;
                maskRectF.right = maskRectF.left + eachRectWidth;
                invalidate();
            }
        });
        valueAnimator.start();

    }


    /**
     * 获取圆角
     *
     * @param outside 是否需要舍去间距{@link #distance}
     */
    private float getRadius(boolean outside) {
        if (outside) {
            return backgroundRectF.height() / 2;
        } else {
            return (backgroundRectF.height() - distance * 2) / 2;
        }
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

    public interface OnSimpleRoundTabViewCallBack {
        void onTabChecked(int fromPosition, int toPosition);
    }
}
