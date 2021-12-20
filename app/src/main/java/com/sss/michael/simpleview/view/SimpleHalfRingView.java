package com.sss.michael.simpleview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;

import androidx.annotation.Nullable;

public class SimpleHalfRingView extends View {
    private boolean pointAnimation = true;
    /**
     * 动画持续时间
     */
    private static final int ANIMATION_DURATION = 1000;
    /**
     * 中心点
     */
    private Point centerPoint = new Point();
    /**
     * 宽高比例
     */
    private float whPercent = 0.5f;
    private float ringPercent = 0.4f;
    /**
     * 宽度
     */
    private int width;
    /**
     * 高度
     */
    private int height;
    private float radius;
    private int strokeWidth = DensityUtil.dp2px(18);
    /**
     * 画笔
     */
    private Paint textPaint = new Paint();
    private Paint drawPaint = new Paint();

    {
        drawPaint.setAntiAlias(true);
        textPaint.setAntiAlias(true);
    }

    public SimpleHalfRingView(Context context) {
        this(context, null);
    }

    public SimpleHalfRingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleHalfRingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setData(888, 0.5f, true);
            }
        });
        setData(888, 0.2f, true);
    }

    public SimpleHalfRingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        if (height == 0) {
            height = (int) (width * whPercent);
            setMeasuredDimension(width, height);
        }
        centerPoint.set(width / 2, height / 2);
        radius = Math.min(width * ringPercent, height * ringPercent);
        ringBackgroundRect.set(
                centerPoint.x - radius,
                centerPoint.y - radius,
                centerPoint.x + radius,
                centerPoint.y + radius
        );
        ringForegroundRect.set(
                centerPoint.x - radius,
                centerPoint.y - radius,
                centerPoint.x + radius,
                centerPoint.y + radius
        );
        pointRect.set(
                centerPoint.x - radius + (strokeWidth >> 1) + DensityUtil.dp2px(5),
                centerPoint.y - radius + (strokeWidth >> 1) + DensityUtil.dp2px(5),
                centerPoint.x + radius - (strokeWidth >> 1) - DensityUtil.dp2px(5),
                centerPoint.y + radius - (strokeWidth >> 1) - DensityUtil.dp2px(5)
        );
    }

    RectF ringBackgroundRect = new RectF();
    RectF ringForegroundRect = new RectF();
    RectF pointRect = new RectF();
    int startAngle = 120, endAngle = 300;
    DashPathEffect pointDashPathEffect = new DashPathEffect(new float[]{DensityUtil.dp2px(2), 10}, 0);
    DashPathEffect lineDashPathEffect = new DashPathEffect(new float[]{3, 5}, 0);
    int forceColor = 0xffffffff;
    int backColor = 0x4dffffff;

    private float percent = 0f;

    /**
     * 指示点半径
     */
    int pointRadius = DensityUtil.dp2px(3);
    /**
     * 指示点与圆环的距离
     */
    int betweenPointAndArc = DensityUtil.dp2px(2);
    /**
     * 文字与指示线之间的距离
     */
    private int distanceBetweenTextAndLine = DensityUtil.dp2px(5);
    /**
     * 上下文字之间的距离
     */
    private int distanceBetweenText = DensityUtil.dp2px(6);
    /**
     * 数字与百分号之间的距离
     */
    private int distanceBetweenNumberAndPercent = DensityUtil.dp2px(3);
    /**
     * 任务数字与单位之间的距离
     */
    private int distanceBetweenTaskNumberAndSuffix = DensityUtil.dp2px(3);
    private float total = 0.9f;
    private int number = 888;

    private Point temp = new Point();
    Rect rect = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /************************************背景透明**************************************/
        setBackgroundColor(Color.TRANSPARENT);
        /************************************圆环背景**************************************/
        drawPaint.setColor(backColor);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(strokeWidth);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawArc(ringBackgroundRect, startAngle, endAngle, false, drawPaint);
        drawPaint.setStrokeCap(Paint.Cap.BUTT);
        /************************************内圈圆点**************************************/
        drawPaint.setColor(backColor);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(DensityUtil.dp2px(1));
        drawPaint.setPathEffect(pointDashPathEffect);
        canvas.drawArc(pointRect, startAngle, endAngle, false, drawPaint);
        drawPaint.setPathEffect(null);
        /************************************圆环前景**************************************/
        drawPaint.setColor(forceColor);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(strokeWidth);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawArc(ringBackgroundRect, startAngle, getSweepAngle(percent * total), false, drawPaint);
        drawPaint.setStrokeCap(Paint.Cap.BUTT);
        /************************************中间文字**************************************/
        int offset = DensityUtil.dp2px(5);

        String numberStr = String.valueOf((int) (percent * number));
        textPaint.setTextSize(DensityUtil.dp2px(36));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        int[] numberSize = DrawViewUtils.getTextWH(textPaint, numberStr);
        canvas.drawText(numberStr, centerPoint.x - (numberSize[0] >> 1) - offset, centerPoint.y + DensityUtil.dp2px(2), textPaint);


        String suffixStr = "个";
        textPaint.setTextSize(DensityUtil.dp2px(18));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(suffixStr, centerPoint.x - (numberSize[0] >> 1) + numberSize[0] + DensityUtil.dp2px(2) + distanceBetweenTaskNumberAndSuffix - offset, centerPoint.y, textPaint);

        String taskStr = "任务";
        textPaint.setTextSize(DensityUtil.dp2px(14));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        int[] taskSize = DrawViewUtils.getTextWH(textPaint, taskStr);
        canvas.drawText(taskStr, centerPoint.x - ((taskSize[1] + offset) >> 1), centerPoint.y + (numberSize[1] >> 1) + taskSize[1], textPaint);
        /************************************左半球指示文字**************************************/
        String strLeftNumber = getProgressStr(true) + "";
        textPaint.setColor(forceColor);
        textPaint.setTextSize(DensityUtil.sp2px(22f));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        int[] strLeftNumberSize = DrawViewUtils.getTextWH(textPaint, strLeftNumber);

        String strLeftPercent = "%";
        textPaint.setColor(forceColor);
        textPaint.setTextSize(DensityUtil.sp2px(12f));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        int[] strLeftPercentSize = DrawViewUtils.getTextWH(textPaint, strLeftPercent);

        String strLeftDown = "已完成";
        textPaint.setColor(forceColor);
        textPaint.setTextSize(DensityUtil.sp2px(12f));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        int[] strLeftDownSize = DrawViewUtils.getTextWH(textPaint, strLeftDown);


        rect.left = getPaddingStart();
        rect.top = centerPoint.y - distanceBetweenTextAndLine - strLeftNumberSize[1];
        rect.right = rect.left + strLeftNumberSize[0] + strLeftPercentSize[0] + distanceBetweenNumberAndPercent;
        rect.bottom = centerPoint.y + distanceBetweenTextAndLine + strLeftDownSize[1];

        textPaint.setTextSize(DensityUtil.sp2px(22f));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(strLeftNumber, rect.left, ((rect.bottom + rect.top) >> 1) - distanceBetweenTextAndLine, textPaint);
        textPaint.setTextSize(DensityUtil.sp2px(12f));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText(strLeftPercent, rect.right - strLeftPercentSize[0], ((rect.bottom + rect.top) >> 1) - distanceBetweenTextAndLine, textPaint);
        textPaint.setTextSize(DensityUtil.sp2px(12f));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText(strLeftDown, rect.left + (rect.width() >> 1) - (strLeftDownSize[0] >> 1), ((rect.bottom + rect.top) >> 1) + distanceBetweenTextAndLine, textPaint);

        /************************************右半球指示文字**************************************/
        String strRightPercent = "%";
        textPaint.setColor(forceColor);
        textPaint.setTextSize(DensityUtil.sp2px(12f));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        int[] strRightPercentSize = DrawViewUtils.getTextWH(textPaint, strRightPercent);


        String strRightNumber = getProgressStr(false) + "";
        textPaint.setColor(forceColor);
        textPaint.setTextSize(DensityUtil.sp2px(22f));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        int[] strRightNumberSize = DrawViewUtils.getTextWH(textPaint, strRightNumber);


        String strRightDown = "未完成";
        textPaint.setColor(forceColor);
        textPaint.setTextSize(DensityUtil.sp2px(12f));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        int[] strRightDownSize = DrawViewUtils.getTextWH(textPaint, strRightDown);

        rect.right = getWidth() - getPaddingEnd();
        rect.left = rect.right - strRightPercentSize[0] - distanceBetweenNumberAndPercent - strRightNumberSize[0];
        rect.top = centerPoint.y - distanceBetweenTextAndLine - strRightNumberSize[1];
        rect.bottom = centerPoint.y + distanceBetweenTextAndLine + strRightDownSize[1];

        textPaint.setTextSize(DensityUtil.sp2px(12f));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText(strRightPercent, rect.right - strRightPercentSize[0], ((rect.bottom + rect.top) >> 1) - distanceBetweenTextAndLine, textPaint);
        textPaint.setTextSize(DensityUtil.sp2px(22f));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(strRightNumber, rect.right - strRightPercentSize[0] - strRightNumberSize[0] - distanceBetweenNumberAndPercent, ((rect.bottom + rect.top) >> 1) - distanceBetweenTextAndLine, textPaint);
        textPaint.setTextSize(DensityUtil.sp2px(12f));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText(strRightDown, rect.left + (rect.width() >> 1) - (strLeftDownSize[0] >> 1), ((rect.bottom + rect.top) >> 1) + distanceBetweenTextAndLine, textPaint);

        /************************************指示点**************************************/


        //左半圆
        float leftAngle;

        if (pointAnimation) {
            float progress = (float) (percent * total);
            leftAngle = startAngle + getSweepAngle(Math.min(progress, 0.5f)) * 0.5f;
        } else {
            leftAngle = startAngle + getSweepAngle((float) (total >= 0.5f ? 0.5 : total)) * 0.5f;
        }
        Point leftPoint = DrawViewUtils.calculatePoint(centerPoint.x, centerPoint.y, (int) (radius + strokeWidth + pointRadius + betweenPointAndArc), leftAngle);
        drawPaint.setColor(forceColor);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(DensityUtil.dp2px(2));
        canvas.drawCircle(leftPoint.x, leftPoint.y, pointRadius, drawPaint);
        //右半圆
//        float rightAngle = 0;
//        if (pointAnimation) {
//            float progress = (float) (percent * total) * 0.5f;
//            rightAngle = startAngle + endAngle - getSweepAngle(progress);
//        } else {
//            float progress = (float) (total >= 0.5f ? 0.5 : total) * 0.5f;
//            rightAngle = startAngle + endAngle - getSweepAngle(progress);
//        }

        float rightAngle = 0;
        if (pointAnimation) {
            float progress = 1 - (float) (percent * total);
            rightAngle = startAngle + endAngle - (endAngle - getSweepAngle(progress));
        } else {
            float progress = (float) (total >= 0.5f ? 0.5 : total) * 0.5f;
            rightAngle = startAngle + endAngle - getSweepAngle(progress);
        }


        Point point = DrawViewUtils.calculatePoint(centerPoint.x, centerPoint.y, (int) (radius + strokeWidth + pointRadius + betweenPointAndArc), rightAngle);
        drawPaint.setColor(forceColor);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(DensityUtil.dp2px(2));
        canvas.drawCircle(point.x, point.y, pointRadius, drawPaint);
    }

    /**
     * 按进度转换为角度
     *
     * @param progress
     * @return
     */
    private int getSweepAngle(float progress) {
        return (int) (progress * endAngle);
    }

    /**
     * 进度转文本
     *
     * @param isLeft 是否位于左侧
     * @return 0-100
     */
    private int getProgressStr(boolean isLeft) {
        return isLeft ? (int) (1f * total * 100) : (int) (1f * (1f - total) * 100);
    }


    /**
     * 开始动画
     */
    public void startAnimation() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        valueAnimator = ValueAnimator.ofFloat(0, 1f);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setDuration(ANIMATION_DURATION);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(listener);
        valueAnimator.start();
    }

    /**
     * 动画
     */
    private ValueAnimator valueAnimator;
    /**
     * 动画监听器
     */
    private ValueAnimator.AnimatorUpdateListener listener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            percent = (float) animation.getAnimatedValue();
            invalidate();
        }
    };

    public void setData(int number, float total, boolean animation) {
//        if (pointAnimation) {
//            percent = 0.5f;
//        } else {
//            percent = 1f;
//        }
        percent = 1f;
        this.number = number;
        this.total = total;
        if (animation) {
            startAnimation();
        } else {
            requestLayout();
        }
    }
}