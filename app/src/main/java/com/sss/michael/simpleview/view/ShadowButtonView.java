package com.sss.michael.simpleview.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.sss.michael.simpleview.R;

public class ShadowButtonView extends View {

    // Text
    private CharSequence text = "";
    private TextPaint textPaint;
    private float textSizeSp = 16f;
    private int textColor = 0xFF222222;
    private int maxLines = Integer.MAX_VALUE;
    private float lineSpacingExtraPx = 0f;

    // Shape
    private float cornerRadiusPx = dp(12);
    private float strokeWidthPx = dp(1);
    private int fillColor = Color.WHITE;
    private int strokeColor = 0xFFDD2C2C; // 按下时的红色描边（可改）
    private int normalStrokeColor = 0x11000000; // 未按下时的轻描边（与阴影区分）

    // Shadow (未按下)
    private float shadowRadiusPx = dp(8);
    private float shadowDxPx = 0f;
    private float shadowDyPx = dp(2);
    private int shadowColor = 0x33000000;

    // Padding（自动基于圆角，亦可手动 setPadding 覆盖）
    private boolean autoPadding = true;
    private int autoHPadding; // 根据圆角计算
    private int autoVPadding;

    // State
    private boolean isPressedState = false;

    // Text layout cache
    private StaticLayout textLayout;
    private CharSequence layoutText; // 可能被“省略”处理后的文本

    private final RectF rect = new RectF();

    public ShadowButtonView(Context c) { this(c, null); }
    public ShadowButtonView(Context c, AttributeSet a) { this(c, a, 0); }
    public ShadowButtonView(Context c, AttributeSet a, int defStyle) {
        super(c, a, defStyle);
        initAttrs(c, a);
        initPaints();
        // 阴影需要软件层（否则 setShadowLayer 不生效）
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private void initAttrs(Context c, AttributeSet attrs) {
        if (attrs != null) {
             TypedArray ta = c.obtainStyledAttributes(attrs, R.styleable.ShadowButtonView);
             text = ta.getText(R.styleable.ShadowButtonView_sbv_text);
             textSizeSp = ta.getDimension(R.styleable.ShadowButtonView_sbv_textSize, sp(16)) / getResources().getDisplayMetrics().scaledDensity;
             textColor = ta.getColor(R.styleable.ShadowButtonView_sbv_textColor, textColor);
             maxLines = ta.getInt(R.styleable.ShadowButtonView_sbv_maxLines, maxLines);
             cornerRadiusPx = ta.getDimension(R.styleable.ShadowButtonView_sbv_cornerRadius, cornerRadiusPx);
             strokeWidthPx = ta.getDimension(R.styleable.ShadowButtonView_sbv_strokeWidth, strokeWidthPx);
             strokeColor = ta.getColor(R.styleable.ShadowButtonView_sbv_strokeColor, strokeColor);
             fillColor = ta.getColor(R.styleable.ShadowButtonView_sbv_fillColor, fillColor);
             shadowRadiusPx = ta.getDimension(R.styleable.ShadowButtonView_sbv_shadowRadius, shadowRadiusPx);
             shadowDxPx = ta.getDimension(R.styleable.ShadowButtonView_sbv_shadowDx, shadowDxPx);
             shadowDyPx = ta.getDimension(R.styleable.ShadowButtonView_sbv_shadowDy, shadowDyPx);
             shadowColor = ta.getColor(R.styleable.ShadowButtonView_sbv_shadowColor, shadowColor);
             autoPadding = ta.getBoolean(R.styleable.ShadowButtonView_sbv_autoPadding, true);
             ta.recycle();
        }
        // 计算默认自动 padding（随圆角变化）
        computeAutoPadding();
    }

    private void initPaints() {
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(sp(textSizeSp));
    }

    private void computeAutoPadding() {
        // 经验系数：横向 ~ 0.8R + 10dp，纵向 ~ 0.5R + 8dp，避免文字贴边
        autoHPadding = (int) Math.ceil(0.8f * cornerRadiusPx + dp(10));
        autoVPadding = (int) Math.ceil(0.5f * cornerRadiusPx + dp(8));
        if (!autoPadding) return; // 用户手动设置 padding 时不覆盖
        super.setPadding(autoHPadding, autoVPadding, autoHPadding, autoVPadding);
    }

    // --- Public APIs ---

    public void setText(CharSequence t) {
        if (t == null) t = "";
        if (!TextUtils.equals(t, this.text)) {
            this.text = t;
            requestLayout();
            invalidate();
        }
    }

    public CharSequence getText() { return text; }

    public void setMaxLines(int lines) {
        if (lines <= 0) lines = 1;
        if (this.maxLines != lines) {
            this.maxLines = lines;
            requestLayout();
            invalidate();
        }
    }

    public void setCornerRadiusDp(float dpVal) {
        float px = dp(dpVal);
        if (px != cornerRadiusPx) {
            cornerRadiusPx = px;
            computeAutoPadding();
            invalidate();
        }
    }

    public void setStrokeColor(int color) { this.strokeColor = color; invalidate(); }
    public void setNormalStrokeColor(int color) { this.normalStrokeColor = color; invalidate(); }
    public void setFillColor(int color) { this.fillColor = color; invalidate(); }
    public void setTextColor(int color) { this.textColor = color; textPaint.setColor(color); invalidate(); }

    public void setTextSizeSp(float spVal) {
        this.textSizeSp = spVal;
        textPaint.setTextSize(sp(spVal));
        requestLayout();
        invalidate();
    }

    public void setLineSpacingExtraPx(float extraPx) {
        this.lineSpacingExtraPx = extraPx;
        requestLayout();
        invalidate();
    }

    // 如果你想手动控制 padding，先调用该方法关闭自动 padding，再 setPadding
    public void disableAutoPadding() { this.autoPadding = false; }

    // --- Measure & Layout ---

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);

        // 可用文本宽度（减去左右内边距）
        int availTextWidth = Math.max(0, (wMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : wSize) - getPaddingLeft() - getPaddingRight());

        buildTextLayout(availTextWidth);

        int desiredW;
        if (wMode == MeasureSpec.UNSPECIFIED) {
            // 自适应宽度：文本最长行 + padding，再至少不小于 40dp 的高度要求不影响宽度
            float maxLineWidth = (textLayout != null) ? getMaxLineWidth(textLayout) : 0f;
            desiredW = (int) Math.ceil(maxLineWidth) + getPaddingLeft() + getPaddingRight();
        } else {
            desiredW = wSize;
        }

        int textHeight = (textLayout != null) ? textLayout.getHeight() : 0;
        int desiredH = textHeight + getPaddingTop() + getPaddingBottom();
        desiredH = Math.max(desiredH, dp(40)); // 最小高度 40dp

        int measuredW = resolveSize(desiredW, widthMeasureSpec);
        int measuredH = resolveSize(desiredH, heightMeasureSpec);

        setMeasuredDimension(measuredW, measuredH);
    }

    private void buildTextLayout(int availTextWidth) {
        if (availTextWidth <= 0) {
            layoutText = "";
            textLayout = null;
            return;
        }

        // 构建初始布局
        StaticLayout layout = makeStaticLayout(text, availTextWidth, maxLines, lineSpacingExtraPx);

        // 处理最大行数：若超行数，截断并加省略号，再重建 layout
        if (layout.getLineCount() > maxLines) {
            int cutEnd = layout.getLineEnd(maxLines - 1);
            // 尽量不截断半个字，并补省略
            CharSequence sub = text.subSequence(0, Math.max(0, cutEnd));
            CharSequence ellipsized = TextUtils.ellipsize(sub, textPaint, availTextWidth, TextUtils.TruncateAt.END);
            layoutText = ellipsized;
            textLayout = makeStaticLayout(layoutText, availTextWidth, maxLines, lineSpacingExtraPx);
        } else {
            layoutText = text;
            textLayout = layout;
        }
    }

    private StaticLayout makeStaticLayout(CharSequence t, int width, int maxLines, float addLineSpacePx) {
        // 兼容 API：使用已废弃构造器在较低版本仍可用
        StaticLayout sl = new StaticLayout(
                t, textPaint, Math.max(0, width),
                Layout.Alignment.ALIGN_NORMAL,
                1.0f, addLineSpacePx, false
        );
        // 注意：此处不依赖 Builder 的 setMaxLines，使用上层手动截断行数
        return sl;
    }

    private float getMaxLineWidth(StaticLayout sl) {
        float w = 0f;
        for (int i = 0; i < sl.getLineCount(); i++) {
            w = Math.max(w, sl.getLineWidth(i));
        }
        return w;
    }

    // --- Draw ---

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        rect.set(getPaddingLeft(), getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom());

        // 背景：未按下 -> 白底 + 阴影 + 轻描边；按下 -> 仅描边
        if (!isPressedState) {
            // 填充 + 阴影
            Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
            fill.setStyle(Paint.Style.FILL);
            fill.setColor(fillColor);
            fill.setShadowLayer(shadowRadiusPx, shadowDxPx, shadowDyPx, shadowColor);
            canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, fill);

            // 轻描边（避免白底在浅色背景上“漂”）
            Paint edge = new Paint(Paint.ANTI_ALIAS_FLAG);
            edge.setStyle(Paint.Style.STROKE);
            edge.setStrokeWidth(strokeWidthPx);
            edge.setColor(normalStrokeColor);
            canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, edge);
        } else {
            // 仅描边，纯色，无渐变
            Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(strokeWidthPx);
            stroke.setColor(strokeColor);
            // 不绘制填充，达到“仅边框描边”的效果
            canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, stroke);
        }

        // 文字（左对齐）
        if (textLayout != null && layoutText != null && layoutText.length() > 0) {
            canvas.save();
            // 将 (0,0) 平移到文本绘制起点（靠左上）
            canvas.translate(getPaddingLeft(), getPaddingTop());
            textLayout.draw(canvas);
            canvas.restore();
        }
    }

    // --- Touch ---

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isPressedState = true;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                isPressedState = false;
                invalidate();
                // 命中判定后触发点击
                if (isPointInside(event.getX(), event.getY())) {
                    performClick();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                isPressedState = false;
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private boolean isPointInside(float x, float y) {
        return x >= 0 && x <= getWidth() && y >= 0 && y <= getHeight();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    // --- Utils ---

    private static int dp(float dp) {
        return (int) Math.ceil(dp * ResourcesHolder.density());
    }
    private static float sp(float sp) {
        return sp * ResourcesHolder.scaledDensity();
    }

    /** 避免频繁 Context.getResources() 调用的小工具 */
    private static class ResourcesHolder {
        private static float density = -1f;
        private static float scaledDensity = -1f;

        static float density() {
            if (density < 0f) {
                density = Resources.getSystem().getDisplayMetrics().density;
            }
            return density;
        }
        static float scaledDensity() {
            if (scaledDensity < 0f) {
                scaledDensity = Resources.getSystem().getDisplayMetrics().scaledDensity;
            }
            return scaledDensity;
        }
    }
}

