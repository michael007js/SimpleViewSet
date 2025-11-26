package com.sss.michael.simpleview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.Log;

/**
 * @author Michael by 61642
 * @date 2025/6/12 11:06
 * @Description 个简单的双头SeekBar
 */
public class SimpleDoubleSeekBar3 extends View {
    private int width, height;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float[] sectionPercents = {20.0f, 80f, 99f}; // 红、黄、绿
    private float leftValue = 0;
    private float rightValue = 0;
    // 滑块的宽度
    private int thumbRadius = DensityUtil.dp2px(12);
    // 滑块内边距
    private float thumbPadding = DensityUtil.dp2px(4);
    // 是否按住左边滑块
    private boolean isLeftThumbPressed = false;
    // 是否按住右边滑块
    private boolean isRightThumbPressed = false;
    // 进度条的y轴中心
    private int centerY;
    // 进度条的高度
    private int barHeight = DensityUtil.dp2px(6f);
    // 每个色块开始的x值
    private float startX;
    // 颜色值
    private int[] colors = {Color.RED, Color.rgb(255, 165, 0), Color.rgb(86, 203, 59)};
    // 滑动最小值
    private float minValue = 1;
    // 滑动最大值
    private float maxValue = 99;
    private float percentValue = 0;
    private String unit = "%";

    public SimpleDoubleSeekBar3(Context context) {
        super(context, null);
    }

    public SimpleDoubleSeekBar3(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.FILL);
        setSectionPercents(0, 100, 60, 80, 50, 70, 100);
    }


    public void setSectionPercents(float minValue, float maxValue, float leftValue, float rightValue, float... sectionPercents) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        this.sectionPercents = sectionPercents;
        invalidate();
    }


    public void setSectionPercents(int leftValue, int rightValue) {
        if (minValue <= leftValue && leftValue < rightValue && rightValue <= maxValue) {
            this.leftValue = leftValue;
            this.rightValue = rightValue;
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        centerY = height / 2;
        startX = getPaddingStart();
        percentValue = (float) (width - getPaddingStart() - getPaddingEnd()) / (maxValue - minValue);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制分段颜色条
        startX = getPaddingStart();
        for (int i = 0; i < sectionPercents.length; i++) {
            float endX = i == 0 ? (startX + sectionPercents[i] * percentValue) : (startX + (sectionPercents[i] - sectionPercents[i - 1]) * percentValue);
            paint.setColor(colors[i]);
            canvas.drawRect(startX, centerY - barHeight / 2f, endX, centerY + barHeight / 2f, paint);
            startX = endX;
        }

        paint.setColor(Color.WHITE);
        // 绘制滑块

        float[] leftSize = getTextSize(leftValue + unit);
        float[] rightSize = getTextSize(leftValue + unit);

        drawThumb(canvas, leftSize, leftValue * percentValue + getPaddingStart(), leftValue + unit, centerY);
        drawThumb(canvas, rightSize, rightValue * percentValue, rightValue + unit, centerY);
    }

    float[] getTextSize(String text) {
        float[] size = new float[2];
        paint.setTextSize(DensityUtil.sp2px(14f));
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float textHeight = fm.bottom - fm.top;
        Log.log(text);
        float textWidth = paint.measureText(text);
        textWidth = textWidth + DensityUtil.dp2px(5);
        size[0] = textWidth;
        size[1] = textHeight;
        return size;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isLeftThumbPressed = Math.abs(x - leftValue * percentValue) < thumbRadius * 2;
                isRightThumbPressed = Math.abs(x - rightValue * percentValue) < thumbRadius * 2;
                return true;

            case MotionEvent.ACTION_MOVE:
                float percent = x / percentValue;
                percent = Math.max(minValue, Math.min(maxValue, percent));
                int newValue = Math.round(percent);
                boolean changed = false;

                if (isLeftThumbPressed) {
                    float newLeft = Math.min(newValue, rightValue - 5);
                    if (newLeft != leftValue) {
                        newLeft = Math.max(newLeft, minValue);
                        leftValue = newLeft;
                        changed = true;
                    }
                    invalidate();
                } else if (isRightThumbPressed) {
                    float newRight = Math.max(newValue, leftValue + 5);
                    if (newRight != rightValue) {
                        newRight = Math.min(newRight, maxValue);
                        rightValue = newRight;
                        changed = true;
                    }
                    invalidate();
                }
                if (changed) {
                    if (onRangeChangeListener != null) {
                        onRangeChangeListener.onRangeChanged(leftValue, rightValue);
                    }
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
                isLeftThumbPressed = false;
                isRightThumbPressed = false;
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void drawThumb(Canvas canvas, float[] textSize, float xCenter, String text, int centerY) {
        float textWidth = textSize[0];
        float rectWidth = textWidth + thumbPadding * 2;
        float rectHeight = textSize[1]/* + thumbPadding * 2*/;

        float left = xCenter - rectWidth / 2;
        float top = centerY - rectHeight / 2;
        float right = xCenter + rectWidth / 2;
        float bottom = centerY + rectHeight / 2;

        // 背景：白底
        paint.setColor(Color.WHITE);
        RectF rect = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rect, rectHeight / 2, rectHeight / 2, paint); // 椭圆效果

        // 边框：红色
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#E9302D"));
        paint.setStrokeWidth(DensityUtil.dp2px(1));
        canvas.drawRoundRect(rect, rectHeight / 2, rectHeight / 2, paint);

        // 文字：红色
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#E9302D"));
        paint.setTextSize(DensityUtil.sp2px(14f));
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float textY = centerY - (fm.ascent + fm.descent) / 2;
        canvas.drawText(text, xCenter, textY, paint);
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    private OnRangeChangeListener onRangeChangeListener;

    public void setOnRangeChangeListener(OnRangeChangeListener listener) {
        this.onRangeChangeListener = listener;
    }

    public interface OnRangeChangeListener {
        void onRangeChanged(float leftPercent, float rightPercent);
    }
}