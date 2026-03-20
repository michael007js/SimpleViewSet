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
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;
import com.sss.michael.simpleview.utils.EmptyUtils;

@SuppressWarnings("all")
public class SimpleRecommendDashboardViewV2 extends View {
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
    private float outerCircleRingEnlargePercent = 0.15f;
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


    private SimpleRecommendDashboardViewV2.SimpleRecommendDashboardViewBean model;

    public void setModel(SimpleRecommendDashboardViewV2.SimpleRecommendDashboardViewBean model, boolean animation) {
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

    public SimpleRecommendDashboardViewV2(Context context) {
        this(context, null);
    }

    public SimpleRecommendDashboardViewV2(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleRecommendDashboardViewV2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (DEBUG) {
            SimpleRecommendDashboardViewV2.SimpleRecommendDashboardViewBean model = new SimpleRecommendDashboardViewV2.SimpleRecommendDashboardViewBean(
                    0.5f, 90, "0", "999", "建议拨打报警电话");
            setModel(model, true);
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setModel(model, true);
                }
            });
        }
    }

    /**
     * 向上的偏移量,用来控制整体向上绘制偏移
     */
    int offsetToUp = DensityUtil.dp2px(20);

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);

        offsetToUp = DensityUtil.dp2px(20);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (MeasureSpec.EXACTLY == MeasureSpec.getMode(heightMeasureSpec)) {
            height = heightSize;
            offsetToUp = DensityUtil.dp2px(20);
        } else {
            height = DensityUtil.dp2px(200);
            offsetToUp = height / 5;
        }
        setMeasuredDimension(width, height);

        int centerX = width / 2;
        int centerY = height - offsetToUp;

        centerPoint.set(centerX, centerY);

        int value = Math.min(width, height);
        outerCircleRingRect.left = centerX - (value >> 1) - outerCircleRingEnlargePercent * value + DensityUtil.dp2px(10);
        outerCircleRingRect.top = centerY - (value >> 1) - outerCircleRingEnlargePercent * value;
        outerCircleRingRect.right = centerX + (value >> 1) + outerCircleRingEnlargePercent * value - DensityUtil.dp2px(10);
        outerCircleRingRect.bottom = centerY;
    }


    //指针X轴起始坐标
    float pointStartX;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (model == null) {
            return;
        }
        if (DEBUG) {
            setBackgroundColor(0xff12345f);
            DEBUG_PAINT.setStyle(Paint.Style.STROKE);
            canvas.drawRect(outerCircleRingRect, DEBUG_PAINT);
        } else {
            //背景色
            setBackgroundColor(Color.TRANSPARENT);
        }
        //开始角度
        int startAngle = 180;//向前偏移角度以填充满底图
        //结束角度
        int maxSweepAngle = 180;//向后偏移角度以填充满底图
        //总绘制角度
        float totalAngle = startAngle + maxSweepAngle - 180f;

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
                startAngle, maxSweepAngle, false, outerCircleRingPaint);

        //绘制内圆环底图
        innerCircleRingPaint.setColor(0x40ffffff);
        canvas.drawArc(
                outerCircleRingRect.left + (outerCircleWidth >> 1) + innerCircleWidth + distanceBetweenOfOuterAndInnerCircleRingRadius,
                outerCircleRingRect.top + (outerCircleWidth >> 1) + innerCircleWidth + distanceBetweenOfOuterAndInnerCircleRingRadius,
                outerCircleRingRect.right - (outerCircleWidth >> 1) - innerCircleWidth - distanceBetweenOfOuterAndInnerCircleRingRadius,
                bottom - distanceBetweenOfOuterAndInnerCircleRingRadius,
                startAngle, maxSweepAngle, false, innerCircleRingPaint);

        //冲起始角度
        float chongStartAngle = startAngle;
        float chongEndAngle = model.current * 180 * progress;
        chongEndAngle = Math.min(chongEndAngle, totalAngle);
        //绘制冲外圆环底图
        outerCircleRingPaint.setColor(0xffd8d8d8);
        canvas.drawArc(
                outerCircleRingRect.left + (outerCircleWidth >> 1),
                outerCircleRingRect.top + (outerCircleWidth >> 1),
                outerCircleRingRect.right - (outerCircleWidth >> 1),
                bottom,
                chongStartAngle, chongEndAngle, false, outerCircleRingPaint);


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
        String textOfCenterUpperNumber;
        if (model.current > 0) {
            textOfCenterUpperNumber = Math.round(model.current * model.centerTotal * progress) + "";
        } else {
            textOfCenterUpperNumber = "--";
        }


        textPaint.setTextSize(DensityUtil.dp2px(40));
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        int[] textOfCenterUpperNumberSize = DrawViewUtils.getTextWH(textPaint, textOfCenterUpperNumber);

        //中心上方百分比符号文本
        String textOfCenterUpperPercent = "";
        textPaint.setTextSize(DensityUtil.dp2px(12));
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        int[] textOfCenterUpperPercentSize = DrawViewUtils.getTextWH(textPaint, textOfCenterUpperPercent);

        //中心下方文本
        String textOfCenterLower = EmptyUtils.isEmpty(model.textOfCenterLower) ? "" : model.textOfCenterLower;
        textPaint.setTextSize(DensityUtil.dp2px(18));
        textPaint.setTypeface(Typeface.DEFAULT);
        int[] textOfCenterLowerSize = DrawViewUtils.getTextWH(textPaint, textOfCenterLower);
        ///////////////////////////👆计算中间文字👆///////////////////////////

        //是否贴底显示，如果是，则跟随矩阵底部
        boolean toBottom = true;


        //中心上方百分比数字文本
        float textOfCenterUpperNumberX = outerCircleRingRect.left + outerCircleRingRect.width() / 2 - textOfCenterUpperPercentSize[0];
        float textOfCenterUpperNumberY;
        if (model.current == 0) {
            //居中
            textOfCenterUpperNumberY = outerCircleRingRect.top + outerCircleRingRect.height() / 2 + textOfCenterLowerSize[1];
        } else {
            if (toBottom) {
                textOfCenterUpperNumberY = outerCircleRingRect.bottom - textOfCenterUpperNumberSize[1] - textOfCenterLowerSize[1];
            } else {
                //垂直90时指针到中心点之间的距离
                float effectiveDistanceOfPointAndCenterPoint = outerCircleRingRect.left + outerCircleRingRect.width() / 2 - pointStartX;
                textOfCenterUpperNumberY = outerCircleRingRect.top + effectiveDistanceOfPointAndCenterPoint / 2 + textOfCenterUpperNumberSize[1] - textOfCenterLowerSize[1];
            }
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


        float current;

        int centerTotal;
        //底部左侧低点文本
        String textOfLowest;
        //底部右侧高点文本
        String textOfMaxest;
        //中心下方文本
        String textOfCenterLower;

        float percent;

        public SimpleRecommendDashboardViewBean(float percent, int centerTotal, String textOfLowest, String textOfMaxest, String textOfCenterLower) {
            this.current = percent;
            this.centerTotal = centerTotal;
            this.textOfLowest = textOfLowest;
            this.textOfMaxest = textOfMaxest;
            this.textOfCenterLower = textOfCenterLower;
        }

    }
}