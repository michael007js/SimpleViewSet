package com.sss.michael.simpleview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author Michael by 61642
 * @date 2024/3/13 11:33
 * @Description 一个简单的居中柱状图
 */
public class SimpleMiddleColumnView extends View {
    private boolean DEBUG = true;
    private Rect contentAxisRect = new Rect();
    private Rect xAxisRect = new Rect();
    private List<SimpleMiddleColumnViewBean> data = new ArrayList<>();
    private Paint paint = new Paint();

    {
        paint.setAntiAlias(true);
    }

    /**
     * 左侧预留区域
     */
    private int leftReservedArea = DensityUtil.dp2px(15);
    /**
     * 每个柱状条的宽度
     */
    private int eachColumnWidth = DensityUtil.dp2px(10);

    /**
     * 每个柱状条之间的间距
     */
    private int eachColumnDistance = DensityUtil.dp2px(40);
    /**
     * 柱状条与文字之间的间距
     */
    private int distanceBetweenTextAndColumn = DensityUtil.dp2px(3);

    /**
     * 内容轴纵向无效绘制区域
     */
    private int invalidVerticalAreaInContentAxis = DensityUtil.dp2px(50);


    private ValueAnimator valueAnimator;
    private float animationValue = 1.0f;


    public SimpleMiddleColumnView(Context context) {
        this(context, null);
    }

    public SimpleMiddleColumnView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleMiddleColumnView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }


    /**
     * 是否反转
     */
    private boolean isReversal = true;

    public SimpleMiddleColumnView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (DEBUG) {
            List<SimpleMiddleColumnViewBean> data = new ArrayList<>();
            setPadding(DensityUtil.dp2px(10), DensityUtil.dp2px(10), DensityUtil.dp2px(10), DensityUtil.dp2px(10));
            data.add(new SimpleMiddleColumnViewBean("1月", "101606", "134198", 101606, 134198));
            data.add(new SimpleMiddleColumnViewBean("1月", "101606", "134198", 101606, 134198));
            data.add(new SimpleMiddleColumnViewBean("2月", "129410", "129410", 129410, 129410));
            data.add(new SimpleMiddleColumnViewBean("3月", "126367", "126367", 126367, 126367));

//            data.add(new SimpleMiddleColumnViewBean("1月", "487", "97", 97, 487));
//            data.add(new SimpleMiddleColumnViewBean("2月", "1535", "103", 103, 1535));
//            data.add(new SimpleMiddleColumnViewBean("3月", "6897", "280", 280, 6897));
//            data.add(new SimpleMiddleColumnViewBean("4月", "117", "117", 117, 117));

            setData(false, true, data);
        }


    }

    @Override
    public void layout(int l, int t, int r, int b) {
        super.layout(l, t, r, b);
        int width = r - l;
        int height = b - t;
        xAxisRect.left = getPaddingLeft() | getPaddingStart();
        xAxisRect.right = width - (getPaddingRight() | getPaddingEnd());
        xAxisRect.bottom = height - getPaddingBottom();

        //计算最高与最低点
        int maxAxisTextHeight = 0;
        int max = 0;
        int min = 0;
        if (data.size() > 0) {
            max = Math.max(data.get(0).topValue, data.get(0).bottomValue);
            min = Math.min(data.get(0).topValue, data.get(0).bottomValue);
            for (int i = 0; i < data.size(); i++) {
                max = Math.max(Math.max(data.get(i).topValue, data.get(i).bottomValue), max);
                min = Math.min(Math.min(data.get(i).topValue, data.get(i).bottomValue), min);
                maxAxisTextHeight = Math.max(DrawViewUtils.getTextWH(paint, data.get(i).xAxisText)[1], maxAxisTextHeight);
            }
        }

        xAxisRect.top = xAxisRect.bottom - maxAxisTextHeight - DensityUtil.dp2px(10 * 2/*x轴文字上下间距*/);

        contentAxisRect.top = getPaddingTop() + invalidVerticalAreaInContentAxis;
        contentAxisRect.left = xAxisRect.left;
        contentAxisRect.right = xAxisRect.right;
        contentAxisRect.bottom = xAxisRect.top - invalidVerticalAreaInContentAxis;


        //向下偏移量，保证绘制出的柱状不贴边，如果为0则会贴边
        float area = Math.abs(contentAxisRect.height()) * 0.5f;
        //每个值所占据的像素范围
        float eachPxForValueHeight = Math.abs(contentAxisRect.height()) * 1.0f / (max);

        //为每个柱状矩阵赋值范围
        for (int i = 0; i < data.size(); i++) {
            if (isReversal) {
                data.get(i).columnRect.top = contentAxisRect.bottom - (int) (eachPxForValueHeight * data.get(i).topValue);
                data.get(i).columnRect.bottom = contentAxisRect.bottom - (int) (eachPxForValueHeight * data.get(i).bottomValue);
            } else {
                data.get(i).columnRect.top = contentAxisRect.top + (int) (eachPxForValueHeight * data.get(i).topValue);
                data.get(i).columnRect.bottom = contentAxisRect.top + (int) (eachPxForValueHeight * data.get(i).bottomValue);
            }
            if (i == 0) {
                data.get(i).columnRect.left = leftReservedArea;
            } else {
                data.get(i).columnRect.left = data.get(i - 1).columnRect.right + eachColumnDistance;
            }
            data.get(i).columnRect.right = data.get(i).columnRect.left + eachColumnWidth;
        }
    }

    DashPathEffect dashPathEffect = new DashPathEffect(new float[]{5, 10}, 0);
    List<PointF> list = new ArrayList<>();
    Path path = new Path();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        list.clear();
        //绘制白色背景

        canvas.drawColor(0xffffffff);
        if (DEBUG) {
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(xAxisRect, paint);
            canvas.drawRect(contentAxisRect, paint);
        }
        //绘制X轴
        paint.setStrokeWidth(DensityUtil.dp2px(1));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xfff0f0f0);
        canvas.drawLine(xAxisRect.left, xAxisRect.top, xAxisRect.right, xAxisRect.top, paint);
        //绘制背景虚线
        int eachHeight = (contentAxisRect.height() + invalidVerticalAreaInContentAxis * 2) / 5;
        paint.setPathEffect(dashPathEffect);
        for (int i = 0; i < 5; i++) {
            canvas.drawLine(contentAxisRect.left, eachHeight * i, contentAxisRect.right, eachHeight * i, paint);
        }
        paint.setPathEffect(null);
        //绘制X轴文字
        paint.setTextSize(DensityUtil.dp2px(10));
        paint.setColor(0xff999999);
        paint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < data.size() * animationValue; i++) {
            int[] wh = DrawViewUtils.getTextWH(paint, data.get(i).xAxisText);
            canvas.drawText(data.get(i).xAxisText, data.get(i).columnRect.centerX(), xAxisRect.top + (xAxisRect.height() >> 1) + (wh[1] >> 1), paint);
        }

        //绘制柱状图区域
        for (int i = 0; i < data.size() * animationValue; i++) {
            paint.setColor(0xffffcd94);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(DensityUtil.dp2px(1.5f));


            float centerX = data.get(i).columnRect.left + data.get(i).columnRect.width() / 2;
            float centerY = data.get(i).columnRect.top + data.get(i).columnRect.height() / 2;

            //绘制柱状贴图
            canvas.drawRoundRect(data.get(i).columnRect, DensityUtil.dp2px(2), DensityUtil.dp2px(2), paint);
            paint.setStrokeWidth(DensityUtil.dp2px(1f));
            //绘制柱状图上的方形圆点
            canvas.save();
            canvas.rotate(45, centerX, centerY);
            int radius = DensityUtil.dp2px(3) / 2;
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(centerX - radius, centerY - radius, centerX + radius, centerY + radius, paint);
            canvas.restore();
            list.add(new PointF(centerX, centerY));
            //绘制柱状图上下两端的文字
            paint.setTextSize(DensityUtil.dp2px(10));
            paint.setColor(0xff666666);
            paint.setTextAlign(Paint.Align.CENTER);
            //待绘制的文字
            //文字尺寸
            int[] topSize = DrawViewUtils.getTextWH(paint, data.get(i).topText + "");
            int[] bottomSize = DrawViewUtils.getTextWH(paint, data.get(i).bottomText + "");
            // 计算Y轴文字的绘制位置
            int topY;
            int bottomY;

            topY = (int) (data.get(i).columnRect.top - distanceBetweenTextAndColumn);
            bottomY = (int) (data.get(i).columnRect.bottom + bottomSize[1] + distanceBetweenTextAndColumn);

            // 绘制柱状图上下两端的文字
            if (DEBUG) {
                paint.setColor(Color.RED);
            }
            canvas.drawText(data.get(i).topText + "", centerX, topY, paint);
            if (DEBUG) {
                paint.setColor(Color.BLACK);
            }
            canvas.drawText(data.get(i).bottomText + "", centerX, bottomY, paint);
        }
        if (list.size() > 0) {
            //绘制方形圆点曲线
            paint.setColor(0xffffcd94);
            paint.setStyle(Paint.Style.STROKE);
            path.reset();
            path.moveTo(list.get(0).x, list.get(0).y);
            DrawViewUtils.calculateBezier3(list, 0.7f, path);
            paint.setStrokeWidth(DensityUtil.dp2px(1));
            canvas.drawPath(path, paint);
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.removeAllListeners();
            valueAnimator.removeAllUpdateListeners();
        }
    }

    /**
     * 设置数据
     *
     * @param animation  是否动画
     * @param isReversal 是否反转
     * @param data       注意：{@link SimpleMiddleColumnViewBean#topValue}必须小于{@link SimpleMiddleColumnViewBean#bottomValue},否则将出现绘制错乱
     */
    public void setData(boolean animation, boolean isReversal, List<SimpleMiddleColumnViewBean> data) {
        this.isReversal = isReversal;
        if (isReversal) {
            for (SimpleMiddleColumnViewBean bean : data) {
                String temp = bean.bottomText;
                bean.bottomText = bean.topText;
                bean.topText = temp;
                int tempInteger = bean.bottomValue;
                bean.bottomValue = bean.topValue;
                bean.topValue = tempInteger;
            }
        }
        this.data = data;
        if (animation) {
            if (valueAnimator != null) {
                valueAnimator.cancel();
                valueAnimator.removeAllListeners();
                valueAnimator.removeAllUpdateListeners();
            }
            valueAnimator = ValueAnimator.ofFloat(0, 1f);
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);
            valueAnimator.setDuration(300);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    animationValue = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            valueAnimator.start();
        } else {
            animationValue = 1.0f;
            invalidate();
        }
    }


    public static class SimpleMiddleColumnViewBean {
        private String xAxisText;
        private int topValue;
        private int bottomValue;
        private String topText;
        private String bottomText;
        private final RectF columnRect = new RectF();

        public SimpleMiddleColumnViewBean(String xAxisText, String topText, String bottomText, int topValue, int bottomValue) {
            this.xAxisText = xAxisText;
            this.topText = topText;
            this.bottomText = bottomText;
            this.topValue = topValue;
            this.bottomValue = bottomValue;
        }

    }
}
