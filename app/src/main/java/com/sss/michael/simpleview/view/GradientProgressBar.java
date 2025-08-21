package com.sss.michael.simpleview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.sss.michael.simpleview.utils.DensityUtil;

public class GradientProgressBar extends View {
    // 进度条的最大值
    private static final int MAX_PROGRESS = 100;
    // 画笔
    private Paint progressPaint;
    private Paint backgroundPaint;
    private Paint textPaint; // 用于正常文本(等级)
    private Paint boldTextPaint; // 用于加粗文本(进度)
    private Paint thumbPaint;
    private Paint textBackgroundPaint;

    // 进度条相关坐标参数
    private int barStartX;
    private int barEndX;
    private int barLength;
    private int barY;
    private int barHeight;
    private int _thumbRadius = DensityUtil.dp2px(5);

    // 拖动相关变量
    private boolean isDragging = false;


    // 右侧文本宽度，如果为空，将自动计算，否则，以本字段为准
    public int rightTextWidth = DensityUtil.dp2px(80);
    // 标题文本
    public String title = "知识认知";
    // 等级文本
    public String levelText = "(优秀)";
    // 平行四边形倾斜程度
    public int skewDegree = DensityUtil.dp2px(1);
    // 文本背景padding
    public int textBgPadding = DensityUtil.dp2px(3);
    // 平行四边形圆角
    public int textBgCornerRadius = DensityUtil.dp2px(5);
    // 当前进度
    private int progress = 0;
    // 边距参数
    public int leftMargin = DensityUtil.dp2px(16); // 默认左边距16dp
    public int rightMargin = DensityUtil.dp2px(16); // 默认右边距16dp
    // 标题和右侧文本之间的间距
    public int titleToBarSpacing = DensityUtil.dp2px(16);
    public int barToRightTextSpacing = DensityUtil.dp2px(16);
    // 两个文本之间的间距
    public int textSpacing = DensityUtil.dp2px(4);
    public OnProgressChangeListener progressChangeListener;

    public GradientProgressBar(Context context) {
        super(context);
        init();
    }

    public GradientProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GradientProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化进度条画笔
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.FILL);

        // 初始化背景画笔
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(0xFFE0E0E0); // 浅灰色背景

        // 初始化普通文本画笔(用于等级文本)
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(DensityUtil.dp2px(12)); // 使用dp转换
        textPaint.setColor(0xFF000000);

        // 初始化加粗文本画笔(用于进度文本)
        boldTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boldTextPaint.setTextSize(DensityUtil.dp2px(16)); // 与普通文本保持相同大小
        boldTextPaint.setColor(0xFF000000);
        boldTextPaint.setFakeBoldText(true); // 设置为加粗

        // 初始化滑块画笔 - 白色带更柔和的阴影
        thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setColor(0xFFFFFFFF); // 白色滑块
        // 修改阴影参数使其更柔和：增大模糊半径，降低透明度
        thumbPaint.setShadowLayer(DensityUtil.dp2px(5), 0, DensityUtil.dp2px(1), 0x40000000); // 黑色阴影，透明度25%
        setLayerType(LAYER_TYPE_SOFTWARE, thumbPaint); // 启用软件渲染以显示阴影

        // 初始化文本背景画笔
        textBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textBackgroundPaint.setStyle(Paint.Style.FILL);
    }

    Path textBgPath = new Path();
    RectF backgroundRect = new RectF();
    RectF progressRect = new RectF();
    Rect progressTextBounds = new Rect();
    Rect levelTextBounds = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // 计算标题文本宽度
        float titleWidth = textPaint.measureText(title);

        // 准备显示的两个文本内容
        String progressText = String.valueOf(progress);

        // 计算两个文本的尺寸
        boldTextPaint.getTextBounds(progressText, 0, progressText.length(), progressTextBounds);
        textPaint.getTextBounds(levelText, 0, levelText.length(), levelTextBounds);

        // 计算两个文本的总宽度
        int totalTextWidth = progressTextBounds.width() + (levelText.isEmpty() ? 0 : (textSpacing + levelTextBounds.width()));

        // 确定右侧文本宽度：如果rightTextWidth不为0则使用该值，否则自动计算
        int actualRightTextWidth = (rightTextWidth > 0) ? rightTextWidth
                : (totalTextWidth + 2 * textBgPadding + skewDegree);

        // 计算进度条区域
        barHeight = DensityUtil.dp2px(10);
        barY = height / 2 - barHeight / 2;

        // 进度条起始位置：左边距 + 标题宽度 + 标题到进度条的间距
        barStartX = leftMargin + (int) titleWidth + titleToBarSpacing;
        // 进度条结束位置：总宽度 - 右边距 - 右侧文本宽度 - 进度条到右侧文本的间距
        barEndX = width - rightMargin - actualRightTextWidth - barToRightTextSpacing;
        barLength = barEndX - barStartX;

        // 确保进度条长度为正数
        if (barLength < 0) {
            barLength = 0;
            barStartX = barEndX;
        }

        // 绘制标题
        canvas.drawText(title, leftMargin, height / 2 + (textPaint.getTextSize() / 4), textPaint);

        // 绘制背景
        backgroundRect.set(barStartX, barY, barEndX, barY + barHeight);
        canvas.drawRoundRect(backgroundRect, barHeight / 2, barHeight / 2, backgroundPaint);

        // 根据进度计算渐变色
        float progressRatio = (float) progress / MAX_PROGRESS;
        int color;

        if (progressRatio < 0.33f) {
            // 黄色到浅橙色
            color = blendColors(0xFFFFFF00, 0xFFFFA500, progressRatio / 0.33f);
        } else if (progressRatio < 0.66f) {
            // 浅橙色到深橙色
            color = blendColors(0xFFFFA500, 0xFFFF6600, (progressRatio - 0.33f) / 0.33f);
        } else {
            // 深橙色到红色
            color = blendColors(0xFFFF6600, 0xFFFF0000, (progressRatio - 0.66f) / 0.34f);
        }

        // 设置进度条颜色
        progressPaint.setColor(color);

        // 绘制进度
        int progressWidth = (int) (barLength * progressRatio);
        progressRect.set(barStartX, barY, barStartX + progressWidth, barY + barHeight);
        canvas.drawRoundRect(progressRect, barHeight >> 1, barHeight >> 1, progressPaint);

        // 绘制滑块 - 白色带柔和阴影
        float thumbX = barStartX + progressWidth;
        canvas.drawCircle(thumbX, barY + (barHeight >> 1), (barHeight >> 1) + _thumbRadius, thumbPaint);

        // 设置文本背景颜色与进度条一致
        textBackgroundPaint.setColor(color);

        // 计算文本背景位置 - 固定在进度条右侧
        int textBgStartX = barEndX + barToRightTextSpacing;
        int textBgStartY = height / 2 - Math.max(progressTextBounds.height(), levelTextBounds.height()) / 2 - textBgPadding;
        // 基于actualRightTextWidth计算背景宽度（减去倾斜度，因为倾斜度是额外的水平偏移）
        int textBgWidth = actualRightTextWidth - skewDegree;
        int textBgHeight = Math.max(progressTextBounds.height(), levelTextBounds.height()) + 2 * textBgPadding;

        // 绘制文本背景（带圆角的平行四边形）
        textBgPath.reset();
        // 左上角（带圆角）
        textBgPath.moveTo(textBgStartX + textBgCornerRadius, textBgStartY);
        // 上边缘到右上角（添加圆角）
        textBgPath.lineTo(textBgStartX + textBgWidth + skewDegree - textBgCornerRadius, textBgStartY);
        textBgPath.quadTo(textBgStartX + textBgWidth + skewDegree, textBgStartY,
                textBgStartX + textBgWidth + skewDegree, textBgStartY + textBgCornerRadius);

        // 右边缘到右下角（添加圆角）
        textBgPath.lineTo(textBgStartX + textBgWidth, textBgStartY + textBgHeight - textBgCornerRadius);
        textBgPath.quadTo(textBgStartX + textBgWidth, textBgStartY + textBgHeight,
                textBgStartX + textBgWidth - textBgCornerRadius, textBgStartY + textBgHeight);

        // 下边缘到左下角（添加圆角）
        textBgPath.lineTo(textBgStartX - skewDegree + textBgCornerRadius, textBgStartY + textBgHeight);
        textBgPath.quadTo(textBgStartX - skewDegree, textBgStartY + textBgHeight,
                textBgStartX - skewDegree, textBgStartY + textBgHeight - textBgCornerRadius);

        // 左边缘回到左上角（添加圆角）
        textBgPath.lineTo(textBgStartX, textBgStartY + textBgCornerRadius);
        textBgPath.quadTo(textBgStartX, textBgStartY,
                textBgStartX + textBgCornerRadius, textBgStartY);

        textBgPath.close();
        canvas.drawPath(textBgPath, textBackgroundPaint);

        // 计算文本绘制位置
        int textBaselineY = height / 2 + (Math.max(progressTextBounds.height(), levelTextBounds.height()) >> 1);
        // 文本区域的右边界（考虑padding）
        int textAreaRight = textBgStartX + textBgWidth - textBgPadding;

        // 先绘制等级文本(居右)
        int levelTextStartX = textAreaRight - levelTextBounds.width();
        canvas.drawText(levelText, levelTextStartX, textBaselineY, textPaint);

        // 再绘制进度文本(在等级文本左侧)
        int progressTextStartX = !levelText.isEmpty() ?
                levelTextStartX - progressTextBounds.width() - textSpacing :
                textAreaRight - progressTextBounds.width();
        canvas.drawText(progressText, progressTextStartX, textBaselineY, boldTextPaint);
    }


    // 颜色混合计算
    private int blendColors(int color1, int color2, float ratio) {
        final float inverseRatio = 1f - ratio;

        final float a = (color1 >>> 24 & 0xff) * inverseRatio + (color2 >>> 24 & 0xff) * ratio;
        final float r = (color1 >>> 16 & 0xff) * inverseRatio + (color2 >>> 16 & 0xff) * ratio;
        final float g = (color1 >>> 8 & 0xff) * inverseRatio + (color2 >>> 8 & 0xff) * ratio;
        final float b = (color1 & 0xff) * inverseRatio + (color2 & 0xff) * ratio;

        return (int) a << 24 | (int) r << 16 | (int) g << 8 | (int) b;
    }

    // 设置进度
    public void setProgress(int progress) {
        if (progress < 0) progress = 0;
        if (progress > MAX_PROGRESS) progress = MAX_PROGRESS;
        this.progress = progress;
        invalidate();

        // 通知进度变化
        if (progressChangeListener != null) {
            progressChangeListener.onProgressChanged(progress);
        }
    }

    // 触摸事件处理，实现拖动功能
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 只有在进度条区域才响应触摸事件
        if (barLength <= 0) {
            return super.onTouchEvent(event);
        }

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 检查是否点击了滑块
                float thumbX = barStartX + (barLength * progress / (float) MAX_PROGRESS);
                float thumbY = barY + (barHeight >> 1);
                float thumbRadius = (barHeight >> 1) + _thumbRadius;

                // 计算点击位置与滑块的距离
                float distance = (float) Math.sqrt(Math.pow(x - thumbX, 2) + Math.pow(y - thumbY, 2));

                // 如果点击了滑块或者进度条区域，开始拖动
                if (distance <= thumbRadius || (y >= barY && y <= barY + barHeight && x >= barStartX && x <= barEndX)) {
                    isDragging = true;
                    updateProgressByX(x);
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    updateProgressByX(x);
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                break;
        }

        return super.onTouchEvent(event);
    }

    // 根据X坐标更新进度
    private void updateProgressByX(float x) {
        // 限制X坐标在进度条范围内
        float clampedX = Math.max(barStartX, Math.min(x, barEndX));

        // 计算对应的进度值
        float ratio = (clampedX - barStartX) / barLength;
        int newProgress = (int) (ratio * MAX_PROGRESS);

        // 设置新进度
        setProgress(newProgress);
    }


    // 获取最大进度
    public int getMaxProgress() {
        return MAX_PROGRESS;
    }

    // 进度变化监听器
    public interface OnProgressChangeListener {
        void onProgressChanged(int progress);
    }
}
