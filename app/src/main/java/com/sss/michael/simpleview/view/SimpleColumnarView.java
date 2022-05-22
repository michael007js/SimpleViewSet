package com.sss.michael.simpleview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.sss.michael.simpleview.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author Michael by SSS
 * @date 2022/5/22 12:02
 * @Description 一个简单的柱状图表
 */
public class SimpleColumnarView extends View {
    private Paint paint = new Paint();

    {
        paint.setAntiAlias(true);
    }

    private boolean isDebug = false;
    /**
     * 顶部预留区域、图表区域与文字区域比例
     */
    private float[] areasRect = {0.15f, 0.7f, 0.15f};
    /**
     * 顶部预留区域（为图标中柱状条顶部文字预留，实际柱状图不会达到本区域）
     */
    private RectF topReserveArea = new RectF();
    /**
     * 有效图表区域
     */
    private RectF effectiveChartArea = new RectF();
    /**
     * 底部文字注释区域
     */
    private RectF bottomNotesArea = new RectF();
    /**
     * 背景虚线数量
     */
    private int dashLineCount = 5;
    /**
     * 背景虚线颜色
     */
    private int dashLineColor = isDebug ? Color.BLACK : Color.parseColor("#eeeeee");
    /**
     * 宽高比例
     */
    private float whPercent = 0.5f;
    /**
     * 宽高
     */
    private int width, height;
    /**
     * 左右边距
     */
    private float paddingLeft = DensityUtil.dp2px(20), paddingRight = DensityUtil.dp2px(20);
    /**
     * 每个柱状图表之间的间距
     */
    private float distance = DensityUtil.dp2px(50);
    /**
     * 柱状集合
     */
    private List<SimpleColumnParameter> list = new ArrayList<>();
    private ValueAnimator valueAnimator;

    public SimpleColumnarView(Context context) {
        this(context, null);
    }

    public SimpleColumnarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleColumnarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isDebug) {
            List<SimpleColumnParameter> data = new ArrayList<>();
            data.add(new SimpleColumnParameter(100, "100", "2022"));
            data.add(new SimpleColumnParameter(80, "80", "2022"));
            data.add(new SimpleColumnParameter(90, "90", "2022"));
            data.add(new SimpleColumnParameter(22, "22", "2022"));
            data.add(new SimpleColumnParameter(88, "88", "2022"));
            setData(data);
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    start();
                }
            });
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        if (width < 1) {
            width = DensityUtil.dp2px(100);
        }
        if (height < 1) {
            height = (int) (width * whPercent);
        }
        setMeasuredDimension(width, height);
        topReserveArea.set(0f, 0f, width, height * areasRect[0]);
        effectiveChartArea.set(0f, topReserveArea.bottom, width, topReserveArea.bottom + height * areasRect[1]);
        bottomNotesArea.set(0f, effectiveChartArea.bottom, width, effectiveChartArea.bottom + height * areasRect[2]);
    }

    DashPathEffect dashPathEffect = new DashPathEffect(new float[]{5, 10}, 0);
    RectF rectF = new RectF();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStyle(Paint.Style.FILL);
        if (isDebug) {
            paint.setColor(Color.RED);
            canvas.drawRect(topReserveArea, paint);
            paint.setColor(Color.YELLOW);
            canvas.drawRect(effectiveChartArea, paint);
            paint.setColor(Color.CYAN);
            canvas.drawRect(bottomNotesArea, paint);
        }
        //背景虚线
        paint.setPathEffect(dashPathEffect);
        paint.setColor(dashLineColor);
        for (float i = 0; i < dashLineCount; i++) {
            canvas.drawLine(paddingLeft, effectiveChartArea.top + (1.0f / dashLineCount * i) * effectiveChartArea.height(), width - paddingRight, effectiveChartArea.top + (1.0f / dashLineCount * i) * effectiveChartArea.height(), paint);
        }
        //X轴
        paint.setPathEffect(null);
        canvas.drawLine(paddingLeft, bottomNotesArea.top, width - paddingRight, bottomNotesArea.top, paint);
        //X轴文字区域
        paint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < list.size(); i++) {
            paint.setColor(list.get(i).xAxisTextColor);
            paint.setTextSize(list.get(i).xAxisTextSize);
            canvas.drawText(list.get(i).xAxisText, list.get(i).drawRectF.left + list.get(i).drawRectF.width() / 2, bottomNotesArea.top + bottomNotesArea.height() / 2, paint);
        }

        //柱状图表
        for (int i = 0; i < list.size(); i++) {
            if (isDebug) {
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(list.get(i).drawRectF, paint);
                paint.setStyle(Paint.Style.FILL);
            }
            //图表
            rectF.left = list.get(i).drawRectF.centerX() - list.get(i).columnWidth / 2;
            rectF.top = list.get(i).drawRectF.top;
            rectF.right = list.get(i).drawRectF.centerX() + list.get(i).columnWidth / 2;
            rectF.bottom = list.get(i).drawRectF.bottom;
            //图表文字
            paint.setColor(list.get(i).columnColor);
            canvas.drawRect(rectF, paint);
            paint.setColor(list.get(i).columnTextColor);
            paint.setTextSize(list.get(i).columnTextSize);
            paint.setFakeBoldText(true);
            canvas.drawText(list.get(i).columnText, list.get(i).drawRectF.left + list.get(i).drawRectF.width() / 2, list.get(i).drawRectF.top - list.get(i).distanceBetweenRemarkWithColumn, paint);
        }

        //X轴文字区域
        paint.setFakeBoldText(false);
        paint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < list.size(); i++) {
            paint.setColor(list.get(i).xAxisTextColor);
            paint.setTextSize(list.get(i).xAxisTextSize);
            canvas.drawText(list.get(i).xAxisText, list.get(i).drawRectF.left + list.get(i).drawRectF.width() / 2, bottomNotesArea.top + bottomNotesArea.height() / 2, paint);
        }


    }

    public void setData(final List<SimpleColumnParameter> data) {
        post(new Runnable() {
            @Override
            public void run() {
                float max = 0, min = Float.MAX_VALUE;
                for (int i = 0; i < data.size(); i++) {
                    max = Math.max(max, data.get(i).yAxisValue);
                    min = Math.min(min, data.get(i).yAxisValue);
                }
                //数据与有效区高度百分比
                float percent = effectiveChartArea.height() / max;
                //每个柱状图宽度=(图表有效区宽度-图表左右边距-每个柱状间的间隔)/柱条数量
                float effectiveWidth = (effectiveChartArea.width() - paddingLeft - paddingRight - (data.size() - 1) * distance) / data.size();
                for (int i = 0; i < data.size(); i++) {
                    if (i == 0) {
                        data.get(i).bestRectF.left = paddingLeft;
                    } else {
                        data.get(i).bestRectF.left = data.get(i - 1).bestRectF.right + distance;
                    }
                    data.get(i).bestRectF.right = data.get(i).bestRectF.left + effectiveWidth;
                    data.get(i).bestRectF.bottom = effectiveChartArea.bottom;
                    data.get(i).bestRectF.top = effectiveChartArea.bottom - (percent * data.get(i).yAxisValue);
                }
                list.clear();
                list.addAll(data);
                start();
            }
        });

    }


    private void start() {
        stop();
        if (list != null && list.size() > 0) {
            valueAnimator = ValueAnimator.ofFloat(0f, 1f);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float f = (float) animation.getAnimatedValue();
                    for (int i = 0; i < list.size(); i++) {
                        list.get(i).drawRectF.left = list.get(i).bestRectF.left;
                        list.get(i).drawRectF.top = list.get(i).bestRectF.bottom - list.get(i).bestRectF.height() * f;
                        list.get(i).drawRectF.right = list.get(i).bestRectF.right;
                        list.get(i).drawRectF.bottom = list.get(i).bestRectF.bottom;
                        invalidate();
                    }
                }
            });
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);
            valueAnimator.setDuration(500);
            valueAnimator.start();
        }
    }

    private void stop() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.removeAllListeners();
            valueAnimator.removeAllUpdateListeners();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    /**
     * @author Michael by SSS
     * @date 2022/5/22 13:08
     * @Description 柱状条参数模型
     */
    public static class SimpleColumnParameter {
        /**
         * y轴数值
         */
        private float yAxisValue;
        /**
         * X轴文字注释
         */
        private String xAxisText;
        /**
         * X轴文字字体
         */
        private float xAxisTextSize = DensityUtil.sp2px(12);
        /**
         * X轴文字色号
         */
        private int xAxisTextColor = Color.parseColor("#999999");
        /**
         * 柱状图表与文字备注之间的距离
         */
        private float distanceBetweenRemarkWithColumn = DensityUtil.dp2px(4);
        /**
         * 柱状图表文字备注
         */
        private String columnText;
        /**
         * 柱状图宽度
         */
        private float columnWidth = DensityUtil.dp2px(8);
        /**
         * 柱状图色值
         */
        private int columnColor = Color.parseColor("#e9302d");
        /**
         * 柱状图表文字备注字体大小
         */
        private float columnTextSize = DensityUtil.sp2px(14);
        /**
         * 柱状图表文字备注色号
         */
        private int columnTextColor = Color.parseColor("#212121");
        /**
         * 每个柱状图的理论最佳绘制区域
         */
        private RectF bestRectF = new RectF();
        /**
         * 每个柱状图的理论实际绘制区域,最后的绘制将以本参数为主，从{@link #bestRectF}镜像而来
         */
        private RectF drawRectF = new RectF();

        public SimpleColumnParameter(float yAxisValue, String columnText, String xAxisText) {
            this.yAxisValue = yAxisValue;
            this.columnText = columnText;
            this.xAxisText = xAxisText;
        }
    }
}
