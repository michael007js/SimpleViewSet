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

/**
 * @author Michael by 61642
 * @date 2025/6/12 11:06
 * @Description 个简单的双头SeekBar
 */
public class SimpleDoubleSeekBar3 extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float[] sectionPercents = {0.1f, 0.5f, 0.4f}; // 红、黄、绿

    private int thumbRadius = 20;
    private int barHeight = 10;

    private float leftPercent = 0.35f;
    private float rightPercent = 0.65f;

    private boolean isLeftThumbPressed = false;
    private boolean isRightThumbPressed = false;

    public SimpleDoubleSeekBar3(Context context) {
        super(context);
        init();
    }

    public SimpleDoubleSeekBar3(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.FILL);
        setSectionPercents(0.2f, 0.5f, 0.3f, 0.7f, 1f);
    }

    public void setSectionPercents(float red, float yellow, float green, float leftPercent, float rightPercent) {
        if (Math.abs(red + yellow + green - 1f) < 0.01f) {
            this.sectionPercents = new float[]{red, yellow, green};
            this.leftPercent = leftPercent;
            this.rightPercent = rightPercent;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth() - getPaddingStart() - getPaddingEnd();
        int height = getHeight();
        int centerY = height / 2;

        // 绘制分段颜色条
        float startX = getPaddingStart();
        int[] colors = {Color.RED, Color.rgb(255, 165, 0), Color.rgb(86, 203, 59)};

        for (int i = 0; i < sectionPercents.length; i++) {
            float endX = startX + width * sectionPercents[i];
            paint.setColor(colors[i]);
            canvas.drawRect(startX, centerY - barHeight / 2f, endX, centerY + barHeight / 2f, paint);
            startX = endX;
        }

        paint.setColor(Color.WHITE);
        //绘制滑块
        drawThumb(canvas, width * leftPercent, leftPercent, centerY);
        drawThumb(canvas, width * rightPercent, rightPercent, centerY);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float width = getWidth() - getPaddingStart() - getPaddingEnd();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isLeftThumbPressed = Math.abs(x - width * leftPercent) < thumbRadius * 2;
                isRightThumbPressed = Math.abs(x - width * rightPercent) < thumbRadius * 2;
                return true;

            case MotionEvent.ACTION_MOVE:
                float percent = x / width;
                percent = Math.max(0, Math.min(1, percent));
                boolean changed = false;

                if (isLeftThumbPressed) {
                    float newLeft = Math.min(percent, rightPercent - 0.05f);
                    if (newLeft != leftPercent) {
                        newLeft = Math.max(newLeft, 0f);
                        leftPercent = newLeft;
                        changed = true;
                    }
                    invalidate();
                } else if (isRightThumbPressed) {
                    float newRight = Math.max(percent, leftPercent + 0.05f);
                    if (newRight != rightPercent) {
                        newRight = Math.min(newRight, 1f);
                        rightPercent = newRight;
                        changed = true;
                    }
                    invalidate();
                }
                if (changed) {
                    if (onRangeChangeListener != null) {
                        onRangeChangeListener.onRangeChanged(leftPercent, rightPercent);
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

    private void drawThumb(Canvas canvas, float xCenter, float percent, int centerY) {
        String text = (int) (percent * 100) + "%";

        paint.setTextSize(30f);
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float textHeight = fm.bottom - fm.top;
        float textWidth = paint.measureText(text);
        textWidth = textWidth + DensityUtil.dp2px(5);

        float padding = DensityUtil.dp2px(2);
        float rectWidth = textWidth + padding * 2;
        float rectHeight = textHeight + padding * 2;

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
        paint.setColor(Color.RED);
        paint.setStrokeWidth(DensityUtil.dp2px(1));
        canvas.drawRoundRect(rect, rectHeight / 2, rectHeight / 2, paint);

        // 文字：红色
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        float textY = centerY - (fm.ascent + fm.descent) / 2;
        canvas.drawText(text, xCenter, textY, paint);
    }


    private OnRangeChangeListener onRangeChangeListener;

    public void setOnRangeChangeListener(OnRangeChangeListener listener) {
        this.onRangeChangeListener = listener;
    }

    public interface OnRangeChangeListener {
        void onRangeChanged(float leftPercent, float rightPercent);
    }
}
