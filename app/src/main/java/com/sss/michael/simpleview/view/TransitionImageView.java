package com.sss.michael.simpleview.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * @author Michael by Administrator
 * @date 2022/8/17 17:45
 * @Description 转场ImageView
 */
@SuppressWarnings("all")
public class TransitionImageView extends AppCompatImageView {
    private OnTransitionImageViewCallBack onTransitionImageViewCallBack;


    public void setOnTransitionImageViewCallBack(OnTransitionImageViewCallBack onTransitionImageViewCallBack) {
        this.onTransitionImageViewCallBack = onTransitionImageViewCallBack;
    }

    public TransitionImageView(@NonNull Context context) {
        this(context, null);
    }

    public TransitionImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransitionImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
    }

    /**
     * 设置图片
     */
    public void setImgUrl(final String from, final String to, final ImageView bannerImageView, final int x, final int y, final int width, final int height) {
        Glide.with(this).asBitmap().load(to).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {

                return false;
            }

            @Override
            public boolean onResourceReady(final Bitmap bitmap, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                setImageBitmap(bitmap);
                if (bannerImageView != null) {
                    setDrawingCacheEnabled(true);
                    try {
                        Bitmap bmp = Bitmap.createBitmap(getDrawingCache());
                        setDrawingCacheEnabled(false);
                        bmp = imageCrop(bmp, x, y, width, height);
                        if (bmp != null) {
                            bannerImageView.setImageBitmap(bmp);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        }).preload();
    }

    /**
     * 透明度渐变
     */
    public void setTransitionAlpha(final String from, final String to) {
        Glide.with(this).asBitmap().load(to).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(final Bitmap bitmap, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                changeAlpha(1.0f, 0.5f, bitmap);
                return false;
            }
        }).preload();
    }

    /**
     * 改变背景透明度动画
     */
    void changeAlpha(final float from, float to, final Bitmap bitmap) {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.setDuration(200);
            valueAnimator.removeAllListeners();
            valueAnimator.removeAllUpdateListeners();
        }
        valueAnimator = ValueAnimator.ofFloat(from, to);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setAlpha((Float) animation.getAnimatedValue());
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (from == 1.0f) {
                    setImageBitmap(bitmap);
                    changeAlpha(0.5f, 1.0f, bitmap);
                }
            }
        });
        valueAnimator.start();
    }

    private ValueAnimator valueAnimator;

    /**
     * 根据控件比例剪裁bitmap成一个固定大小的图片
     *
     * @param resource 需要裁剪的图片的bitmap值
     * @param x        从图片的x轴的x处开始裁剪
     * @param y        从图片的y轴的y处开始裁剪
     * @param width    裁剪生成新图皮的宽
     * @param height   裁剪生成新图皮的高
     * @return 裁剪之后的bitmap
     */
    public static Bitmap imageCrop(Bitmap resource, int x, int y, int width, int height) {
        if (resource == null) {
            return null;
        }
        if (resource.getWidth() == 0 || resource.getHeight() == 0) {
            return null;
        }
        if (x > resource.getWidth() || x < 0) {
            x = 0;
        }

        if (y > resource.getHeight() || y < 0) {
            y = 0;
        }
        if (x + width > resource.getWidth()) {
            return null;
        }
        if (y + height > resource.getHeight()) {
            return null;
        }
        width = Math.min(width, resource.getWidth());
        height = Math.min(height, resource.getHeight());
        return Bitmap.createBitmap(resource, x, y, width, height);
    }

    public interface OnTransitionImageViewCallBack {
        void onImageChange(Bitmap bitmap, ImageView bannerImageView);
    }
}