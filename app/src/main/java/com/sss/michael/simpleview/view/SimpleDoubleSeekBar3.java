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
    private float reallyValueWidth; // 可用像素宽度（不含 padding）
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float[] sectionPercents = {20.0f, 80f, 99f}; // 分段的“值”位置（假定为与 min/max 同量纲）
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
    // 每单位值对应的像素（value -> px 的比例）
    private float percentValue = 0;
    //步进
    float step = 0.01f;

    public SimpleDoubleSeekBar3(Context context) {
        super(context, null);
    }

    public SimpleDoubleSeekBar3(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.FILL);
        setSectionPercents(220, 995, 400, 666, 500, 800, 995);
        setOnRangeChangeListener(new OnRangeChangeListener() {
            @Override
            public void onRangeChanged(float leftValue, float leftPercent, float rightValue, float rightPercent) {
                Log.log(leftValue, leftPercent, rightValue, rightPercent, percentValue, reallyValueWidth);
            }

            @Override
            public String getPreviewText(boolean leftThumb, float value) {
                return String.format("%.1f", value);
            }

            @Override
            public float horizontalOffsetOfThumbAtxAxis(boolean leftThumb, float value) {
                return 0;
            }

            @Override
            public int onDrawSection(float sectionPercent, int sectionIndexInArray) {
                if (sectionIndexInArray == 0) {
                    return 0xffff0000;
                } else if (sectionIndexInArray == 1) {
                    return 0xff00ff00;
                } else if (sectionIndexInArray == 2) {
                    return 0xff0000ff;
                }
                return 0xffe9302d;
            }
        });
    }


    /**
     * @param minValue        最小值
     * @param maxValue        最大值
     * @param leftValue       左边滑块值
     * @param rightValue      右边滑块值
     * @param sectionPercents 分段背景百分比，多段（这里的数组表示“值”位置，必须在 [minValue,maxValue] 范围）
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
        paddingStart = Math.max(getPaddingStart(), getPaddingLeft());
        paddingEnd = Math.max(getPaddingEnd(), getPaddingRight());
        reallyValueWidth = width - paddingStart - paddingEnd;
        if (reallyValueWidth < 0) reallyValueWidth = 0;
        float valueRange = maxValue - minValue;
        percentValue = valueRange == 0 ? 0 : (reallyValueWidth / valueRange);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //计算进度条关键坐标（左滑块、右滑块、最大宽度对应的X）
        float leftX = valueToX(leftValue);
        float rightX = valueToX(rightValue);
        float maxX = paddingStart + (maxValue - minValue) * percentValue;
        float barTop = centerY - barHeight / 2f;
        float barBottom = centerY + barHeight / 2f;
        RectF barRect = new RectF(paddingStart, barTop, maxX, barBottom);

        // 绘制进度条整体边框（灰色）
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setColor(0xffcccccc); // 边框颜色（浅灰）
//        paint.setStrokeWidth(DensityUtil.dp2px(1));
        float cornerRadius = Math.max(DensityUtil.dp2px(4), barHeight / 2f);
        canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, paint);
        paint.setStyle(Paint.Style.FILL); // 恢复填充模式

        // 绘制左区域（min → 左滑块）：浅灰色背景 + 边框（只绘制在有效区内）
        if (leftX > paddingStart) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xfff2f2f2); // 浅灰底色
            canvas.drawRoundRect(paddingStart, barTop, leftX, barBottom, cornerRadius, cornerRadius, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(0xfff2f2f2);
            paint.setStrokeWidth(DensityUtil.dp2px(1));
            canvas.drawRoundRect(paddingStart, barTop, leftX, barBottom, cornerRadius, cornerRadius, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        // 绘制中间区域（左滑块 → 右滑块）：按原分段逐段绘制，但只绘制与 [leftX,rightX] 有交集的部分
        if (rightX > leftX) {
            float startX = paddingStart;
            float prevValue = minValue;
            for (int i = 0; i < sectionPercents.length; i++) {
                float secVal = sectionPercents[i];
                secVal = Math.max(minValue, Math.min(maxValue, secVal));
                float segStartX = startX;
                float segEndX = startX + (secVal - prevValue) * percentValue;
                // 计算当前分段与选中区的交集区间（如果有）
                float drawLeft = Math.max(segStartX, leftX);
                float drawRight = Math.min(segEndX, rightX);
                if (drawRight > drawLeft) {
                    int color = onRangeChangeListener != null ? onRangeChangeListener.onDrawSection(secVal, i) : 0x00000000;
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(color);
                    canvas.drawRect(drawLeft, barTop, drawRight, barBottom, paint);
                }
                // 进度继续累加
                startX = segEndX;
                prevValue = secVal;
            }
            // 如果 sectionPercents 没覆盖到 maxValue），需要考虑其与选中区的交集
            if (prevValue < maxValue) {
                float segStartX = startX;
                float segEndX = paddingStart + (maxValue - minValue) * percentValue;
                float drawLeft = Math.max(segStartX, leftX);
                float drawRight = Math.min(segEndX, rightX);
                if (drawRight > drawLeft) {
                    // 没有回调颜色的话保持透明或默认色（这里用默认红）
                    int color = onRangeChangeListener != null ? onRangeChangeListener.onDrawSection(maxValue, sectionPercents.length) : 0xffe9302d;
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(color);
                    canvas.drawRect(drawLeft, barTop, drawRight, barBottom, paint);
                }
            }
        }

        // 5. 绘制右区域（右滑块 → max）：浅灰色背景 + 边框（只绘制在有效区内）
        if (rightX < maxX) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xfff2f2f2); // 浅灰底色
            canvas.drawRoundRect(rightX, barTop, maxX, barBottom, cornerRadius, cornerRadius, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(0xfff2f2f2);
            paint.setStrokeWidth(DensityUtil.dp2px(1));
            canvas.drawRoundRect(rightX, barTop, maxX, barBottom, cornerRadius, cornerRadius, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        // -------------- 以下保留原滑块绘制逻辑 --------------
        paint.setColor(Color.WHITE);
        String leftText = onRangeChangeListener == null ? leftValue + "" : onRangeChangeListener.getPreviewText(true, leftValue);
        String rightText = onRangeChangeListener == null ? rightValue + "" : onRangeChangeListener.getPreviewText(false, rightValue);
        float[] leftSize = getTextSize(leftText);
        float[] rightSize = getTextSize(rightText);

        float leftCenterX = valueToX(leftValue);
        float rightCenterX = valueToX(rightValue);

        float leftOffset = onRangeChangeListener == null ? 0f : onRangeChangeListener.horizontalOffsetOfThumbAtxAxis(true, leftValue);
        float rightOffset = onRangeChangeListener == null ? 0f : onRangeChangeListener.horizontalOffsetOfThumbAtxAxis(false, rightValue);

        drawThumb(true, canvas, leftSize, leftCenterX + leftOffset, leftText, centerY);
        drawThumb(false, canvas, rightSize, rightCenterX + rightOffset, rightText, centerY);
    }


    float[] getTextSize(String text) {
        paint.setTextSize(DensityUtil.sp2px(14f));
        paint.setTextAlign(Paint.Align.CENTER);
        return DrawViewUtils.getTextWHF(paint, text);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isLeftThumbPressed = leftRect.contains(x, y);
                isRightThumbPressed = rightRect.contains(x, y);
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;

            case MotionEvent.ACTION_MOVE:
                // 像素 -> 值（扣除 paddingStart）
                float rawValue = xToValue(x);
                rawValue = Math.max(minValue, Math.min(maxValue, rawValue));
                // 对齐到 step
                float stepped = alignToStep(rawValue);

                boolean changed = false;

                if (isLeftThumbPressed) {
                    // 不超过右侧 - step
                    float newLeft = Math.min(stepped, rightValue - step);
                    newLeft = Math.max(newLeft, minValue);
                    if (Float.compare(newLeft, leftValue) != 0) {
                        leftValue = newLeft;
                        changed = true;
                    }
                } else if (isRightThumbPressed) {
                    float newRight = Math.max(stepped, leftValue + step);
                    newRight = Math.min(newRight, maxValue);
                    if (Float.compare(newRight, rightValue) != 0) {
                        rightValue = newRight;
                        changed = true;
                    }
                }
                if (changed) {
                    if (onRangeChangeListener != null) {
                        float leftPercent = (leftValue - minValue) / (maxValue - minValue);
                        float rightPercent = (rightValue - minValue) / (maxValue - minValue);
                        leftPercent = Math.max(0f, Math.min(1f, leftPercent));
                        rightPercent = Math.max(0f, Math.min(1f, rightPercent));
                        onRangeChangeListener.onRangeChanged(leftValue, leftPercent, rightValue, rightPercent);
                    }
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isLeftThumbPressed = false;
                isRightThumbPressed = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    RectF leftRect = new RectF();
    RectF rightRect = new RectF();

    private void drawThumb(boolean leftThumb, Canvas canvas, float[] textSize, float xCenter, String text, int centerY) {
        float textWidth = textSize[0];
        float rectWidth = textWidth + thumbPadding * 2;
        float rectHeight = textSize[1]; // 文字高度已经包含 ascent/descent
        float left = xCenter - rectWidth / 2;
        float top = centerY - rectHeight / 2;
        float right = xCenter + rectWidth / 2;
        float bottom = centerY + rectHeight / 2;

        // 背景：白底
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);

        if (leftThumb) {
            leftRect.set(left, top, right, bottom);
        } else {
            rightRect.set(left, top, right, bottom);
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
        canvas.drawText(text, xCenter, textY, paint);
    }

    /**
     * 将值转换为控件内的 x 坐标（像素）
     */
    private float valueToX(float value) {
        if (percentValue == 0) {
            return paddingStart;
        }
        float clamped = Math.max(minValue, Math.min(maxValue, value));
        return paddingStart + (clamped - minValue) * percentValue;
    }

    /**
     * 将 x（像素）转换为对应的值（在 minValue..maxValue 范围内）
     */
    private float xToValue(float x) {
        if (percentValue == 0) return minValue;
        float relative = x - paddingStart;
        float val = minValue + (relative / percentValue);
        return val;
    }

    /**
     * 按 step 对齐（避免浮点抖动）
     */
    private float alignToStep(float raw) {
        if (step <= 0) return raw;
        float steps = Math.round(raw / step);
        return steps * step;
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
         * @param leftPercent  左边滑块条百分比（0..1）
         * @param rightValue   右边滑块条值（不是百分比）
         * @param rightPercent 右边滑块条百分比（0..1）
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
         * @return 偏移量（像素）
         */
        float horizontalOffsetOfThumbAtxAxis(boolean leftThumb, float value);

        /**
         * 绘制分段背景进度
         *
         * @param sectionPercent      分段百分比数值（这里传入的是该段对应的“值”位置）
         * @param sectionIndexInArray 分段数组中的下标
         * @return 分段颜色
         */
        int onDrawSection(float sectionPercent, int sectionIndexInArray);
    }
}
