package com.sss.michael.simpleview.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.sss.michael.simpleview.R;
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
     * 两行文字之间的行间距
     */
    private int textLineSpacing = DensityUtil.dp2px(3);
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
    private int maskColor = Color.parseColor("#ffffff");
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
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.SimpleRoundTabView, defStyleAttr, defStyleAttr);
        distance = a.getDimensionPixelOffset(R.styleable.SimpleRoundTabView_srtv_distance, distance);
        maskColor = a.getColor(R.styleable.SimpleRoundTabView_srtv_maskColor, maskColor);
        backgroundColor = a.getColor(R.styleable.SimpleRoundTabView_srtv_backgroundColor, backgroundColor);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int maxContentHeight = 0;
        for (SimpleRoundTabBean bean : list) {
            int h = bean.getMainTextHeight();
            if (bean.hasSubText()) {
                h += textLineSpacing + bean.getSubTextHeight();
            }
            maxContentHeight = Math.max(maxContentHeight, h);
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
                height = maxContentHeight + getPaddingTop() + getPaddingBottom() + distance * 2;
                break;
        }


        backgroundRectF.left = 0;
        backgroundRectF.top = 0;
        backgroundRectF.right = width;
        backgroundRectF.bottom = height;


        if (list.size() > 0) {
            // 等分每个tab的宽度
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

            for (int i = 0; i < list.size(); i++) {
                list.get(i).rectF.set(
                        distance + i * eachRectWidth,
                        distance,
                        distance + (i + 1) * eachRectWidth,
                        height - distance
                );
            }
        }
        setMeasuredDimension(width, height);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制背景
        paint.setColor(backgroundColor);
        canvas.drawRoundRect(backgroundRectF, getRadius(false), getRadius(false), paint);

        // 绘制前景tab矩阵
        for (SimpleRoundTabBean bean : list) {
            bean.getPaint(null).setColor(bean.backgroundColor);
            canvas.drawRoundRect(bean.rectF, getRadius(false), getRadius(false), bean.getPaint(null));
        }
        // 绘制遮罩
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
        // 绘制文字
        for (SimpleRoundTabBean bean : list) {
            float centerX = bean.rectF.centerX();
            float centerY = bean.rectF.centerY();

            if (bean.hasSubText()) {
                // 两行模式
                int mainH = bean.getMainTextHeight();
                int subH = bean.getSubTextHeight();
                float totalH = mainH + textLineSpacing + subH;

                // 绘制主标题
                Paint mainTextPaint = bean.getPaint(true);
                mainTextPaint.setTextSize(bean.textSize);
                mainTextPaint.setColor(bean.getTextColor());
                mainTextPaint.setTypeface(bean.checked ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                float mainBaseline = centerY - (totalH / 2) + mainH - DensityUtil.dp2px(1);
                canvas.drawText(bean.text, centerX, mainBaseline, mainTextPaint);

                // 绘制副标题
                Paint subTextPaint = bean.getPaint(false);
                subTextPaint.setTextSize(bean.subTextSize);
                subTextPaint.setColor(bean.getSubTextColor());
                subTextPaint.setTypeface(Typeface.DEFAULT);
                float subBaseline = mainBaseline + textLineSpacing + subH;
                canvas.drawText(bean.subText, centerX, subBaseline, subTextPaint);

            } else {
                // 单行模式
                Paint mainTextPaint = bean.getPaint(true);
                mainTextPaint.setTextSize(bean.textSize);
                mainTextPaint.setColor(bean.getTextColor());
                mainTextPaint.setTypeface(bean.checked ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                float baseline = centerY + (bean.getMainTextHeight() >> 1) - DensityUtil.dp2px(2);
                canvas.drawText(bean.text, centerX, baseline, mainTextPaint);
            }

            // 绘制圆点
            if (bean.showCornerMark) {
                bean.getPaint(null).setColor(bean.cornerMarkColor);
                float dotX = centerX + (bean.getMainTextWidth() / 2f) + (bean.showRadius * 2);
                float dotY = centerY - (bean.hasSubText() ? (bean.getMainTextHeight()) : (bean.getMainTextHeight() / 2f));
                canvas.drawCircle(dotX, dotY, bean.showRadius, bean.getPaint(null));
            }
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
                        // 获取起始下标
                        for (int ii = 0; ii < list.size(); ii++) {
                            if (list.get(ii).checked) {
                                fromPosition = ii;
                                break;
                            }
                        }
                        // 重置所有选中状态
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
        requestLayout();
    }

    public void cornerMark(int position, boolean showCornerMark, boolean invalidateEnable) {
        if (position >= 0 && position < list.size()) {
            list.get(position).showCornerMark = showCornerMark;
            if (invalidateEnable) {
                invalidate();
            }
        }
    }

    public void select(int position) {
        if (position >= 0 && position < list.size()) {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).checked = false;
            }
            list.get(position).checked = true;
            invalidate();
        }
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


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.removeAllListeners();
            valueAnimator.removeAllUpdateListeners();
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
         * 副标题文字
         */
        public String subText = "";
        /**
         * 未选中时文字颜色
         */
        public int normalTextColor = Color.parseColor("#666666");
        /**
         * 选中时文字颜色
         */
        public int checkedTextColor = Color.parseColor("#e9302d");
        /**
         * 副标题未选中时文字颜色
         */
        public int subNormalTextColor = Color.parseColor("#666666");
        /**
         * 副标题选中时文字颜色
         */
        public int subCheckedTextColor = Color.parseColor("#e9302d");
        /**
         * 背景色
         */
        public int backgroundColor = Color.parseColor("#f2f2f2");
        /**
         * 文字大小
         */
        public int textSize = DensityUtil.sp2px(14);
        /**
         * 副标题文字大小
         */
        public int subTextSize = DensityUtil.sp2px(10);
        /**
         * 显示角标
         */
        public boolean showCornerMark = false;
        /**
         * 角标半径
         */
        public int showRadius = DensityUtil.dp2px(2);
        /**
         * 有选中数据时的小圆点颜色
         */
        public int cornerMarkColor = Color.parseColor("#e9302d");

        public boolean hasSubText() {
            return subText != null && !subText.isEmpty();
        }

        int getTextColor() {
            if (checked) {
                return checkedTextColor;
            } else {
                return normalTextColor;
            }
        }

        int getSubTextColor() {
            return checked ? subCheckedTextColor : subNormalTextColor;
        }

        Paint getPaint(Boolean isSubText) {
            paint.setTextAlign(Paint.Align.CENTER);
            if (isSubText != null) {
                paint.setTextSize(isSubText ? subTextSize : textSize);
            }
            if (checked) {
                if (isSubText != null) {
                    paint.setTypeface(isSubText ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD);
                } else {
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                }
            } else {
                paint.setTypeface(Typeface.DEFAULT);
            }
            return paint;
        }

        // 获取主标题宽度
        int getMainTextWidth() {
            getPaint(true).setTextSize(textSize);
            return DrawViewUtils.getTextWH(paint, text)[0];
        }

        // 获取主标题高度
        int getMainTextHeight() {
            getPaint(true).setTextSize(textSize);
            return DrawViewUtils.getTextWH(paint, text)[1];
        }

        // 获取副标题高度
        int getSubTextHeight() {
            getPaint(false).setTextSize(subTextSize);
            return DrawViewUtils.getTextWH(paint, subText)[1];
        }
    }

    public interface OnSimpleRoundTabViewCallBack {
        void onTabChecked(int fromPosition, int toPosition);
    }
}
