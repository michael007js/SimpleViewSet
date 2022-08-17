package com.sss.michael.simpleview.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author Michael by Administrator
 * @date 2022/8/17 17:45
 * @Description 转场ImageView
 */
public class TransitionImageView extends androidx.appcompat.widget.AppCompatImageView {
    /**
     * 模型队列
     */
    private List<TransitionImageViewBean> drawables = new ArrayList<>();

    public TransitionImageView(@NonNull Context context) {
        super(context);
    }

    public TransitionImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TransitionImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * 占位
     * 一般用于网络图片的加载，预先在队列中指定一个位置，等到图片下载完成后再调用{@link #updateBean(int, TransitionImageViewBean)}来更新队列
     *
     * @param targetPosition 占位索引
     */
    public void prepareBean(int targetPosition) {
        TransitionImageView.TransitionImageViewBean transitionImageViewBean = new TransitionImageView.TransitionImageViewBean();
        transitionImageViewBean.setPosition(targetPosition);
        insertBean(transitionImageViewBean);
    }

    /**
     * 新增
     * 在队列尾开辟一个新的位置
     */
    public void insertBean(TransitionImageView.TransitionImageViewBean transitionImageViewBean) {
        drawables.add(transitionImageViewBean);
    }

    /**
     * 更新队列中指定位置的模型
     */
    public void updateBean(int targetPosition, TransitionImageViewBean transitionImageViewBean) {
        for (int i = 0; i < drawables.size(); i++) {
            if (drawables.get(i).position == targetPosition) {
                drawables.get(i).drawable = transitionImageViewBean.drawable;
                drawables.get(i).url = transitionImageViewBean.url;
                break;
            }
        }
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
    public void start(int fromPosition, int toPosition) {
        Drawable[] drawable = new Drawable[2];
        for (int i = 0; i < drawables.size(); i++) {
            if (drawables.get(i).position == fromPosition) {
                drawable[0] = drawables.get(i).drawable;
            }
            if (drawables.get(i).position == toPosition) {
                drawable[1] = drawables.get(i).drawable;
            }
        }

        if (drawable[0] != null && drawable[1] != null) {
            TransitionDrawable transitionDrawable = new TransitionDrawable(drawable);
            setImageDrawable(transitionDrawable);
            transitionDrawable.startTransition(300);
        }
    }

    public static class TransitionImageViewBean {
        /**
         * 图像
         */
        private Drawable drawable;
        /**
         * 唯一索引，一般由调用者指定
         */
        private int position;
        /**
         * 网络图片链接
         */
        private String url;


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

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public String toString() {
            return "TransitionImageViewBean{" +
                    "drawable=" + drawable +
                    ", position=" + position +
                    ", url='" + url + '\'' +
                    '}';
        }
    }
}