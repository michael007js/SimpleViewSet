package com.sss.michael.simpleview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;
import com.sss.michael.simpleview.utils.EmptyUtils;
import com.sss.michael.simpleview.utils.Log;

@SuppressWarnings("all")
public class SimpleRecommendDashboardView extends View {
    private final boolean DEBUG = true;
    /**
     * 中心点
     */
    private Point centerPoint = new Point();
    /**
     * 宽度
     */
    private int width;
    /**
     * 高度
     */
    private int height;
    /**
     * 外圆环宽度
     */
    private int outerCircleWidth = DensityUtil.dp2px(10);
    /**
     * 放大量，建议小于0.5
     * 外圆环半径百分比（取宽高最小的一个）
     * 控制{@link #outerCircleRingRect}大小
     * 因所有元素都是基于{@link #outerCircleRingRect}绘制
     * 同时也是控制所有元素的相对位置
     */
    private float outerCircleRingEnlargePercent = 0.12f;
    /**
     * 外圆环位置
     */
    private RectF outerCircleRingRect = new RectF();

    /**
     * 外圆环与内圆环半径之间的间距
     */
    private int distanceBetweenOfOuterAndInnerCircleRingRadius = DensityUtil.dp2px(8);
    /**
     * 内环宽度
     */
    private int innerCircleWidth = DensityUtil.dp2px(5);


    private SimpleRecommendDashboardViewBean model;

    public void setModel(SimpleRecommendDashboardViewBean model, boolean animation) {
        this.model = model;
        if (animation) {
            progress = 0f;
            animation();
        } else {
            progress = 1f;
            invalidate();
        }
    }

    ValueAnimator valueAnimator;

    float progress = 0f;

    void animation() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                progress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator(1.2f));
        valueAnimator.setDuration((long) (progress * 10 + 500));
        valueAnimator.start();
    }

    public SimpleRecommendDashboardView(Context context) {
        this(context, null);
    }

    public SimpleRecommendDashboardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleRecommendDashboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (DEBUG) {
            SimpleRecommendDashboardViewBean model = new SimpleRecommendDashboardViewBean(662, 177, 88, "所需冲刺", "所较稳妥", "所可保底", "0", "999", "建议尝试拨打报警电话");
            setModel(model, false);
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setModel(model, true);
                }
            });
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (MeasureSpec.EXACTLY == MeasureSpec.getMode(heightMeasureSpec)) {
            height = heightSize;
        } else {
            height = DensityUtil.dp2px(200);
        }
        setMeasuredDimension(width, height);

        centerPoint.set(width / 2, height);

        int value = Math.min(width, height);

        outerCircleRingRect.left = centerPoint.x - (value >> 1) - outerCircleRingEnlargePercent * value;
        outerCircleRingRect.top = centerPoint.y - (value >> 1) - outerCircleRingEnlargePercent * value;
        outerCircleRingRect.right = centerPoint.x + (value >> 1) + outerCircleRingEnlargePercent * value;
        outerCircleRingRect.bottom = centerPoint.y;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPointerPath = new Path();
        pointStartX = outerCircleRingRect.left + (outerCircleWidth >> 1) + innerCircleWidth;
        mPointerPath.moveTo(pointStartX + DensityUtil.dp2px(22), h - DensityUtil.dp2px(5));
        mPointerPath.lineTo(pointStartX + DensityUtil.dp2px(19), h - DensityUtil.dp2px(4f));
        mPointerPath.lineTo(pointStartX + DensityUtil.dp2px(22), h - DensityUtil.dp2px(3));
        mPointerPath.close();
    }

    //指针X轴起始坐标
    float pointStartX;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (DEBUG) {
            setBackgroundColor(0xff000000);
            DEBUG_PAINT.setStyle(Paint.Style.STROKE);
            canvas.drawRect(outerCircleRingRect, DEBUG_PAINT);
        } else {
            //背景色
            setBackgroundColor(Color.TRANSPARENT);
        }
        //开始角度
        int startAngle = 179;//向前偏移角度以填充满底图
        //结束角度
        int endAngle = 182;//向后偏移角度以填充满底图
        //总绘制角度
        float totalAngle = startAngle + endAngle - 180f;

        //外圆环向底部偏移，因outerCircleRingRect是一整个矩形，只需要画一半的圆，绘制圆环时需要向下偏移outerCircleRingRect的一半高度
        int circleRingOffsetToBottom = (int) (outerCircleRingRect.bottom / 2);
        //外圆环底部需要根据放大量来修正底部位置【外圆环位置底部  + 圆环向底部偏移 + 外圆环矩阵高度 * 放大量】
        float bottom = outerCircleRingRect.bottom + circleRingOffsetToBottom + outerCircleRingRect.height() * outerCircleRingEnlargePercent;
        //绘制外圆环底图
        outerCircleRingPaint.setColor(0x40ffffff);
        canvas.drawArc(
                outerCircleRingRect.left + (outerCircleWidth >> 1),
                outerCircleRingRect.top + (outerCircleWidth >> 1),
                outerCircleRingRect.right - (outerCircleWidth >> 1),
                bottom,
                startAngle, endAngle, false, outerCircleRingPaint);

        //绘制内圆环底图
        innerCircleRingPaint.setColor(0x40ffffff);
        canvas.drawArc(
                outerCircleRingRect.left + (outerCircleWidth >> 1) + innerCircleWidth + distanceBetweenOfOuterAndInnerCircleRingRadius,
                outerCircleRingRect.top + (outerCircleWidth >> 1) + innerCircleWidth + distanceBetweenOfOuterAndInnerCircleRingRadius,
                outerCircleRingRect.right - (outerCircleWidth >> 1) - innerCircleWidth - distanceBetweenOfOuterAndInnerCircleRingRadius,
                bottom,
                180, 180, false, innerCircleRingPaint);

        //绘制内圆环前景
        innerCircleRingPaint.setColor(Color.WHITE);
        canvas.drawArc(
                outerCircleRingRect.left + (outerCircleWidth >> 1) + innerCircleWidth + distanceBetweenOfOuterAndInnerCircleRingRadius,
                outerCircleRingRect.top + (outerCircleWidth >> 1) + innerCircleWidth + distanceBetweenOfOuterAndInnerCircleRingRadius,
                outerCircleRingRect.right - (outerCircleWidth >> 1) - innerCircleWidth - distanceBetweenOfOuterAndInnerCircleRingRadius,
                bottom,
                180, model.getRecommendDegree() * 180 * progress, false, innerCircleRingPaint);

        //绘制箭头
        canvas.save();
        canvas.rotate(model.getRecommendDegree() * 180 * progress, centerPoint.x, centerPoint.y);
        canvas.drawPath(mPointerPath, pointerPaint);
        canvas.restore();

        //冲起始角度
        float chongStartAngle = startAngle;
        float chongEndAngle = model.getSweepAngle(1, endAngle);
        chongEndAngle = Math.min(chongEndAngle, totalAngle);
        //绘制冲外圆环底图
        outerCircleRingPaint.setColor(0xffe9302d);
        canvas.drawArc(
                outerCircleRingRect.left + (outerCircleWidth >> 1),
                outerCircleRingRect.top + (outerCircleWidth >> 1),
                outerCircleRingRect.right - (outerCircleWidth >> 1),
                bottom,
                chongStartAngle, chongEndAngle, false, outerCircleRingPaint);

        //稳起始角度
        float wenStartAngle = chongStartAngle + chongEndAngle;
        float wenEndAngle = model.getSweepAngle(2, endAngle);
        wenEndAngle = Math.min(wenEndAngle, totalAngle);

        //绘制稳外圆环底图
        outerCircleRingPaint.setColor(0xff00ff00);
        canvas.drawArc(
                outerCircleRingRect.left + (outerCircleWidth >> 1),
                outerCircleRingRect.top + (outerCircleWidth >> 1),
                outerCircleRingRect.right - (outerCircleWidth >> 1),
                bottom,
                wenStartAngle, wenEndAngle, false, outerCircleRingPaint);


        //保起始角度
        float baoStartAngle = wenStartAngle + wenEndAngle;
        float baoEndAngle = model.getSweepAngle(3, endAngle);
        baoEndAngle = Math.min(baoEndAngle, totalAngle);

        //绘制保外圆环底图
        outerCircleRingPaint.setColor(0xff0000ff);
        canvas.drawArc(
                outerCircleRingRect.left + (outerCircleWidth >> 1),
                outerCircleRingRect.top + (outerCircleWidth >> 1),
                outerCircleRingRect.right - (outerCircleWidth >> 1),
                bottom,
                baoStartAngle, baoEndAngle, false, outerCircleRingPaint);

        ///////////////////////////
        //底部文字与底边之间的距离
        int textOffsetToBottom = DensityUtil.dp2px(3);

        //底部两侧文字与外圆环之间的距离
        int distanceBetweenOfBottomTextAndOuterCircleRing = distanceBetweenOfOuterAndInnerCircleRingRadius * 2;

        //底部左侧低点文本
        String textOfLowest = EmptyUtils.isEmpty(model.textOfLowest) ? "" : model.textOfLowest;
        textPaint.setTextSize(DensityUtil.dp2px(10f));
        textPaint.setTypeface(Typeface.DEFAULT);
        int[] textOfLowestSize = DrawViewUtils.getTextWH(textPaint, textOfLowest);
        canvas.drawText(textOfLowest, outerCircleRingRect.left - textOfLowestSize[0] / 2 - distanceBetweenOfBottomTextAndOuterCircleRing, outerCircleRingRect.bottom - textOffsetToBottom, textPaint);

        //底部右侧高点文本
        String textOfMaxest = EmptyUtils.isEmpty(model.textOfMaxest) ? "" : model.textOfMaxest;
        textPaint.setTextSize(DensityUtil.dp2px(10f));
        textPaint.setTypeface(Typeface.DEFAULT);
        int[] textOfMaxestSize = DrawViewUtils.getTextWH(textPaint, textOfMaxest);
        canvas.drawText(textOfMaxest, outerCircleRingRect.right + textOfMaxestSize[0] / 2 + distanceBetweenOfBottomTextAndOuterCircleRing, outerCircleRingRect.bottom - textOffsetToBottom, textPaint);

        ///////////////////////////👇计算中间文字👇///////////////////////////

        //中心上方百分比数字文本
        String textOfCenterUpperNumber = Math.round(model.getRecommendDegree() * progress * 100) + "";
        textPaint.setTextSize(DensityUtil.dp2px(40));
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        int[] textOfCenterUpperNumberSize = DrawViewUtils.getTextWH(textPaint, textOfCenterUpperNumber);

        //中心上方百分比符号文本
        String textOfCenterUpperPercent = "%";
        textPaint.setTextSize(DensityUtil.dp2px(12));
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        int[] textOfCenterUpperPercentSize = DrawViewUtils.getTextWH(textPaint, textOfCenterUpperPercent);

        //中心下方文本
        String textOfCenterLower = EmptyUtils.isEmpty(model.textOfCenterLower) ? "" : model.textOfCenterLower;
        textPaint.setTextSize(DensityUtil.dp2px(18));
        textPaint.setTypeface(Typeface.DEFAULT);
        int[] textOfCenterLowerSize = DrawViewUtils.getTextWH(textPaint, textOfCenterLower);
        ///////////////////////////👆计算中间文字👆///////////////////////////

        boolean toBottom = true;//是否贴底显示，如果是，则跟随矩阵底部


        //中心上方百分比数字文本
        float textOfCenterUpperNumberX = outerCircleRingRect.left + outerCircleRingRect.width() / 2 - textOfCenterUpperPercentSize[0];
        float textOfCenterUpperNumberY;
        if (toBottom) {
            textOfCenterUpperNumberY = outerCircleRingRect.bottom - textOfCenterUpperNumberSize[1] - textOfCenterLowerSize[1];
        } else {
            //垂直90时指针到中心点之间的距离
            float effectiveDistanceOfPointAndCenterPoint = outerCircleRingRect.left + outerCircleRingRect.width() / 2 - pointStartX;
            textOfCenterUpperNumberY = outerCircleRingRect.top + effectiveDistanceOfPointAndCenterPoint / 2 + textOfCenterUpperNumberSize[1] - textOfCenterLowerSize[1];
        }
        textPaint.setTextSize(DensityUtil.dp2px(40));
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText(textOfCenterUpperNumber, textOfCenterUpperNumberX, textOfCenterUpperNumberY, textPaint);


        //中心上方百分号文本
        textPaint.setTextSize(DensityUtil.dp2px(12));
        textPaint.setTypeface(Typeface.DEFAULT);
        float textOfCenterUpperPercentX = textOfCenterUpperNumberX + textOfCenterUpperNumberSize[0] / 2 + textOfCenterUpperPercentSize[0];
        float textOfCenterUpperPercentY = textOfCenterUpperNumberY;
        canvas.drawText(textOfCenterUpperPercent, textOfCenterUpperPercentX, textOfCenterUpperPercentY, textPaint);

        //中心下方文本
        float textOfCenterLowerX = outerCircleRingRect.left + outerCircleRingRect.width() / 2;
        float textOfCenterLowerY;
        if (toBottom) {
            textOfCenterLowerY = outerCircleRingRect.bottom - textOffsetToBottom;
        } else {
            textOfCenterLowerY = textOfCenterUpperNumberY + textOfCenterLowerSize[1] + DensityUtil.dp2px(10);
        }
        textPaint.setTextSize(DensityUtil.dp2px(18));
        textPaint.setTypeface(Typeface.DEFAULT);
        canvas.drawText(textOfCenterLower, textOfCenterLowerX, textOfCenterLowerY, textPaint);

        /////////////////////////////////////////////////////////////////////

        //半径(如果控件小于该值+冲稳保的最大文字尺寸，冲稳保文字注释将显示不全)
        int radius = (int) (distanceBetweenOfOuterAndInnerCircleRingRadius + outerCircleRingRect.width() / 2);


        getQuadrantPositionByAngle(canvas, model.chongText + "", 0, chongStartAngle, chongEndAngle, radius);
        getQuadrantPositionByAngle(canvas, model.wenText + "", 0, wenStartAngle, wenEndAngle, radius);
        getQuadrantPositionByAngle(canvas, model.baoText + "", 0, baoStartAngle, baoEndAngle, radius);


    }


    void getQuadrantPositionByAngle(Canvas canvas, String text, int offset, float startAngle, float sweepAngle, int radius) {

        //第一段折线长度
        int brokenLineFirstLineLength = DensityUtil.dp2px(10);

        textPaint.setTextSize(DensityUtil.dp2px(10));
        textPaint.setTypeface(Typeface.DEFAULT);
        int[] textSize = DrawViewUtils.getTextWH(textPaint, text);
        ///////////////////////////👇计算折线坐标👇///////////////////////////
        //折线第一段起点坐标
        Point firstStartPoint = DrawViewUtils.calculatePoint(centerPoint.x, centerPoint.y, radius, (startAngle + startAngle + sweepAngle) / 2);

        int angle = (int) ((startAngle + startAngle + sweepAngle) / 2);
        //折线第一段终点坐标
        Point firstEndPoint = null;
        int auadrant = DrawViewUtils.getQuadrantPositionByAngle(angle, 0);
        switch (auadrant) {
            case 1:
                firstEndPoint = DrawViewUtils.calculatePoint(centerPoint.x, centerPoint.y, radius + brokenLineFirstLineLength, angle);
                break;
            case 2:
                firstEndPoint = DrawViewUtils.calculatePoint(centerPoint.x, centerPoint.y, radius + brokenLineFirstLineLength, angle);
                break;
        }
        ///////////////////////////👆计算折线坐标👆///////////////////////////
        if (firstEndPoint != null) {
            brokenLinePaint.setStrokeWidth(DensityUtil.dp2px(1));
            brokenLinePaint.setColor(0x40ffffff);
            canvas.drawLine(firstStartPoint.x, firstStartPoint.y, firstEndPoint.x, firstEndPoint.y, brokenLinePaint);

            //折线第一段终点坐标
            if (auadrant == 1) {
                Point secondEndPoint = new Point(firstEndPoint.x + textSize[0], firstEndPoint.y);
                if (secondEndPoint != null) {
                    canvas.drawLine(firstEndPoint.x, firstEndPoint.y, secondEndPoint.x, secondEndPoint.y, brokenLinePaint);
                    canvas.drawText(text, firstEndPoint.x + textSize[0] / 2, firstEndPoint.y - textSize[1] / 2, textPaint);
                }
            } else if (auadrant == 2) {
                Point secondEndPoint = new Point(firstEndPoint.x - textSize[0], firstEndPoint.y);
                if (secondEndPoint != null) {
                    canvas.drawLine(firstEndPoint.x, firstEndPoint.y, secondEndPoint.x, secondEndPoint.y, brokenLinePaint);
                    canvas.drawText(text, firstEndPoint.x - textSize[0] / 2, firstEndPoint.y - textSize[1] / 2, textPaint);
                }
            }

        }
    }

    /**
     * debug画笔
     */
    Paint DEBUG_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 文字画笔
     */
    Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    {
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);
    }

    /**
     * 折线画笔
     */
    Paint brokenLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    {
        int separation = DensityUtil.dp2px(2);
        brokenLinePaint.setColor(0x40ffffff);
        brokenLinePaint.setStyle(Paint.Style.STROKE);
        brokenLinePaint.setDither(true);
        brokenLinePaint.setStrokeWidth(innerCircleWidth);
        brokenLinePaint.setPathEffect(new DashPathEffect(new float[]{separation * 2, separation}, 0));
    }

    /**
     * 内圆环画笔
     */
    Paint innerCircleRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    {
        int separation = DensityUtil.dp2px(3);
        innerCircleRingPaint.setStyle(Paint.Style.STROKE);
        innerCircleRingPaint.setDither(true);
        innerCircleRingPaint.setStrokeWidth(innerCircleWidth);
        innerCircleRingPaint.setPathEffect(new DashPathEffect(new float[]{separation, separation}, 0));
    }

    /**
     * 圆环画笔
     */
    private Paint outerCircleRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    {
        outerCircleRingPaint.setStyle(Paint.Style.STROKE);
        outerCircleRingPaint.setStrokeWidth(outerCircleWidth);
    }

    /**
     * 指针
     */
    Paint pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    {
        pointerPaint.setStrokeWidth(5);
        pointerPaint.setColor(Color.WHITE);
        pointerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        pointerPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * 指针路径
     */
    private Path mPointerPath = new Path();

    public static class SimpleRecommendDashboardViewBean {

        //冲
        int chong;
        //稳
        int wen;
        //保
        int bao;

        //冲文字说明
        String chongText;
        //稳文字说明
        String wenText;
        //保文字说明
        String baoText;

        //底部左侧低点文本
        String textOfLowest;
        //底部右侧高点文本
        String textOfMaxest;
        //中心上方百分比数字文本
        String textOfCenterUpperNumper;
        //中心下方文本
        String textOfCenterLower;

        public SimpleRecommendDashboardViewBean(int chong, int wen, int bao, String chongText, String wenText, String baoText, String textOfLowest, String textOfMaxest, String textOfCenterLower) {
            this.chong = chong;
            this.wen = wen;
            this.bao = bao;
            this.chongText = chong + chongText;
            this.wenText = wen + wenText;
            this.baoText = bao + baoText;
            this.textOfLowest = textOfLowest;
            this.textOfMaxest = textOfMaxest;
            this.textOfCenterLower = textOfCenterLower;
        }

        /**
         * 获取推荐度
         *
         * @return (冲 + 稳)/（冲+稳+保）
         */
        float getRecommendDegree() {
            float v1 = chong + wen;
            float v2 = chong + wen + bao;
            float r = v1 / v2;
            return r;
        }

        float getSweepAngle(int type, int TotalAngle) {
            if (type == 1) {
                return 1.0f * chong / (chong + wen + bao) * TotalAngle;
            } else if (type == 2) {
                return 1.0f * wen / (chong + wen + bao) * TotalAngle;
            } else if (type == 3) {
                return 1.0f * bao / (chong + wen + bao) * TotalAngle;
            }
            return 1;
        }
    }
}
