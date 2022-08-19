package com.sss.michael.simpleview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
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
    private ValueAnimator valueAnimator;

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
                    Bitmap bmp = Bitmap.createBitmap(getDrawingCache());
                    setDrawingCacheEnabled(false);
                    bmp = imageCrop(bmp, x, y, width, height);
                    if (bmp != null && bannerImageView != null) {
                        bannerImageView.setImageBitmap(bmp);
                    }
                }
                return false;
            }
        }).preload();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }


    public void setTransitionDrawable(Drawable drawable) {
        TransitionDrawable td = createDrawable(drawable);
        td.startTransition(150);
        setImageDrawable(td);
        valueAnimator = ValueAnimator.ofFloat(1.0f,0.5f);
    }

    /**
     * 复用drawable,防止内存泄漏
     */
    private TransitionDrawable createDrawable(Drawable drawable) {
        Drawable oldDrawable = getDrawable();
        Drawable oldTd = null;
        if (oldDrawable == null) {
            oldTd = new ColorDrawable(Color.TRANSPARENT);
        } else if (oldDrawable instanceof TransitionDrawable) {
            oldTd = ((TransitionDrawable) oldDrawable).getDrawable(1);
        } else {
            oldTd = oldDrawable;
        }
        return new TransitionDrawable(new Drawable[]{
                oldTd,
                drawable
        });

    }

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