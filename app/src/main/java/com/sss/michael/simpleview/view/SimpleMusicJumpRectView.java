package com.sss.michael.simpleview.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.sss.michael.simpleview.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.Nullable;

/**
 * @author Michael by SSS
 * @date 2021/12/23 20:46
 * @Description 一个简单的音乐矩阵跳动view
 */
public class SimpleMusicJumpRectView extends View {
    /**
     * 线条之间的间距
     */
    private int distance = DensityUtil.dp2px(2);

    /**
     * 条状数量
     */
    private int count = 4;
    /**
     * 矩阵集合
     */
    private List<Rect> rectList = new ArrayList<>();
    private ValueAnimator valueAnimator;

    private Paint paint = new Paint();

    {
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(1);
    }

    public SimpleMusicJumpRectView(Context context) {
        this(context, null);
    }

    public SimpleMusicJumpRectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleMusicJumpRectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int totalWidth = w - (count - 1) * distance;
        int eachWidth = totalWidth / count;
        rectList.clear();
        for (int i = 0; i < count; i++) {
            Rect rect = new Rect();
            rect.left = rectList.size() == 0 ? 0 : rectList.get(rectList.size() - 1).right + distance;
            rect.top = 0;
            rect.right = rect.left + eachWidth;
            rect.bottom = h;
            rectList.add(rect);
        }

    }

    private Random random = new Random();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < rectList.size(); i++) {
            rectList.get(i).top = random.nextInt(getHeight());
            canvas.drawRect(rectList.get(i), paint);
        }
    }


    public void start() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.removeAllUpdateListeners();
            valueAnimator.removeAllListeners();
        }
        valueAnimator = ValueAnimator.ofFloat(0.1f, 1f);
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                invalidate();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                invalidate();
            }
        });
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(200);
        valueAnimator.start();
    }

    public void stop() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.removeAllUpdateListeners();
            valueAnimator.removeAllListeners();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        start();
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

}
