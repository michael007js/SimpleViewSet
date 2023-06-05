package com.sss.michael.simpleview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.sss.michael.simpleview.bean.BottomBarModel;
import com.sss.michael.simpleview.utils.AssetsUtil;
import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;
import com.sss.michael.simpleview.utils.JsonUtils;
import com.sss.michael.simpleview.utils.Log;


import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

@SuppressWarnings("ALL")
public class BottomNavigationBar extends View {
    private final boolean DEBUG = false;

    private final int DEFAULT_HEIGHT = DensityUtil.dp2px(50);
    /**
     * 宽高
     */
    private int width, height = DEFAULT_HEIGHT;
    /**
     * 预留高度
     * 用来充当大图模式的伸出区域
     */
    private int reserveArealHeight = DensityUtil.dp2px(10);
    /**
     * 上下内边距
     */
    private int paddingTop = DensityUtil.dp2px(5), paddingBottom = DensityUtil.dp2px(5);
    /**
     * 角标是否由配置决定 值为true,{@link #setCornerMarkByLabel(String, String)}将失效
     */
    private boolean cornerMarkByConfig;
    /**
     * 背景矩阵
     */
    private Rect backgroundRect = new Rect();
    private OnBottomNavigationBarCallBack onBottomNavigationBarCallBack;


    private List<BottomNavigationBarItem> items = new ArrayList<>();

    public void clear() {
        items.clear();
        invalidate();
    }

    public BottomNavigationBar(Context context) {
        this(context, null);
    }

    public BottomNavigationBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomNavigationBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    public void setOnBottomNavigationBarCallBack(OnBottomNavigationBarCallBack onBottomNavigationBarCallBack) {
        this.onBottomNavigationBarCallBack = onBottomNavigationBarCallBack;
    }


    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
        height = params.height;
        requestLayout();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, height);
        //背景区域
        backgroundRect.left = 0;
        backgroundRect.top = reserveArealHeight;
        backgroundRect.right = width;
        backgroundRect.bottom = height;

        for (int i = 0; i < items.size(); i++) {
            //无效绘制区域
            if (i == 0) {
                items.get(i).inVaildRect.left = 0;
            } else {
                items.get(i).inVaildRect.left = items.get(i - 1).inVaildRect.right;
            }
            items.get(i).inVaildRect.right = (int) (items.get(i).inVaildRect.left + width * items.get(i).builder.weight);
            items.get(i).inVaildRect.top = 0;
            items.get(i).inVaildRect.bottom = reserveArealHeight;

            //有效绘制区域
            if (i == 0) {
                items.get(i).vaildRect.left = 0;
            } else {
                items.get(i).vaildRect.left = items.get(i - 1).vaildRect.right;
            }
            items.get(i).vaildRect.right = (int) (items.get(i).vaildRect.left + width * items.get(i).builder.weight);
            items.get(i).vaildRect.top = paddingTop + reserveArealHeight;
            items.get(i).vaildRect.bottom = height - paddingBottom;

            if (items.get(i).builder.bigImage) {
                //大图，不包含文字区域，如果图片过大有可能会超出有效绘制区域
                items.get(i).imageRect.left = getRectCenterX(items.get(i).vaildRect) - items.get(i).getImageWidth() / 2;
                items.get(i).imageRect.right = items.get(i).imageRect.left + items.get(i).getImageWidth();
                items.get(i).imageRect.bottom = items.get(i).vaildRect.bottom;
                items.get(i).imageRect.top = items.get(i).imageRect.bottom - items.get(i).getImageHeight();
            } else {
                //非大图，正常图片区域
                items.get(i).imageRect.left = getRectCenterX(items.get(i).vaildRect) - items.get(i).getImageWidth() / 2;
                items.get(i).imageRect.right = items.get(i).imageRect.left + items.get(i).getImageWidth();
                items.get(i).imageRect.top = items.get(i).vaildRect.top;
                items.get(i).imageRect.bottom = items.get(i).imageRect.top + items.get(i).getImageHeight();

                //文字区域
                int[] size = DrawViewUtils.getTextWH(items.get(i).getLabelPaint(textPaint), items.get(i).builder.label);
                items.get(i).textRect.left = items.get(i).imageRect.left;
                items.get(i).textRect.right = items.get(i).imageRect.right;
                items.get(i).textRect.top = items.get(i).imageRect.bottom + items.get(i).builder.betweenImageAndText;
                items.get(i).textRect.bottom = items.get(i).textRect.top + size[1];
            }
            //角标区域
            if (items.get(i).builder.cornerMark != null && !"".equals(items.get(i).builder.cornerMark)) {
                int[] size = DrawViewUtils.getTextWH(items.get(i).getCornerMarkPaint(cornerMarkPaint), items.get(i).builder.cornerMark);
                items.get(i).cornerMarkRect.left = items.get(i).imageRect.right - size[0] / 2 - items.get(i).builder.cornerMarkPaddingHorizontal;
                items.get(i).cornerMarkRect.right = items.get(i).cornerMarkRect.left + size[0] + items.get(i).builder.cornerMarkPaddingHorizontal * 2;
                items.get(i).cornerMarkRect.top = items.get(i).imageRect.top;
                items.get(i).cornerMarkRect.bottom = items.get(i).cornerMarkRect.top + size[1] + items.get(i).builder.cornerMarkPaddingVertical * 2;
            }
        }
    }


    Paint debugPaint = new Paint();
    Paint paint = new Paint();
    Paint textPaint = new Paint();
    Paint cornerMarkPaint = new Paint();

    {
        debugPaint.setAntiAlias(true);
        debugPaint.setStrokeWidth(1.0f);
        debugPaint.setColor(Color.BLACK);

        paint.setAntiAlias(true);
        cornerMarkPaint.setAntiAlias(true);
        textPaint.setAntiAlias(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int position = -1;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).vaildRect.contains((int) event.getX(), (int) event.getY())) {
                        position = i;
                        break;
                    }
                }
                if (position != -1) {
                    if (!items.get(position).builder.bigImage) {
                        for (int i = 0; i < items.size(); i++) {
                            items.get(i).builder.isChecked = i == position;
                        }
                        invalidate();
                    }
                    if (onBottomNavigationBarCallBack != null) {
                        onBottomNavigationBarCallBack.onBottomNavigationBarItemClick(position, items.get(position).builder.bigImage, items.get(position).extra);
                    }
                }

            default:
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //透明背景
        paint.setColor(Color.TRANSPARENT);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(paint);

        //绘制白色有效区域
        paint.setColor(Color.WHITE);
        paint.setXfermode(null);
        canvas.drawRect(backgroundRect, paint);

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getImage() != null) {
                if (items.get(i).getImageHeight() != 0 && items.get(i).getImageWidth() != 0) {
                    if (items.get(i).getImage() instanceof GifDrawable) {
                        ((GifDrawable) items.get(i).getImage()).start();
                        ((GifDrawable) items.get(i).getImage()).setBounds(items.get(i).imageRect.left, items.get(i).imageRect.top, items.get(i).imageRect.right, items.get(i).imageRect.bottom);
                        ((GifDrawable) items.get(i).getImage()).draw(canvas);
                    } else if (items.get(i).getImage() instanceof Bitmap) {
                        if (!((Bitmap) items.get(i).getImage()).isRecycled()) {
                            canvas.drawBitmap((Bitmap) items.get(i).getImage(), null, items.get(i).imageRect, paint);
                        }
                    }
                }
            }

            if (!items.get(i).builder.bigImage) {
                //绘制文字
                if (items.get(i).builder.isChecked) {
                    textPaint.setColor(items.get(i).builder.checkTextColor);
                } else {
                    textPaint.setColor(items.get(i).builder.unCheckTextColor);
                }
                int[] size = DrawViewUtils.getTextWH(items.get(i).getLabelPaint(textPaint), items.get(i).builder.label);
                canvas.drawText(
                        items.get(i).builder.label,
                        getRectCenterX(items.get(i).textRect),
                        getRectCenterY(items.get(i).textRect) + items.get(i).builder.textOffsetY,
                        items.get(i).getLabelPaint(textPaint)
                );
            }

            if (items.get(i).builder.cornerMark != null && !"".equals(items.get(i).builder.cornerMark)) {
                cornerMarkPaint.setColor(Color.RED);
                int[] size = DrawViewUtils.getTextWH(items.get(i).getLabelPaint(textPaint), items.get(i).builder.cornerMark);
                float roundX = Math.min(items.get(i).cornerMarkRect.width(), items.get(i).cornerMarkRect.height()) / 2;
                float roundY = Math.min(items.get(i).cornerMarkRect.width(), items.get(i).cornerMarkRect.height()) / 2;
                canvas.drawRoundRect(items.get(i).cornerMarkRect, roundX, roundY, items.get(i).getCornerMarkPaint(cornerMarkPaint));
                cornerMarkPaint.setColor(Color.WHITE);
                canvas.drawText(
                        items.get(i).builder.cornerMark,
                        items.get(i).cornerMarkRect.left + items.get(i).cornerMarkRect.width() / 2,
                        items.get(i).cornerMarkRect.top + items.get(i).cornerMarkRect.height() / 2 + items.get(i).builder.textOffsetY,
                        items.get(i).getCornerMarkPaint(cornerMarkPaint)
                );

            }


            if (DEBUG) {
                debugPaint.setStyle(Paint.Style.STROKE);
                //辅助绘制有效区域矩形
                canvas.drawRect(items.get(i).vaildRect, debugPaint);
                //辅助绘制图形区域矩形
                canvas.drawRect(items.get(i).imageRect, debugPaint);
                //辅助绘制文字区域矩形
                canvas.drawRect(items.get(i).textRect, debugPaint);
                //辅助绘制角标文字区域矩形
                canvas.drawRect(items.get(i).cornerMarkRect, debugPaint);
                //辅助绘制中心线
                canvas.drawLine(items.get(i).vaildRect.left, getRectCenterY(items.get(i).vaildRect), items.get(i).vaildRect.right, getRectCenterY(items.get(i).vaildRect), debugPaint);
            }


        }

        invalidate();


    }

    private int getRectCenterX(Rect rect) {
        return rect.left + rect.width() / 2;
    }

    private int getRectCenterY(Rect rect) {
        return rect.top + rect.height() / 2;
    }


    /**
     * 设置选中项
     *
     * @param position 选中项索引
     */
    public void setItemSelected(int position) {
        if (position >= 0 && position < items.size() && items.size() > 0) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).builder != null) {
                    items.get(i).builder.isChecked = false;
                }
            }

            if (items.get(position).builder != null) {
                items.get(position).builder.isChecked = true;
                invalidate();
            }
        }
    }

    /**
     * 设置触摸选中项
     *
     * @param position 摸选中项索引
     */
    public void setTouchItemSelected(int position) {
        if (items != null && position >= 0 && position < items.size()) {
            if (!items.get(position).builder.bigImage) {
                for (int i = 0; i < items.size(); i++) {
                    items.get(i).builder.isChecked = i == position;
                }
                invalidate();
            }
            if (onBottomNavigationBarCallBack != null) {
                onBottomNavigationBarCallBack.onBottomNavigationBarItemClick(position, items.get(position).builder.bigImage, items.get(position).extra);
            }
        }
    }

    /**
     * 获取选中项索引
     *
     * @param defaultPosition 默认选中项，如果没找到，将返回此项
     * @return 索引
     */
    public int getItemSelectedPosition(int defaultPosition) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).builder.isChecked) {
                return i;
            }
        }
        return defaultPosition;
    }


    /**
     * 通过标签设置角标
     *
     * @param label      标签
     * @param cornerMark 角标内容
     */
    public void setCornerMarkByLabel(String label, String cornerMark) {
        if (cornerMarkByConfig) {
            return;
        }
        if (label != null) {
            for (int i = 0; i < items.size(); i++) {
                if (label.equals(items.get(i).builder.label)) {
                    items.get(i).builder.cornerMark = cornerMark;
                }
            }
            requestLayout();
        }
    }

    /**
     * 设置导航栏元素
     *
     * @param items 导航栏元素
     */
    public void setItems(List<BottomNavigationBarItem> items) {
        this.items = items;
        requestLayout();
    }

    /**
     * 角标是否由配置决定
     *
     * @param cornerByConfig 值为true,{@link #setCornerMarkByLabel(String, String)}将失效
     */
    public BottomNavigationBar setCornerMarkByConfig(boolean cornerMarkByConfig) {
        this.cornerMarkByConfig = cornerMarkByConfig;
        return this;
    }

    /**
     * 设置保留区域高度
     *
     * @param reserveArealHeight 保留区域高度
     */
    public BottomNavigationBar setReserveArealHeight(int reserveArealHeight) {
        this.reserveArealHeight = DensityUtil.dp2px(reserveArealHeight);
        return this;
    }

    /**
     * 设置顶部内边距
     *
     * @param paddingTop 顶边距
     */
    public BottomNavigationBar setPaddingTop(int paddingTop) {
        this.paddingTop = DensityUtil.dp2px(paddingTop);
        return this;
    }

    /**
     * 设置底部内边距
     *
     * @param paddingBottom 底边距
     */
    public BottomNavigationBar setPaddingBottom(int paddingBottom) {
        this.paddingBottom = DensityUtil.dp2px(paddingBottom);
        return this;
    }

    public static class BottomNavigationBarItem {
        /**
         * 参数
         */
        private Builder builder;
        /**
         * 额外参数
         */
        private Extra extra;
        /**
         * 理论有效绘制区域
         */
        Rect vaildRect = new Rect();
        /**
         * 理论有效绘制区域
         */
        Rect inVaildRect = new Rect();
        /**
         * 图片矩阵
         */
        Rect imageRect = new Rect();
        /**
         * 文字矩阵
         */
        Rect textRect = new Rect();
        /**
         * 角标矩阵
         */
        RectF cornerMarkRect = new RectF();
        /**
         * 选中与未选中对象
         */
        Object checked, unChecked;

        public BottomNavigationBarItem() {
            throw new RuntimeException("please call constructor with parameter");
        }

        public BottomNavigationBarItem(Extra extra) {
            this.extra = extra;
        }

        Object getImage() {
            return builder.isChecked ? checked : unChecked;
        }

        int getImageWidth() {
            if (builder.imageWidth > 0) {
                return builder.imageWidth;
            } else if (getImage() instanceof Bitmap) {
                return ((Bitmap) getImage()).getWidth();
            } else if (getImage() instanceof GifDrawable) {
                return ((GifDrawable) getImage()).getIntrinsicWidth();
            }
            return 1;
        }

        int getImageHeight() {
            if (builder.imageHeight > 0) {
                return builder.imageHeight;
            } else if (getImage() instanceof Bitmap) {
                return ((Bitmap) getImage()).getHeight();
            } else if (getImage() instanceof GifDrawable) {
                return ((GifDrawable) getImage()).getIntrinsicHeight();
            }
            return 1;
        }

        Paint getLabelPaint(Paint paint) {
            paint.setTextSize(builder.textSize);
            paint.setTypeface(Typeface.DEFAULT);
            paint.setTextAlign(Paint.Align.CENTER);
            return paint;
        }

        Paint getCornerMarkPaint(Paint paint) {
            paint.setTextSize(builder.cornerMarkTextSize);
            paint.setTypeface(Typeface.DEFAULT);
            paint.setTextAlign(Paint.Align.CENTER);
            return paint;
        }

        public BottomNavigationBarItem load(final AppCompatActivity activity, final Builder builder) {
            this.builder = builder;
            image(activity, true, builder.checkedUrl);
            image(activity, false, builder.unCheckedUrl);


            return this;
        }

        private void image(final AppCompatActivity activity, final boolean isCheckedUrl, final String url) {
            if (url != null) {
                if (url.toLowerCase().endsWith(".gif")) {
                    if (checked == null) {
                        Glide.with(activity)
                                .asGif()
                                .load(url)
                                .addListener(new RequestListener<GifDrawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(final GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (isCheckedUrl) {
                                                    checked = resource;
                                                } else {
                                                    unChecked = resource;
                                                }
                                                builder.bottomNavigationBar.requestLayout();
                                            }
                                        });
                                        return false;
                                    }
                                }).submit();
                    }
                } else {
                    Glide.with(activity).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            if (isCheckedUrl) {
                                checked = resource;
                            } else {
                                unChecked = resource;
                            }
                            builder.bottomNavigationBar.requestLayout();
                        }

                        public void onLoadFailed(@Nullable Drawable errorDrawable) {


                        }
                    });
                }
            }
        }

    }


    public static class Builder {

        /**
         * 角标
         */
        private String cornerMark;
        /**
         * 角标字体大小
         */
        private float cornerMarkTextSize;
        /**
         * 角标竖向内边距
         */
        private float cornerMarkPaddingVertical;
        /**
         * 角标横向内边距
         */
        private float cornerMarkPaddingHorizontal;
        /**
         * 额外参数
         */
        private Extra extra = new Extra();
        /**
         * 是否为大图
         */
        private boolean bigImage;
        /**
         * 选中与未选中图片链接
         */
        private String checkedUrl, unCheckedUrl;
        /**
         * 图片宽高
         */
        private int imageWidth, imageHeight;
        /**
         * 权重（横向）
         */
        private float weight;
        /**
         * 图片与文字之间的距离
         */
        private int betweenImageAndText;
        /**
         * 标题
         */
        private String label;
        /**
         * 字体大小
         */
        private float textSize;
        /**
         * 选中与未选中文字颜色
         */
        private int checkTextColor = Color.parseColor("#ff0000"), unCheckTextColor = Color.parseColor("#333333");
        /**
         * 文字部分Y轴偏移量
         */
        private int textOffsetY;
        /**
         * 是否选中
         */
        private boolean isChecked;

        private BottomNavigationBar bottomNavigationBar;

        public Builder(BottomNavigationBar bottomNavigationBar) {
            this.bottomNavigationBar = bottomNavigationBar;
        }

        public Builder setChecked(boolean checked) {
            isChecked = checked;
            return this;
        }

        public Builder setBigImage(boolean bigImage) {
            this.bigImage = bigImage;
            return this;
        }

        public Builder setCheckedUrl(String checkedUrl) {
            this.checkedUrl = checkedUrl;
            return this;
        }

        public Builder setUnCheckedUrl(String unCheckedUrl) {
            this.unCheckedUrl = unCheckedUrl;
            return this;
        }

        public Builder setImageWidth(int imageWidth) {
            this.imageWidth = imageWidth;
            return this;
        }

        public Builder setImageHeight(int imageHeight) {
            this.imageHeight = imageHeight;
            return this;
        }

        public Builder setWeight(float weight) {
            this.weight = weight;
            return this;
        }

        public Builder setBetweenImageAndText(int betweenImageAndText) {
            this.betweenImageAndText = betweenImageAndText;
            return this;
        }

        public Builder setLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder setTextSize(float textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder setTextOffsetY(int textOffsetY) {
            this.textOffsetY = textOffsetY;
            return this;
        }

        public Builder setCheckTextColor(int checkTextColor) {
            this.checkTextColor = checkTextColor;
            return this;
        }

        public Builder setUnCheckTextColor(int unCheckTextColor) {
            this.unCheckTextColor = unCheckTextColor;
            return this;
        }

        public Builder setFragmentIndex(int fragmentIndex) {
            extra.fragmentIndex = fragmentIndex;
            return this;
        }

        public Builder setPageUrl(String pageUrl) {
            extra.pageUrl = pageUrl;
            return this;
        }

        public Builder setCornerMark(String cornerMark) {
            this.cornerMark = cornerMark;
            return this;
        }

        public Builder setCornerMarkTextSize(float cornerMarkTextSize) {
            this.cornerMarkTextSize = cornerMarkTextSize;
            return this;
        }

        public Builder setCornerMarkPaddingVertical(float cornerMarkPaddingVertical) {
            this.cornerMarkPaddingVertical = cornerMarkPaddingVertical;
            return this;
        }

        public Builder setCornerMarkPaddingHorizontal(float cornerMarkPaddingHorizontal) {
            this.cornerMarkPaddingHorizontal = cornerMarkPaddingHorizontal;
            return this;
        }

        public BottomNavigationBarItem build(AppCompatActivity context) {
            weight = weight > 0 ? weight : 1.0f;
            imageWidth = DensityUtil.dp2px(imageWidth > 0 ? imageWidth : 20);
            imageHeight = DensityUtil.dp2px(imageHeight > 0 ? imageHeight : 20);
            betweenImageAndText = DensityUtil.dp2px(betweenImageAndText > 0 ? betweenImageAndText : 3);
            label = label == null ? "" : label;
            textSize = DensityUtil.sp2px(textSize > 0 ? textSize : 10);
            cornerMarkTextSize = DensityUtil.sp2px(cornerMarkTextSize > 0 ? cornerMarkTextSize : 8);
            cornerMarkPaddingVertical = DensityUtil.sp2px(cornerMarkPaddingVertical > 0 ? cornerMarkPaddingVertical : 2);
            cornerMarkPaddingHorizontal = DensityUtil.sp2px(cornerMarkPaddingHorizontal > 0 ? cornerMarkPaddingHorizontal : 4);
            textOffsetY = DensityUtil.dp2px(textOffsetY != 0 ? textOffsetY : 3);
            return new BottomNavigationBarItem(extra).load(context, this);
        }
    }

    public static class Extra {
        /**
         * fragment的实际位置（考虑到有大图，实际索引可能有变化）
         */
        private int fragmentIndex;
        /**
         * 页面链接
         */
        private String pageUrl;

        public int getFragmentIndex() {
            return fragmentIndex;
        }

        public String getPageUrl() {
            return pageUrl;
        }
    }

    /**
     * 初始化配置
     *
     * @param config
     */
    public BottomBarModel initBottomBarConfig(BottomBarModel config) {
        if (config == null) {
            config = JsonUtils.formatToObject(AssetsUtil.getFileFromAssets(getContext(), "main_tabs_config.json"), BottomBarModel.class);
        } else {
            config = config;
        }
        return config;
    }

    public interface OnBottomNavigationBarCallBack {
        void onBottomNavigationBarItemClick(int position, boolean bigImage, Extra extra);
    }
}