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
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;

/**
 * @Author：fc
 * @Date：2025/10/15
 * @Desc：
 */
public class HorizontalProgressView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private Paint shadowPaint;
    private Paint glossPaint;
    int padding = DensityUtil.dp2px(0);//进度条与背景之间的间距
    private int maxValue = 50;
    private int currentValue = 0;
    private ValueAnimator valueAnimator;

    // 新增属性
    private int[] progressColors = new int[]{Color.parseColor("#FFF06B"), Color.parseColor("#FFC80F"), Color.parseColor("#FFA63A")};
    private float[] progressPositions = new float[]{0f, 0.5f, 1f}; // 渐变位置
    private boolean drawText = true;
    private boolean drawShadow = true;
    private boolean drawGloss = true;

    public HorizontalProgressView(Context context) {
        this(context, null);
    }

    public HorizontalProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#EACABF"));
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        backgroundPaint.setAntiAlias(true);

        glossPaint = new Paint();
        glossPaint.setColor(Color.parseColor("#66FFFFFF"));
        glossPaint.setStyle(Paint.Style.FILL);
        glossPaint.setStrokeCap(Paint.Cap.ROUND);
        glossPaint.setAntiAlias(true);

        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.FILL);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint();
        textPaint.setTextSize(DensityUtil.sp2px(16));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.parseColor("#9D1500"));
        textPaint.setShadowLayer(2, 1, 1, Color.parseColor("#80000000")); // 文字阴影

        // 阴影画笔
        shadowPaint = new Paint();
        shadowPaint.setColor(Color.parseColor("#40000000"));
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setAntiAlias(true);
    }

    RectF glossRect = new RectF();
    RectF backgroundShadowRect = new RectF();
    RectF backgroundRect = new RectF();
    RectF progressShadowRect = new RectF();
    LinearGradient gradient;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        backgroundShadowRect = new RectF(2, 2, getWidth() - 2, getHeight() + 2);
        backgroundRect = new RectF(0, 0, getWidth(), getHeight());

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制背景阴影
        if (drawShadow) {
            canvas.drawRoundRect(backgroundShadowRect, getHeight() / 2f, getHeight() / 2f, shadowPaint);
        }
        // 绘制背景
        canvas.drawRoundRect(backgroundRect, getHeight() / 2f, getHeight() / 2f, backgroundPaint);

        // 绘制进度条
        float progressWidth = (float) currentValue / maxValue * getWidth();
        if (progressWidth > 0) {
            // 计算进度条的实际绘制区域（添加3dp间距）
            float progressLeft = padding;
            float progressTop = padding;
            float progressRight = progressLeft + progressWidth - padding * 2;
            float progressBottom = getHeight() - padding;
            float progressHeight = progressBottom - progressTop;

            // 确保进度条不会超出背景范围
            if (progressRight > getWidth() - padding) {
                progressRight = getWidth() - padding;
            }

            // 绘制进度条阴影
            if (drawShadow) {
                progressShadowRect.left = progressLeft + 1;
                progressShadowRect.top = progressTop + 1;
                progressShadowRect.right = progressRight + 1;
                progressShadowRect.bottom = progressBottom + 1;
                canvas.drawRoundRect(progressShadowRect, progressHeight / 2f, progressHeight / 2f, shadowPaint);
            }

            // 创建进度条路径
            Path progressPath = new Path();
            float radius = progressHeight / 2f; // 圆角半径

            // 绘制圆角矩形进度条
            progressPath.addRoundRect(progressLeft, progressTop, progressRight, progressBottom, radius, radius, Path.Direction.CW);

            // 处理渐变色绘制
            if (progressColors != null && progressColors.length > 0) {
                if (progressColors.length == 1) {
                    // 单色情况
                    progressPaint.setShader(null);
                    progressPaint.setColor(progressColors[0]);
                } else {
                    // 渐变色情况
                    gradient = new LinearGradient(
                            progressLeft, progressTop, progressLeft, progressBottom,
                            progressColors,
                            progressPositions,
                            Shader.TileMode.CLAMP
                    );
                    progressPaint.setShader(gradient);
                }
                canvas.drawPath(progressPath, progressPaint);
                progressPaint.setShader(null);
            }

            if (drawGloss) {
                glossRect.left = progressLeft + DensityUtil.dp2px(15);
                glossRect.top = DensityUtil.dp2px(5);
                glossRect.right = progressRight - DensityUtil.dp2px(15);
                glossRect.bottom = glossRect.top + DensityUtil.dp2px(5);
                canvas.drawRoundRect(glossRect, DensityUtil.dp2px(2.5f), DensityUtil.dp2px(2.5f), glossPaint);
            }
        }

        // 绘制文本（在进度条终点显示）
        if (drawText) {
            String currentText = String.valueOf(currentValue);
            String maxText = "/" + maxValue;

            //在布局中心绘制文字
            float textY = getHeight() / 2f - (textPaint.getFontMetrics().ascent + textPaint.getFontMetrics().descent) / 2f;

            // 计算currentText的宽度
            float currentTextWidth = DrawViewUtils.getTextWH(textPaint, currentText)[0];
            // 计算maxText的宽度
            Paint maxPaint = new Paint(textPaint);
            maxPaint.setColor(Color.parseColor("#E18B48"));
            float maxTextWidth = DrawViewUtils.getTextWH(maxPaint, maxText)[0];

            // 计算整体文本的起始X坐标，使其居中
            float totalTextWidth = currentTextWidth + maxTextWidth + DensityUtil.dp2px(4);
            float startX = getWidth() / 2f - totalTextWidth / 2f;

            // 分别绘制当前值文本和最大值文本
            canvas.drawText(currentText, startX + currentTextWidth / 2f, textY, textPaint);
            canvas.drawText(maxText, startX + currentTextWidth + DensityUtil.dp2px(4) + maxTextWidth / 2f, textY, maxPaint);
        }
    }

    public void setProgress(int value, int maxValue) {
        if (value < 0) {
            value = 0;
        }
        if (value > maxValue) {
            value = maxValue;
        }
        currentValue = value;
        this.maxValue = maxValue;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.removeAllUpdateListeners();
            valueAnimator.cancel();
        }
        valueAnimator = ValueAnimator.ofInt(0, currentValue);
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                currentValue = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.start();
    }

    // 设置进度条背景色的方法
    public void setProgressBackgroundColor(int color) {
        backgroundPaint.setColor(color);
        invalidate();
    }

    // 设置进度条颜色的方法（单色）
    public void setProgressColor(int color) {
        this.progressColors = new int[]{color};
        // 单色不需要位置数组
        this.progressPositions = null;
        invalidate();
    }

    // 设置进度条颜色的方法（渐变色）
    public void setProgressColors(int[] colors) {
        if (colors == null || colors.length == 0) {
            return;
        }
        this.progressColors = colors;
        // 如果只有一个颜色，不需要位置数组
        if (colors.length == 1) {
            this.progressPositions = null;
        } else {
            // 多个颜色时，重新计算位置数组
            this.progressPositions = new float[colors.length];
            for (int i = 0; i < colors.length; i++) {
                this.progressPositions[i] = (float) i / (colors.length - 1);
            }
        }
        invalidate();
    }

    // 设置是否绘制文字的方法
    public void setDrawText(boolean drawText) {
        this.drawText = drawText;
        invalidate();
    }

    // 设置是否绘制阴影的方法
    public void setDrawShadow(boolean drawShadow) {
        this.drawShadow = drawShadow;
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (valueAnimator != null) {
            valueAnimator.removeAllUpdateListeners();
            valueAnimator.cancel();
            valueAnimator = null;
        }
    }
}