package com.sss.michael.simpleview.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * @author Michael by Administrator
 * @date 2022/8/17 17:45
 * @Description 转场ImageView
 */
public class TransitionImageView extends AppCompatImageView {
    /**
     * 模型队列
     */
    private List<TransitionImageViewBean> drawables = new ArrayList<>();
    /**
     * 上一次的开始结束下标
     */
    private int lastPosition;

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
     * 占位
     * 一般用于网络图片的加载，预先在队列中指定一个位置，等到图片下载完成后再调用{@link #updateBean(int, TransitionImageViewBean)}来更新队列
     */
    public void prepareBean() {
        TransitionImageView.TransitionImageViewBean transitionImageViewBean = new TransitionImageView.TransitionImageViewBean();
        drawables.add(transitionImageViewBean);
    }

    /**
     * 更新队列中指定位置的模型
     */
    public void updateBean(int targetPosition, TransitionImageViewBean transitionImageViewBean) {
        if (targetPosition >= 0 && targetPosition <= drawables.size() - 1) {
            drawables.get(targetPosition).drawable = transitionImageViewBean.drawable;
            drawables.get(targetPosition).url = transitionImageViewBean.url;
        }
        lastPosition = 0;
    }

    /**
     * 获取队列
     */
    public List<TransitionImageViewBean> getDrawables() {
        return new ArrayList<>(drawables);
    }

    /**
     * 获取队列长度
     */
    public int getSize() {
        return drawables.size();
    }

    /**
     * 开始过渡转场
     */
    public void start() {
        int position = lastPosition + 1;
        start(position);
    }

    /**
     * 开始过渡转场
     */
    public void start(int position) {
        int fromPosition = lastPosition;
        int nextPosition = position;

        if (nextPosition > drawables.size() - 1) {
            nextPosition = 0;
        }
        if (nextPosition < 0) {
            nextPosition = 0;
        }


//        Log.e("SSSSS", fromPosition + "---"+nextPosition);
        Drawable[] drawable = new Drawable[2];

        drawable[0] = drawables.get(fromPosition).drawable;
        drawable[1] = drawables.get(nextPosition).drawable;
        lastPosition = nextPosition;

        if (drawable[0] != null && drawable[1] != null) {
            setScaleType(ImageView.ScaleType.CENTER_CROP);
            setImageDrawable(createDrawable(drawable[1]));
        }
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
        TransitionDrawable td = new TransitionDrawable(new Drawable[]{
                oldTd,
                drawable
        });
        td.startTransition(1000);
        return td;
    }

    public static class TransitionImageViewBean {
        /**
         * 图像
         */
        private Drawable drawable;
        /**
         * 网络图片链接
         */
        private String url;

        public TransitionImageViewBean() {
        }

        public TransitionImageViewBean(String url, Drawable drawable) {
            this.url = url;
            this.drawable = drawable;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Drawable getDrawable() {
            return drawable;
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

        @Override
        public String toString() {
            return "TransitionImageViewBean{" +
                    "drawable=" + drawable +
                    ", url='" + url + '\'' +
                    '}';
        }
    }
}