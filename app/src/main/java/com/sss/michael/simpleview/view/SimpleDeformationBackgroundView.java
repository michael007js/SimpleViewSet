package com.sss.michael.simpleview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.sss.michael.simpleview.R;
import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;

import androidx.annotation.Nullable;

/**
 * @author Michael by Administrator
 * @date 2022/8/12 15:13
 * @Description 一个简单的异形背景view
 */
@SuppressWarnings("ALL")
public class SimpleDeformationBackgroundView extends View {
    /**
     * 宽高
     */
    private int width, height;
    /**
     * 画笔
     */
    private Paint paint = new Paint();
    /**
     * 轨迹
     */
    private Path path = new Path();

    /**
     * 文字
     */
    private String text = "标签";
    /**
     * 文字颜色
     */
    private int textColor = Color.BLACK;
    /**
     * 文字大小
     */
    private float textSize = DensityUtil.dp2px(14);
    /**
     * 文字字体
     */
    private Typeface textStyle = Typeface.DEFAULT_BOLD;
    /**
     * 背景颜色
     */
    private int backgroundColor = Color.RED;

    {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
    }

    /**
     * 斜边往内部溃缩量
     */
    private float obliqueOffset = DensityUtil.dp2px(13);
    /**
     * 右上角圆角
     */
    private float radiusRightTop = DensityUtil.dp2px(10);

    /**
     * 右下角圆角
     */
    private float radiusRightBottom = DensityUtil.dp2px(10);

    /**
     * 左下角圆角
     */
    private float radiusLeftBottom = DensityUtil.dp2px(10);

    /**
     * 左上角圆角
     */
    private float radiusLeftTop = DensityUtil.dp2px(10);

    /**
     * 左侧边是否斜置 为true时radiusLeftTop不可用，默认为0
     */
    private boolean obliqueLeft;
    /**
     * 右侧边是否斜置 为true时radiusRightBottom不可用，默认为0
     */
    private boolean obliqueRight;


    public SimpleDeformationBackgroundView(Context context) {
        this(context, null);
    }

    public SimpleDeformationBackgroundView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleDeformationBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SimpleDeformationBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SimpleDeformationBackgroundView);
        textSize = ta.getFloat(R.styleable.SimpleDeformationBackgroundView_dbv_text_size, textSize);
        text= ta.getString(R.styleable.SimpleDeformationBackgroundView_dbv_text);
        if (text != null || !"".equals(text)) {
            text = "label";
        }
        int style = ta.getInt(R.styleable.SimpleDeformationBackgroundView_dbv_text_style, 0);
        if (style == 0) {
            textStyle = Typeface.DEFAULT_BOLD;
        } else {
            textStyle = Typeface.DEFAULT;
        }

        textColor = ta.getColor(R.styleable.SimpleDeformationBackgroundView_dbv_textColor, textColor);
        backgroundColor = ta.getColor(R.styleable.SimpleDeformationBackgroundView_dbv_background_color, backgroundColor);
        obliqueOffset = ta.getDimension(R.styleable.SimpleDeformationBackgroundView_dbv_oblique_offset, 13);
        radiusRightTop = ta.getDimension(R.styleable.SimpleDeformationBackgroundView_dbv_radius_right_top, 10);
        radiusRightBottom = ta.getDimension(R.styleable.SimpleDeformationBackgroundView_dbv_radius_right_bottom, 10);
        radiusLeftBottom = ta.getDimension(R.styleable.SimpleDeformationBackgroundView_dbv_radius_left_bottom, 10);
        radiusLeftTop = ta.getDimension(R.styleable.SimpleDeformationBackgroundView_dbv_radius_left_top, 10);

        obliqueLeft = ta.getBoolean(R.styleable.SimpleDeformationBackgroundView_dbv_oblique_left, obliqueLeft);
        obliqueRight = ta.getBoolean(R.styleable.SimpleDeformationBackgroundView_dbv_oblique_right, obliqueRight);


        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int[] size;
        if (text == null || "".equals(text)) {
            size = new int[]{10, DensityUtil.dp2px(10)};
        } else {
            paint.setTypeface(textStyle);
            paint.setTextSize(textSize);
            size = DrawViewUtils.getTextWH(paint, text);
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        switch (widthMode) {
            case MeasureSpec.AT_MOST:
                width = size[0] + getPaddingStart() + getPaddingEnd();
                break;
            case MeasureSpec.EXACTLY:
                width = widthSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                width = size[0] + getPaddingStart() + getPaddingEnd();
                break;
            default:
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.AT_MOST:
                height = size[1] + getPaddingTop() + getPaddingBottom();
                break;
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
            case MeasureSpec.UNSPECIFIED:

                height = size[1] + getPaddingTop() + getPaddingBottom();
                break;
            default:
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        /*
        以下是不带斜边的轨迹点
        path.reset();
        if (radiusLeftTop > 0) {
            path.moveTo(radiusLeftTop, 0);
        } else {
            path.moveTo(0, 0);
        }

        if (radiusRightTop > 0) {
            path.lineTo(getWidth() - radiusRightTop, 0);
            path.quadTo(getWidth(), 0, getWidth(), radiusRightTop);
        } else {
            path.lineTo(getWidth(), 0);
        }
        if (radiusRightBottom > 0) {
            path.lineTo(getWidth(), getHeight() - radiusRightBottom);
            path.quadTo(getWidth(), getHeight(), getWidth() - radiusRightBottom, getHeight());
        } else {
            path.lineTo(getWidth(), getHeight());
        }


        if (radiusLeftBottom > 0) {
            path.lineTo(radiusLeftBottom, getHeight());
            path.quadTo(0, getHeight(), 0, getHeight() - radiusLeftBottom);
        } else {
            path.lineTo(0, getHeight());
        }
        if (radiusLeftTop > 0) {
            path.lineTo(0, radiusLeftTop);
            path.quadTo(0, 0, radiusLeftTop, 0);
        } else {
            path.lineTo(0, 0);
        }
        path.close();
        */

        path.reset();
        if (radiusLeftTop > 0) {
            path.moveTo(radiusLeftTop, 0);
        } else {
            path.moveTo(0, 0);
        }
        if (obliqueRight) {
            radiusRightBottom = 0;
        }
        if (obliqueLeft) {
            radiusLeftTop = 0;
        }


        if (obliqueRight) {
            if (radiusRightTop > 0) {
                path.lineTo(getWidth() - radiusRightTop - obliqueOffset, 0);
                path.quadTo(getWidth() - obliqueOffset, 0, getWidth() - radiusRightTop, radiusRightTop);
            } else {
                path.lineTo(getWidth(), 0);
            }
        } else {
            if (radiusRightTop > 0) {
                path.lineTo(getWidth() - radiusRightTop, 0);
                path.quadTo(getWidth(), 0, getWidth(), radiusRightTop);
            } else {
                path.lineTo(getWidth(), 0);
            }
        }
        if (radiusRightBottom > 0) {
            path.lineTo(getWidth(), getHeight() - radiusRightBottom);
            path.quadTo(getWidth(), getHeight(), getWidth() - radiusRightBottom, getHeight());
        } else {
            path.lineTo(getWidth(), getHeight());
        }

        if (obliqueLeft) {
            if (radiusLeftBottom > 0) {
                path.lineTo(radiusLeftBottom + obliqueOffset, getHeight());
                path.quadTo(obliqueOffset, getHeight(), radiusLeftBottom, getHeight() - radiusLeftBottom);
            } else {
                path.lineTo(0, getHeight());
            }
        } else {
            if (radiusLeftBottom > 0) {
                path.lineTo(radiusLeftBottom, getHeight());
                path.quadTo(0, getHeight(), 0, getHeight() - radiusLeftBottom);
            } else {
                path.lineTo(0, getHeight());
            }
        }

        if (radiusLeftTop > 0) {
            path.lineTo(0, radiusLeftTop);
            path.quadTo(0, 0, radiusLeftTop, 0);
        } else {
            path.lineTo(0, 0);
        }
        path.close();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        paint.setColor(backgroundColor);
        canvas.drawPath(path, paint);


        paint.setColor(textColor);
        paint.setTypeface(textStyle);
        paint.setTextSize(textSize);
        int[] size = DrawViewUtils.getTextWH(paint, text);

        int offset = text.matches("^[\u4e00-\u9fa5]*") ? -DensityUtil.dp2px(1.2f) /*纯中文偏移量1.2*/ : -DensityUtil.dp2px(3);/*非纯中文字符考虑‘g’或‘y’的极端情况的偏移2*/
        canvas.drawText(
                text,
                width / 2,
                height / 2 + size[1] / 2 + (offset),
                paint
        );
    }
}
