package com.sss.michael.simpleview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author Michael by 61642
 * @date 2023/11/14 11:16
 * @Description 一个简单的纵向区间多点图
 */
public class SimpleVerticalRangMultiplePointView extends View {
    private boolean debug = true;
    /**
     * 调试色值
     */
    private int debugColor = 0xff000000;
    /**
     * 背景线色值
     */
    private int backgroundLineColor = 0xffd8d8d8;
    /**
     * 背景文字色值
     */
    private int backgroundTextColor = 0xff999999;
    /**
     * 无效垂直内边距，这个数值范围内的将不算作有效高度
     */
    private int disableVerticalPaddingPercent = DensityUtil.dp2px(3);
    /**
     * 区间画笔
     */
    private Paint rangPaint = new Paint();
    /**
     * 背景画笔
     */
    private Paint bgPaint = new Paint();
    /**
     * 背景画笔
     */
    private Paint fgPaint = new Paint();
    /**
     * 文字画笔
     */
    private Paint textPaint = new Paint();
    /**
     * 背景数据
     */
    private List<BackgroundBean> backgroundData = new ArrayList<>();
    /**
     * 文字与背景横线之间的距离
     */
    private float distance = DensityUtil.dp2px(3);
    /**
     * 柱状宽度
     */
    private int columnWidth = DensityUtil.dp2px(12);
    /**
     * 圆点宽度
     */
    private int pointWidth = DensityUtil.dp2px(4);
    /**
     * 前景数据
     */
    private List<Bean> foregroundData = new ArrayList<>();

    {
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(DensityUtil.dp2px(10));
        rangPaint.setAntiAlias(true);
        bgPaint.setAntiAlias(true);
        bgPaint.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));
        fgPaint.setAntiAlias(true);
    }


    public SimpleVerticalRangMultiplePointView(Context context) {
        this(context, null);
    }

    public SimpleVerticalRangMultiplePointView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleVerticalRangMultiplePointView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (debug) {
            List<Integer> bg = new ArrayList<>();
            bg.add(666);
            bg.add(999);
            bg.add(9999);
            bg.add(111);

            List<Bean> fg = new ArrayList<>();
            fg.add(new Bean(3296));
            fg.add(new Bean(3100));
            setBgData(true, bg, fg);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height;
        if (MeasureSpec.EXACTLY == MeasureSpec.getMode(widthMeasureSpec)) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            height = DensityUtil.dp2px(100);
        }
        rect.left = width / 2 - columnWidth / 2;
        rect.right = width / 2 + columnWidth / 2;
        for (Bean bean : foregroundData) {
            bean.rect.left = rect.left + rect.width() / 2 - pointWidth / 2;
            bean.rect.right = bean.rect.left + pointWidth;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (debug) {
            setBackgroundColor(0xffeeeeee);
        }
        float minHeight = 0;
        float maxHeight = 0;

        float eachDataRangHeight = 0;
        float effectiveHeight = getHeight() - disableVerticalPaddingPercent * 2.0f;
        if (backgroundData.size() > 0) {
            int max = 0;
            int min = 0;


            for (int i = 0; i < backgroundData.size(); i++) {
                if (i == 0) {
                    max = backgroundData.get(i).value;
                    min = backgroundData.get(i).value;
                } else {
                    max = Math.max(max, backgroundData.get(i).value);
                    min = Math.min(min, backgroundData.get(i).value);
                }
            }

            if (max == 0) {
                return;
            }
            //数据区间
            float dataRange = (max - min) * 1.0f / (backgroundData.size() - 1);
            //每一个数据所占据的视图高度
            eachDataRangHeight = effectiveHeight / (max - min);

            for (int i = 0; i < backgroundData.size(); i++) {
                if (i != 0 && i != backgroundData.size() - 1) {
                    backgroundData.get(i).previewText = String.valueOf(((int) (dataRange * i)));
                } else {
                    backgroundData.get(i).previewText = String.valueOf(backgroundData.get(i).value);
                }
            }

            float x;
            if (drawText) {
                x = maxTextWidth == 0 ? 0 : maxTextWidth + distance;
            } else {
                x = 0;
            }
            for (int i = 0; i < backgroundData.size(); i++) {
                //真实数据Y轴
                float reallyY = effectiveHeight - backgroundData.get(i).value * eachDataRangHeight;
                //等分数据Y轴
                float y = i * dataRange * eachDataRangHeight;
                float textHeight = DrawViewUtils.getTextWH(textPaint, backgroundData.get(i).previewText)[1];
                float yWithOffset;
                if (i == 0) {
                    if (drawText) {
                        textPaint.setColor(backgroundTextColor);
                        canvas.drawText(backgroundData.get(i).previewText, 0, y + textHeight + disableVerticalPaddingPercent, textPaint);
                        yWithOffset = textHeight / 2 + disableVerticalPaddingPercent;
                        bgPaint.setColor(backgroundLineColor);
                        canvas.drawLine(x, y + yWithOffset, getWidth(), y + yWithOffset, bgPaint);
                    } else {
                        bgPaint.setColor(backgroundLineColor);
                        yWithOffset = (keepYAxisForBackgroundLineSameWithTextYAxisWhileTextDisable ? textHeight / 2 : 0) + disableVerticalPaddingPercent;
                        canvas.drawLine(x, y + yWithOffset, getWidth(), y + yWithOffset, bgPaint);
                    }
                    minHeight = y + yWithOffset;
                    maxHeight = y + yWithOffset;
                    rect.top = (int) yWithOffset;
                    if (debug) {
                        //绘制真实背景数据线
                        bgPaint.setColor(debugColor);
                        canvas.drawLine(x, reallyY + yWithOffset, getWidth(), reallyY + yWithOffset, bgPaint);
                    }
                } else if (i == backgroundData.size() - 1) {
                    textPaint.setColor(backgroundTextColor);
                    if (drawText) {
                        canvas.drawText(backgroundData.get(i).previewText, 0, y, textPaint);
                        yWithOffset = textHeight / 2;
                        bgPaint.setColor(backgroundLineColor);
                        canvas.drawLine(x, y - yWithOffset, getWidth(), y - yWithOffset, bgPaint);
                    } else {
                        yWithOffset = (keepYAxisForBackgroundLineSameWithTextYAxisWhileTextDisable ? textHeight / 2 : 0);
                        canvas.drawLine(x, y - yWithOffset, getWidth(), y - yWithOffset, bgPaint);
                    }
                    rect.bottom = (int) (y - yWithOffset);
                    if (debug) {
                        //绘制真实背景数据线
                        bgPaint.setColor(debugColor);
                        canvas.drawLine(x, reallyY - yWithOffset, getWidth(), reallyY - yWithOffset, bgPaint);
                    }
                } else {
                    if (drawText) {
                        textPaint.setColor(backgroundTextColor);
                        canvas.drawText(backgroundData.get(i).previewText, 0, y + textHeight - disableVerticalPaddingPercent, textPaint);
                        yWithOffset = textHeight / 2 - disableVerticalPaddingPercent;
                        bgPaint.setColor(backgroundLineColor);
                        canvas.drawLine(x, y + yWithOffset, getWidth(), y + yWithOffset, bgPaint);
                    } else {
                        bgPaint.setColor(backgroundLineColor);
                        yWithOffset = (keepYAxisForBackgroundLineSameWithTextYAxisWhileTextDisable ? textHeight / 2 : 0) - disableVerticalPaddingPercent;
                        canvas.drawLine(x, y + yWithOffset, getWidth(), y + yWithOffset, bgPaint);
                    }
                    minHeight = Math.min(minHeight, y + yWithOffset);
                    maxHeight = Math.max(maxHeight, y + yWithOffset);
                    if (debug) {
                        //绘制真实背景数据线
                        bgPaint.setColor(debugColor);
                        canvas.drawLine(x, reallyY, getWidth(), reallyY, bgPaint);
                    }
                }

            }

        }
        fgPaint.setColor(0x12ff7d00);
        canvas.drawRect(rect, fgPaint);
        fgPaint.setColor(0xffff7d00);
        for (Bean bean : foregroundData) {
            float y = effectiveHeight - bean.value * eachDataRangHeight;
            int yOffset = pointWidth / 2;
            bean.rect.top = (int) (y - pointWidth / 2) + yOffset;
            bean.rect.bottom = (int) (y + pointWidth / 2) + yOffset;
            canvas.save();
            canvas.rotate(45, bean.rect.left + (bean.rect.width() >> 1), bean.rect.top + (bean.rect.height() >> 1));
            canvas.drawRect(bean.rect, fgPaint);
            canvas.restore();
        }
    }

    Rect rect = new Rect();

    /**
     * 文字最大宽度
     */
    float maxTextWidth;
    /**
     * 是否绘制文字
     */
    boolean drawText;
    /**
     * 不绘制文本时背景线的Y轴保持与文字存在时相同的Y轴位置
     */
    boolean keepYAxisForBackgroundLineSameWithTextYAxisWhileTextDisable = true;

    public void setBgData(boolean drawText, List<Integer> backgroundData, List<Bean> foregroundData) {
        this.drawText = drawText;
        Collections.sort(backgroundData, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        this.backgroundData.clear();
        this.foregroundData.clear();
        this.foregroundData = foregroundData;
        for (Integer i : backgroundData) {
            maxTextWidth = Math.max(maxTextWidth, textPaint.measureText(String.valueOf(i)));
            this.backgroundData.add(new BackgroundBean(i));
        }
        invalidate();
    }

    static class BackgroundBean {
        private int value;
        private String previewText = "";

        BackgroundBean(int i) {
            this.value = i;

        }
    }

    public static class Bean {
        private Rect rect = new Rect();
        private int value;

        public Bean(int value) {
            this.value = value;
        }
    }
}
