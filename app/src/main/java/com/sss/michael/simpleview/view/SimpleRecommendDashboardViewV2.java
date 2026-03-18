package com.sss.michael.simpleview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.EmptyUtils;

@SuppressWarnings("all")
public class SimpleRecommendDashboardViewV2 extends View {
    private final boolean DEBUG = true;

    // 中心点
    private float centerX, centerY;
    // 控件宽高
    private int width, height;
    // 圆环画笔
    private Paint outerCirclePaint;
    // 刻度画笔 (虚线)
    private Paint scalePaint;
    // 文字画笔
    private Paint textPaint;
    // 进度条画笔
    private Paint progressPaint;

    // 圆环绘制区域
    private RectF circleRect = new RectF();
    // 圆环宽度
    private int circleStrokeWidth = DensityUtil.dp2px(8);
    // 整体向上偏移量
    private int offsetToUp = DensityUtil.dp2px(80);
    // 圆环半径（0表示自动计算）
    private int customRadius = 0;
    // padding
    private int padding = 0;

    // 数据模型
    private SimpleDashboardBean model;
    // 动画进度
    private float progress = 0f;
    private ValueAnimator valueAnimator;

    public SimpleRecommendDashboardViewV2(Context context) {
        this(context, null);
    }

    public SimpleRecommendDashboardViewV2(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleRecommendDashboardViewV2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.TRANSPARENT);

        // 外圆环画笔
        outerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outerCirclePaint.setStyle(Paint.Style.STROKE);
        outerCirclePaint.setStrokeWidth(circleStrokeWidth);
        outerCirclePaint.setColor(0x80FFFFFF);

        // 刻度画笔 (圆环内侧的虚线)
        scalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scalePaint.setStyle(Paint.Style.STROKE);
        scalePaint.setStrokeWidth(DensityUtil.dp2px(3));
        scalePaint.setColor(0x40FFFFFF);
        // 设置虚线效果
        scalePaint.setPathEffect(new DashPathEffect(
                new float[]{DensityUtil.dp2px(3), DensityUtil.dp2px(4)}, 0));

        // 文字画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);

        // 进度条画笔
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(circleStrokeWidth);
        progressPaint.setColor(Color.WHITE);

        if (DEBUG) {
            setModel(new SimpleDashboardBean(0, 0, 0, "适合你的大学"), true);
        }
    }

    /**
     * 设置数据并刷新
     *
     * @param bean      数据实体
     * @param animation 是否开启动画
     */
    public void setModel(SimpleDashboardBean bean, boolean animation) {
        this.model = bean;
        if (animation) {
            startAnim();
        } else {
            progress = 1f;
            invalidate();
        }
    }

    /**
     * 设置圆环半径
     *
     * @param radius 半径（dp），0表示自动计算
     */
    public void setRadius(int radius) {
        this.customRadius = DensityUtil.dp2px(radius);
        requestLayout();
    }

    private void startAnim() {
        if (valueAnimator != null) valueAnimator.cancel();
        valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setDuration(800);
        valueAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
        valueAnimator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        valueAnimator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);

        // 高度处理：如果是精确模式就用给定值，否则默认200dp
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (MeasureSpec.EXACTLY == MeasureSpec.getMode(heightMeasureSpec)) {
            height = heightSize;
        } else {
            height = DensityUtil.dp2px(200);
        }
        setMeasuredDimension(width, height);

        // 计算中心点
        centerX = width / 2f;
        centerY = height;

        // 计算圆环的边界 RectF
        int diameter;
        if (customRadius > 0) {
            // 使用自定义半径
            diameter = customRadius * 2;
        } else {
            // 自动计算：取宽高中较小的值来确定直径
            diameter = Math.min(width, height * 2) - DensityUtil.dp2px(20);
        }
        padding = Math.max(Math.max(getPaddingLeft(), getPaddingRight()), Math.max(getPaddingTop(), getPaddingBottom()));
        diameter -= padding * 2;
        int textSize = DensityUtil.dp2px(40);
        circleRect.left = (centerX - diameter / 2) + textSize;
        circleRect.top = (centerY - diameter / 2f) + textSize - offsetToUp;
        circleRect.right = (centerX + diameter / 2f) - textSize;
        circleRect.bottom = centerY + diameter / 2f - offsetToUp;
    }

    float mainTextSize = DensityUtil.sp2px(50f);
    float unitTextSize = DensityUtil.sp2px(14f);

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (model == null) return;

        // 绘制外围实线环
        canvas.drawArc(circleRect, 180, 180, false, outerCirclePaint);

        // 绘制进度条（与实线圆弧重合，根据count与maxValue的百分比显示）
        float countProgress = (float) model.count / model.maxValue;
        float sweepAngle = 180 * countProgress * progress;
        canvas.drawArc(circleRect, 180, sweepAngle, false, progressPaint);

        // 绘制内侧的虚线刻度环 (稍微比外圆环小一点)
        RectF scaleRect = new RectF(circleRect);
        float inset = circleStrokeWidth + DensityUtil.dp2px(8); // 向内偏移
        scaleRect.inset(inset, inset);
        canvas.drawArc(scaleRect, 180, 180, false, scalePaint);

        // 绘制底部左右的数字 (0 和 107)
        textPaint.setTextSize(DensityUtil.dp2px(16));
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setAlpha((int) (255 * progress));

        // 计算圆环半径
        float radius = (circleRect.right - circleRect.left) / 2f;
        // 计算数字X轴位置：与圆环左右端点对齐
        float leftNumberX = circleRect.left;
        float rightNumberX = circleRect.right;

        // 底部左侧 - 显示minValue
        String leftText = String.valueOf(model.minValue);
        // 位置：与圆环底部Y轴对齐（圆环底部是centerY - offsetToUp）
        canvas.drawText(leftText, leftNumberX - DensityUtil.dp2px(20), centerY - offsetToUp + DensityUtil.dp2px(20), textPaint);

        // 底部右侧 - 显示maxValue
        String rightText = String.valueOf(model.maxValue);
        canvas.drawText(rightText, rightNumberX + DensityUtil.dp2px(10), centerY - offsetToUp + DensityUtil.dp2px(20), textPaint);

        // 绘制中心的主要文字内容
        // 计算动画过程中的数值
        int currentCount = (int) (model.count * progress);
        String mainNumber = String.valueOf(currentCount);
        String suffix = "所";
        String subText = EmptyUtils.isEmpty(model.descText) ? "适合你的大学" : model.descText;

        // 绘制下方描述适合你的大学
        textPaint.setTextSize(DensityUtil.dp2px(18));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAlpha((int) (255 * progress));
        // 先确定"适合你的大学"的位置，跟随offsetToUp偏移
        float subTextBaseline = centerY + DensityUtil.dp2px(15) - offsetToUp;
        canvas.drawText(subText, centerX, subTextBaseline, textPaint);

        // 绘制主数字
        textPaint.setTextSize(mainTextSize); // 大号字体
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setAlpha((int) (255 * progress));

        // 测量文字高度
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        // 计算适合你的大学的Top位置
        Paint.FontMetrics subTextFm = new Paint.FontMetrics();
        textPaint.setTextSize(DensityUtil.dp2px(18));
        textPaint.getFontMetrics(subTextFm);
        float subTextTop = subTextBaseline + subTextFm.top;
        textPaint.setTextSize(mainTextSize);

        // 计算100所的Bottom位置，与适合你的大学Top位置相差10dp
        float mainTextBottom = subTextTop - DensityUtil.dp2px(10);
        // 计算100的baseline
        float mainTextBaseline = mainTextBottom - fm.bottom;

        // 计算总宽度（数字 + 后缀）
        textPaint.setTextAlign(Paint.Align.LEFT);
        float numberWidth = textPaint.measureText(mainNumber);

        // 临时设置后缀字体大小测量宽度
        float originalTextSize = textPaint.getTextSize();
        textPaint.setTextSize(unitTextSize);
        float suffixWidth = textPaint.measureText(suffix);
        textPaint.setTextSize(originalTextSize);

        // 计算起始X坐标，确保整体居中
        textPaint.setTextSize(mainTextSize);
        float totalWidth = numberWidth + suffixWidth + DensityUtil.dp2px(5);
        float startX = centerX - totalWidth / 2;

        canvas.drawText(mainNumber, startX, mainTextBaseline, textPaint);

        // 绘制小后缀 "所
        textPaint.setTextSize(unitTextSize);
        textPaint.setTypeface(Typeface.DEFAULT);
        // 位置：紧跟在数字后面，且靠上一点
        canvas.drawText(suffix, startX + numberWidth + DensityUtil.dp2px(5), mainTextBaseline - DensityUtil.dp2px(5), textPaint);
    }

    public static class SimpleDashboardBean {
        int count;
        int minValue;
        int maxValue;
        String descText;

        public SimpleDashboardBean(int count, int minValue, int maxValue, String descText) {
            this.count = count;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.descText = descText;
        }
    }
}