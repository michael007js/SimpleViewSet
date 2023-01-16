package com.sss.michael.simpleview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author Michael by 61642
 * @date 2023/1/13 16:48
 * @Description 一个简单的涂鸦控件
 */
public class SimpleGraffitiView extends View {
    private int value = 5;
    private List<PointTrajectory> points = new ArrayList<>();
    private Paint paint = new Paint();

    {


        paint.setAntiAlias(true); // 消除锯齿
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
    }

    private OnSimpleGraffitiViewCallBack onSimpleGraffitiViewCallBack;

    public void setOnSimpleGraffitiViewCallBack(OnSimpleGraffitiViewCallBack onSimpleGraffitiViewCallBack) {
        this.onSimpleGraffitiViewCallBack = onSimpleGraffitiViewCallBack;
    }

    public SimpleGraffitiView(Context context) {
        super(context);
    }

    public SimpleGraffitiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleGraffitiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    PointF point = new PointF(0, 0);

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                PointTrajectory pointTrajectory = new PointTrajectory();
                points.add(pointTrajectory);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (points.size() > 0) {
                    if (Math.abs(event.getX() - point.x) > value || Math.abs(event.getY() - point.y) > value) {
                        points.get(points.size() - 1).trajectoryPoints.add(new PointF(event.getX(), event.getY()));
                        point.set(event.getX(), event.getY());


                        if (points.size() > 0) {
                            if (points.get(points.size() - 1).trajectoryPoints.size() >= 1) {
                                for (int i = 0; i < points.get(points.size() - 1).trajectoryPoints.size(); i++) {
                                    float x = points.get(points.size() - 1).trajectoryPoints.get(i).x;
                                    float y = points.get(points.size() - 1).trajectoryPoints.get(i).y;
                                    if (i == 0) {

                                        points.get(points.size() - 1).path.moveTo(x,y);
                                    } else if (i == points.get(points.size() - 1).trajectoryPoints.size() - 1) {

                                    } else {
                                        float nextX = points.get(points.size() - 1).trajectoryPoints.get(i+1).x;
                                        float nextY = points.get(points.size() - 1).trajectoryPoints.get(i+1).y;
                                        points.get(points.size() - 1).path.quadTo(x,y,nextX,nextY);
                                        points.get(points.size() - 1).path.moveTo(nextX,nextY);
                                    }
                                }
                            }
                        }
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (onSimpleGraffitiViewCallBack != null) {
                    onSimpleGraffitiViewCallBack.onTrajectoryCreated(points);
                }
                break;
        }
        return super.onTouchEvent(event);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (points.size() > 0) {
            for (int i = 0; i < points.size(); i++) {
                if (!points.get(i).isRevoked) {
//                    for (int j = 0; j < points.get(i).trajectoryPoints.size(); j++) {
//                        if (j >= 1) {
//                            canvas.drawLine(
//                                    points.get(i).trajectoryPoints.get(j - 1).x,
//                                    points.get(i).trajectoryPoints.get(j - 1).y,
//                                    points.get(i).trajectoryPoints.get(j).x, points.get(i).trajectoryPoints.get(j).y,
//                                    paint
//                            );
//                        }
//                    }

                    canvas.drawPath(points.get(i).path,paint);
                }
            }
        }
    }


    /**
     * 撤销最近一条绘制轨迹
     */
    public void revoked() {
        int index = -1;
        for (int i = points.size() - 1; i >= 0; i--) {
            if (!points.get(i).isRevoked) {
                index = i;
                break;
            }

        }
        if (index >= 0 && index <= points.size() - 1) {
            points.get(index).isRevoked = true;
        }
        if (onSimpleGraffitiViewCallBack != null) {
            onSimpleGraffitiViewCallBack.onTrajectoryCreated(points);
        }
        invalidate();
    }

    /**
     * 重做最近撤销的轨迹
     */
    public void recovery() {
        int index = -1;
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).isRevoked) {
                index = i;
                break;
            }
        }
        if (index >= 0 && index <= points.size() - 1) {
            points.get(index).isRevoked = false;
        }
        if (onSimpleGraffitiViewCallBack != null) {
            onSimpleGraffitiViewCallBack.onTrajectoryCreated(points);
        }
        invalidate();
    }


    /**
     * 轨迹模型
     */
    public static class PointTrajectory {
        /**
         * 路径
         */
        private Path path = new Path();
        /**
         * 轨迹是否撤销
         */
        private boolean isRevoked;
        /**
         * 轨迹点
         */
        private List<PointF> trajectoryPoints = new ArrayList<>();

        public boolean isRevoked() {
            return isRevoked;
        }
    }


    public interface OnSimpleGraffitiViewCallBack {
        void onTrajectoryCreated(List<PointTrajectory> points);
    }
}
