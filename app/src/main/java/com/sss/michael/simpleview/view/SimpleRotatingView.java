package com.sss.michael.simpleview.view;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * @author Michael by Administrator
 * @date 2021/2/2 18:00
 * @Description 一个简单的带3D翻转的约束布局
 */
public class SimpleRotatingView extends ConstraintLayout {
    /**
     * 动画持续时间
     */
    private static final int ANIMATION_DURATION = 200;
    /**
     * 中心点X
     */
    private int centerX;
    /**
     * 中心点Y
     */
    private int centerY;
    /**
     * Z轴缩放程序
     */
    private int depthZ = 400;
    private Rotate3dAnimation openAnimation;
    private OnRotatingViewCallBack onRotatingViewCallBack;

    public SimpleRotatingView setOnRotatingViewCallBack(OnRotatingViewCallBack onRotatingViewCallBack) {
        this.onRotatingViewCallBack = onRotatingViewCallBack;
        return this;
    }

    public SimpleRotatingView(Context context) {
        super(context);
    }

    public SimpleRotatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleRotatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void start() {
//        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                if (!isAttachedToWindow()) {
//                    return;
//                }
                centerX = getMeasuredWidth() / 2;
                centerY = getMeasuredHeight() / 2;
                if (openAnimation == null) {
                    openAnimation = new Rotate3dAnimation(0, 90, centerX, centerY, depthZ, true);
                    openAnimation.setDuration(ANIMATION_DURATION);
                    openAnimation.setFillAfter(true);
                    openAnimation.setInterpolator(new AccelerateInterpolator());
                    openAnimation.setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if (onRotatingViewCallBack != null) {
                                onRotatingViewCallBack.onMiddle(SimpleRotatingView.this);
                            }
                            //从270到360度，顺时针旋转视图，此时reverse参数为false，达到360度动画结束时视图变得可见
                            Rotate3dAnimation rotateAnimation = new Rotate3dAnimation(270, 360, centerX, centerY, depthZ, false);
                            rotateAnimation.setDuration(ANIMATION_DURATION);
                            rotateAnimation.setFillAfter(true);
                            rotateAnimation.setInterpolator(new DecelerateInterpolator());
                            rotateAnimation.startNow();
                            setAnimation(rotateAnimation);
                            startAnimation(rotateAnimation);
                        }
                    });
                }

                setAnimation(openAnimation);
                startAnimation(openAnimation);
//            }
//        });
    }

    public interface OnRotatingViewCallBack {
        void onMiddle(SimpleRotatingView rotatingView);
    }


    public static class Rotate3dAnimation extends Animation {
        private final float mFromDegrees;
        private final float mToDegrees;
        private final float mCenterX;
        private final float mCenterY;
        private final float mDepthZ;
        private final boolean mReverse;
        private Camera mCamera;

        public Rotate3dAnimation(float fromDegrees, float toDegrees,
                                 float centerX, float centerY, float depthZ, boolean reverse) {
            mFromDegrees = fromDegrees;
            mToDegrees = toDegrees;
            mCenterX = centerX;
            mCenterY = centerY;
            mDepthZ = depthZ;
            mReverse = reverse;
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            mCamera = new Camera();
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            final float fromDegrees = mFromDegrees;
            float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

            final float centerX = mCenterX;
            final float centerY = mCenterY;
            final Camera camera = mCamera;

            final Matrix matrix = t.getMatrix();

            camera.save();
            if (mReverse) {
                camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
            } else {
                camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));
            }
            camera.rotateY(degrees);
            camera.getMatrix(matrix);
            camera.restore();

            matrix.preTranslate(-centerX, -centerY);
            matrix.postTranslate(centerX, centerY);
        }
    }
}
