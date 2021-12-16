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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;

import androidx.annotation.Nullable;

public class SimpleHalfRingView extends View {
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
    private float whPercent = 5f;
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
        setData(888,1.0f);
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
    private int distanceBetweenNumberAndPercent = DensityUtil.dp2px(2);
    /**
     * 任务数字与单位之间的距离
     */
    private int distanceBetweenTaskNumberAndSuffix = DensityUtil.dp2px(3);
    private float total = 0.9f;
    private int number = 888;

    private Point temp = new Point();

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
        /************************************指示区域**************************************/

        //左侧前景
        if (total >= 0.5f) {
            //大于圆环的一半
            /************************************指示点**************************************/
            float angle = startAngle + (endAngle >> 2);
            Point point = DrawViewUtils.calculatePoint(centerPoint.x, centerPoint.y, (int) (radius + strokeWidth + pointRadius + betweenPointAndArc), angle);
            drawPaint.setColor(forceColor);
            drawPaint.setStyle(Paint.Style.STROKE);
            drawPaint.setStrokeWidth(DensityUtil.dp2px(2));
            canvas.drawCircle(point.x, point.y, pointRadius, drawPaint);
            /************************************虚线**************************************/
            drawPaint.setColor(forceColor);
            drawPaint.setStyle(Paint.Style.FILL);
            drawPaint.setStrokeWidth(DensityUtil.dp2px(2));
            drawPaint.setPathEffect(lineDashPathEffect);
            float centerPointX = point.x - DensityUtil.dp2px(20);
            float centerPointY = point.y;
            float endPointX = centerPointX - DensityUtil.dp2px(5);
            float endPointY = centerPointY;
            canvas.drawLine(point.x - pointRadius, point.y, centerPointX, centerPointY, drawPaint);
            canvas.drawLine(centerPointX, centerPointY, endPointX, endPointY, drawPaint);
            drawPaint.setPathEffect(null);
            /************************************文字**************************************/
            //上部文字百分号
            String strPercent = "%";
            String strDown = "已完成";
            temp.set((int) endPointX, (int) endPointY);
            Rect[] strRect = calcPointTextRect(temp, true, strPercent, strDown, 12, 12f);
            textPaint.setTextSize(DensityUtil.dp2px(12f));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(strPercent, strRect[0].left, strRect[0].top + strRect[2].height(), textPaint);
            //下部文字
            int offset = DensityUtil.dp2px(5);
            textPaint.setTextSize(DensityUtil.dp2px(12f));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            canvas.drawText(strDown, strRect[1].left - offset, strRect[1].top + strRect[3].height(), textPaint);
            //上部数字
            String strNumber = getProgressStr(true) + "";
            temp.set(strRect[0].left - distanceBetweenNumberAndPercent - strRect[2].width(), (int) (strRect[0].top + strRect[2].height() * 1.5f));
            Rect[] numberRect = calcPointTextRect(temp, true, strNumber, strNumber, 12f, 12f);
            textPaint.setTextSize(DensityUtil.dp2px(22));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(strNumber, numberRect[0].left-DensityUtil.dp2px(5), numberRect[0].top + numberRect[2].height(), textPaint);
        } else {
            //小于圆环的一半
            /************************************指示点**************************************/
            float angle = startAngle + (getSweepAngle(1f * total) >> 1);
            Point point = DrawViewUtils.calculatePoint(centerPoint.x, centerPoint.y, (int) (radius + strokeWidth + pointRadius + betweenPointAndArc), angle);
            drawPaint.setColor(forceColor);
            drawPaint.setStyle(Paint.Style.STROKE);
            drawPaint.setStrokeWidth(DensityUtil.dp2px(2));
            canvas.drawCircle(point.x, point.y, pointRadius, drawPaint);
            /************************************虚线**************************************/
            drawPaint.setColor(forceColor);
            drawPaint.setStyle(Paint.Style.FILL);
            drawPaint.setStrokeWidth(DensityUtil.dp2px(2));
            drawPaint.setPathEffect(lineDashPathEffect);
            float centerPointX = point.x - DensityUtil.dp2px(20);
            float centerPointY = point.y - DensityUtil.dp2px(15);
            float endPointX = centerPointX - DensityUtil.dp2px(20);
            float endPointY = centerPointY;
            canvas.drawLine(point.x - pointRadius, point.y - pointRadius, centerPointX, centerPointY, drawPaint);
            canvas.drawLine(centerPointX, centerPointY, endPointX, endPointY, drawPaint);
            drawPaint.setPathEffect(null);
            /************************************文字**************************************/
            //上部文字百分号
            String strPercent = "%";
            String strDown = "已完成";
            temp.set((int) endPointX, (int) endPointY);
            Rect[] strRect = calcPointTextRect(temp, true, strPercent, strDown, 12f, 12f);
            textPaint.setTextSize(DensityUtil.dp2px(12f));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(strPercent, strRect[0].left, strRect[0].top + strRect[2].height(), textPaint);
            //下部文字
            int offset = DensityUtil.dp2px(5);
            textPaint.setTextSize(DensityUtil.dp2px(12f));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            canvas.drawText(strDown, strRect[1].left - offset, strRect[1].top + strRect[3].height(), textPaint);
            //上部数字
            String strNumber = getProgressStr(true) + "";
            temp.set(strRect[0].left - distanceBetweenNumberAndPercent - strRect[2].width(), (int) (strRect[0].top + strRect[2].height() * 1.5f));
            Rect[] numberRect = calcPointTextRect(temp, true, strNumber, strNumber, 12f, 12f);
            textPaint.setTextSize(DensityUtil.dp2px(22f));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(strNumber, numberRect[0].left, numberRect[0].top + numberRect[2].height(), textPaint);
        }

        //右侧背景
        if (total <= 0.5f) {
            //大于圆环的一半
            /************************************指示点**************************************/
            float angle = startAngle + (endAngle >> 1) + (endAngle >> 2);
            Point point = DrawViewUtils.calculatePoint(centerPoint.x, centerPoint.y, (int) (radius + strokeWidth + pointRadius + betweenPointAndArc), angle);
            drawPaint.setColor(forceColor);
            drawPaint.setStyle(Paint.Style.STROKE);
            drawPaint.setStrokeWidth(DensityUtil.dp2px(2));
            canvas.drawCircle(point.x, point.y, pointRadius, drawPaint);
            /************************************虚线**************************************/
            drawPaint.setColor(forceColor);
            drawPaint.setStyle(Paint.Style.FILL);
            drawPaint.setStrokeWidth(DensityUtil.dp2px(2));
            drawPaint.setPathEffect(lineDashPathEffect);
            float centerPointX = point.x + DensityUtil.dp2px(20);
            float centerPointY = point.y;
            float endPointX = centerPointX + DensityUtil.dp2px(5);
            float endPointY = centerPointY;
            canvas.drawLine(point.x + pointRadius, point.y, centerPointX, centerPointY, drawPaint);
            canvas.drawLine(centerPointX, centerPointY, endPointX, endPointY, drawPaint);
            drawPaint.setPathEffect(null);
            /************************************文字**************************************/
            //上部数字
            String strNumber = getProgressStr(false) + "";
            String strDown = "已完成";
            temp.set((int) endPointX, (int) endPointY);
            Rect[] strRect = calcPointTextRect(temp, false, strNumber, strDown, 22f, 12f);
            textPaint.setTextSize(DensityUtil.dp2px(22f));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(strNumber, strRect[0].left, strRect[0].top + strRect[2].height(), textPaint);
            //下部文字
            textPaint.setTextSize(DensityUtil.dp2px(12f));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            canvas.drawText(strDown, strRect[1].left, strRect[1].top + strRect[3].height(), textPaint);
            //上部文字百分号
            String strPercent = "%";
            temp.set(strRect[0].left + distanceBetweenNumberAndPercent + strRect[2].width(), (int) (strRect[0].top + strRect[2].height() * 1.2f));
            Rect[] numberRect = calcPointTextRect(temp, false, strPercent, strPercent, 12, 12f);
            textPaint.setTextSize(DensityUtil.dp2px(12f));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(strPercent, numberRect[0].left, numberRect[0].top + numberRect[2].height(), textPaint);
        } else {
            //小于圆环的一半
            //指示点
            float progressAngle = getSweepAngle(1f * total);
            float angle = startAngle + progressAngle + (endAngle - progressAngle) / 2;
            Point point = DrawViewUtils.calculatePoint(centerPoint.x, centerPoint.y, (int) (radius + strokeWidth + pointRadius + betweenPointAndArc), angle);
            drawPaint.setColor(forceColor);
            drawPaint.setStyle(Paint.Style.STROKE);
            drawPaint.setStrokeWidth(DensityUtil.dp2px(2));
            canvas.drawCircle(point.x, point.y, pointRadius, drawPaint);
            //虚线
            drawPaint.setColor(forceColor);
            drawPaint.setStyle(Paint.Style.FILL);
            drawPaint.setStrokeWidth(DensityUtil.dp2px(2));
            drawPaint.setPathEffect(lineDashPathEffect);
            float centerPointX = point.x + DensityUtil.dp2px(20);
            float centerPointY = point.y - DensityUtil.dp2px(15);
            float endPointX = centerPointX + DensityUtil.dp2px(20);
            float endPointY = centerPointY;
            canvas.drawLine(point.x + pointRadius, point.y - pointRadius, centerPointX, centerPointY, drawPaint);
            canvas.drawLine(centerPointX, centerPointY, endPointX, endPointY, drawPaint);
            drawPaint.setPathEffect(null);
            /************************************文字**************************************/
            //上部数字
            String strNumber = getProgressStr(false) + "";
            String strDown = "已完成";
            temp.set((int) endPointX, (int) endPointY);
            Rect[] strRect = calcPointTextRect(temp, false, strNumber, strDown, 22f, 12f);
            textPaint.setTextSize(DensityUtil.dp2px(22));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(strNumber, strRect[0].left, strRect[0].top + strRect[2].height(), textPaint);
            //下部文字
            textPaint.setTextSize(DensityUtil.dp2px(12));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            canvas.drawText(strDown, strRect[1].left, strRect[1].top + strRect[3].height(), textPaint);
            //上部文字百分号
            String strPercent = "%";
            temp.set(strRect[0].left + distanceBetweenNumberAndPercent + strRect[2].width(), (int) (strRect[0].top + strRect[2].height() * 1.2f));
            Rect[] numberRect = calcPointTextRect(temp, false, strPercent, strPercent, 12, 12f);
            textPaint.setTextSize(DensityUtil.dp2px(12));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(strPercent, numberRect[0].left, numberRect[0].top + numberRect[2].height(), textPaint);
        }


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
     * 计算指示点文字
     *
     * @param point       延长线重点坐标
     * @param isLeft      是否是左边
     * @param upStr       上部分文字
     * @param downStr     下部文字
     * @param topStrSize  上部分文字大小
     * @param downStrSize 下部分文字大小
     * @return 返回4个元素的矩阵，第一个是上部文字的尺寸，第二个是下部文字的尺寸，第三个是上部文字宽高，第四个是下部文字的宽高
     */
    private Rect[] calcPointTextRect(Point point, boolean isLeft, String upStr, String downStr, float topStrSize, float downStrSize) {
        Rect[] rects = new Rect[4];
        textPaint.setTextAlign(Paint.Align.LEFT);

        if (isLeft) {
            //计算上部文字位置
            Rect rectTopLeft = new Rect();
            textPaint.setColor(forceColor);
            textPaint.setTextSize(DensityUtil.sp2px(topStrSize));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            int[] textSizeUpLeft = DrawViewUtils.getTextWH(textPaint, upStr);
            rectTopLeft.right = point.x - distanceBetweenTextAndLine;
            rectTopLeft.left = rectTopLeft.right - textSizeUpLeft[0];
            rectTopLeft.top = point.y - textSizeUpLeft[1] - distanceBetweenText / 2;
            rectTopLeft.bottom = rectTopLeft.top + textSizeUpLeft[1];
            rects[0] = rectTopLeft;
            //计算下部文字位置
            Rect rectDownLeft = new Rect();
            textPaint.setColor(forceColor);
            textPaint.setTextSize(DensityUtil.sp2px(downStrSize));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            int[] textSizeDownLeft = DrawViewUtils.getTextWH(textPaint, downStr);
            rectDownLeft.right = point.x - distanceBetweenTextAndLine;
            rectDownLeft.left = rectDownLeft.right - textSizeDownLeft[0];
            rectDownLeft.top = point.y + distanceBetweenText / 2;
            rectDownLeft.bottom = rectDownLeft.top + textSizeDownLeft[1];
            rects[1] = rectDownLeft;

            Rect textSizeUpLeftRect = new Rect();
            textSizeUpLeftRect.set(0, 0, textSizeUpLeft[0], textSizeUpLeft[1]);
            rects[2] = textSizeUpLeftRect;

            Rect textSizeDownLeftRect = new Rect();
            textSizeDownLeftRect.set(0, 0, textSizeDownLeft[0], textSizeDownLeft[1]);
            rects[3] = textSizeDownLeftRect;
        } else {
            //计算上部文字位置
            Rect rectTopRight = new Rect();
            textPaint.setColor(forceColor);
            textPaint.setTextSize(DensityUtil.sp2px(topStrSize));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            int[] textSizeUpRight = DrawViewUtils.getTextWH(textPaint, upStr);
            rectTopRight.left = point.x + distanceBetweenTextAndLine;
            rectTopRight.right = rectTopRight.left + textSizeUpRight[0];
            rectTopRight.top = point.y - textSizeUpRight[1] - distanceBetweenText / 2;
            rectTopRight.bottom = rectTopRight.top + textSizeUpRight[1];
            rects[0] = rectTopRight;
            //计算下部文字位置
            Rect rectDownRight = new Rect();
            textPaint.setColor(forceColor);
            textPaint.setTextSize(DensityUtil.sp2px(downStrSize));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            int[] textSizeDownRight = DrawViewUtils.getTextWH(textPaint, downStr);
            rectDownRight.left = point.x + distanceBetweenTextAndLine;
            rectDownRight.right = rectDownRight.left + textSizeDownRight[0];
            rectDownRight.top = point.y + distanceBetweenText / 2;
            rectDownRight.bottom = rectDownRight.top + textSizeDownRight[1];
            rects[1] = rectDownRight;

            Rect textSizeUpRightRect = new Rect();
            textSizeUpRightRect.set(0, 0, textSizeUpRight[0], textSizeUpRight[1]);
            rects[2] = textSizeUpRightRect;

            Rect textSizeDownRightRect = new Rect();
            textSizeDownRightRect.set(0, 0, textSizeDownRight[0], textSizeDownRight[1]);
            rects[3] = textSizeDownRightRect;

        }
        return rects;
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

    public void setData(int number, float total) {
        this.number = number;
        this.total = total;
        startAnimation();
    }
}