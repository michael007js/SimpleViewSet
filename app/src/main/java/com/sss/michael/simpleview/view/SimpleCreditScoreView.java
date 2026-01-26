package com.sss.michael.simpleview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import com.sss.michael.simpleview.utils.DensityUtil;

import java.util.Locale;

/**
 * @author Michael by 61642
 * @date 2026/1/26 13:25
 * @Description 信用分仪表盘
 */
public class SimpleCreditScoreView extends View {
    // 外部文字空间比例
    private final float proportionOfExternalTextSpace = 0.22f;
    // 段落间的间隔角度
    private final float paragraphsBetweenDistance = 8f;

    // 背景轨道颜色
    private int colorBg = 0xFFE9EEF3;
    // 绿色段渐变颜色（低分段）
    private int colorLowStart = 0xFF5EE5A5;
    private int colorLowEnd = 0xFFDDF264;
    // 黄色段渐变颜色（中分段）
    private int colorMidStart = 0xFFFCE144;
    private int colorMidEnd = 0xFFFF9A42;
    // 红色段渐变颜色（高分段）
    private int colorHighStart = 0xFFFF9A42;
    private int colorHighEnd = 0xFFF75841;
    // 内部细线颜色
    private int colorInnerLine = 0xFFE0F7FA;
    // 内部圆点颜色
    private int colorDot = 0xFF81D4FA;
    // 中心数值文字颜色
    private int colorValueText = 0xFF333333;
    // 标签文字高亮色（激活态）
    private int colorLabelLowHighlight = 0xFF5EE5A5;
    private int colorLabelMidHighlight = 0xFFFBA13A;
    private int colorLabelHighHighlight = 0xFFF75841;

    // 高度占宽度的最大百分比
    private float maximumPercentageOfHeightToWidth = 0.6f;

    // 绘制相关变量
    private Paint arcPaint, innerLinePaint, textPaint, valuePaint, dotPaint;
    private RectF arcRect, innerArcRect;
    private float mRadius, mCenterX, mCenterY;
    private float arcStrokeWidth;
    private float sectionMaxSweep;

    // 动画相关
    private float currentAnimScore = 0f;
    private ValueAnimator scoreAnimator;

    public SimpleCreditScoreView(Context context) {
        this(context, null);
    }

    public SimpleCreditScoreView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleCreditScoreView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.BUTT);

        innerLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerLinePaint.setStyle(Paint.Style.STROKE);
        innerLinePaint.setColor(colorInnerLine);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(colorDot);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);

        valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setColor(colorValueText);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        try {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "typeface/BEBAS___.TTF");
            valuePaint.setTypeface(tf);
        } catch (Exception e) {
            e.printStackTrace();
        }

        sectionMaxSweep = (180f - 2 * paragraphsBetweenDistance) / 3f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (width * maximumPercentageOfHeightToWidth);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float availableW = w - getPaddingLeft() - getPaddingRight();
        float availableH = h - getPaddingTop() - getPaddingBottom();

        mRadius = Math.min(availableW / (2f * (1f + proportionOfExternalTextSpace)), availableH / (1f + proportionOfExternalTextSpace));
        mCenterX = getPaddingLeft() + availableW / 2f;
        mCenterY = getPaddingTop() + availableH - (mRadius * 0.15f);

        arcStrokeWidth = mRadius * 0.18f;
        arcPaint.setStrokeWidth(arcStrokeWidth);
        innerLinePaint.setStrokeWidth(mRadius * 0.008f);
        textPaint.setTextSize(mRadius * 0.15f);
        valuePaint.setTextSize(mRadius * 0.35f);

        arcRect = new RectF(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius, mCenterY + mRadius);

        float innerOffset = arcStrokeWidth + (mRadius * 0.1f);
        innerArcRect = new RectF(arcRect.left + innerOffset, arcRect.top + innerOffset, arcRect.right - innerOffset, arcRect.bottom - innerOffset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mRadius <= 0) return;

        drawDynamicArcs(canvas);
        drawInnerDecoration(canvas);
        drawLabels(canvas);
        drawCenterValue(canvas);
    }

    private void drawDynamicArcs(Canvas canvas) {
        float startAngle1 = 180f;
        float startAngle2 = 180f + sectionMaxSweep + paragraphsBetweenDistance;
        float startAngle3 = 180f + 2 * (sectionMaxSweep + paragraphsBetweenDistance);

        // 绘制背景轨道
        arcPaint.setShader(null);
        arcPaint.setStrokeCap(Paint.Cap.BUTT);
        arcPaint.setColor(colorBg);
        canvas.drawArc(arcRect, startAngle1, sectionMaxSweep, false, arcPaint);
        canvas.drawArc(arcRect, startAngle2, sectionMaxSweep, false, arcPaint);
        canvas.drawArc(arcRect, startAngle3, sectionMaxSweep, false, arcPaint);

        // 补全背景轨道的物理首尾圆角
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawArc(arcRect, 180f, 0.1f, false, arcPaint);
        canvas.drawArc(arcRect, 360f, 0.1f, false, arcPaint);

        // 绘制彩色进度
        float totalAvailableSweep = 180f - 2 * paragraphsBetweenDistance;
        float currentTotalSweep = (currentAnimScore / 100f) * totalAvailableSweep;

        // 低
        float sweep1 = Math.min(currentTotalSweep, sectionMaxSweep);
        if (sweep1 > 0) {
            arcPaint.setShader(new LinearGradient(
                    arcRect.left, arcRect.centerY(),
                    arcRect.left + arcRect.width() / 3, arcRect.centerY(),
                    colorLowStart, colorLowEnd,
                    Shader.TileMode.CLAMP
            ));
            arcPaint.setStrokeCap(sweep1 < sectionMaxSweep ? Paint.Cap.ROUND : Paint.Cap.BUTT);
            canvas.drawArc(arcRect, startAngle1, sweep1, false, arcPaint);

            arcPaint.setShader(null);
            arcPaint.setColor(colorLowStart);
            arcPaint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawArc(arcRect, 180f, 0.1f, false, arcPaint);
        }

        // 中
        if (currentTotalSweep > sectionMaxSweep) {
            float sweep2 = Math.min(currentTotalSweep - sectionMaxSweep, sectionMaxSweep);
            if (sweep2 > 0) {
                arcPaint.setShader(new LinearGradient(
                        arcRect.centerX() - arcRect.width() / 6, arcRect.centerY(),
                        arcRect.centerX() + arcRect.width() / 6, arcRect.centerY(),
                        colorMidStart, colorMidEnd,
                        Shader.TileMode.CLAMP
                ));
                arcPaint.setStrokeCap(Paint.Cap.BUTT);
                canvas.drawArc(arcRect, startAngle2, sweep2, false, arcPaint);
                if (sweep2 < sectionMaxSweep) {
                    arcPaint.setStrokeCap(Paint.Cap.ROUND);
                    canvas.drawArc(arcRect, startAngle2 + sweep2, 0.1f, false, arcPaint);
                }
            }
        }

        // 高
        if (currentTotalSweep > sectionMaxSweep * 2) {
            float sweep3 = Math.min(currentTotalSweep - sectionMaxSweep * 2, sectionMaxSweep);
            if (sweep3 > 0) {
                arcPaint.setShader(new LinearGradient(
                        arcRect.right - arcRect.width() / 3, arcRect.centerY(),
                        arcRect.right, arcRect.centerY(),
                        colorHighStart, colorHighEnd,
                        Shader.TileMode.CLAMP
                ));
                arcPaint.setStrokeCap(Paint.Cap.BUTT);
                canvas.drawArc(arcRect, startAngle3, sweep3, false, arcPaint);
                arcPaint.setStrokeCap(Paint.Cap.ROUND);
                canvas.drawArc(arcRect, startAngle3 + sweep3, 0.1f, false, arcPaint);
            }
        }
    }

    private void drawInnerDecoration(Canvas canvas) {
        canvas.drawArc(innerArcRect, 180f, 180f, false, innerLinePaint);
        float innerRadius = innerArcRect.width() / 2f;
        float dotRadius = mRadius * 0.025f;
        for (int i = 0; i <= 4; i++) {
            double angle = Math.toRadians(180 + (180 * i / 4.0));
            float dx = (float) (mCenterX + innerRadius * Math.cos(angle));
            float dy = (float) (mCenterY + innerRadius * Math.sin(angle));
            canvas.drawCircle(dx, dy, dotRadius, dotPaint);
        }
    }

    private void drawLabels(Canvas canvas) {
        float labelRadius = mRadius + arcStrokeWidth * 0.8f + (mRadius * 0.04f);

        boolean isLowActive = currentAnimScore > 0;
        drawTextOnArc(canvas, "低", 180f, sectionMaxSweep, labelRadius,
                isLowActive ? colorLabelLowHighlight : colorLowStart, isLowActive);

        boolean isMidActive = currentAnimScore > 33.33f;
        drawTextOnArc(canvas, "中", 180f + sectionMaxSweep + paragraphsBetweenDistance, sectionMaxSweep, labelRadius,
                isMidActive ? colorLabelMidHighlight : colorMidStart, isMidActive);

        boolean isHighActive = currentAnimScore > 66.66f;
        drawTextOnArc(canvas, "高", 180f + 2 * (sectionMaxSweep + paragraphsBetweenDistance), sectionMaxSweep, labelRadius,
                isHighActive ? colorLabelHighHighlight : colorHighStart, isHighActive);
    }

    private void drawTextOnArc(Canvas canvas, String text, float startAngle, float sweep, float radius, int color, boolean isBold) {
        textPaint.setColor(color);
        textPaint.setTypeface(isBold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);

        RectF pathRect = new RectF(mCenterX - radius, mCenterY - radius, mCenterX + radius, mCenterY + radius);
        Path path = new Path();
        path.addArc(pathRect, startAngle, sweep);
        canvas.drawTextOnPath(text, path, 0, 0, textPaint);
    }

    private void drawCenterValue(Canvas canvas) {
        valuePaint.setSubpixelText(true);
        valuePaint.setLinearText(true);

        float valueTextSize = mRadius * 0.46f;
        float unitTextSize = mRadius * 0.25f;
        float distance = DensityUtil.dp2px(4);

        float y = mCenterY - (mRadius * 0.02f);

        String scoreStr = String.format(Locale.getDefault(), "%.1f", currentAnimScore);
        String unitStr = "%";

        valuePaint.setTextAlign(Paint.Align.LEFT);


        valuePaint.setTextSize(valueTextSize);
        float valueWidth = valuePaint.measureText(scoreStr);

        valuePaint.setTextSize(unitTextSize);
        float unitWidth = valuePaint.measureText(unitStr);

        // 计算整体起始点，使整体在 mCenterX 居中
        float totalWidth = valueWidth + distance + unitWidth;
        float startX = mCenterX - totalWidth / 2f;

        // 绘制数字
        valuePaint.setTextSize(valueTextSize);
        canvas.drawText(scoreStr, startX, y, valuePaint);

        // 绘制百分号
        valuePaint.setTextSize(unitTextSize);
        canvas.drawText(unitStr, startX + valueWidth + distance, y - (valueTextSize * 0.05f/*上浮数字高度的 5%*/), valuePaint);

        valuePaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * 设置分数
     */
    public void setScore(float score) {
        float targetScore = Math.max(0, Math.min(100, score));
        if (scoreAnimator != null && scoreAnimator.isRunning()) {
            scoreAnimator.cancel();
        }

        scoreAnimator = ValueAnimator.ofFloat(currentAnimScore, targetScore);
        scoreAnimator.setDuration(1000);
        scoreAnimator.setInterpolator(new DecelerateInterpolator());
        scoreAnimator.addUpdateListener(animation -> {
            currentAnimScore = (float) animation.getAnimatedValue();
            invalidate();
        });
        scoreAnimator.start();
    }

}