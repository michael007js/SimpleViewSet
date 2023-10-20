package com.sss.michael.simpleview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.EmptyUtils;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;

/**
 * @author Michael by 61642
 * @date 2023/10/19 17:19
 * @Description 一个简单的圆角柱状图（带文本）
 */
public class SimpleColumnTextView extends View {

    /**
     * 调试
     */
    private boolean debug = true;
    /**
     * path画笔
     */
    private Paint pathPaint = new Paint();
    /**
     * 文本画笔
     */
    private Paint textPaint = new Paint();
    /**
     * 遮罩画笔
     */
    private Paint maskPaint = new Paint();
    /**
     * 线宽，调试时使用
     */
    private int strokeWidth = DensityUtil.dp2px(debug ? 1 : 0);
    /**
     * 圆角
     */
    private float radius = DensityUtil.dp2px(4);
    /**
     * 截断百分比
     */
    private float limitPercent = 0.7f;
    /**
     * 截断柱状图最大绘制区域
     */
    private RectF limitRect = new RectF();
    /**
     * 最终实际绘制区域
     */
    private RectF previewRect = new RectF();
    /**
     * 实际绘制区域最大百分比（相对于{@link #limitRect}的最终高度，和{@link #limitPercent}没有任何关系）
     */
    private float maxValue = 1.0f;
    /**
     * 标签文本
     */
    private String axisLabel = "";
    /**
     * 文本与柱状顶部之间的距离
     */
    private float distanceBetweenLabelAndLimit = DensityUtil.dp2px(6);
    /**
     * 文本字体大小
     */
    private float axisLabelTextSize = DensityUtil.dp2px(16f);
    /**
     * 后缀单位
     */
    private String unit = "";
    /**
     * 单位文本字体大小
     */
    private float unitTextSize = DensityUtil.dp2px(10f);
    /**
     * 轨迹
     */
    Path path = new Path();
    private OnSimpleColumnTextViewCallBack onSimpleColumnTextViewCallBack;

    public void setOnSimpleColumnTextViewCallBack(OnSimpleColumnTextViewCallBack onSimpleColumnTextViewCallBack) {
        this.onSimpleColumnTextViewCallBack = onSimpleColumnTextViewCallBack;
    }

    ValueAnimator valueAnimator;

    {
        maskPaint.setAntiAlias(true);
        pathPaint.setStrokeWidth(strokeWidth);
        pathPaint.setAntiAlias(true);
        textPaint.setAntiAlias(true);
    }

    public SimpleColumnTextView(Context context) {
        this(context, null);
    }

    public SimpleColumnTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleColumnTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        try {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "typeface/BEBAS___.TTF");
            if (tf != null) {
                pathPaint.setTypeface(tf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (debug) {
            setOnSimpleColumnTextViewCallBack(new OnSimpleColumnTextViewCallBack() {
                @Override
                public void onClick(String axisLabel, String unit, float maxValue, SimpleColumnTextView view) {
                    setData(false, true, 1.0f, "靓妞！？！？", "😍", null, null, null, null);
                }
            });
            setData(true, false, 1.0f, "发现靓妞！！！", "", null, null, null, null);

        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height;
        if (MeasureSpec.EXACTLY == MeasureSpec.getMode(widthMeasureSpec)) {
            height = DensityUtil.dp2px(100);
        } else {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        setMeasuredDimension(width, height);

        float left = strokeWidth + (getPaddingLeft() | getPaddingStart());
        float right = width - strokeWidth - (getPaddingRight() | getPaddingEnd());
        float bottom = height - strokeWidth - getPaddingBottom();

        limitRect.bottom = bottom - strokeWidth;
        limitRect.left = left;
        limitRect.right = right;
        limitRect.top = limitRect.bottom - height * limitPercent;

        previewRect.left = limitRect.left;
        previewRect.right = limitRect.right;
        previewRect.bottom = limitRect.bottom;
        previewRect.top = limitRect.bottom;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (debug) {
            pathPaint.setStrokeWidth(strokeWidth);
            pathPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(limitRect, pathPaint);
        }

        pathPaint.setStyle(Paint.Style.STROKE);
        path.reset();
        path.moveTo(previewRect.left, previewRect.bottom);
        path.lineTo(previewRect.left, previewRect.top + radius);
        path.cubicTo(previewRect.left, previewRect.top + radius, previewRect.left, previewRect.top, previewRect.left + radius, previewRect.top);

        path.lineTo(previewRect.right - radius, previewRect.top);
        path.cubicTo(previewRect.right - radius, previewRect.top, previewRect.right, previewRect.top, previewRect.right, previewRect.top + radius);

        path.lineTo(previewRect.right, previewRect.bottom);
        path.lineTo(previewRect.left, previewRect.bottom);
        path.close();

        canvas.drawPath(path, maskPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        if (EmptyUtils.isEmpty(unit)) {

            textPaint.setTextSize(axisLabelTextSize);
            canvas.drawText(axisLabel, previewRect.left + previewRect.width() / 2f, previewRect.top - distanceBetweenLabelAndLimit, textPaint);
        } else {
            textPaint.setTextAlign(Paint.Align.RIGHT);
            textPaint.setTextSize(axisLabelTextSize);
            float axisLabelWidth = textPaint.measureText(axisLabel);

            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTextSize(unitTextSize);
            float unitWidth = textPaint.measureText(unit);

            textPaint.setTextAlign(Paint.Align.RIGHT);
            textPaint.setTextSize(axisLabelTextSize);
            canvas.drawText(axisLabel, previewRect.left + previewRect.width() / 2f + (axisLabelWidth + unitWidth) / 2, previewRect.top - distanceBetweenLabelAndLimit, textPaint);

            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTextSize(unitTextSize);
            canvas.drawText(unit, previewRect.left + previewRect.width() / 2f + (axisLabelWidth + unitWidth) / 2, previewRect.top - distanceBetweenLabelAndLimit, textPaint);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (limitRect.contains(event.getX(), event.getY())) {
                if (onSimpleColumnTextViewCallBack != null) {
                    onSimpleColumnTextViewCallBack.onClick(axisLabel, unit, maxValue, this);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    int[] checkedGradientColors = new int[]{Color.parseColor("#FF7D00"), Color.parseColor("#FF942D"), Color.parseColor("#FFCB98")};
    int[] unCheckedGradientColors = new int[]{Color.parseColor("#7EA5FF"), Color.parseColor("#91B5FF"), Color.parseColor("#B9D9FF")};
    float[] checkedPositions = new float[]{0.0f, 0.35f, 1.0f};
    float[] unCheckedPositions = new float[]{0.0f, 0.35f, 1.0f};
    boolean checkedMode;

    /**
     * 设置数据
     *
     * @param animation               是否启用动画
     * @param checkedMode             是否触摸
     * @param maxValue                实际绘制区域最大百分比，详见{@link #maxValue}
     * @param axisLabel               绘制文本
     * @param unit                    后缀单位
     * @param checkedGradientColors   选中渐变色
     * @param unCheckedGradientColors 未选中渐变色
     * @param checkedPositions        选中渐变色位置
     * @param unCheckedPositions      未选中渐变色位置
     */
    public void setData(boolean animation, boolean checkedMode, @FloatRange(from = 0f, to = 1f) float maxValue, String axisLabel, String unit, @Nullable int[] checkedGradientColors, @Nullable int[] unCheckedGradientColors, @Nullable float[] checkedPositions, @Nullable float[] unCheckedPositions) {
        this.checkedMode = checkedMode;
        this.axisLabel = axisLabel;
        this.unit = unit;
        this.maxValue = maxValue;
        if (checkedGradientColors != null) {
            this.checkedGradientColors = checkedGradientColors;
        }
        if (unCheckedGradientColors != null) {
            this.unCheckedGradientColors = unCheckedGradientColors;
        }
        if (checkedPositions != null) {
            this.checkedPositions = checkedPositions;
        }
        if (unCheckedPositions != null) {
            this.unCheckedPositions = unCheckedPositions;
        }
        if (animation) {
            startAnimation();
        } else {
            createMask(1f);
            invalidate();
        }
    }

    /**
     * 创建遮罩
     *
     * @param value 遮罩高度
     */
    void createMask(float value) {
        if (checkedGradientColors.length > 0 && checkedPositions.length > 0) {
            previewRect.top = previewRect.bottom - limitRect.height() * Math.min(maxValue, value);
            maskPaint.setShader(
                    new LinearGradient(previewRect.right,
                            previewRect.top,
                            previewRect.right,
                            previewRect.top + previewRect.bottom,
                            checkedMode ? checkedGradientColors : unCheckedGradientColors,
                            checkedMode ? checkedPositions : unCheckedPositions, Shader.TileMode.CLAMP)
            );
        }

    }

    /**
     * 召唤神龙
     */
    void startAnimation() {
        onDetachedFromWindow();
        valueAnimator = ValueAnimator.ofFloat(0f, Math.min(maxValue, 1f));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                createMask(value);
                invalidate();
            }
        });
        valueAnimator.setDuration(1000);
        valueAnimator.start();
    }

    /**
     * 打扫卫生
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.removeAllListeners();
            valueAnimator.removeAllUpdateListeners();
        }
    }

    public interface OnSimpleColumnTextViewCallBack {
        void onClick(String axisLabel, String unit, float maxValue, SimpleColumnTextView view);
    }
}
