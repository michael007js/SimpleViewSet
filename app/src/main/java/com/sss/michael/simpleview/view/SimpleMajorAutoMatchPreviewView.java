package com.sss.michael.simpleview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.sss.michael.simpleview.utils.DensityUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michael by 61642
 * @date 2025/5/27 11:28
 * @Description 一个简单的专业等级自动匹配渲染视图
 */
public class SimpleMajorAutoMatchPreviewView extends AppCompatTextView {

    public SimpleMajorAutoMatchPreviewView(@NonNull Context context) {
        this(context, null);
    }

    public SimpleMajorAutoMatchPreviewView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleMajorAutoMatchPreviewView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTextSize(16f);
        setText("我是一个自动匹配文案，内容是不招色盲色弱，含服装与服饰设计A+、工艺美术A+、艺术与科技A-等5个专业");
    }

    private final float cornerRadius = DensityUtil.dp2px(3);
    private final int padding = DensityUtil.dp2px(3);
    private final RectF rect = new RectF();
    private final float levelSize = 12f;
    int verticalOffset = DensityUtil.dp2px(1.5f);

    @Override
    public void setText(CharSequence text, BufferType type) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        Matcher matcher = Pattern.compile("[A-C][+-]?").matcher(text);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            spannable.setSpan(
                    new ReplacementSpan() {
                        @Override
                        public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
                            return (int) (paint.measureText(text, start, end) + padding * 2);
                        }

                        @Override
                        public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end,
                                         float x, int top, int y, int bottom, @NonNull Paint paint) {
                            float textSizePx = DensityUtil.dp2px(levelSize);
                            Paint spanPaint = new Paint(paint);
                            spanPaint.setTextSize(textSizePx);
                            spanPaint.setAntiAlias(true);

                            float textWidth = spanPaint.measureText(text, start, end);
                            float textHeight = spanPaint.getFontMetrics().bottom - spanPaint.getFontMetrics().top;

                            rect.left = x;
                            rect.top = y + spanPaint.ascent() - verticalOffset;
                            rect.right = x + textWidth + padding * 2;
                            rect.bottom = rect.top + textHeight;

                            // 背景色
                            Paint bgPaint = new Paint(spanPaint);
                            bgPaint.setColor(Color.parseColor("#2678e3"));
                            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint);

                            // 字体颜色
                            spanPaint.setColor(Color.parseColor("#E9302D"));
                            canvas.drawText(text, start, end, x + padding, y - (float) verticalOffset, spanPaint);
                        }
                    },
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        super.setText(spannable, type);
    }
}
