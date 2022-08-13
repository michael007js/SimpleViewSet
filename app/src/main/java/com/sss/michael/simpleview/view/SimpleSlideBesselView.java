package com.sss.michael.simpleview.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;


import com.sss.michael.simpleview.utils.DensityUtil;

import androidx.annotation.Nullable;

/**
 * @author Michael by Administrator
 * @date 2022/8/13 12:06
 * @Description 滑动边界贝塞尔效果
 */
public class SimpleSlideBesselView extends View {
    /**
     * 方向>底侧
     */
    public static int DIRECTION_BOTTOM = 0;
    /**
     * 方向>顶侧
     */
    public static int DIRECTION_TOP = 1;
    /**
     * 方向>左侧
     */
    public static int DIRECTION_LEFT = 3;
    /**
     * 方向>右侧
     */
    public static int DIRECTION_RIGHT = 4;
    /**
     * 方向
     */
    private int direction = DIRECTION_BOTTOM;
    /**
     * 保留的贝塞尔偏移量
     */
    private int originBesselOffsetValue = DensityUtil.dp2px(30);
    /**
     * 实时贝塞尔偏移量
     */
    private int currentBesselOffsetValue;
    /**
     * 宽高
     */
    private int width, height;
    /**
     * 偏移量占全局尺寸的最大百分比
     */
    private float maxOffsetPercent = 0.6f;
    /**
     * 贝塞尔部分背景色
     */
    private int besselBackground = Color.parseColor("#2678e3");
    /**
     * 背景渐变色
     */
    private String[] backgroundColor = {"#FFBE9B", "#FF7573"};
    /**
     * 画笔
     */
    private Paint paint = new Paint();
    /**
     * 轨迹
     */
    private Path path = new Path();

    {
        paint.setAntiAlias(true);
    }

    public SimpleSlideBesselView(Context context) {
        this(context, null);
    }

    public SimpleSlideBesselView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleSlideBesselView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SimpleSlideBesselView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private LinearGradient linearGradient;

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        int[] colors = new int[backgroundColor.length];
        for (int i = 0; i < backgroundColor.length; i++) {
            colors[i] = Color.parseColor(backgroundColor[i]);
        }
         linearGradient = new LinearGradient(
                0,
                 width >> 1,
                width,
                height, colors,
                null,
                Shader.TileMode.MIRROR
        );
        setOffset(0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setShader(linearGradient);
        canvas.drawRect(0, 0, width, height,paint);
        paint.setColor(besselBackground);
        paint.setShader(null);
        canvas.drawPath(path, paint);
    }


    public void setOffset(int offset) {
        if (direction == DIRECTION_TOP) {
            int max = (int) (height * maxOffsetPercent) - originBesselOffsetValue;
            if (offset > max) {
                currentBesselOffsetValue = max;
            } else {
                currentBesselOffsetValue = offset;
            }
            path.reset();
            path.moveTo(0, originBesselOffsetValue);
            path.lineTo(0, originBesselOffsetValue);
            path.quadTo(width >> 1, currentBesselOffsetValue, width, originBesselOffsetValue);
            path.lineTo(width, originBesselOffsetValue);
            path.lineTo(width, 0);
            path.lineTo(0, 0);
            path.lineTo(0, originBesselOffsetValue);
            path.close();
        } else if (direction == DIRECTION_BOTTOM) {
            int max = (int) (height * maxOffsetPercent) - originBesselOffsetValue;
            if (offset > max) {
                currentBesselOffsetValue = max;
            } else {
                currentBesselOffsetValue = offset;
            }
            path.reset();
            path.moveTo(0, height - originBesselOffsetValue);
            path.lineTo(0, height - originBesselOffsetValue);
            path.quadTo(width >> 1, height - currentBesselOffsetValue, width, height - originBesselOffsetValue);
            path.lineTo(width, height - originBesselOffsetValue);
            path.lineTo(width, height);
            path.lineTo(0, height);
            path.lineTo(0, height - originBesselOffsetValue);
            path.close();
        } else if (direction == DIRECTION_LEFT) {
            int max = (int) (width * maxOffsetPercent) - originBesselOffsetValue;
            if (offset > max) {
                currentBesselOffsetValue = max;
            } else {
                currentBesselOffsetValue = offset;
            }
            path.reset();
            path.moveTo(originBesselOffsetValue, 0);
            path.lineTo(originBesselOffsetValue, 0);
            path.quadTo(currentBesselOffsetValue, height >> 1, originBesselOffsetValue, height);
            path.lineTo(originBesselOffsetValue, height);
            path.lineTo(0, height);
            path.lineTo(0, 0);
            path.lineTo(originBesselOffsetValue, 0);
            path.close();
        } else if (direction == DIRECTION_RIGHT) {
            int max = (int) (width * maxOffsetPercent) - originBesselOffsetValue;
            if (offset > max) {
                currentBesselOffsetValue = max;
            } else {
                currentBesselOffsetValue = offset;
            }
            path.reset();
            path.moveTo(width - originBesselOffsetValue, 0);
            path.lineTo(width - originBesselOffsetValue, 0);
            path.quadTo(width - currentBesselOffsetValue, height >> 1, width - originBesselOffsetValue, height);
            path.lineTo(width - originBesselOffsetValue, height);
            path.lineTo(width, height);
            path.lineTo(width, 0);
            path.lineTo(width - originBesselOffsetValue, 0);
            path.close();
        }
        invalidate();
    }
}
