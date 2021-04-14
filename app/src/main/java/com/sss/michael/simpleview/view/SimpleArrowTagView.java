package com.sss.michael.simpleview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;

import androidx.annotation.Nullable;

/**
 * @author Michael by Administrator
 * @date 2021/4/13 15:20
 * @Description 一个简单的3D箭头指示标签
 */
public class SimpleArrowTagView extends View {

    private int width;
    private int height;

    private int tag = 1;
    private int[] smallTriangleColor = {Color.parseColor("#C32525"), Color.parseColor("#C27501"), Color.parseColor("#36A400")};
    private int[] bigTriangleColor = {Color.parseColor("#F56C6C"), Color.parseColor("#E6A23C"), Color.parseColor("#67C23A")};

    private int distance = DensityUtil.dp2px(6);

    /**
     * 小三角矩阵宽高
     */
    private int triangleRectWidthHeight = DensityUtil.dp2px(4);

    private Paint paint = new Paint();

    {
        paint.setAntiAlias(true);
    }

    private Path path = new Path();

    public SimpleArrowTagView(Context context) {
        super(context);
    }

    public SimpleArrowTagView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleArrowTagView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (MeasureSpec.UNSPECIFIED == MeasureSpec.getMode(widthMeasureSpec)) {
            width = DensityUtil.dp2px(26);
        } else {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        if (MeasureSpec.UNSPECIFIED == MeasureSpec.getMode(heightMeasureSpec)) {
            height = DensityUtil.dp2px(16) + triangleRectWidthHeight;
        } else {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        path.reset();
        path.moveTo(triangleRectWidthHeight, 0);
        path.lineTo(triangleRectWidthHeight, 0);
        path.lineTo(triangleRectWidthHeight, triangleRectWidthHeight);
        path.lineTo(0, triangleRectWidthHeight);
        path.lineTo(triangleRectWidthHeight, 0);
        path.close();
        paint.setColor(getSmallTriangleColor());
        canvas.drawPath(path, paint);


        path.reset();
        path.moveTo(0, triangleRectWidthHeight);
        path.lineTo(0, triangleRectWidthHeight);
        path.lineTo(getWidth() - distance, triangleRectWidthHeight);
        path.lineTo(getWidth(), triangleRectWidthHeight + (getHeight() - triangleRectWidthHeight) / 2);
        path.lineTo(getWidth() - distance, getHeight());
        path.lineTo(0, getHeight());
        path.lineTo(0, triangleRectWidthHeight);
        path.close();
        paint.setColor(getBigTriangleColor());
        canvas.drawPath(path, paint);

        paint.setTextSize(DensityUtil.sp2px(12f));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        int[] wh = DrawViewUtils.getTextWH(paint, tag + "");
        canvas.drawText(tag + "", getWidth() / 2 - triangleRectWidthHeight / 2, (getHeight() - triangleRectWidthHeight) / 2 + wh[1], paint);
    }

    public void setTag(int tag) {
        this.tag = tag;
        invalidate();
    }

    private int getSmallTriangleColor(){
        switch (tag){
            case 1:
                return smallTriangleColor[0];
            case 2:
                return smallTriangleColor[1];
            case 3:
                return smallTriangleColor[2];
            default:
                return smallTriangleColor[0];
        }
    }

    private int getBigTriangleColor(){
        switch (tag){
            case 1:
                return bigTriangleColor[0];
            case 2:
                return bigTriangleColor[1];
            case 3:
                return bigTriangleColor[2];
            default:
                return bigTriangleColor[0];
        }
    }
}
