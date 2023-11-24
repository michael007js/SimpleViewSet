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
    private int disableVerticalPaddingPercent = DensityUtil.dp2px(5);
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
     * 圆点宽度
     */
    private int pointWidth = DensityUtil.dp2px(6);
    /**
     * 柱状宽度
     */
    private int columnWidth = pointWidth * 3;
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
            bg.add(660);
            bg.add(630);
            bg.add(690);

            List<Bean> fg = new ArrayList<>();
            fg.add(new Bean(688));
            fg.add(new Bean(652));
            fg.add(new Bean(666));
            fg.add(new Bean(650));
            setData(true, bg, fg);
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
        effectiveRect.top = disableVerticalPaddingPercent + pointWidth / 2;
        effectiveRect.bottom = height - disableVerticalPaddingPercent - pointWidth / 2;
        effectiveRect.left = width / 2 - columnWidth / 2;
        effectiveRect.right = width / 2 + columnWidth / 2;
        for (Bean bean : foregroundData) {
            bean.rect.left = effectiveRect.left + effectiveRect.width() / 2 - pointWidth / 2;
            bean.rect.right = bean.rect.left + pointWidth;
        }
        setMeasuredDimension(width, height);
    }


    float getReallyY(float effectiveHeight, float eachDataRangHeight, int data, int maxData, int minData) {
        return effectiveRect.top + convert(data, minData, maxData) * eachDataRangHeight + effectiveHeight;
    }

    float convert(int data, int minData, int maxData) {
        return maxData - data;
    }

    Rect effectiveRect = new Rect();
    Rect foregroundRect = new Rect();


    private int debugColor = 0x66000000;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (debug) {
            setBackgroundColor(0xffeeeeee);
            bgPaint.setColor(debugColor);
//            canvas.drawRect(effectiveRect, bgPaint);
        }
        int maxData = 0;
        int minData = 0;
        for (int i = 0; i < backgroundData.size(); i++) {
            if (i == 0) {
                maxData = backgroundData.get(i).value;
                minData = backgroundData.get(i).value;
            } else {
                maxData = Math.max(maxData, backgroundData.get(i).value);
                minData = Math.min(minData, backgroundData.get(i).value);
            }
        }
        //有效宽度
        float effectiveHeight = effectiveRect.height();
        //每1个数据占据的高度
        float eachDataRangHeight = effectiveHeight / (maxData - minData);
        //背景x位置
        float x;
        if (drawText) {
            x = maxTextWidth == 0 ? 0 : maxTextWidth + distance;
        } else {
            x = 0;
        }


        for (BackgroundBean backgroundBean : backgroundData) {
            bgPaint.setColor(backgroundLineColor);
            float y = getReallyY(effectiveHeight, eachDataRangHeight, backgroundBean.value, minData, maxData);
            if (drawText) {
                float textHeight = DrawViewUtils.getTextWH(textPaint, backgroundBean.previewText)[1];
                textPaint.setColor(backgroundTextColor);
                canvas.drawText(backgroundBean.previewText, 0, y + textHeight / 2, textPaint);
                bgPaint.setColor(backgroundLineColor);
                canvas.drawLine(x, y, getWidth(), y, bgPaint);
            } else {
                bgPaint.setColor(backgroundLineColor);
                canvas.drawLine(0, y, getWidth(), y, bgPaint);
            }
        }

        if (foregroundData.size() > 1) {
            float topY = getReallyY(effectiveHeight, eachDataRangHeight, foregroundData.get(0).value, minData, maxData);
            float bottomY = getReallyY(effectiveHeight, eachDataRangHeight, foregroundData.get(foregroundData.size() - 1).value, minData, maxData);
            foregroundRect.top = (int) topY;
            foregroundRect.bottom = (int) bottomY;
            foregroundRect.left = effectiveRect.left;
            foregroundRect.right = effectiveRect.right;
            fgPaint.setColor(0x12ff7d00);
            canvas.drawRect(foregroundRect, fgPaint);
        }

        for (Bean bean : foregroundData) {
            float y = getReallyY(effectiveHeight, eachDataRangHeight, bean.value, minData, maxData);
            if (debug) {
                fgPaint.setColor(debugColor);
                canvas.drawLine(x, y, getWidth(), y, fgPaint);
            }
            bean.rect.top = (int) (y - pointWidth / 2);
            bean.rect.bottom = bean.rect.top + pointWidth;
            canvas.save();
            canvas.rotate(45, bean.rect.left + (bean.rect.width() >> 1), bean.rect.top + (bean.rect.height() >> 1));
            fgPaint.setColor(0xffff7d00);
            canvas.drawRect(bean.rect, fgPaint);
            canvas.restore();

        }

    }


    /**
     * 文字最大宽度
     */
    float maxTextWidth;
    /**
     * 是否绘制文字
     */
    boolean drawText;

    public void setData(boolean drawText, List<Integer> backgroundData, List<Bean> foregroundData) {
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
            previewText = String.valueOf(value);

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
