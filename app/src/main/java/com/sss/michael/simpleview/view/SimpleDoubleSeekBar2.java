package com.sss.michael.simpleview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

import com.sss.michael.simpleview.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author Michael by Administrator
 * @date 2021/2/3 9:28
 * @Description 一个简单的双头SeekBar
 */
@SuppressWarnings("ALL")
public class SimpleDoubleSeekBar2 extends View {
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
    private String[] foregroundColor = {"#E9302D", "#FF5053"};
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
    private int sliderRadius = DensityUtil.dp2px(9);
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
    private int sliderInnerColor = Color.parseColor("#E9302D");
    /**
     * 滑块条中间文字
     */
    private String sliderLeftText = "", sliderRightText = "";
    /**
     * 滑块条中间文字颜色
     */
    private int sliderTextColor = Color.parseColor("#FFFFFF");
    /**
     * 滑块条中间文字大小
     */
    private float sliderTextSize = DensityUtil.sp2px(10f);
    /**
     * 滑块条中间文字字体
     */
    private Typeface sliderTextStyle = Typeface.DEFAULT_BOLD;
    /**
     * 滑块条中间文字与滑块条间距
     */
    private float distanceBetweenSliderTextAndSeek = DensityUtil.dp2px(5f);
    /**
     * 滑块区域
     */
    private RectF leftSliderRect = new RectF(), rightSliderRect = new RectF();
    /**
     * 是否浮动文字
     */
    private boolean isShowFloatText = true;
    /**
     * 是否显示滑块条中间文字
     */
    private boolean isShowSliderText = true;
    /**
     * 左滑块长度
     */
    private int leftSlideLength = DensityUtil.dp2px(7.5f);
    /**
     * 右滑块长度
     */
    private int rightSlideLength = DensityUtil.dp2px(7.5f);

    private OnSimpleDoubleSeekBar2CallBack onSimpleDoubleSeekBarCallBack;

    public void setOnSimpleDoubleSeekBarCallBack(OnSimpleDoubleSeekBar2CallBack onSimpleDoubleSeekBarCallBack) {
        this.onSimpleDoubleSeekBarCallBack = onSimpleDoubleSeekBarCallBack;
    }

    public SimpleDoubleSeekBar2(Context context) {
        this(context, null);
    }

    public SimpleDoubleSeekBar2(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleDoubleSeekBar2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

        paint.setTextSize(sliderTextSize);
        paint.setTypeface(sliderTextStyle);


        backgroundArea.left = 0;
        backgroundArea.right = width;
        backgroundArea.top = centerPoint.y - backgroundHeight / 2;
        backgroundArea.bottom = centerPoint.y + backgroundHeight / 2;

        float totalEffectiveWidth = backgroundArea.width() - sliderRadius * 2;


        eachPercentByWidth = totalEffectiveWidth / 100;
        rectFList.clear();
        for (int i = 0; i < 100; i++) {
            RectF rectF = new RectF();
            if (i == 0) {
                rectF.left = backgroundArea.left + sliderRadius;
                rectF.right = backgroundArea.left + eachPercentByWidth + sliderRadius;
            } else {
                rectF.left = rectFList.get(rectFList.size() - 1).right;
                rectF.right = rectFList.get(rectFList.size() - 1).right + eachPercentByWidth;
            }
            rectF.top = 0;
            rectF.bottom = height;
            rectFList.add(rectF);
        }

        calc(false);
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
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(sliderInnerColor);
        canvas.drawRoundRect(leftSliderRect, sliderRadius, sliderRadius, paint);

        //右滑块
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(sliderInnerColor);
        canvas.drawRoundRect(rightSliderRect, sliderRadius, sliderRadius, paint);


        if (isShowSliderText) {
            paint.setColor(sliderTextColor);
            paint.setTextSize(sliderTextSize);
            paint.setTypeface(sliderTextStyle);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(sliderLeftText, currentLeftPoint.x + leftSlideLength, currentLeftPoint.y + DensityUtil.dp2px(2.5f), paint);
            canvas.drawText(sliderRightText, currentRightPoint.x - rightSlideLength, currentRightPoint.y + DensityUtil.dp2px(2.5f), paint);
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
                            currentMinPosition = i == 0 ? percent : (i + 1 + percent);
                            calc(true);
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
                            calc(true);
                            invalidate();
                            break;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                effectiveLeftTouch = false;
                effectiveRightTouch = false;
                calc(true);
                break;
            default:
                effectiveLeftTouch = false;
                effectiveRightTouch = false;

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
        if (x > currentLeftPoint.x - sliderRadius - leftSlideLength && y > currentLeftPoint.y - sliderRadius && x < currentLeftPoint.x + sliderRadius + leftSlideLength && y < currentLeftPoint.y + sliderRadius) {
            return 1;
        }
        if (x > currentRightPoint.x - sliderRadius - rightSlideLength && y > currentRightPoint.y - sliderRadius && x < currentRightPoint.x + sliderRadius + rightSlideLength && y < currentRightPoint.y + sliderRadius) {
            return 2;
        }
        return -1;
    }

    /**
     * 左右滑块碰撞纠正
     */
    public void correctPosition() {
        currentLeftPoint.set(eachPercentByWidth * currentMinPosition + sliderRadius, height / 2);
        currentRightPoint.set(eachPercentByWidth * currentMaxPosition + sliderRadius, height / 2);
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
    }

    private LinearGradient linearGradient;
    private PopupWindow popWindow;

    /**
     * 计算各组件绘制位置
     */
    private void calc(boolean touchFromUser) {
        correctPosition();
        if (onSimpleDoubleSeekBarCallBack != null) {
            if (popWindow == null) {
                popWindow = new PopupWindow(this);
                popWindow.setContentView(new SimplePopView(getContext()));
                popWindow.setWidth(DensityUtil.dp2px(40));
                popWindow.setHeight(DensityUtil.dp2px(30));
                popWindow.getContentView().setVisibility(touchFromUser ? VISIBLE : INVISIBLE);
            }
            popWindow.dismiss();
            if (effectiveLeftTouch) {
                popWindow.getContentView().setVisibility(VISIBLE);
                popWindow.showAsDropDown(this, (int) currentLeftPoint.x - popWindow.getContentView().getWidth() / 2 + leftSlideLength, 0 - popWindow.getContentView().getHeight() - getHeight());
            } else if (effectiveRightTouch) {
                popWindow.getContentView().setVisibility(VISIBLE);
                popWindow.showAsDropDown(this, (int) currentRightPoint.x - popWindow.getContentView().getWidth() / 2 - rightSlideLength, 0 - popWindow.getContentView().getHeight() - getHeight());
            } else {
                popWindow.getContentView().setVisibility(INVISIBLE);
            }
            onSimpleDoubleSeekBarCallBack.onValueChanged(Math.round(currentMinPosition * eachPercentByValue) + minValue, Math.round(currentMaxPosition * eachPercentByValue) + minValue, currentMinPosition, currentMaxPosition);
        }
        foregroundArea.left = currentLeftPoint.x;
        foregroundArea.top = centerPoint.y - foregroundHeight / 2;
        foregroundArea.right = currentRightPoint.x;
        foregroundArea.bottom = centerPoint.y + foregroundHeight / 2;

        leftSliderRect.set(
                Math.max(currentLeftPoint.x - leftSlideLength - sliderRadius + leftSlideLength, 0),
                currentLeftPoint.y - DensityUtil.dp2px(1) - sliderRadius,
                currentLeftPoint.x + leftSlideLength + sliderRadius + leftSlideLength,
                currentLeftPoint.y + DensityUtil.dp2px(1) + sliderRadius

        );

        rightSliderRect.set(
                currentRightPoint.x - rightSlideLength - sliderRadius - rightSlideLength,
                currentRightPoint.y - DensityUtil.dp2px(1) - sliderRadius,
                currentRightPoint.x + rightSlideLength + sliderRadius - rightSlideLength,
                currentRightPoint.y + DensityUtil.dp2px(1) + sliderRadius

        );

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
        if (maxValue < minValue || currentMaxValue < currentMinValue) {
            return;
        }
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
        calc(false);
        invalidate();
    }

    public interface OnSimpleDoubleSeekBar2CallBack {
        void onValueChanged(int currentMinValue, int currentMaxValue, float currentMinPosition, float currentMaxPosition);
    }


    public class SimplePopView extends View {
        private int color = Color.parseColor("#aa000000");
        private Path path = new Path();
        private Paint paint = new Paint();

        {
            paint.setAntiAlias(true);
        }

        private int triangleHeight = DensityUtil.dp2px(4);
        private int width = DensityUtil.dp2px(40);
        private int height = DensityUtil.dp2px(30);

        public SimplePopView(Context context) {
            super(context);
        }

        public SimplePopView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public SimplePopView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            setMeasuredDimension(width, height);
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            paint.setColor(color);
            canvas.drawRoundRect(0, 0, width, height - triangleHeight, height - triangleHeight, height - triangleHeight, paint);
            path.reset();
            path.moveTo(getWidth() / 2 - triangleHeight, height - triangleHeight);
            path.lineTo(getWidth() / 2 - triangleHeight, height - triangleHeight);
            path.lineTo(getWidth() / 2 + triangleHeight, height - triangleHeight);
            path.lineTo(getWidth() / 2, height);
            path.lineTo(getWidth() / 2 - triangleHeight, height - triangleHeight);
            path.close();
            canvas.drawPath(path, paint);
            paint.setTextSize(DensityUtil.sp2px(12f));
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            if (isInTouchMode()) {
                if (effectiveLeftTouch) {
                    canvas.drawText(String.valueOf(Math.round(currentMinPosition * eachPercentByValue) + minValue), width / 2, height / 2, paint);
                } else {
                    canvas.drawText(String.valueOf(Math.round(currentMaxPosition * eachPercentByValue) + minValue), width / 2, height / 2, paint);
                }

            }
        }

    }
}
