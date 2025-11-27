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
import com.sss.michael.simpleview.utils.DrawViewUtils;
import com.sss.michael.simpleview.utils.Log;

/**
 * @author Michael by 61642
 * @date 2025/6/12 11:06
 * @Description 个简单的双头SeekBar
 */
public class SimpleDoubleSeekBar3 extends View {
    private float reallyValueWidth;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float[] sectionPercents = {20.0f, 80f, 99f}; // 红、黄、绿
    private float leftValue = 0;
    private float rightValue = 0;
    private float paddingStart, paddingEnd;
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
    // 滑动最小值
    private float minValue = 1;
    // 滑动最大值
    private float maxValue = 99;
    //每百分之1占据的像素值
    private float percentValue = 0;
    //步进
    float step = 0.1f;

    public SimpleDoubleSeekBar3(Context context) {
        super(context, null);
    }

    public SimpleDoubleSeekBar3(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.FILL);
        setSectionPercents(1.0f, 5.0f, 1.0f, 2.0f, 5.0f);
        setOnRangeChangeListener(new OnRangeChangeListener() {
            @Override
            public void onRangeChanged(float leftValue, float leftPercent, float rightValue, float rightPercent) {
                Log.log(leftValue, leftPercent, rightValue, rightPercent, percentValue, reallyValueWidth);
            }

            @Override
            public String getPreviewText(boolean leftThumb, float value) {
                return value + "";
            }

            @Override
            public float horizontalOffsetOfThumbAtxAxis(boolean leftThumb, float value) {
                return 0;
            }

            @Override
            public int onDrawSection(float sectionPercent, int sectionIndexInArray) {
                return 0xffe9302d;
            }
        });
    }


    /**
     * @param minValue        最小值
     * @param maxValue        最大值
     * @param leftValue       左边滑块值
     * @param rightValue      右边滑块值
     * @param sectionPercents 分段背景百分比，多段
     */
    public void setSectionPercents(float minValue, float maxValue, float leftValue, float rightValue, float... sectionPercents) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        this.sectionPercents = sectionPercents;
        invalidate();
    }

    public float getLeftValue() {
        return leftValue;
    }

    public float getRightValue() {
        return rightValue;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        centerY = height / 2;
        paddingStart = getPaddingStart() | getPaddingLeft();
        paddingEnd = getPaddingEnd() | getPaddingRight();
        reallyValueWidth = width - paddingStart - paddingEnd;
        percentValue = reallyValueWidth / (maxValue - minValue);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制分段颜色条
        float startX = paddingStart;
        for (int i = 0; i < sectionPercents.length; i++) {
            float endX;
            if (i == 0) {
                endX = startX + sectionPercents[i] * percentValue;
            } else if (i == sectionPercents.length - 1) {
                endX = startX + (sectionPercents[i] - sectionPercents[i - 1]) * percentValue;
            } else {
                endX = startX + (sectionPercents[i] - sectionPercents[i - 1]) * percentValue - paddingEnd;
            }
            int color;

            if (onRangeChangeListener != null) {
                color = onRangeChangeListener.onDrawSection(sectionPercents[i], i);
            } else {
                color = 0x00000000;
            }
            paint.setColor(color);
            canvas.drawRect(startX, centerY - barHeight / 2f, endX, centerY + barHeight / 2f, paint);
            startX = endX;
        }

        paint.setColor(Color.WHITE);
        // 绘制滑块

        String leftText = onRangeChangeListener == null ? leftValue + "" : onRangeChangeListener.getPreviewText(true, leftValue);
        String rightText = onRangeChangeListener == null ? rightValue + "" : onRangeChangeListener.getPreviewText(false, rightValue);
        float[] leftSize = getTextSize(leftText);
        float[] rightSize = getTextSize(rightText);

        drawThumb(true, canvas, leftSize, leftValue * percentValue + leftSize[0] / 2, leftText, centerY);
        drawThumb(false, canvas, rightSize, rightValue * percentValue - paddingEnd + rightSize[0] / 2, rightText, centerY);
    }

    float[] getTextSize(String text) {
        paint.setTextSize(DensityUtil.sp2px(14f));
        paint.setTextAlign(Paint.Align.CENTER);
        return DrawViewUtils.getTextWHF(paint, text);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isLeftThumbPressed = leftRect.contains(x, event.getY());
                isRightThumbPressed = rightRect.contains(x, event.getY());
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;

            case MotionEvent.ACTION_MOVE:
                float percent = x / percentValue;
                percent = Math.max(minValue, Math.min(maxValue, percent));
                int newValue = Math.round(percent);
                boolean changed = false;

                if (isLeftThumbPressed) {
                    float newLeft = Math.min(newValue, rightValue - step);
                    if (newLeft != leftValue) {
                        newLeft = Math.max(newLeft, minValue);
                        leftValue = newLeft;
                        changed = true;
                    }
                    invalidate();
                } else if (isRightThumbPressed) {
                    float newRight = Math.max(newValue, leftValue + step);
                    if (newRight != rightValue) {
                        newRight = Math.min(newRight, maxValue);
                        rightValue = newRight;
                        changed = true;
                    }
                    invalidate();
                }
                if (changed) {
                    if (onRangeChangeListener != null) {
                        float leftPercent = leftValue / reallyValueWidth * 10;
                        leftPercent = Math.max(leftPercent, 0);
                        leftPercent = Math.min(leftPercent, 1);
                        float rightPercent = rightValue / reallyValueWidth * 10;
                        rightPercent = Math.min(rightPercent, 1);
                        rightPercent = Math.max(rightPercent, 0);
                        onRangeChangeListener.onRangeChanged(leftValue, leftPercent, rightValue, rightPercent);
                    }
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
                isLeftThumbPressed = false;
                isRightThumbPressed = false;
                getParent().requestDisallowInterceptTouchEvent(false);
        }
        return super.onTouchEvent(event);
    }

    RectF leftRect = new RectF();
    RectF rightRect = new RectF();

    private void drawThumb(boolean leftThumb, Canvas canvas, float[] textSize, float xCenter, String text, int centerY) {
        float textWidth = textSize[0];
        float rectWidth = textWidth + thumbPadding * 2;
        float rectHeight = textSize[1]/* + thumbPadding * 2*/;
        float left = xCenter - rectWidth / 2;
        float top = centerY - rectHeight / 2;
        float right = xCenter + rectWidth / 2;
        float bottom = centerY + rectHeight / 2;

        // 背景：白底
        paint.setColor(Color.WHITE);
        float offset = 0;
        if (onRangeChangeListener != null) {
            offset = onRangeChangeListener.horizontalOffsetOfThumbAtxAxis(leftThumb, leftThumb ? leftValue : rightValue);
        } else {
            offset = leftThumb ? (leftValue < 2 ? DensityUtil.dp2px(5) : 0) : 0;
        }

        if (leftThumb) {
            leftRect.set(left + offset, top, right + offset, bottom);
        } else {
            rightRect.set(left + offset, top, right + offset, bottom);
        }
        canvas.drawRoundRect(leftThumb ? leftRect : rightRect, rectHeight / 2, rectHeight / 2, paint); // 椭圆效果

        // 边框：红色
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#E9302D"));
        paint.setStrokeWidth(DensityUtil.dp2px(1));
        canvas.drawRoundRect(leftThumb ? leftRect : rightRect, rectHeight / 2, rectHeight / 2, paint);

        // 文字：红色
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#E9302D"));
        paint.setTextSize(DensityUtil.sp2px(14f));
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float textY = centerY - (fm.ascent + fm.descent) / 2;
        canvas.drawText(text, xCenter + offset, textY, paint);
    }


    private OnRangeChangeListener onRangeChangeListener;

    public void setOnRangeChangeListener(OnRangeChangeListener listener) {
        this.onRangeChangeListener = listener;
    }

    /*
    eg:
       setOnRangeChangeListener(new OnRangeChangeListener() {
            @Override
            public void onRangeChanged(float leftPercent, float rightPercent) {
                Log.log(leftPercent, rightPercent);
            }

            @Override
            public String getPreviewText(boolean left, float value) {
                return value + "" + left;
            }

            @Override
            public float offset(boolean leftThumb, float value) {

                if (leftThumb) {
                    return value <= 2 ? DensityUtil.dp2px(5) : 0;
                } else {
                    return value >= 98 ? -DensityUtil.dp2px(10) : 0;
                }
            }

            @Override
             public int onDrawSection(float sectionPercent, int sectionIndexInArray) {
                 if (sectionIndexInArray == 0) {
                     return Color.RED;
                 } else if (sectionIndexInArray == 1) {
                     return Color.rgb(255, 165, 0);
                 } else if (sectionIndexInArray == 2) {
                     return Color.rgb(86, 203, 59);

                 }
                 return 0;
             }
        });
    * */
    public interface OnRangeChangeListener {
        /**
         * 范围已更改时被调用
         *
         * @param leftValue    左边滑块条值（不是百分比）
         * @param leftPercent  左边滑块条百分比
         * @param rightValue   右边滑块条值（不是百分比）
         * @param rightPercent 右边滑块条百分比
         */
        void onRangeChanged(float leftValue, float leftPercent, float rightValue, float rightPercent);

        /**
         * 渲染文字（实时）
         *
         * @param leftThumb 是否是左边滑块
         * @param value     值
         * @return 渲染文字
         */
        String getPreviewText(boolean leftThumb, float value);

        /**
         * 滑块的X轴横向偏移量（应对滑动到左右顶点时不灵敏的问题）
         *
         * @param leftThumb 是否是左边滑块
         * @param value     值
         * @return 偏移量
         */
        float horizontalOffsetOfThumbAtxAxis(boolean leftThumb, float value);

        /**
         * 绘制分段背景进度
         *
         * @param sectionPercent      分段百分比数值
         * @param sectionIndexInArray 分段数组中的下标
         * @return 分段颜色
         */
        int onDrawSection(float sectionPercent, int sectionIndexInArray);
    }
}
