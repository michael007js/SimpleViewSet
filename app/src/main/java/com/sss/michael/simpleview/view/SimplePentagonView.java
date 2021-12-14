package com.sss.michael.simpleview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.sss.michael.simpleview.R;
import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael by Administrator
 * @date 2021/1/28 9:41
 * @Description 一个简单的五边形图
 */
@SuppressWarnings("ALL")
public class SimplePentagonView extends android.view.View {
    private int maginTopSize = DensityUtil.dp2px(118);
    private int maginBottomSize = DensityUtil.dp2px(25);
    /**
     * 动画持续时间
     */
    private static final int ANIMATION_DURATION = 500;
    /**
     * 整体背景
     */
    private LinearGradient fullBack;
    /**
     * 五星前景
     */
    private LinearGradient pentagonForeground;
    /**
     * 宽高比例
     */
    private float whPercent = 0.5f;
    /**
     * 文字与文字之间的距离
     */
    private int betweenTextDistance = DensityUtil.dp2px(3);
    /**
     * 文字与网格线之间的距离
     */
    private int betweenWebAndTextDistance = DensityUtil.dp2px(10);

    /**
     * 内部的多边形坐标集（包含最外一层）
     */
    private java.util.List<Point> pointList = new ArrayList<>();

    /**
     * 前景多边形坐标集
     */
    private java.util.List<Point> foregroundList = new ArrayList<>();
    /**
     * 数据模型
     */
    private java.util.List<SimpleSpiderViewBean> data = new ArrayList<>();

    {
        data.add(new SimpleSpiderViewBean(R.mipmap.ic_launcher, "", "升学目标", 0f));
        data.add(new SimpleSpiderViewBean(R.mipmap.ic_launcher, "", "学业水平", 0f));
        data.add(new SimpleSpiderViewBean(R.mipmap.ic_launcher, "", "心理健康", 0f));
        data.add(new SimpleSpiderViewBean(R.mipmap.ic_launcher, "", "应用达人", 0f));
        data.add(new SimpleSpiderViewBean(R.mipmap.ic_launcher, "", "自我认知", 0f));


    }

    /**
     * 中心点
     */
    private Point center = new Point(0, 0);
    /**
     * 两个顶点之间的夹角
     */
    private int angle = 360 / data.size();


    /**
     * 半径
     */
    private int radius;
    /**
     * 小圆点半径
     */
    private static int circleRadius = DensityUtil.dp2px(3f);
    /**
     * 画笔
     */
    private Paint backgroundPaint = new Paint();
    private Paint paint = new Paint();
    private Path path = new Path();
    private int backgroundColor = Color.parseColor("#1AFFFFFF");
    private Paint foregroundPaint = new Paint();
    private Paint spiderPaint = new Paint();


    {


        backgroundPaint.setStrokeWidth(1);
        backgroundPaint.setAntiAlias(true);
        paint.setStrokeWidth(1);
        paint.setAntiAlias(true);
        foregroundPaint.setStrokeWidth(1);
        foregroundPaint.setAntiAlias(true);
        spiderPaint.setStrokeWidth(3);
        spiderPaint.setAntiAlias(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                List<SimpleSpiderViewBean> data = new ArrayList<>();
                data.add(new SimpleSpiderViewBean(R.mipmap.ic_launcher, "B", "目标规划", 0.6f));
                data.add(new SimpleSpiderViewBean(R.mipmap.ic_launcher, "A+", "自我认知", 0.9f));
                data.add(new SimpleSpiderViewBean(R.mipmap.ic_launcher, "A", "学习状态", 0.67f));
                data.add(new SimpleSpiderViewBean(R.mipmap.ic_launcher, "A+", "心理健康", 1.0f));
                data.add(new SimpleSpiderViewBean(R.mipmap.ic_launcher, "A", "生涯学习", 0.8f));
                setData(data, true);
            }
        });
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
            if (data.size() > 0) {
                foregroundList.clear();
                float percent = (float) animation.getAnimatedValue();
                for (int i = 0; i < data.size(); i++) {
                    foregroundList.add(DrawViewUtils.calculatePoint(center.x, center.y, (int) (radius * percent * data.get(i).percent), getAngleForEach(i)));
                }
                invalidate();
            }
        }
    };

    public SimplePentagonView(Context context) {
        this(context, null);
    }

    public SimplePentagonView(Context context, @androidx.annotation.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimplePentagonView(Context context, @androidx.annotation.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.removeAllUpdateListeners();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (height == 0) {
            height = (int) (width * whPercent);
            setMeasuredDimension(width, height);
        }
        int maxSize = 0;
        for (int i = 0; i < data.size(); i++) {
            int[][] size = data.get(i).getSize(backgroundPaint);
            //比较各点的文字宽高取得最大宽或高尺寸
            maxSize = Math.max(Math.max(size[0][0], size[0][1]), Math.max(size[1][0], size[1][1]));
        }
        radius = Math.min(width, height) / 2 - betweenTextDistance / 2 + betweenWebAndTextDistance / 2 - maxSize;
        center.set(width / 2, height / 2);
        pointList.clear();
        java.util.List<Point> points = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            pointList.add(DrawViewUtils.calculatePoint(center.x, center.y, radius, getAngleForEach(i)));
        }
        fullBack = new LinearGradient(0f, 0f, width, height, 0xff2c80ff, 0xff0043ff, android.graphics.Shader.TileMode.MIRROR);

        pentagonForeground = new LinearGradient(0f, 0f, width, height, 0xe0ffffff, 0x26ffffff, android.graphics.Shader.TileMode.MIRROR);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /*************************************整体渐变背景*****************************************/
        backgroundPaint.setShader(fullBack);
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        backgroundPaint.setShader(null);
        /*************************************五星背景*****************************************/
        path.reset();
        path.moveTo(pointList.get(0).x, pointList.get(0).y);
        for (int i = 0; i < pointList.size(); i++) {
            path.lineTo(pointList.get(i).x, pointList.get(i).y);
        }
        path.close();
        paint.setColor(backgroundColor);
        canvas.drawPath(path, paint);

        /*************************************五星前景*****************************************/
        if (foregroundList != null && foregroundList.size() > 0) {
            backgroundPaint.setShader(pentagonForeground);
            canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

            path.reset();
            path.moveTo(foregroundList.get(0).x, foregroundList.get(0).y);
            for (int i = 0; i < pointList.size(); i++) {
                path.lineTo(foregroundList.get(i).x, foregroundList.get(i).y);
            }
            path.close();
            paint.setColor(backgroundColor);
            canvas.drawPath(path, paint);
            backgroundPaint.setShader(null);
        }
    }

    /**
     * 设置数据
     */
    public void setData(java.util.List<SimpleSpiderViewBean> list, boolean withAnimation) {
        if (data.size() == 0) {
            return;
        }
        this.data = list;
        angle = 360 / data.size();
        if (withAnimation) {
            startAnimation();
        } else {
            foregroundList.clear();
            float percent = 1f;
            for (int i = 0; i < data.size(); i++) {
                foregroundList.add(DrawViewUtils.calculatePoint(center.x, center.y, (int) (radius * percent * data.get(i).percent), getAngleForEach(i)));
            }
            invalidate();
        }
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
     * 计算背景点
     */
    private Point[] calcBackgroundPoint(int i, int radius) {
        Point[] points = new Point[2];
        if (i == 0) {
            points[0] = DrawViewUtils.calculatePoint(center.x, center.y, radius, getAngleForEach(i));
            points[1] = DrawViewUtils.calculatePoint(center.x, center.y, radius, getAngleForEach(data.size() - 1));
        } else if (i == data.size() - 1) {
            points[0] = DrawViewUtils.calculatePoint(center.x, center.y, radius, getAngleForEach(data.size()));
            points[1] = DrawViewUtils.calculatePoint(center.x, center.y, radius, angle - getAngleForZero());
        } else {
            points[0] = DrawViewUtils.calculatePoint(center.x, center.y, radius, getAngleForEach(i));
            points[1] = DrawViewUtils.calculatePoint(center.x, center.y, radius, getAngleForEach(i + 1));
        }
        return points;
    }

    /**
     * 计算角度与0度间的夹角
     */
    private int getAngleForZero() {
        return 90 - angle;
    }

    /**
     * 计算每个顶点的角度
     */
    private int getAngleForEach(int i) {
        return i * angle - getAngleForZero();
    }

    /**
     * 数据模型
     */
    public static class SimpleSpiderViewBean {
        private String text;
        private float textSize = DensityUtil.dp2px(16);
        private int textColor = android.graphics.Color.parseColor("#FFFFFF");
        private int textStyle = Typeface.BOLD;
        private String remark;
        private float remarkSize = DensityUtil.dp2px(12);
        private int remarkColor = android.graphics.Color.parseColor("#FFFFFF");
        private int remarkStyle = Typeface.NORMAL;
        private int iconRes;

        private float percent;


        public SimpleSpiderViewBean(int iconRes, String text, String remark, float percent) {
            this.text = text;
            this.remark = remark;
            this.percent = percent;
            this.iconRes = iconRes;
        }

        public SimpleSpiderViewBean(String text, float textSize, int textColor, int textStyle, String remark, float remarkSize, int remarkColor, int remarkStyle, float percent) {
            this.text = text;
            this.textSize = textSize;
            this.textColor = textColor;
            this.textStyle = textStyle;
            this.remark = remark;
            this.remarkSize = remarkSize;
            this.remarkColor = remarkColor;
            this.remarkStyle = remarkStyle;
            this.percent = percent;
        }

        public String getText() {
            return text;
        }

        public String getRemark() {
            return remark;
        }

        public float getPercent() {
            return percent;
        }

        public int getIconRes() {
            return iconRes;
        }

        private int[][] getSize(Paint paint) {

            int size[][] = new int[2][2];
            paint.setTextSize(textSize);
            paint.setColor(textColor);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, textStyle));
            int[] textSize = DrawViewUtils.getTextWH(paint, text);
            paint.setTextSize(remarkSize);
            paint.setColor(remarkColor);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, remarkStyle));
            int[] remarkSize = DrawViewUtils.getTextWH(paint, remark);
            size[0] = textSize;
            size[1] = remarkSize;
            return size;
        }
    }
}
