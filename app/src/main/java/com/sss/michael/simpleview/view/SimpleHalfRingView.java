package com.sss.michael.simpleview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;

import androidx.annotation.Nullable;

public class SimpleHalfRingView extends View {
    /**
     * 动画持续时间
     */
    private static final int ANIMATION_DURATION = 500;
    /**
     * 中心点
     */
    private Point centerPoint = new Point();
    /**
     * 宽高比例
     */
    private float whPercent = 5f;
    private float ringPercent = 0.35f;
    /**
     * 宽度
     */
    private int width;
    /**
     * 高度
     */
    private int height;
    private float radius;
    private int rotateAngle = 120;
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

            }
        });
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
        ringRect.set(
                centerPoint.x - radius,
                centerPoint.y - radius,
                centerPoint.x + radius,
                centerPoint.y + radius
        );
    }

    RectF ringRect = new RectF();
    int startAngle = 0, endAngle = 300;

    RectF ringStartRect=new RectF(), ringEndRect = new RectF();
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setBackgroundColor(Color.BLACK);
        drawPaint.setColor(0x4dffffff);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(strokeWidth);

        canvas.drawPoint(centerPoint.x, centerPoint.y, drawPaint);
        canvas.rotate(rotateAngle, centerPoint.x, centerPoint.y);
        canvas.drawArc(ringRect, startAngle, endAngle, false, drawPaint);


        drawPaint.setStyle(Paint.Style.FILL);
        drawPaint.setStrokeWidth(DensityUtil.dp2px(1));
        Point point = DrawViewUtils.calculatePoint(centerPoint.x, centerPoint.y, (int) radius, 90 + getAngleRotateOffset());
        canvas.drawLine(centerPoint.x, centerPoint.y, point.x, point.y, drawPaint);


        Point startPoint = DrawViewUtils.calculatePoint(centerPoint.x, centerPoint.y, (int) radius, startAngle);
        Point endPoint = DrawViewUtils.calculatePoint(centerPoint.x, centerPoint.y, (int) radius, endAngle);
        ringStartRect.set(startPoint.x - strokeWidth / 2, startPoint.y - strokeWidth / 2, startPoint.x + strokeWidth / 2, startPoint.y + strokeWidth / 2);
        ringEndRect.set(endPoint.x - strokeWidth / 2, endPoint.y - strokeWidth / 2, endPoint.x + strokeWidth / 2, endPoint.y + strokeWidth / 2);
        canvas.drawArc(ringEndRect, -62,180,true, drawPaint);
        drawPaint.setXfermode(null);
    }

    private int getAngleRotateOffset() {
        return -rotateAngle;
    }
}