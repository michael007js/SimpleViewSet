package com.sss.michael.simpleview.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;

import com.sss.michael.simpleview.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

public class AvatarFlowView extends View {
    private final Paint bmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final List<Bitmap> avatars = new ArrayList<>();

    private int visibleCount = 5; // 最多显示的头像数量
    private float avatarSize = DensityUtil.dp2px(40); // 每个头像的尺寸
    private float overlap = DensityUtil.dp2px(12); // 头像之间重叠的距离
    private float scaleLevel = 0.5f;//头像缩放程度
    private ValueAnimator valueAnimator;
    private float value = 0f; // 动画进度 0~1

    public AvatarFlowView(Context c) {
        this(c, null);
    }

    public AvatarFlowView(Context c, AttributeSet a) {
        this(c, a, 0);
    }

    public AvatarFlowView(Context c, AttributeSet a, int d) {
        super(c, a, d);
        setWillNotDraw(false);
    }

    /**
     * 设置头像数据（Bitmap 列表）
     */
    public void setAvatars(List<Bitmap> list) {
        avatars.clear();
        if (list != null) {
            for (Bitmap bmp : list) {
                if (bmp != null && !bmp.isRecycled()) {
                    avatars.add(Bitmap.createScaledBitmap(bmp, (int) avatarSize, (int) avatarSize, true));
                }
            }
        }
        invalidate();
    }

    /**
     * 开始动画（头像从左向右流动）
     */
    public void start() {
        if (avatars.size() <= visibleCount) return;

        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator = null;
        }

        valueAnimator = ValueAnimator.ofFloat(0f, 0f, 1f);
        valueAnimator.setDuration(2000); // 每秒滚动一格
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);

        valueAnimator.addUpdateListener(animation -> {
            value = (float) animation.getAnimatedValue();
            invalidate();
        });

        valueAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(@NonNull android.animation.Animator animation) {
                // 动画滚动一格后，把第一个头像移到队尾，形成循环
                if (!avatars.isEmpty()) {
                    Bitmap first = avatars.remove(avatars.size() - 1);
                    avatars.add(0,first);
                    invalidate();
                }
            }
        });

        valueAnimator.start();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        int count = Math.min(avatars.size(), visibleCount + 1); // 多画一个头像用于入场动画

        if (count == 0) {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setColor(0x11888888);
            c.drawRect(0, 0, getWidth(), getHeight(), p);
            p.setColor(0xFF888888);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(DensityUtil.dp2px(14));
            c.drawText("请设置头像数据", getWidth() / 2f, getHeight() / 2f, p);
            return;
        }

        for (int i = count - 1; i >= 0; i--) {
            if (i >= avatars.size()) break;

            Bitmap bmp = avatars.get(i);
            if (bmp == null || bmp.isRecycled()) continue;
            // 基础位置
            float baseLeft = i * (avatarSize - overlap);
            float offset = value * (avatarSize - overlap); // 平移动画
            float left = baseLeft + offset;

            float scale = 1f;
            int alpha = 255;

            if (i == 0) {
                // 第一个头像：缩小 + 渐隐
                scale = 0.5f + scaleLevel * value;
                alpha = (int) (255 * value);
            } else if (i == visibleCount) {
                // 新入场头像：放大 + 渐显
                scale = 1f - scaleLevel * value;
                alpha = (int) (255 * (1f - value));
            }

            bmpPaint.setAlpha(alpha);
            c.save();
            c.translate(left + avatarSize / 2f, avatarSize / 2f); // 平移到头像中心
            c.scale(scale, scale); // 缩放
            c.translate(-avatarSize / 2f, -avatarSize / 2f); // 移回原位
            c.drawBitmap(bmp, 0, 0, bmpPaint);
            c.restore();
        }

        bmpPaint.setAlpha(255); // 重置透明度
    }

    public void setVisibleCount(int count) {
        visibleCount = count;
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator = null;
        }
    }
}
