package com.sss.michael.simpleview.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.sss.michael.simpleview.R;
import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;
import com.sss.michael.simpleview.utils.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE;

/**
 * @author Michael by 61642
 * @date 2024/2/28 10:01
 * @Description 一个简单的圆角tab(支持四边圆角, 带底部圆角异形)
 */
public class SimpleRoundTabViewV3 extends View {
    private boolean DEBUG;
    /**
     * 四个顶点圆角半径
     */
    private float topLeftRadius = DensityUtil.dp2px(16);
    private float topRightRadius = DensityUtil.dp2px(16);
    private float bottomLeftRadius = 0f;
    private float bottomRightRadius = 0f;

    /**
     * 底部反圆角
     */
    private float reverseRadius = DensityUtil.dp2px(16);

    /**
     * 每一个矩阵的宽度
     */
    private int eachRectWidth;
    /**
     * 背景矩阵
     */
    private RectF backgroundRectF = new RectF();
    /**
     * 背景色
     */
    private int backgroundColor = Color.parseColor("#EEF8FF");
    /**
     * 遮罩色
     */
    private int maskColor = Color.parseColor("#ffffff");
    /**
     * 遮罩矩阵
     */
    private RectF maskRectF = new RectF();
    /**
     * 底部指示线矩阵
     */
    private RectF lineRectF = new RectF();
    /**
     * 内部矩阵与背景之间的间距
     */
    private int distance = DensityUtil.dp2px(0);
    /**
     * 是否绘制底部指示横线
     */
    private boolean drawIndicator;
    /**
     * 底部指示横线的高度
     */
    private int indicatorHeight = DensityUtil.dp2px(3);
    /**
     * 底部指示横线的宽度
     */
    private int indicatorWidth = DensityUtil.dp2px(20);
    /**
     * 底部指示横线的颜色
     */
    private int indicatorColor = Color.parseColor("#e9302d");

    /**
     * 底部指示横线背景矩阵
     */
    private RectF indicatorBackgroundRectF = new RectF();
    /**
     * 底部指示横线背景色
     */
    private int indicatorBackgroundColor = maskColor;
    /**
     * 画笔
     */
    private Paint paint = new Paint();
    /**
     * 画笔
     */
    private Paint maskPaint = new Paint();
    /**
     * 绘制指示器
     */
    private Bitmap indicatorBitmap;

    /**
     * 底部覆盖矩阵高度
     */
    private int bottomCoverageRectFHeight;

    /**
     * 底部覆盖矩阵颜色
     */
    private int bottomCoverageRectFColor;
    /**
     * 底部覆盖矩阵(用于解决底部一行细线)
     */
    private RectF bottomCoverageRectF = new RectF();

    {
        paint.setAntiAlias(true);
        maskPaint.setAntiAlias(true);
    }

    /**
     * 宽高
     */
    private int width, height;
    /**
     * tab列表
     */
    private List<SimpleRoundTabBean> list = new ArrayList();
    Path path = new Path();
    private OnSimpleRoundTabViewCallBack onSimpleRoundTabViewCallBack;

    public void setOnSimpleRoundTabViewV3CallBack(OnSimpleRoundTabViewCallBack onSimpleRoundTabViewCallBack) {
        this.onSimpleRoundTabViewCallBack = onSimpleRoundTabViewCallBack;
    }

    public SimpleRoundTabViewV3(Context context) {
        this(context, null);
    }

    public SimpleRoundTabViewV3(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    int gradientMaskColor[] = {};
    float gradientMaskPosition[] = {};

    public SimpleRoundTabViewV3(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SimpleRoundTabViewV3);
        backgroundColor = array.getColor(R.styleable.SimpleRoundTabViewV3_srtv3_backgroundColor, Color.parseColor("#EEF8FF"));
        maskColor = array.getColor(R.styleable.SimpleRoundTabViewV3_srtv3_maskColor, Color.parseColor("#ffffff"));
        bottomCoverageRectFHeight = array.getDimensionPixelOffset(R.styleable.SimpleRoundTabViewV3_srtv3_bottomCoverageRectFHeight, 0);
        bottomCoverageRectFColor = array.getColor(R.styleable.SimpleRoundTabViewV3_srtv3_bottomCoverageRectFColor, Color.parseColor("#00000000"));

        try {
            String gradientColor = array.getString(R.styleable.SimpleRoundTabViewV3_srtv3_gradientMaskColor);
            String gradientPositions = array.getString(R.styleable.SimpleRoundTabViewV3_srtv3_gradientMaskPositions);
            String colorArrays[] = gradientColor.split(",");
            String maskArrays[] = gradientPositions.split(",");
            if (colorArrays.length == maskArrays.length) {
                gradientMaskColor = new int[colorArrays.length];
                gradientMaskPosition = new float[maskArrays.length];
                for (int i = 0; i < colorArrays.length; i++) {
                    gradientMaskColor[i] = Color.parseColor(colorArrays[i]);
                }
                for (int i = 0; i < maskArrays.length; i++) {
                    gradientMaskPosition[i] = Float.parseFloat(maskArrays[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        array.recycle();
        if (DEBUG) {
            for (int i = 0; i < 3; i++) {
                SimpleRoundTabBean simpleRoundTabBean = new SimpleRoundTabBean();
                simpleRoundTabBean.text = "标签No." + (i + 1);
                simpleRoundTabBean.checked = i == 0;
                list.add(simpleRoundTabBean);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int maxTextHeight = 0;
        for (int i = 0; i < list.size(); i++) {
            maxTextHeight = Math.max(maxTextHeight, list.get(i).getSize()[1]);
        }
        switch (widthMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                width = widthSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                width = DensityUtil.dp2px(100);
                break;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                height = maxTextHeight + getPaddingTop() + getPaddingBottom();
                break;
        }


        backgroundRectF.left = 0;
        backgroundRectF.top = 0;
        backgroundRectF.right = width;
        backgroundRectF.bottom = height;

        indicatorBackgroundRectF.left = backgroundRectF.left;
        indicatorBackgroundRectF.right = backgroundRectF.right;
        indicatorBackgroundRectF.top = backgroundRectF.bottom - (drawIndicator ? indicatorHeight : DensityUtil.dp2px(1)/*这里的2是为了突出遮罩底部反向圆角*/);
        indicatorBackgroundRectF.bottom = backgroundRectF.bottom;

        if (list.size() > 0) {
            // 等分每个tab的宽度
            eachRectWidth = (width - distance * 2) / list.size();
            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {
                    list.get(i).rectF.left = backgroundRectF.left + distance;
                } else {
                    list.get(i).rectF.left = list.get(i - 1).rectF.right;
                }
                list.get(i).rectF.top = distance;
                list.get(i).rectF.bottom = indicatorBackgroundRectF.bottom - (drawIndicator ? indicatorHeight : DensityUtil.dp2px(1)/*这里的2是为了突出遮罩底部反向圆角*/);


                if (i == 0) {
                    list.get(i).rectF.right = list.get(i).rectF.left + eachRectWidth;
                } else {
                    list.get(i).rectF.right = list.get(i - 1).rectF.right + eachRectWidth;
                }
            }
        }
        bottomCoverageRectF.bottom = height;
        bottomCoverageRectF.left = 0;
        bottomCoverageRectF.top = bottomCoverageRectF.bottom - bottomCoverageRectFHeight;
        bottomCoverageRectF.right = width;
        setMeasuredDimension(width, height);
    }

    public boolean backgroundRadius = true;

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        invalidate();
    }

    public void setBackgroundRadius(boolean backgroundRadius) {
        this.backgroundRadius = backgroundRadius;
        invalidate();
    }

    public void setDrawIndicator(boolean drawIndicator) {
        this.drawIndicator = drawIndicator;
        invalidate();
    }
    /**
     * tab栏显示红点
     */
    public void setRedPoint(boolean[] state) {
        if (state.length != list.size()) {
            return;
        }
        try {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).showRedPoint = state[i];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        invalidate();
    }

    public void setIndicatorBitmap(Bitmap indicatorBitmap) {
        this.indicatorBitmap = indicatorBitmap;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制背景
        paint.setColor(backgroundColor);
        float[] radii = getCornerRadii();

        path.reset();
        if (backgroundRadius) {
            path.addRoundRect(backgroundRectF, radii, Path.Direction.CW);
        } else {
            canvas.drawRect(backgroundRectF, paint);
        }
        canvas.drawPath(path, paint);
        paint.setColor(indicatorBackgroundColor);
        canvas.drawRect(indicatorBackgroundRectF, paint);

        // 绘制遮罩
        for (SimpleRoundTabBean bean : list) {
            if (bean.checked) {
                if (!animationIsRunning) {
                    maskRectF.top = bean.rectF.top;
                    maskRectF.bottom = bean.rectF.bottom;
                }
                maskPaint.setColor(maskColor);
                if (maskPaint.getShader() == null && gradientMaskColor.length == gradientMaskPosition.length && gradientMaskColor.length > 0) {
                    maskPaint.setShader(new LinearGradient(
                            maskRectF.left + maskRectF.width() / 2,
                            0,
                            maskRectF.left + maskRectF.width() / 2,
                            getHeight(),
                            gradientMaskColor,
                            gradientMaskPosition,
                            Shader.TileMode.CLAMP
                    ));
                }
                path.reset();
                path.addRoundRect(maskRectF, radii, Path.Direction.CW);
                canvas.drawPath(path, maskPaint);
                if (indicatorBitmap != null) {
                    // 绘制指示器图片
                    canvas.drawBitmap(
                            indicatorBitmap,
                            maskRectF.left + maskRectF.width() / 2 - (float) indicatorBitmap.getWidth() / 2,
                            maskRectF.top + maskRectF.height() / 2 + (bean.getSize()[1] >> 1),
                            paint);
                }
                break;
            }
        }
        // 绘制遮罩左侧反向圆角
        if (maskRectF.left > reverseRadius) {
            path.reset();
            path.moveTo(maskRectF.left, maskRectF.bottom - reverseRadius);
            path.cubicTo(maskRectF.left, maskRectF.bottom - reverseRadius,
                    maskRectF.left, maskRectF.bottom,
                    maskRectF.left - reverseRadius, maskRectF.bottom);
            path.lineTo(maskRectF.left - reverseRadius, maskRectF.bottom);
            path.lineTo(maskRectF.left, maskRectF.bottom);
            path.lineTo(maskRectF.left, maskRectF.bottom - reverseRadius);
            path.close();
            canvas.drawPath(path, paint);
        }

        // 绘制遮罩右侧反向圆角
        if (maskRectF.right < backgroundRectF.right - reverseRadius) {
            path.reset();
            path.moveTo(maskRectF.right, maskRectF.bottom - reverseRadius);
            path.cubicTo(maskRectF.right, maskRectF.bottom - reverseRadius,
                    maskRectF.right, maskRectF.bottom,
                    maskRectF.right + reverseRadius, maskRectF.bottom);
            path.lineTo(maskRectF.right + reverseRadius, maskRectF.bottom);
            path.lineTo(maskRectF.right, maskRectF.bottom);
            path.lineTo(maskRectF.right, maskRectF.bottom - reverseRadius);
            path.close();
            canvas.drawPath(path, paint);
        }

        // 绘制底部指示横线
        if (drawIndicator) {
            lineRectF.top = maskRectF.bottom;
            lineRectF.bottom = height - distance;
            lineRectF.left = maskRectF.left + (maskRectF.width() / 2) - (indicatorWidth >> 1);
            lineRectF.right = maskRectF.left + (maskRectF.width() / 2) + (indicatorWidth >> 1);
            paint.setColor(indicatorColor);
            canvas.drawRoundRect(lineRectF, indicatorHeight >> 1, indicatorHeight >> 1, paint);
        }

        // 绘制文字
        for (SimpleRoundTabBean bean : list) {
            bean.getPaint().setColor(bean.getTextColor());
            bean.getPaint().setTypeface(bean.getTextStyle());
            float textX = bean.rectF.left + bean.rectF.width() / 2;
            float textY = bean.rectF.top + bean.rectF.height() / 2 + (bean.getSize()[1] >> 1) - DensityUtil.dp2px(2);
            canvas.drawText(bean.text, textX, textY, bean.getPaint());

            // 绘制文字右上角小圆点
            if (bean.showRedPoint) {
                int radius = DensityUtil.dp2px(2);
                paint.setColor(0xffe9302d);
                int[] size = bean.getSize();
                canvas.drawCircle(textX + DensityUtil.dp2px(4) + (float) size[0] / 2 + radius, size[1] + DensityUtil.dp2px(1.5f) - radius, radius, paint);
            }
        }
        paint.setColor(bottomCoverageRectFColor);
        canvas.drawRect(bottomCoverageRectF, paint);
    }


    private float clickX = 0, clickY = 0;
    private long time;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                clickX = event.getX();
                clickY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                if (System.currentTimeMillis() - time > 500) {
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).rectF.contains(clickX, clickY)) {
                            setSelectPosition(i, true);
                            break;
                        }
                    }
                }
                time = System.currentTimeMillis();
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setSelectPosition(int position, boolean animation) {
        int fromPosition = 0;
        // 获取起始下标
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).checked) {
                fromPosition = i;
                break;
            }
        }
        // 重置所有选中状态
        for (int i = 0; i < list.size(); i++) {
            list.get(i).checked = false;
        }
        list.get(position).checked = true;
        if (animation) {
            change(fromPosition, position);
        } else {
            changeMask(eachRectWidth * position);
            invalidate();
            if (onSimpleRoundTabViewCallBack != null) {
                onSimpleRoundTabViewCallBack.onTabChecked(fromPosition, position);
            }
            invalidate();
            if (viewPager != null) {
                viewPager.setCurrentItem(position);
            }
        }
    }

    public int getSelectPosition() {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).checked) {
                return i;
            }
        }
        return 0;
    }


    /**
     * 设置tab
     */
    public void setTab(List<SimpleRoundTabBean> list) {
        this.list = list;
        requestLayout();
    }

    /**
     * 设置圆角
     */
    public void setRadius(float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
        this.topLeftRadius = topLeftRadius;
        this.topRightRadius = topRightRadius;
        this.bottomLeftRadius = bottomLeftRadius;
        this.bottomRightRadius = bottomRightRadius;
        invalidate();
    }

    /**
     * 设置背景颜色
     */
    public void setBackGroundColor(@ColorInt int color) {
        backgroundColor = color;
        invalidate();
    }


    ViewPager viewPager;
    ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (changeByClickTab) {
                return;
            }
            for (SimpleRoundTabBean bean : list) {
                bean.checked = false;
            }
            list.get(position).checked = true;
            changeMask(eachRectWidth * position + positionOffset * eachRectWidth);
            invalidate();

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == SCROLL_STATE_IDLE) {
                changeByClickTab = false;
            }
        }
    };


    /**
     * 设置遮罩矩阵左右位置
     */
    private void changeMask(float left) {
        maskRectF.left = left;
        maskRectF.right = maskRectF.left + eachRectWidth;
    }

    /**
     * 依附到viewpager
     */
    public void attachToViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        this.viewPager.addOnPageChangeListener(listener);
    }

    private ValueAnimator valueAnimator;
    private boolean animationIsRunning;
    /**
     * 改变意图由点击tab触发
     */
    private boolean changeByClickTab;

    /**
     * 选中tab被改变
     *
     * @param fromPosition 开始索引
     * @param toPosition   结束索引
     */
    private void change(final int fromPosition, final int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        changeByClickTab = true;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.removeAllListeners();
            valueAnimator.removeAllUpdateListeners();
        }
        int dPosition = toPosition - fromPosition;
        valueAnimator = ValueAnimator.ofFloat(maskRectF.left, maskRectF.left + dPosition * eachRectWidth);

        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(150);
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                animationIsRunning = true;
                maskRectF.set(list.get(fromPosition).rectF);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animationIsRunning = false;
                if (onSimpleRoundTabViewCallBack != null) {
                    onSimpleRoundTabViewCallBack.onTabChecked(fromPosition, toPosition);
                }
                invalidate();
                if (viewPager != null) {
                    viewPager.setCurrentItem(toPosition);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                changeMask((float) animation.getAnimatedValue());
                invalidate();
            }
        });
        valueAnimator.start();

    }


    /**
     * 获取四个顶点圆角
     */
    private float[] getCornerRadii() {
        return new float[]{
                topLeftRadius, topLeftRadius,
                topRightRadius, topRightRadius,
                bottomRightRadius, bottomRightRadius,
                bottomLeftRadius, bottomLeftRadius
        };
    }

    public static class SimpleRoundTabBean {
        private Paint paint = new Paint();

        {
            paint.setAntiAlias(true);
        }

        /**
         * 矩阵
         */
        private RectF rectF = new RectF();

        /**
         * 是否选中
         */
        public boolean checked;
        /**
         * 文字
         */
        public String text = "";
        /**
         * 未选中时文字颜色
         */
        public int normalTextColor = Color.parseColor("#666666");
        /**
         * 选中时文字颜色
         */
        public int checkedTextColor = Color.parseColor("#212121");
        /**
         * 未选中时文字颜色
         */
        public Typeface normalTextStyle = Typeface.DEFAULT;
        /**
         * 选中时文字颜色
         */
        public Typeface checkedTextStyle = Typeface.DEFAULT_BOLD;
        /**
         * 背景色
         */
        public int backgroundColor = Color.parseColor("#f2f2f2");
        /**
         * 文字大小
         */
        public int textSize = DensityUtil.sp2px(14);
        /**
         * 是否显示红点
         */
        public boolean showRedPoint;

        int getTextColor() {
            if (checked) {
                return checkedTextColor;
            } else {
                return normalTextColor;
            }
        }

        Typeface getTextStyle() {
            if (checked) {
                return checkedTextStyle;
            } else {
                return normalTextStyle;
            }
        }

        Paint getPaint() {
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(textSize);
            if (checked) {
                paint.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                paint.setTypeface(Typeface.DEFAULT);
            }
            return paint;
        }

        int[] getSize() {
            return DrawViewUtils.getTextWH(getPaint(), text);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (viewPager != null) {
            viewPager.removeOnPageChangeListener(listener);
        }
    }

    public interface OnSimpleRoundTabViewCallBack {
        void onTabChecked(int fromPosition, int toPosition);
    }
}


