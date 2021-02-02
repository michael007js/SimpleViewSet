package com.sss.michael.simpleview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.sss.michael.simpleview.R;
import com.sss.michael.simpleview.utils.DensityUtil;


/**
 * @author Michael by Administrator
 * @date 2021/1/7 18:05
 * @Description 一个简单的带注释的渐变进度条
 */
public class SimpleProgressBar extends View {

    /**
     * 进度条绘制区域高度
     */
    private int progressHeight = DensityUtil.dp2px(8);

    /**
     * 进度条绘制区域高度
     */
    private float progressWidth;
    /**
     * 文字大小
     */
    private float textSize;
    /**
     * 文字
     */
    private String text = "100%";
    /**
     * 依附于前景或背景绘制
     */
    private boolean attachToBackground = true;
    /**
     * 文字与进度条之间的距离
     */
    private int distance = DensityUtil.dp2px(5);
    /**
     * 按百分比绘制
     */
    private boolean isPercent = true;
    /**
     * 圆角
     */
    private int raduis = progressHeight / 2;
    /**
     * 总进度(百分比模式下无效，将启用View宽度做为总进度)
     */
    private int totalProgress = 100;
    /**
     * 实时进度
     */
    private int currentProgress = 50;
    /**
     * 文字色
     */
    private int textColor;
    /**
     * 前景色
     */
    private String[] foregroundColor = {"#FFBE9B", "#FF7573"};
    /**
     * 背景色
     */
    private int backgroundColor = Color.parseColor("#F56767");
    /**
     * 宽度
     */
    private int width = 0;
    /**
     * 高度
     */
    private int height = 0;
    /**
     * 文字宽度
     */
    private int textMaxWidth = 0;
    /**
     * 文字画笔
     */
    private Paint textPaint = new Paint();
    /**
     * 前景画笔
     */
    private Paint foregroundPaint = new Paint();
    /**
     * 背景画笔
     */
    private Paint backgroundPaint = new Paint();
    /**
     * 前景区域
     */
    private RectF foregroundArea = new RectF();
    /**
     * 背景区域
     */
    private RectF backgroundArea = new RectF();

    public SimpleProgressBar(Context context) {
        this(context, null);
    }

    public SimpleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SimpleProgressBar);
        textMaxWidth = ta.getDimensionPixelSize(R.styleable.SimpleProgressBar_simple_progressbar_textMaxWidth, textMaxWidth);
        progressHeight = ta.getDimensionPixelSize(R.styleable.SimpleProgressBar_simple_progressbar_progressHeight, progressHeight);
        textSize = ta.getDimensionPixelSize(R.styleable.SimpleProgressBar_simple_progressbar_textSize, 0);
        textSize = textSize == 0 ? DensityUtil.sp2px(12) : textSize;
        text = ta.getString(R.styleable.SimpleProgressBar_simple_progressbar_text);
        text = text == null ? "" : text;
        attachToBackground = ta.getBoolean(R.styleable.SimpleProgressBar_simple_progressbar_attachToBackground, attachToBackground);
        distance = ta.getDimensionPixelSize(R.styleable.SimpleProgressBar_simple_progressbar_distance, 0);
        isPercent = ta.getBoolean(R.styleable.SimpleProgressBar_simple_progressbar_isPercent, isPercent);
        raduis = ta.getDimensionPixelSize(R.styleable.SimpleProgressBar_simple_progressbar_raduis, raduis);
        raduis = raduis == 0 ? progressHeight / 2 : raduis;
        totalProgress = ta.getInteger(R.styleable.SimpleProgressBar_simple_progressbar_totalProgress, totalProgress);
        currentProgress = ta.getInteger(R.styleable.SimpleProgressBar_simple_progressbar_currentProgress, currentProgress);

        String color = ta.getString(R.styleable.SimpleProgressBar_simple_progressbar_foregroundColor);
        foregroundColor = color != null ? color.split(",") : foregroundColor;
        backgroundColor = ta.getColor(R.styleable.SimpleProgressBar_simple_progressbar_backgroundColor, backgroundColor);
        int tc = ta.getColor(R.styleable.SimpleProgressBar_simple_progressbar_textColor, textColor);
        textColor=tc == 0 ? textColor: tc;
        ta.recycle();
        initPaint();
        invalidate();
    }


    private void initPaint() {

        foregroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setAntiAlias(true);

        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.AT_MOST:
                width = DensityUtil.dp2px(100);
                break;
            case MeasureSpec.EXACTLY:
                width = widthSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                width = widthSize;
                break;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.AT_MOST:
                height = getTextSize(false, text, textPaint);
                break;
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                height = heightSize;
                break;
        }
        setRaduis(height / 2);
        progressWidth = width - getAllTextAreaSize();
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        backgroundArea.left = 0;
        backgroundArea.top = height / 2 - progressHeight / 2;
        backgroundArea.right = progressWidth;
        backgroundArea.bottom = height / 2 + progressHeight / 2;
        foregroundArea.left = (int) backgroundArea.left;
        foregroundArea.top = (int) backgroundArea.top;
        foregroundArea.bottom = (int) backgroundArea.bottom;
        foregroundArea.right = getForegroundProgress();
        if (!attachToBackground) {
            backgroundArea.right = width;
            foregroundArea.right = foregroundArea.right + getAllTextAreaSize() >= getWidth() ? (int) (backgroundArea.right - getAllTextAreaSize()) : foregroundArea.right;
        }


        int[] colors = new int[foregroundColor.length];
        for (int i = 0; i < foregroundColor.length; i++) {
            colors[i] = Color.parseColor(foregroundColor[i]);
        }
        LinearGradient linearGradient = new LinearGradient(
                foregroundArea.left,
                foregroundArea.top + foregroundArea.height() / 2,
                foregroundArea.left + foregroundArea.width(),
                top + foregroundArea.height() / 2, colors,
                null,
                Shader.TileMode.MIRROR
        );
        foregroundPaint.setShader(linearGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect(backgroundArea, progressHeight / 2, progressHeight / 2, backgroundPaint);
        canvas.drawRoundRect(foregroundArea, progressHeight / 2, progressHeight / 2, foregroundPaint);
        if (attachToBackground) {
            canvas.drawText(text, backgroundArea.right + distance, height / 2 + getTextSize(false, text, textPaint) / 2, textPaint);
        } else {
            canvas.drawText(text, foregroundArea.right + distance, height / 2 + getTextSize(false, text, textPaint) / 2, textPaint);
        }
    }


    public void setTextColor(int textColor) {
        this.textColor = textColor;
        initPaint();
        invalidate();
    }

    /**
     * 进度条绘制区域高度
     */
    public void setProgressHeight(int progressHeight) {
        this.progressHeight = progressHeight;
        invalidate();
    }

    /**
     * 设置文字大小
     */
    public void setTextSize(float textSize) {
        this.textSize = textSize;
        initPaint();
        invalidate();
    }

    /**
     * 设置文字
     */
    public void setText(String text) {
        this.text = text;
        invalidate();
    }

    /**
     * 设置文字与进度条之间的距离
     */
    public void setDistance(int distance) {
        this.distance = distance;
    }

    /**
     * 设置绘制方式
     */
    public void setPercent(boolean percent) {
        isPercent = percent;
        invalidate();
    }

    /**
     * 设置圆角
     */
    public void setRaduis(int raduis) {
        this.raduis = raduis;
        invalidate();
    }


    /**
     * 设置文字依附模式
     */
    public void setAttachToBackground(boolean attachToBackground) {
        this.attachToBackground = attachToBackground;
        invalidate();
    }


    /**
     * 设置前景色
     */
    public void setForegroundColor(String[] color) {
        foregroundColor = color != null || color.length == 0 ? foregroundColor : color;
        initPaint();
        invalidate();
    }

    /**
     * 设置背景色
     */
    public void setBackgroundColor_(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        initPaint();
        invalidate();
    }

    /**
     * 设置普通模式总进度
     */
    public void setTotalProgress(int totalProgress) {
        isPercent = false;
        this.totalProgress = totalProgress;
        invalidate();
    }

    /**
     * 设置实时进度
     */
    public void setCurrentProgress(int currentProgress) {
        isPercent = false;
        this.currentProgress = currentProgress;
        requestLayout();
    }

    public int getCurrentProgress() {
        return currentProgress;
    }


    /**
     * 获取前景进度
     */
    private int getForegroundProgress() {
        return (int) (isPercent ? backgroundArea.width() * currentProgress / 100.0 : backgroundArea.width() * 1.0 * currentProgress / totalProgress);
    }

    /**
     * 获取文字区域宽度
     */
    private int getAllTextAreaSize() {
        return distance + getTextSize(true, text, textPaint);
    }

    /**
     * 获取字符串宽高
     */

    private int getTextSize(boolean isWidth, String str, Paint p) {
        if (str == null || str.length() == 0) {
            return 0;
        }
        if (isWidth) {
            return textMaxWidth == 0 ? (int) p.measureText(str) : textMaxWidth;
        } else {
            return (int) (p.getFontMetrics().descent - p.getFontMetrics().ascent) / 2;
        }

    }
}
