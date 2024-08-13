package com.sss.michael.simpleview.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.sss.michael.simpleview.R;
import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author Michael by 61642
 * @date 2024/8/11 16:49
 * @Description 一个简单的抽奖机
 */
public class SimpleLuckDrawView extends View {
    /**
     * 数据集合
     */
    private List<LuckDrawBean> list = new ArrayList<>();
    /**
     * 指针位图
     */
    private Bitmap pointer;
    /**
     * 回调
     */
    private OnLuckDrawCallBack onLuckDrawCallBack;

    public void setOnLuckDrawCallBack(OnLuckDrawCallBack onLuckDrawCallBack) {
        this.onLuckDrawCallBack = onLuckDrawCallBack;
    }

    public SimpleLuckDrawView(Context context) {
        this(context, null);
    }

    public SimpleLuckDrawView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    boolean DEBUG = false;

    public SimpleLuckDrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        pointer = BitmapFactory.decodeResource(getResources(), R.mipmap.luck_draw_pointer);
        if (DEBUG) {
            list.add(new LuckDrawBean(context, "幸运符x20", "https://img.duoziwang.com/2019/01/02132028910281.jpg"));
            list.add(new LuckDrawBean(context, "50史诗自选罐x2", "https://img.duoziwang.com/2019/01/02132028910280.jpg"));
            list.add(new LuckDrawBean(context, "史诗跨界石x1", "https://img.duoziwang.com/2019/01/02132028910288.jpg"));
            list.add(new LuckDrawBean(context, "炉岩碳x2000", "https://img.duoziwang.com/2019/01/02132028910287.jpg"));
            list.add(new LuckDrawBean(context, "金币x100W", "https://img.duoziwang.com/2019/01/02132028910285.jpg"));
            list.add(new LuckDrawBean(context, "Lv50升级券x1", "https://img.duoziwang.com/2019/01/02132028910293.jpg"));
            setOnLuckDrawCallBack(new OnLuckDrawCallBack() {
                @Override
                public void onPrepareComplete(int count, boolean success) {

                }

                @Override
                public void onLoadComplete(boolean ready) {
                    if (ready) {
                        invalidate();
                        setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                start(-9);
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "图片加载出错", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onLuckDrawResult(int position) {
                    Toast.makeText(getContext(), "恭喜获得" + list.get(position).label, Toast.LENGTH_SHORT).show();
                }
            });
            preview(list);
        }
    }

    /**
     * 画笔
     */
    Paint paint = new Paint();
    /**
     * 中心点
     */
    Point centerPoint = new Point();
    /**
     * 宽高
     */
    int width, height;
    /**
     * 渐变
     */
    private LinearGradient linearGradient;

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        centerPoint.set(width / 2, height / 2);
        linearGradient = new LinearGradient(0,
                0,
                width,
                height,
                new int[]{Color.parseColor("#FFDFDA"), Color.parseColor("#FF6843"), Color.parseColor("#FFA89B")},
                new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP);

        if (list.size() > 1) {
            eachAngle = 360 / list.size();

            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {
                    list.get(i).startAngle = 0;
                    list.get(i).endAngle = eachAngle;
                } else {
                    list.get(i).startAngle = list.get(i - 1).endAngle;
                    list.get(i).endAngle = list.get(i).startAngle + eachAngle;
                }
                list.get(i).startPoint = DrawViewUtils.calculatePoint(centerPoint.x, centerPoint.y, (Math.min(width, height) >> 1) - outerCircle1Width - outerCircle2Width, list.get(i).startAngle);
                list.get(i).endPoint = DrawViewUtils.calculatePoint(centerPoint.x, centerPoint.y, (Math.min(width, height) >> 1) - outerCircle1Width - outerCircle2Width, list.get(i).endAngle);
                list.get(i).centerPoint = DrawViewUtils.calculatePoint(centerPoint.x, centerPoint.y, (Math.min(width, height) >> 1) - outerCircle1Width - outerCircle2Width, list.get(i).endAngle - list.get(i).startAngle);
                if (i % 2 == 0) {
                    list.get(i).backgroundColor = Color.parseColor("#FFE6E2");
                } else {
                    list.get(i).backgroundColor = Color.parseColor("#FFFDFB");
                }
            }
        }
    }

    /**
     * 虚线
     */
    DashPathEffect dashPathEffect = new DashPathEffect(new float[]{10, 5}, 0);
    /**
     * 每一块开始于结束间隔的角度
     */
    int eachAngle;
    /**
     * 外边1宽度
     */
    int outerCircle1Width = DensityUtil.dp2px(15);
    /**
     * 外边2宽度
     */
    int outerCircle2Width = DensityUtil.dp2px(5);

    /**
     * 指针{@link #pointer}垂直偏移量
     */
    int verticalOffset = DensityUtil.dp2px(1);

    /**
     * 摇奖机当前旋转角度
     */
    int currentRotationAngle = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        //中心指针宽高
        float pointWidth = (float) pointer.getWidth();
        float pointHeight = (float) pointer.getHeight();
        //指针外圆半径
        float pointOuterRadius = Math.min(pointWidth, pointHeight) / 2;
        //背景透明
        paint.setColor(Color.TRANSPARENT);
        canvas.drawRect(0, 0, width, height, paint);
        canvas.rotate(currentRotationAngle + offset(), centerPoint.x, centerPoint.y);
        //背景
        paint.setShader(linearGradient);
        paint.setColor(Color.RED);
        canvas.drawCircle(centerPoint.x, centerPoint.y, Math.min(width, height) >> 1, paint);
        paint.setShader(null);
        //外边距内圆（与背景的视差形成的圆环）
        paint.setColor(Color.parseColor("#FF554C"));
        float ringRadius = (Math.min(width, height) >> 1) - outerCircle1Width;
        canvas.drawCircle(centerPoint.x, centerPoint.y, ringRadius, paint);
        //转盘奖励区
        if (list.size() > 1) {
            // 半径，从圆心到奖励区边框，不包含外圆
            int radius = (Math.min(width, height) >> 1) - outerCircle1Width - outerCircle2Width;
            for (LuckDrawBean bean : list) {
                if (list.size() % 2 == 0) {
                    // 双数，绘制底色
                    paint.setColor(bean.backgroundColor);
                } else {
                    // 单数，无法绘制色值，使用白色底色
                    paint.setColor(Color.parseColor("#FFFFFF"));
                }
                canvas.drawArc(centerPoint.x - radius, centerPoint.y - radius, centerPoint.x + radius, centerPoint.y + radius,
                        bean.startAngle, bean.endAngle - bean.startAngle, true, paint);


                // Bitmap的位置，使其位于圆环的中心位置
                float angle = (bean.startAngle + bean.endAngle) / 2f;  // 扇形中心的角度
                float bitmapRadius = radius - (outerCircle1Width >> 1) - DensityUtil.dp2px(50);  // 控制 Bitmap 的绘制位置，靠近圆环的中心

                // Bitmap的中心点坐标
                float bitmapX = centerPoint.x + bitmapRadius * (float) Math.cos(Math.toRadians(angle));
                float bitmapY = centerPoint.y + bitmapRadius * (float) Math.sin(Math.toRadians(angle));

                canvas.save();
                // 旋转画布，使Bitmap随圆环角度旋转
                canvas.rotate(angle + 90, bitmapX, bitmapY);  // 旋转中心为 Bitmap 的中心，+90 度是为了调整方向
                if (bean.bitmap != null) {
                    // 绘制Bitmap，保持中心点位置不变
                    canvas.drawBitmap(bean.bitmap, bitmapX - (bean.bitmap.getWidth() >> 1), bitmapY - (bean.bitmap.getHeight() >> 1), null);

                    // 绘制文字
                    paint.setTextSize(DensityUtil.dp2px(13));
                    paint.setColor(Color.parseColor("#CD2901"));
                    paint.setTextAlign(Paint.Align.CENTER);
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                    canvas.drawText(bean.getLabel(), bitmapX, bitmapY - bean.bitmap.getHeight() / 1.3f, paint);
                    paint.setTypeface(Typeface.DEFAULT);//还原
                }
                // 恢复画布状态
                canvas.restore();


            }
        }
        // 绘制虚线
        for (LuckDrawBean bean : list) {
            paint.setPathEffect(dashPathEffect);
            paint.setColor(Color.parseColor("#5B0B0B"));
            paint.setStrokeWidth(DensityUtil.dp2px(1));
            canvas.drawLine(centerPoint.x, centerPoint.y, bean.startPoint.x, bean.startPoint.y, paint);
            paint.setPathEffect(null);
        }

        //每个虚线两端上的小圆点
        for (LuckDrawBean bean : list) {
            paint.setColor(Color.parseColor("#FCCDB2"));
            float rrr = DensityUtil.dp2px(5);
            //靠近指针的小圆点
            Point point = DrawViewUtils.calculatePoint(centerPoint.x, centerPoint.y, (int) pointOuterRadius, bean.endAngle);
            canvas.drawCircle(point.x, point.y, rrr, paint);
            //靠近外圆的小圆点
            int startAngle = bean.startAngle + 90;
            canvas.drawArc(bean.startPoint.x - rrr, bean.startPoint.y - rrr, bean.startPoint.x + rrr, bean.startPoint.y + rrr,
                    startAngle, 180, true, paint);
        }

        canvas.rotate(currentRotationAngle);
        canvas.restore();
        //指针外圆
        paint.setColor(Color.parseColor("#FCCDB2"));
        canvas.drawCircle(centerPoint.x, centerPoint.y, pointOuterRadius, paint);
        //指针
        canvas.drawBitmap(pointer, centerPoint.x - pointWidth / 2, centerPoint.y - pointHeight / 2 - verticalOffset, null);


    }

    /**
     * 旋转偏移量，用于修正0-90
     *
     * @return
     */
    int offset() {
        return -90 - eachAngle / 2;
//        return 0;
    }

    public void preview(List<LuckDrawBean> list) {
        if (list.size() <= 1) {
            return;
        }
        this.list = list;
        final int[] successSize = {0};
        final int[] failSize = {0};
        for (int i = 0; i < list.size(); i++) {
            list.get(i).prepare(i, new OnLuckDrawCallBack() {
                @Override
                public void onPrepareComplete(int count, boolean success) {
                    if (success) {
                        successSize[0]++;
                    } else {
                        failSize[0]++;
                    }
                    if (onLuckDrawCallBack != null) {
                        if (successSize[0] + failSize[0] == list.size()) {
                            if (successSize[0] == list.size()) {
                                onLuckDrawCallBack.onLoadComplete(true);
                                requestLayout();
                            } else {
                                onLuckDrawCallBack.onLoadComplete(false);
                            }
                        }
                    }

                }

                @Override
                public void onLoadComplete(boolean ready) {

                }

                @Override
                public void onLuckDrawResult(int position) {

                }
            });
        }
    }


    ValueAnimator animator;


    public void start(int targetPosition) {
        if (isEnabled()) {
            int targetAngle = 360 * 20 - targetPosition * eachAngle;
            if (animator != null) {
                animator.cancel();
                animator.removeAllUpdateListeners();
                animator.removeAllListeners();
            }
            animator = ValueAnimator.ofInt(0, targetAngle);
            animator.setDuration(2000);
            animator.setInterpolator(new DecelerateInterpolator(1.2f));
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    currentRotationAngle = (int) animation.getAnimatedValue();
                    invalidate();
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    setEnabled(false);
                    currentRotationAngle = 0;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    setEnabled(true);
                    invalidate();
                    if (onLuckDrawCallBack != null) {
                        onLuckDrawCallBack.onLuckDrawResult(Math.abs(targetPosition % list.size()));
                    }
                }
            });
            animator.start();
        }
    }

    public static class LuckDrawBean {
        private Context context;
        private String label;
        private String url;
        private Bitmap bitmap;
        private int startAngle, endAngle;
        private int backgroundColor;
        Point startPoint = new Point();
        Point centerPoint = new Point();
        Point endPoint = new Point();

        public LuckDrawBean(Context context, String label, String url) {
            this.label = label;
            this.context = context;
            this.url = url;
        }

        private void prepare(int count, OnLuckDrawCallBack onLuckDrawCallBack) {
            Glide.with(context).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    //尺寸设置40以节省内存
                    bitmap = Bitmap.createScaledBitmap(resource, DensityUtil.dp2px(40), DensityUtil.dp2px(40), true);
                    if (onLuckDrawCallBack != null) {
                        onLuckDrawCallBack.onPrepareComplete(count, true);
                    }

                }

                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    if (onLuckDrawCallBack != null) {
                        onLuckDrawCallBack.onPrepareComplete(count, false);
                    }
                }
            });
        }

        public String getLabel() {
            return label;
        }

        public String getUrl() {
            return url;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
            animator.removeAllUpdateListeners();
            animator.removeAllListeners();
        }
    }

    public interface OnLuckDrawCallBack {
        void onPrepareComplete(int count, boolean success);

        void onLoadComplete(boolean ready);

        void onLuckDrawResult(int position);


    }
}
