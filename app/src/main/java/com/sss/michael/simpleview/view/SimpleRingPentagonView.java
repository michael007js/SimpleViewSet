package com.sss.michael.simpleview.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael by Administrator
 * @date 2021/1/28 9:41
 * @Description 一个简单的圆环背景五边形图
 */
@SuppressWarnings("ALL")
public class SimpleRingPentagonView extends View {
    public static boolean DEBUG = true;
    /**
     * 动画持续时间
     */
    private static final int ANIMATION_DURATION = 500;
    /**
     * 五星前景
     */
    private LinearGradient pentagonForeground;
    /**
     * 宽高比例
     */
    private float whPercent = 0.5f;
    /**
     * 半径比例
     */
    private float radiusPercent = 0.8f;

    /**
     * 文字与网格线之间的距离
     */
    private int betweenWebAndTextDistance = DensityUtil.dp2px(15);

    /**
     * 内部的多边形坐标集（包含最外一层）
     */
    private List<Point> pointList = new ArrayList<>();

    /**
     * 前景多边形坐标集
     */
    private List<Point> foregroundList = new ArrayList<>();
    /**
     * 数据模型
     */
    private List<SimpleRingPentagonViewBean> data = new ArrayList<>();

    {
        if (DEBUG) {
            data.add(new SimpleRingPentagonViewBean("心理与人格", 0f));
            data.add(new SimpleRingPentagonViewBean("社会价值观", 0f));
            data.add(new SimpleRingPentagonViewBean("职业与行业", 0f));
            data.add(new SimpleRingPentagonViewBean("升学规划", 0f));
            data.add(new SimpleRingPentagonViewBean("教育理念", 0f));
        }

    }

    private int total = 888;
    private int number;


    private String desc = "超过95%的同学";

    /**
     * 中心点
     */
    private Point center = new Point(0, 0);
    /**
     * 两个顶点之间的夹角
     */
    private int angle = 360 / ((data == null || data.size() == 0) ? 1 : data.size());


    /**
     * 半径
     */
    private int radius;
    /**
     * 小圆点半径
     */
    private static int circleRadius = DensityUtil.dp2px(3f);
    /**
     * 五星背景色
     */
    private int pentagonbackgroundColor = Color.parseColor("#1AFFFFFF");
    /**
     * 五星骨架色
     */
    private int pentagonSpiderColor = Color.parseColor("#FFD1C3");

    /**
     * 进度
     */
    private float progress;

    /**
     * 画笔
     */
    private Paint topPointtPaint = new Paint();
    private Paint backgroundPaint = new Paint();
    private Paint pentagonBackgroundPaint = new Paint();
    private Path path = new Path();
    private Paint pentagonForegroundPaint = new Paint();
    private Paint spiderPaint = new Paint();
    private Paint textCenterPaint = new Paint();

    {
        topPointtPaint.setStrokeWidth(1);
        topPointtPaint.setAntiAlias(true);
        textCenterPaint.setStrokeWidth(1);
        textCenterPaint.setAntiAlias(true);
        backgroundPaint.setStrokeWidth(1);
        backgroundPaint.setAntiAlias(true);
        pentagonBackgroundPaint.setStrokeWidth(DensityUtil.dp2px(1));
        pentagonBackgroundPaint.setAntiAlias(true);
        pentagonForegroundPaint.setStrokeWidth(1);
        pentagonForegroundPaint.setAntiAlias(true);
        spiderPaint.setStrokeWidth(DensityUtil.dp2px(1));
        spiderPaint.setAntiAlias(true);
        if (DEBUG) {
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<SimpleRingPentagonViewBean> data = new ArrayList<>();
                    data.add(new SimpleRingPentagonViewBean("心理与人格", 0.9f));
                    data.add(new SimpleRingPentagonViewBean("社会价值观", 0.9f));
                    data.add(new SimpleRingPentagonViewBean("职业与行业", 0.8f));
                    data.add(new SimpleRingPentagonViewBean("升学规划", 0.6f));
                    data.add(new SimpleRingPentagonViewBean("教育理念", 1.0f));
                    setData(data, 888, true);
                }
            });
        }
    }


    public SimpleRingPentagonView(Context context) {
        this(context, null);
    }

    public SimpleRingPentagonView(Context context, @androidx.annotation.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleRingPentagonView(Context context, @androidx.annotation.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.removeAllListeners();
            valueAnimator.removeAllUpdateListeners();
        }
        if (progressAnimator != null) {
            progressAnimator.cancel();
            progressAnimator.removeAllListeners();
            progressAnimator.removeAllUpdateListeners();
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
            int[] size = data.get(i).getSize(topPointtPaint);
            maxSize = Math.max(size[0], size[1]);
        }
        radius = Math.min(width, height) / 2 + betweenWebAndTextDistance / 2 - maxSize;
        radius = (int) (radius * radiusPercent);
        center.set(width / 2, height / 2 + getPaddingTop() - getPaddingBottom());
        pointList.clear();
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            pointList.add(DrawViewUtils.calculatePoint(center.x, center.y, radius, getAngleForEach(i)));
        }
        pentagonForeground = new LinearGradient(0f, 0f, width, height, 0xB3FF4F3C, 0xB3FF7E52, Shader.TileMode.MIRROR);
        radialGradien = new RadialGradient(center.x, center.y, radius + shadowRadius, 0x92000000, 0x00000000, Shader.TileMode.MIRROR);

    }

    float shadowRadius = DensityUtil.dp2px(10);
    RadialGradient radialGradien;
    DashPathEffect dashPathEffect = new DashPathEffect(new float[]{10, 10}, 0);

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /*************************************整体背景*****************************************/
        backgroundPaint.setShader(radialGradien);
        backgroundPaint.setShadowLayer(10, 10, 10, Color.parseColor("#22000000"));
        canvas.drawCircle(center.x, center.y, radius + shadowRadius, backgroundPaint);
        backgroundPaint.setShader(null);


        /*************************************五星背景*****************************************/

        pentagonBackgroundPaint.setColor(Color.parseColor("#FFFFFF"));
        pentagonBackgroundPaint.setPathEffect(null);
        canvas.drawCircle(center.x, center.y, radius, pentagonBackgroundPaint);

        pentagonBackgroundPaint.setColor(Color.parseColor("#FFEEEC"));
        pentagonBackgroundPaint.setPathEffect(null);
        canvas.drawCircle(center.x, center.y, radius * 0.86f, pentagonBackgroundPaint);

        pentagonBackgroundPaint.setColor(Color.parseColor("#FFD9D3"));
        pentagonBackgroundPaint.setPathEffect(null);
        canvas.drawCircle(center.x, center.y, radius * 0.7f, pentagonBackgroundPaint);

        pentagonBackgroundPaint.setStyle(Paint.Style.STROKE);
        pentagonBackgroundPaint.setColor(Color.parseColor("#F8AC93"));
        pentagonBackgroundPaint.setPathEffect(dashPathEffect);
        canvas.drawCircle(center.x, center.y, radius * 0.7f, pentagonBackgroundPaint);

        pentagonBackgroundPaint.setStyle(Paint.Style.FILL);
        pentagonBackgroundPaint.setColor(Color.parseColor("#FFFEEC"));
        canvas.drawCircle(center.x, center.y, radius * 0.57f, pentagonBackgroundPaint);

        pentagonBackgroundPaint.setStyle(Paint.Style.FILL);
        pentagonBackgroundPaint.setColor(Color.parseColor("#FFD9D3"));
        pentagonBackgroundPaint.setPathEffect(null);
        canvas.drawCircle(center.x, center.y, radius * 0.42f, pentagonBackgroundPaint);

        pentagonBackgroundPaint.setStyle(Paint.Style.STROKE);
        pentagonBackgroundPaint.setColor(Color.parseColor("#F8AC93"));
        pentagonBackgroundPaint.setPathEffect(dashPathEffect);
        canvas.drawCircle(center.x, center.y, radius * 0.42f, pentagonBackgroundPaint);


        pentagonBackgroundPaint.setStyle(Paint.Style.FILL);

        /*************************************五星骨架*****************************************/
        for (int i = 0; i < data.size(); i++) {
            Point point = DrawViewUtils.calculatePoint(center.x, center.y, radius, getAngleForEach(i));
            spiderPaint.setColor(pentagonSpiderColor);
            canvas.drawLine(center.x, center.y, point.x, point.y, spiderPaint);
        }


        if (foregroundList != null && foregroundList.size() > 0) {
            /*************************************五星前景*****************************************/
            pentagonForegroundPaint.setShader(pentagonForeground);
            path.reset();
            path.moveTo(foregroundList.get(0).x, foregroundList.get(0).y);
            for (int i = 0; i < pointList.size(); i++) {
                path.lineTo(foregroundList.get(i).x, foregroundList.get(i).y);
            }
            path.close();
            canvas.drawPath(path, pentagonForegroundPaint);
            pentagonForegroundPaint.setShader(null);
            /*************************************五星顶点*****************************************/
            for (int i = 0; i < pointList.size(); i++) {
                pentagonForegroundPaint.setColor(Color.parseColor("#FF5C50"));
                canvas.drawCircle(foregroundList.get(i).x, foregroundList.get(i).y, DensityUtil.dp2px(5), pentagonForegroundPaint);
                pentagonForegroundPaint.setColor(Color.parseColor("#FFFFFF"));
                canvas.drawCircle(foregroundList.get(i).x, foregroundList.get(i).y, DensityUtil.dp2px(2.5f), pentagonForegroundPaint);
            }
        }

        /*************************************五星顶点文字*****************************************/

        for (int i = 0; i < data.size() * progress; i++) {
            topPointtPaint.setTextSize(data.get(i).textSize);
            topPointtPaint.setColor(data.get(i).textColor);
            topPointtPaint.setTypeface(Typeface.create(Typeface.DEFAULT, data.get(i).textStyle));

            Point point = DrawViewUtils.calculatePoint(center.x, center.y, radius, getAngleForEach(i));


            if (i == 0) {
                canvas.drawText(data.get(i).text, point.x + betweenWebAndTextDistance, point.y, topPointtPaint);

            } else if (i == 1) {
                canvas.drawText(data.get(i).text, point.x + betweenWebAndTextDistance - DensityUtil.dp2px(30), point.y + betweenWebAndTextDistance + DensityUtil.dp2px(10), topPointtPaint);

            } else if (i == 2) {
                canvas.drawText(data.get(i).text, point.x - data.get(i).getSize(topPointtPaint)[0] - betweenWebAndTextDistance + DensityUtil.dp2px(30), point.y + betweenWebAndTextDistance + DensityUtil.dp2px(10), topPointtPaint);

            } else if (i == 3) {
                canvas.drawText(data.get(i).text, point.x - betweenWebAndTextDistance - data.get(i).getSize(topPointtPaint)[0], point.y, topPointtPaint);
            } else if (i == 4) {
                canvas.drawText(data.get(i).text, point.x - data.get(i).getSize(topPointtPaint)[0] / 2, point.y - betweenWebAndTextDistance, topPointtPaint);
            }
        }

        /*************************************中间文字*****************************************/
        textCenterPaint.setTextSize(DensityUtil.sp2px(34));
        textCenterPaint.setColor(Color.parseColor("#FFFFFF"));
        textCenterPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textCenterPaint.setTextAlign(Paint.Align.CENTER);

        if (number > 0) {
            canvas.drawText(String.valueOf(number), center.x, center.y + DrawViewUtils.getTextWH(topPointtPaint, String.valueOf(number))[1], textCenterPaint);
        }
    }

    private Bitmap getBitmap(int res, float scale) {
        Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(res)).getBitmap();
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return dstbmp;
    }

    private int getBitMapSize(Bitmap bitmap) {
        if (bitmap != null) {
            return Math.max(bitmap.getWidth(), bitmap.getHeight());
        }
        return 0;
    }

    /**
     * 设置数据
     */
    public void setData(List<SimpleRingPentagonViewBean> list, int total, boolean withAnimation) {
        if (data.size() == 0) {
            return;
        }
        this.data = list;
        this.total = total;
        angle = 360 / data.size();
        if (withAnimation) {
            startAnimation();
        } else {
            foregroundList.clear();
            float percent = 1f;
            for (int i = 0; i < data.size(); i++) {
                foregroundList.add(DrawViewUtils.calculatePoint(center.x, center.y, (int) (radius * percent * data.get(i).percent), getAngleForEach(i)));
            }

            int distance = 0;
            if (foregroundList.size() > 0) {
                for (int i = 0; i < foregroundList.size(); i++) {
                    distance = (int) Math.max(distance, DrawViewUtils.calculateLength(center.x, center.y, foregroundList.get(i).x, foregroundList.get(i).y));
                }
                pentagonForeground = new LinearGradient(distance, 0f, distance * 2, distance * 2, 0xB3FF4F3C, 0xB3FF7E52, Shader.TileMode.MIRROR);
            }
            invalidate();
        }
    }

    /**
     * 动画
     */
    private ValueAnimator valueAnimator, progressAnimator;
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
    /**
     * 动画监听器
     */
    private ValueAnimator.AnimatorUpdateListener progressListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (data.size() > 0) {
                progress = (float) animation.getAnimatedValue();
                number = (int) (progress * total);
                invalidate();
            }
        }
    };

    /**
     * 开始动画
     */
    public void startAnimation() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
        valueAnimator = ValueAnimator.ofFloat(0, 1f);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setDuration(ANIMATION_DURATION);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                progress = 0;
                number = 0;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                int distance = 0;
                if (foregroundList.size() > 0) {
                    for (int i = 0; i < foregroundList.size(); i++) {
                        distance = (int) Math.max(distance, DrawViewUtils.calculateLength(center.x, center.y, foregroundList.get(i).x, foregroundList.get(i).y));
                    }
                    pentagonForeground = new LinearGradient(distance, 0f, distance * 2, distance * 2, 0xB3FF4F3C, 0xB3FF7E52, Shader.TileMode.MIRROR);

                }
                startProgressAnimation();
            }
        });
        valueAnimator.addUpdateListener(listener);
        valueAnimator.start();
    }

    /**
     * 开始动画
     */
    public void startProgressAnimation() {
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
        progressAnimator = ValueAnimator.ofFloat(0, 1f);
        progressAnimator.setRepeatMode(ValueAnimator.RESTART);
        progressAnimator.setDuration(ANIMATION_DURATION * 2);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener(progressListener);
        progressAnimator.start();
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

    private float getScreenPercent() {
        DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();
        float density = dm.density;
        float screenWidth = dm.widthPixels * density + 0.5f;        // 屏幕宽（px，如：480px）
        float screenHeight = dm.heightPixels * density + 0.5f;        // 屏幕高（px，如：800px）

        float xdpi = dm.xdpi;
        float ydpi = dm.ydpi;
        return screenWidth / screenHeight;
    }

    /**
     * 数据模型
     */
    public static class SimpleRingPentagonViewBean {
        private String text;
        private float textSize = DensityUtil.sp2px(14);
        private int textColor = Color.parseColor("#333333");
        private int textStyle = Typeface.NORMAL;

        private float percent;


        public SimpleRingPentagonViewBean(String text, float percent) {
            this.text = text;
            this.percent = percent;
        }


        private int[] getSize(Paint paint) {

            paint.setTextSize(textSize);
            paint.setColor(textColor);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, textStyle));
            int[] textSize = DrawViewUtils.getTextWH(paint, text);
            return textSize;
        }
    }
}
