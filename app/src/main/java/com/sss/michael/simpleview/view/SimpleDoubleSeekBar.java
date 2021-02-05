package com.sss.michael.simpleview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author Michael by Administrator
 * @date 2021/2/3 9:28
 * @Description 一个简单的双头SeekBar
 */
@SuppressWarnings("ALL")
public class SimpleDoubleSeekBar extends View {
    /**
     * View宽度
     */
    private float width;
    /**
     * View高度
     */
    private float height;
    /**
     * 中心点
     */
    private PointF centerPoint = new PointF();
    /**
     * 当前左边坐标点
     */
    private PointF currentLeftPoint = new PointF();
    /**
     * 当前右边坐标点
     */
    private PointF currentRightPoint = new PointF();

    /**
     * 宽高比例
     */
    private float whPercent = 0.1f;
    /**
     * 画笔
     */
    private Paint paint = new Paint();

    {
        paint.setAntiAlias(true);
    }

    /**
     * 背景色
     */
    private int backgroundColor = Color.parseColor("#F2F2F2");
    /**
     * 背景区域
     */
    private RectF backgroundArea = new RectF();
    /**
     * 背景高度
     */
    private float backgroundHeight = DensityUtil.dp2px(4);
    /**
     * 前景色
     */
    private String[] foregroundColor = {"#FFBE9B", "#FF7573"};
    /**
     * 前景区域
     */
    private RectF foregroundArea = new RectF();
    /**
     * 前景高度
     */
    private float foregroundHeight = DensityUtil.dp2px(8);
    /**
     * 每百分之一的进度换算成对应总宽度的值
     */
    private float eachPercentByWidth;
    /**
     * 进度条每百分之一所对应的值1
     */
    private float eachPercentByValue;
    /**
     * 当前进度位置
     */
    private float currentMinPosition = 0f, currentMaxPosition = 100f;
    /**
     * 滑块半径
     */
    private int sliderRadius = DensityUtil.dp2px(13);
    /**
     * 滑块边框宽度
     */
    private int sliderStrokeWidth = DensityUtil.dp2px(1);
    /**
     * 滑块边框颜色
     */
    private int sliderStrokeColor = Color.parseColor("#E9302D");
    /**
     * 滑块内部颜色
     */
    private int sliderInnerColor = Color.parseColor("#FFFFFF");
    /**
     * 滑块条顶部文字
     */
    private String sliderLeftText = "", sliderRightText = "";
    /**
     * 滑块条顶部文字颜色
     */
    private int sliderTextColor = Color.parseColor("#212121");
    /**
     * 滑块条顶部文字大小
     */
    private float sliderTextSize = DensityUtil.sp2px(14f);
    /**
     * 滑块条顶部文字字体
     */
    private Typeface sliderTextStyle = Typeface.DEFAULT_BOLD;
    /**
     * 滑块条顶部文字与滑块条间距
     */
    private float distanceBetweenSliderTextAndSeek = DensityUtil.dp2px(5f);
    /**
     * 滑块条两边文字
     */
    private String sideLeftText = "", sideRightText = "";
    /**
     * 滑块条两边文字颜色
     */
    private int sideTextColor = Color.parseColor("#4A4A4A");
    /**
     * 滑块条两边文字大小
     */
    private float sideTextSize = DensityUtil.sp2px(12f);
    /**
     * 滑块条两边文字字体
     */
    private Typeface sideTextStyle = Typeface.DEFAULT;
    /**
     * 滑块条两边文字与滑块条间距
     */
    private float distanceBetweenSideTextAndSeek = DensityUtil.dp2px(10f);
    /**
     * 文字尺寸
     */
    private float[] sideLeftTextSize = new float[]{0, 0}, sideRightTextSize = new float[]{0, 0}, sliderLeftTextSize = new float[]{0, 0}, sliderRightTextSize = new float[]{0, 0};
    /**
     * 是否显示文字
     */
    private boolean isShowTextMode = true;

    private OnSimpleDoubleSeekBarCallBack onSimpleDoubleSeekBarCallBack;

    public void setOnSimpleDoubleSeekBarCallBack(OnSimpleDoubleSeekBarCallBack onSimpleDoubleSeekBarCallBack) {
        this.onSimpleDoubleSeekBarCallBack = onSimpleDoubleSeekBarCallBack;
    }

    public SimpleDoubleSeekBar(Context context) {
        this(context, null);
    }

    public SimpleDoubleSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleDoubleSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private List<RectF> rectFList = new ArrayList<>();


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        if (height == 0) {
            height = Math.round(width * whPercent);
            setMeasuredDimension(Math.round(width), Math.round(height));
        }
        centerPoint.set(width / 2, height / 2);

        paint.setTextSize(sideTextSize);
        paint.setTypeface(sideTextStyle);
        sideLeftTextSize = DrawViewUtils.getTextWHF(paint, sideLeftText);
        sideRightTextSize = DrawViewUtils.getTextWHF(paint, sideRightText);
        paint.setTextSize(sliderTextSize);
        paint.setTypeface(sliderTextStyle);
        sliderLeftTextSize = DrawViewUtils.getTextWHF(paint, sliderLeftText);
        sliderRightTextSize = DrawViewUtils.getTextWHF(paint, sliderRightText);


        if (isShowTextMode) {
            backgroundArea.left = distanceBetweenSideTextAndSeek + sideLeftTextSize[0];
            backgroundArea.right = width - distanceBetweenSideTextAndSeek - sideRightTextSize[0];
        } else {
            backgroundArea.left = 0;
            backgroundArea.right = width;
        }
        backgroundArea.top = centerPoint.y - backgroundHeight / 2;
        backgroundArea.bottom = centerPoint.y + backgroundHeight / 2;

        float totalEffectiveWidth = backgroundArea.width() - sliderRadius * 2;


        eachPercentByWidth = totalEffectiveWidth / 100;
        rectFList.clear();
        for (int i = 0; i < 100; i++) {
            RectF rectF = new RectF();
            if (isShowTextMode) {
                if (i == 0) {
                    rectF.left = backgroundArea.left + sliderRadius;
                    rectF.right = backgroundArea.left + eachPercentByWidth + sliderRadius;
                } else {
                    rectF.left = rectFList.get(rectFList.size() - 1).right;
                    rectF.right = rectFList.get(rectFList.size() - 1).right + eachPercentByWidth;
                }


            } else {
                rectF.left = (i == 0) ? backgroundArea.left : sliderRadius + eachPercentByWidth * (i + 1);
                rectF.right = rectF.left + eachPercentByWidth;
            }
            rectF.top = 0;
            rectF.bottom = height;
            rectFList.add(rectF);
        }

        calc();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //背景
        paint.setColor(backgroundColor);
        canvas.drawRoundRect(backgroundArea, backgroundHeight / 2, backgroundHeight / 2, paint);
        //前景
        paint.setShader(linearGradient);
        canvas.drawRoundRect(foregroundArea, foregroundHeight / 2, foregroundHeight / 2, paint);
        paint.setShader(null);
        //左滑块
        paint.setStrokeWidth(sliderStrokeWidth);
        paint.setColor(sliderStrokeColor);
        canvas.drawCircle(currentLeftPoint.x, currentLeftPoint.y, sliderRadius, paint);
        paint.setColor(sliderInnerColor);
        canvas.drawCircle(currentLeftPoint.x, currentLeftPoint.y, sliderRadius - sliderStrokeWidth, paint);
        //右滑块
        paint.setStrokeWidth(sliderStrokeWidth);
        paint.setColor(sliderStrokeColor);
        canvas.drawCircle(currentRightPoint.x, currentRightPoint.y, sliderRadius, paint);
        paint.setColor(sliderInnerColor);
        canvas.drawCircle(currentRightPoint.x, currentRightPoint.y, sliderRadius - sliderStrokeWidth, paint);
        if (isShowTextMode) {
            paint.setColor(sideTextColor);
            paint.setTextSize(sideTextSize);
            paint.setTypeface(sideTextStyle);
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(sideLeftText, backgroundArea.left - distanceBetweenSideTextAndSeek - sideLeftTextSize[0], centerPoint.y + sideLeftTextSize[1] / 3, paint);
            canvas.drawText(sideRightText, backgroundArea.left + backgroundArea.width() + distanceBetweenSideTextAndSeek, centerPoint.y + sideRightTextSize[1] / 3, paint);

            paint.setColor(sliderTextColor);
            paint.setTextSize(sliderTextSize);
            paint.setTypeface(sliderTextStyle);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(sliderLeftText, currentLeftPoint.x, currentLeftPoint.y - sliderLeftTextSize[1] - distanceBetweenSliderTextAndSeek, paint);
            canvas.drawText(sliderRightText, currentRightPoint.x, currentRightPoint.y - sliderLeftTextSize[1] - distanceBetweenSliderTextAndSeek, paint);
        }

    }

    /**
     * 当前左滑触摸是否有效
     */
    private boolean effectiveLeftTouch;
    /**
     * 当前右滑触摸是否有效
     */
    private boolean effectiveRightTouch;
    /**
     * 上一次X轴的触摸位置
     */
    private float lastX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                effectiveLeftTouch = isInclude(event.getX(), event.getY()) == 1;
                effectiveRightTouch = isInclude(event.getX(), event.getY()) == 2;
                if (effectiveLeftTouch || effectiveRightTouch) {
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                return effectiveLeftTouch || effectiveRightTouch;
            case MotionEvent.ACTION_MOVE:
                if (effectiveLeftTouch || effectiveRightTouch) {
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (effectiveLeftTouch) {
                    float widthDiffer = event.getX() - lastX;
                    //当前X轴方向微调相对于一格（等宽/100格）的百分比
                    float percent = Math.abs(widthDiffer / eachPercentByWidth);
                    lastX = event.getX();
                    for (int i = 0; i < rectFList.size(); i++) {
                        if (rectFList.get(i).contains(event.getX(), event.getY())) {
                            if (isShowTextMode) {
                                Log.e("SSSSS", i + "");
                                currentMinPosition = i == 0 ? percent : (i + 1 + percent);
                            } else {
                                currentMinPosition = i == 0 ? 0 : (i + 1 + percent);
                            }
                            calc();
                            invalidate();
                            break;
                        }
                    }
                } else if (effectiveRightTouch) {
                    float widthDiffer = event.getX() - lastX;
                    //当前X轴方向微调相对于一格（等宽/100格）的百分比
                    float percent = Math.abs(widthDiffer / eachPercentByWidth);
                    lastX = event.getX();
                    for (int i = 0; i < rectFList.size(); i++) {
                        if (rectFList.get(i).contains(event.getX(), event.getY())) {
                            currentMaxPosition = i == rectFList.size() - 1 ? 100 : (i + 1 + percent);
                            calc();
                            invalidate();
                            break;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                effectiveLeftTouch = false;
                effectiveRightTouch = false;
            default:
        }
        if (effectiveLeftTouch || effectiveRightTouch) {
            return true;
        }
        return super.onTouchEvent(event);
    }


    /**
     * 判断落点是否在滑块上
     *
     * @return 左滑块返回1 右滑块返回2 不在返回-1
     */
    private int isInclude(float x, float y) {
        if (x > currentLeftPoint.x - sliderRadius && y > currentLeftPoint.y - sliderRadius && x < currentLeftPoint.x + sliderRadius && y < currentLeftPoint.y + sliderRadius) {
            return 1;
        }
        if (x > currentRightPoint.x - sliderRadius && y > currentRightPoint.y - sliderRadius && x < currentRightPoint.x + sliderRadius && y < currentRightPoint.y + sliderRadius) {
            return 2;
        }
        return -1;
    }

    /**
     * 左右滑块碰撞纠正
     */
    public void correctPosition() {
        if (isShowTextMode) {
            currentLeftPoint.set(eachPercentByWidth * currentMinPosition + sliderRadius + sideLeftTextSize[0] + distanceBetweenSideTextAndSeek, height / 2);
            currentRightPoint.set(eachPercentByWidth * currentMaxPosition + sliderRadius + sideLeftTextSize[0] + distanceBetweenSideTextAndSeek, height / 2);
        } else {
            currentLeftPoint.set(eachPercentByWidth * currentMinPosition + sliderRadius, height / 2);
            currentRightPoint.set(eachPercentByWidth * currentMaxPosition + sliderRadius, height / 2);
        }
        //实时最大值出界纠正
        if (currentMaxPosition > 100f && effectiveRightTouch) {
            currentMaxPosition -= 0.01f;
            if (currentMaxPosition >= 99.99f) {
                currentMaxPosition = 100f;
            }
            correctPosition();
        }

        //实时最小值
        if (currentMinPosition < 0f && effectiveLeftTouch) {
            currentMinPosition += 0.01f;
            if (currentMinPosition <= 0.1f) {
                currentMinPosition = 0f;
            }
            correctPosition();
        }

        if (currentLeftPoint.x + sliderRadius > currentRightPoint.x - sliderRadius && effectiveLeftTouch) {
            //左边滑块的最右侧大于右边滑块的最左侧
            currentMinPosition -= 0.1f;
            correctPosition();
        } else if (currentRightPoint.x - sliderRadius < currentLeftPoint.x + sliderRadius && effectiveRightTouch) {
            //右边滑块的最左侧小于左边滑块的最右侧
            currentMaxPosition += 0.1f;
            correctPosition();
        }
        sliderLeftText = String.valueOf(Math.round(currentMinPosition * eachPercentByValue) + minValue);
        sliderRightText = String.valueOf(Math.round(currentMaxPosition * eachPercentByValue) + minValue);
        sideLeftText = String.valueOf(minValue);
        sideRightText = String.valueOf(maxValue);
    }

    private LinearGradient linearGradient;

    /**
     * 计算各组件绘制位置
     */
    private void calc() {
        correctPosition();
        if (onSimpleDoubleSeekBarCallBack != null) {
            onSimpleDoubleSeekBarCallBack.onValueChanged(Math.round(currentMinPosition * eachPercentByValue) + minValue, Math.round(currentMaxPosition * eachPercentByValue) + minValue, currentMinPosition, currentMaxPosition);
        }
        foregroundArea.left = currentLeftPoint.x;
        foregroundArea.top = centerPoint.y - foregroundHeight / 2;
        foregroundArea.right = currentRightPoint.x;
        foregroundArea.bottom = centerPoint.y + foregroundHeight / 2;

        int[] colors = new int[foregroundColor.length];
        for (int i = 0; i < foregroundColor.length; i++) {
            colors[i] = Color.parseColor(foregroundColor[i]);
        }
        linearGradient = new LinearGradient(
                foregroundArea.left,
                foregroundArea.top + foregroundArea.height() / 2,
                foregroundArea.left + foregroundArea.width(),
                backgroundHeight / 2, colors,
                null,
                Shader.TileMode.MIRROR
        );
    }

    int currentMinValue, currentMaxValue;
    int minValue, maxValue;

    /**
     * 设置数据
     *
     * @param mirroring       镜像模式，整个滑块条反转
     * @param currentMinValue 左滑块当前值
     * @param currentMaxValue 右滑块当前值
     * @param minValue        最小值
     * @param maxValue        最大值
     */
    public void setData(boolean mirroring, int currentMinValue, int currentMaxValue, int minValue, int maxValue) {
        if (mirroring) {
            this.currentMinValue = currentMaxValue;
            this.currentMaxValue = currentMinValue;
            this.minValue = maxValue;
            this.maxValue = minValue;
        } else {
            this.currentMinValue = currentMinValue;
            this.currentMaxValue = currentMaxValue;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        eachPercentByValue = (this.maxValue - this.minValue) / 100f;

        currentMinPosition = (this.currentMinValue - this.minValue) / eachPercentByValue;
        currentMaxPosition = (this.currentMaxValue - this.minValue) / eachPercentByValue;
        calc();
        invalidate();
    }

    public interface OnSimpleDoubleSeekBarCallBack {
        void onValueChanged(int currentMinValue, int currentMaxValue, float currentMinPosition, float currentMaxPosition);
    }
}
