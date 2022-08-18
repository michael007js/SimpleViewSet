package com.sss.michael.simpleview.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.service.autofill.ImageTransformation;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sss.michael.simpleview.R;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class MyViewPager<T> extends ViewGroup {
    private List<T> models = new ArrayList<>();
    private TransitionImageView left, middle, right;
    private int width;
    private int height;
    /**
     * 上一次的开始结束下标
     */
    private int currentPosition;
    private OnMyViewPagerCallBack onMyViewPagerCallBack;

    public void setOnMyViewPagerCallBack(OnMyViewPagerCallBack onMyViewPagerCallBack) {
        this.onMyViewPagerCallBack = onMyViewPagerCallBack;
    }

    public MyViewPager(Context context) {
        this(context, null);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MyViewPager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (left == null) {
            left = new TransitionImageView(context);
            left.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        if (middle == null) {
            middle = new TransitionImageView(context);
            middle.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        if (right == null) {
            right = new TransitionImageView(context);
            right.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        addView(left);
        addView(middle);
        addView(right);


//        models.add(R.mipmap.xhr1);
//        models.add(R.mipmap.xhr2);
//        models.add(R.mipmap.xhr3);
//        models.add(R.mipmap.xhr4);
//        models.add(R.mipmap.xhr5);
//        int[] position = getPosition(currentPosition);
//        left.setImageResource(models.get(position[0]));
//        middle.setImageResource(models.get(position[1]));
//        right.setImageResource(models.get(position[2]));
    }

    int[] getPosition(int position) {
        int[] positions = new int[3];

        int prePosition = position;
        if (prePosition > models.size() - 1) {
            positions[1] = 0;
        } else if (prePosition < 0) {
            positions[1] = models.size() - 1;
        } else {
            positions[1] = prePosition;
        }
        currentPosition = positions[1];


        int preLastPosition = positions[1] - 1;
        if (preLastPosition < 0) {
            positions[0] = models.size() - 1;
        } else if (preLastPosition > models.size() - 1) {
            positions[0] = 0;
        } else {
            positions[0] = preLastPosition;
        }


        int nextPosition = positions[1] + 1;
        if (nextPosition > models.size() - 1) {
            positions[2] = 0;
        } else if (nextPosition < 0) {
            positions[2] = models.size() - 1;
        } else {
            positions[2] = nextPosition;
        }

        return positions;
    }

    int originLeft, originTop, originRight, originBottom;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (originRight == 0 || originBottom == 0) {
            originLeft = l;
            originTop = t;
            originRight = r;
            originBottom = b;
        }

        width = originRight - originLeft;
        height = originBottom - originTop;
        middle.layout(0, 0, width, height);
        layoutChild(0);
    }


    private void layoutChild(float x) {
        if (touchLock || releaseLock) {
            middle.layout((int) (middle.getLeft() + x), 0, (int) (middle.getLeft() + width + x), height);
            left.layout(middle.getLeft() - width, 0, middle.getLeft(), height);
            right.layout(middle.getRight(), 0, middle.getRight() + width, height);
        }
    }

    ValueAnimator valueAnimator;

    void releaseToOriginal(final boolean update, final Direction direction, final float from, final float end) {
        if (!touchLock) {
            return;
        }
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.removeAllUpdateListeners();
            valueAnimator.removeAllListeners();
        }
        valueAnimator = ValueAnimator.ofFloat(from, end);
        valueAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                releaseLock = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (direction == Direction.LEFT_TO_RIGHT) {
                    if (update) {
                        currentPosition--;
                        int[] position = getPosition(currentPosition);
                        if (onMyViewPagerCallBack != null) {
                            onMyViewPagerCallBack.setImage(models.get(position[0]), left);
                            onMyViewPagerCallBack.setImage(models.get(position[1]), middle);
                            onMyViewPagerCallBack.setImage(models.get(position[2]), right);
                        }
//                        left.setImageResource(models.get(position[0]));
//                        middle.setImageResource(models.get(position[1]));
//                        right.setImageResource(models.get(position[2]));
                        layoutChild(-width);
                    }
                } else if (direction == Direction.RIGHT_TO_LEFT) {
                    if (update) {
                        currentPosition++;
                        int[] position = getPosition(currentPosition);
                        if (onMyViewPagerCallBack != null) {
                            onMyViewPagerCallBack.setImage(models.get(position[0]), left);
                            onMyViewPagerCallBack.setImage(models.get(position[1]), middle);
                            onMyViewPagerCallBack.setImage(models.get(position[2]), right);
                        }
//                        left.setImageResource(models.get(position[0]));
//                        middle.setImageResource(models.get(position[1]));
//                        right.setImageResource(models.get(position[2]));
                        layoutChild(width);
                    }
                }
                releaseLock = false;
            }
        });
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                middle.layout((int) (value), 0, (int) (width + value), height);
                left.layout(middle.getLeft() - width, 0, middle.getLeft(), height);
                right.layout(middle.getRight(), 0, middle.getRight() + width, height);
            }
        });
        valueAnimator.setDuration(500);
        valueAnimator.start();

    }


    /**
     * 触摸方向意图
     */
    private Direction direction = Direction.NORMAL;
    /**
     * 上一次X轴Y轴的触摸位置
     */
    private float downX;
    /**
     * X轴偏移量
     */
    private float offsetX;
    /**
     * X轴相较于上一个点的偏移量
     */
    private float offsetByLastX;
    /**
     * 上一个点的X轴位置
     */
    private float lastX;
    private boolean touchLock;
    private boolean releaseLock;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                downX = event.getX();
                lastX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                offsetX = event.getX() - downX;
                if (offsetX < 0) {
                    direction = Direction.RIGHT_TO_LEFT;
                } else {
                    direction = Direction.LEFT_TO_RIGHT;
                }
                offsetByLastX = event.getX() - lastX;
                lastX = event.getX();
                if (Math.abs(offsetByLastX) > 10) {
                    touchLock = true;
                }
                layoutChild(offsetByLastX);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (offsetX < 0) {
                    if (Math.abs(offsetX) > width / 2) {
                        releaseToOriginal(true, Direction.RIGHT_TO_LEFT, middle.getLeft(), -width);
                    } else {
                        //没有滑动到一半，回到原始位置
                        releaseToOriginal(false, Direction.RIGHT_TO_LEFT, offsetX, 0);
                    }
                } else {
                    if (Math.abs(offsetX) > width / 2) {
                        releaseToOriginal(true, Direction.LEFT_TO_RIGHT, middle.getLeft(), width);
                    } else {
                        //没有滑动到一半，回到原始位置
                        releaseToOriginal(false, Direction.LEFT_TO_RIGHT, offsetX, 0);
                    }
                }
                touchLock = false;
                break;
        }

        return true;
    }


    public enum Direction {
        RIGHT_TO_LEFT,
        LEFT_TO_RIGHT,
        NORMAL,
    }


    public interface OnMyViewPagerCallBack<T> {
        void onDataChange(T t, int position);

        void setImage(T t, TransitionImageView imageView);
    }
}