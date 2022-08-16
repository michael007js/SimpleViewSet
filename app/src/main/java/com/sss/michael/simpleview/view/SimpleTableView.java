package com.sss.michael.simpleview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.utils.DrawViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael by Administrator
 * @date 2021/4/7 11:05
 * @Description 一个简单的固定行列的可拖动列表(注意 ： 宽高取的是父布局 ， 如需正常使用的话则需要在外层再套用一个父布局)
 */
@SuppressWarnings("all")
public class SimpleTableView<T> extends View {
    private boolean debug = true;
    /**
     * 是否消费滑动事件
     */
    private boolean handleDistributionEvent = false;
    /**
     * 如果最后一行不满的话是否补满一行
     */
    private boolean isFillUp = true;
    /**
     * 当用户触发滚动事件后如果滚动到底部时最后一行的bottom未与view高度一致时自动补偿差值
     */
    private boolean autoScrollToBottomWhileScrollByUser = false;
    /**
     * 禁止拖动队列
     */
    private int[] disableColumn = {0, 1, 2};
    /**
     * 拖动偏移量
     */
    private float offsetX, offsetY;
    /**
     * 是否已经滑动到最顶部/最底部
     */
    private boolean isScrollToTop = true, isScrollToBottom = false;
    /**
     * 内边距
     */
    private float paddingLeft = DensityUtil.dp2px(10), paddingTop = DensityUtil.dp2px(12), paddingRight = DensityUtil.dp2px(10), paddingBottom = DensityUtil.dp2px(12);
    /**
     * 行/列数
     */
    private int columnCount, lineCount;
    /**
     * 上一次X轴Y轴的触摸位置
     */
    private float previousX, previousY;
    /**
     * 左右触摸方向意图
     */
    private Direction leftRightDirection = Direction.NORMAL;
    /**
     * 上下触摸方向意图
     */
    private Direction upDownDirection = Direction.NORMAL;
    /**
     * 四个方向意图，优先级高于其他两个触摸方向意图
     */
    private Direction mainDirection = Direction.NORMAL;
    /**
     * 列表集合
     */
    private List<SimpleTableViewBean> list = new ArrayList<>();
    /**
     * 画笔
     */
    private Paint paint = new Paint();
    /**
     * 动画
     */
    private ValueAnimator valueAnimator;

    private OnSimpleTableViewCallBack onSimpleTableViewCallBack;

    public void setOnSimpleTableViewCallBack(OnSimpleTableViewCallBack onSimpleTableViewCallBack) {
        this.onSimpleTableViewCallBack = onSimpleTableViewCallBack;
    }

    public SimpleTableView(Context context) {
        super(context);
        init();
    }

    public SimpleTableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleTableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setAntiAlias(true);
        if (debug) {
            List<SimpleTableViewBean> title = new ArrayList<>();
            title.add(new SimpleTableViewBean("年份", true, Color.parseColor("#ff0000")));
            title.add(new SimpleTableViewBean("科类", true, Color.parseColor("#ff0000")));
            title.add(new SimpleTableViewBean("批段", true, Color.parseColor("#ff0000")));
            title.add(new SimpleTableViewBean("录取数", true, Color.parseColor("#ff0000")));
            title.add(new SimpleTableViewBean("最低分", true, Color.parseColor("#ff0000")));
            title.add(new SimpleTableViewBean("最低位", true, Color.parseColor("#ff0000")));
            title.add(new SimpleTableViewBean("最高分", true, Color.parseColor("#ff0000")));
            title.add(new SimpleTableViewBean("平均分", true, Color.parseColor("#ff0000")));
            title.add(new SimpleTableViewBean("选考", true, Color.parseColor("#ff0000")));

            setOnSimpleTableViewCallBack(new OnSimpleTableViewCallBack<String>() {
                @Override
                public String onLabelValue(String o) {
                    return o;
                }

                @Override
                public void onClickItem(String s, int[] position) {
                    Log.e("SSSSS",s);
                }

                @Override
                public void onMeasureSize(int width, int height) {

                }
            });
            List<SimpleTableViewBean> content = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                Suffix suffix = new Suffix("征");
                suffix.textSize = DensityUtil.dp2px(10);
                suffix.textColor = Color.BLACK;
                suffix.backgroundColor = Color.RED;
                suffix.radius = DensityUtil.dp2px(2);
                suffix.typeface = Typeface.DEFAULT;
                content.add(new SimpleTableViewBean(200 + ""));
                content.add(new SimpleTableViewBean(201 + ""));
                content.add(new SimpleTableViewBean(202 + "-/202/202"));
                content.add(new SimpleTableViewBean(203 + ""));
                content.add(new SimpleTableViewBean(204 + "", suffix));

                content.add(new SimpleTableViewBean(205 + "/-/205", suffix));
                content.add(new SimpleTableViewBean(206 + "456155455456", suffix));
                content.add(new SimpleTableViewBean(207 + "", suffix));
                content.add(new SimpleTableViewBean(208 + "", suffix));
            }
            setData(title, content);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth = 0;
        int measureHeight = 0;
        if (onSimpleTableViewCallBack == null) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Suffix suffix = list.get(i).suffix;
            float[] suffixSize = {0, 0};
            if (suffix != null) {
                suffixSize = suffix.getTextSize(true);
            }


            paint.setTextSize(list.get(i).textSize);
            float[] size = DrawViewUtils.getTextWHF(paint, getValueByPosition(i));
            size[0] = size[0] + suffixSize[0];
            size[1] = Math.max(size[1], suffixSize[1]);

            list.get(i).size = size;
            int[] position = getPosition(i);
            list.get(i).position = position;
        }

        //取同一列最大宽度
        List<Float> sizes = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            paint.setTextSize(list.get(i).textSize);
            sizes.add(DrawViewUtils.getTextWHF(paint, getValueByPosition(i))[0] + paddingLeft + paddingRight);
        }
        for (int i = 0; i < list.size(); i++) {
            paint.setTextSize(list.get(i).textSize);
            float suffixSize = list.get(i).suffix != null ? list.get(i).suffix.getTextSize(true)[0] + paddingLeft + paddingRight : 0;

            float maxWidth = Math.max(sizes.get(list.get(i).position[0]), DrawViewUtils.getTextWHF(paint, getValueByPosition(i))[0] + suffixSize);
            sizes.set(list.get(i).position[0], maxWidth);
        }

        for (int i = 0; i < list.size(); i++) {
            //计算cell的X轴反向是否为可拖动
            for (int j = 0; j < disableColumn.length; j++) {
                if (list.get(i).position[0] == disableColumn[j]) {
                    list.get(i).xCanSlide = false;
                }
            }
            list.get(i).yCanSlide = list.get(i).position[1] != 0;

            if (list.get(i).position[0] % columnCount == 0) {
                list.get(i).realTimeRectF.left = 0;
            } else {
                list.get(i).realTimeRectF.left = list.get(i - 1).realTimeRectF.left + sizes.get(list.get(i - 1).position[0])/*getMaxTextWidthForColumn(i - 1)*/ + paddingLeft + paddingRight;
            }
            list.get(i).realTimeRectF.right = list.get(i).realTimeRectF.left + sizes.get(list.get(i).position[0])/*getMaxTextWidthForColumn(i)*/ + (list.get(i).suffix != null ? list.get(i).suffix.getTextSize(true)[0] : 0) + paddingLeft + paddingRight;
            //统计第一行宽度
            if (i < columnCount) {
                measureWidth = (int) (measureWidth + list.get(i).realTimeRectF.width());
            }
            /****************/
            if (i == 0) {
                list.get(i).realTimeRectF.top = 0 + getPaddingTop();
            } else {
                if (list.get(i).position[0] == 0) {
                    list.get(i).realTimeRectF.top = list.get(i - 1).realTimeRectF.top + list.get(i).size[1] + paddingTop + paddingBottom;
                } else {
                    list.get(i).realTimeRectF.top = list.get(i - 1).realTimeRectF.top;
                }
            }
            list.get(i).realTimeRectF.bottom = list.get(i).realTimeRectF.top + list.get(i).size[1] + paddingTop + paddingBottom;

            if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
                measureHeight = MeasureSpec.getSize(heightMeasureSpec);
            } else {
                //统计第一列高度
                if (i < lineCount) {
                    measureHeight = (int) (measureHeight + list.get(i).realTimeRectF.height());
                }
            }

            list.get(i).originalRectF.left = list.get(i).realTimeRectF.left;
            list.get(i).originalRectF.top = list.get(i).realTimeRectF.top;
            list.get(i).originalRectF.right = list.get(i).realTimeRectF.right;
            list.get(i).originalRectF.bottom = list.get(i).realTimeRectF.bottom;


        }
        setMeasuredDimension(measureWidth, measureHeight);
        if (onSimpleTableViewCallBack != null) {
            onSimpleTableViewCallBack.onMeasureSize(measureWidth, measureHeight);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (valueAnimator != null) {
                    valueAnimator.cancel();
                    valueAnimator.removeAllUpdateListeners();
                }
                previousX = event.getX();
                previousY = event.getY();
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;

            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - previousX) > Math.abs(event.getY() - previousY)) {
                    mainDirection = Direction.MAIN_LEFT_RIGHT;
                } else {
                    mainDirection = Direction.MAIN_UP_DOWN;
                }
                if (event.getX() > previousX) {
                    leftRightDirection = Direction.RIGHT;
                } else {
                    leftRightDirection = Direction.LEFT;
                }
                if (event.getY() < previousY) {
                    upDownDirection = Direction.UP;
                } else {
                    upDownDirection = Direction.DOWN;
                }
//                Log.e("SSSSS", leftRightDirection.name() + "---" + upDownDirection.name());

                offsetX = event.getX() - previousX;
                offsetY = event.getY() - previousY;
                //设置有效拖动
                setEffectiveSlideOffset();


                //滑动触点消费处理
                boolean consumption = false;
                if (handleDistributionEvent) {
                    if (mainDirection == Direction.MAIN_LEFT_RIGHT) {
                        if (leftRightDirection == Direction.RIGHT) {
                            //X轴方向取禁止拖动的cell后一个cell的位置，也就是X轴方向可滑动的第一个cell，至于同一列纵向的其他cell不考虑，因为他们是同步的
                            consumption = list.get(disableColumn.length).realTimeRectF.left + offsetX <= list.get(disableColumn.length).originalRectF.left;
                        } else {
                            //取标题最后一个cell
                            consumption = list.get(columnCount - 1).realTimeRectF.right + offsetX >= getParentWidthHeightSize()[0];
                        }
                    } else {
                        if (upDownDirection == Direction.DOWN) {
                            //columnCount+1：取标题下一个，表项可滑动cell的第一个
                            consumption = list.get(columnCount + 1).realTimeRectF.top + offsetY <= list.get(columnCount + 1).originalRectF.top;
                            if (!consumption) {
                                //滑动到顶部了，将原先初始化矩阵Y轴位置设置到各cell上以补偿Y轴位置偏差
                                if (isScrollToTop) {
                                    for (int i = 0; i < list.size(); i++) {
                                        if (!list.get(i).isHeaderTitle) {
                                            list.get(i).realTimeRectF.top = list.get(i).originalRectF.top;
                                            list.get(i).realTimeRectF.bottom = list.get(i).originalRectF.bottom;
                                        }
                                    }
                                }
                            }
                        } else {
                            //底部，用列表最后一个cell的位置来判断，不需要关心column,因为在同一行
                            consumption = list.get(list.size() - 1).realTimeRectF.bottom + offsetY >= getParentWidthHeightSize()[1];
                            if (!consumption) {
                                //滑动到底部了，取最后一个 cell的底部与View高度之间的距离，补偿到所有cell的Y轴
                                if (isScrollToBottom) {
                                    float offset = getParentWidthHeightSize()[1] - list.get(list.size() - 1).realTimeRectF.bottom;
//                                    Log.e("SSSSS", offset + "");
                                    for (int i = 0; i < list.size(); i++) {
                                        if (!list.get(i).isHeaderTitle) {
                                            list.get(i).realTimeRectF.top = list.get(i).realTimeRectF.top + offset;
                                            list.get(i).realTimeRectF.bottom = list.get(i).realTimeRectF.bottom + offset;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                getParent().requestDisallowInterceptTouchEvent(consumption);

                previousX = event.getX();
                previousY = event.getY();

//                Log.e("SSSSS", offsetX + "***" + offsetY);

                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (onSimpleTableViewCallBack != null) {
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).realTimeRectF.contains(event.getX(), event.getY())) {
                            onSimpleTableViewCallBack.onClickItem(list.get(i).t, list.get(i).position);
                            break;
                        }
                    }
                }
            case MotionEvent.ACTION_CANCEL:
//                Log.e("SSSSS", offsetY + "---" + upDownDirection + "---" + isScrollToTop + "---" + isScrollToBottom);
                if (Math.abs(offsetY) > Math.abs(offsetX)) {
                    if (valueAnimator != null) {
                        valueAnimator.cancel();
                        valueAnimator.removeAllUpdateListeners();
                    }
                    valueAnimator = ValueAnimator.ofFloat(offsetY, 0);
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            if (!isScrollToTop && !isScrollToBottom) {
                                offsetY = (float) animation.getAnimatedValue();
                                setEffectiveSlideOffset();
                                invalidate();
                            }
                        }
                    });
//                    Log.e("SSSSS", offsetY + "");
                    valueAnimator.setDuration((long) (Math.abs(offsetY) * 20));
                    valueAnimator.setRepeatMode(ValueAnimator.RESTART);
                    valueAnimator.setInterpolator(new LinearInterpolator());
                    valueAnimator.start();
                }
                leftRightDirection = Direction.NORMAL;
                upDownDirection = Direction.NORMAL;
                mainDirection = Direction.NORMAL;
                break;
            default:
                leftRightDirection = Direction.NORMAL;
                upDownDirection = Direction.NORMAL;
                mainDirection = Direction.NORMAL;
        }
        return super.onTouchEvent(event);
    }


    /**
     * 设置有效拖动偏移量
     */
    public void setEffectiveSlideOffset() {
        for (int i = 0; i < list.size(); i++) {
            if (mainDirection == Direction.MAIN_LEFT_RIGHT) {
                if (leftRightDirection == Direction.RIGHT) {
                    //头部标题可动部分
                    if (list.get(i).isHeaderTitle && list.get(i).xCanSlide) {
                        //向右滑动时判断所有cell的left是否大于原始left
                        if (list.get(i).realTimeRectF.left + offsetX < list.get(i).originalRectF.left) {
                            list.get(i).realTimeRectF.left = list.get(i).realTimeRectF.left + offsetX;
                            list.get(i).realTimeRectF.right = list.get(i).realTimeRectF.right + offsetX;
                        }
                        //XY轴都可拖动部分横向
                        for (int j = 0; j < list.size(); j++) {
                            if (list.get(j).position[0] == list.get(i).position[0]) {
                                list.get(j).realTimeRectF.left = list.get(i).realTimeRectF.left;
                                list.get(j).realTimeRectF.right = list.get(i).realTimeRectF.right;
                            }
                        }
                    }
                } else {
                    //头部标题可动部分
                    if (list.get(i).isHeaderTitle && list.get(i).xCanSlide) {
                        //columnCount-1：取标题最后一个cell
                        if (list.get(columnCount - 1).realTimeRectF.right + offsetX > getParentWidthHeightSize()[0]) {
                            list.get(i).realTimeRectF.left = list.get(i).realTimeRectF.left + offsetX;
                            list.get(i).realTimeRectF.right = list.get(i).realTimeRectF.right + offsetX;

                        }
                        //XY轴都可拖动部分横向
                        for (int j = 0; j < list.size(); j++) {
                            if (list.get(j).position[0] == list.get(i).position[0]) {
                                list.get(j).realTimeRectF.left = list.get(i).realTimeRectF.left;
                                list.get(j).realTimeRectF.right = list.get(i).realTimeRectF.right;
                            }
                        }
                    }
                }
            } else {
                if (upDownDirection == Direction.DOWN) {

                    //XY轴都可拖动部分纵向
                    if (list.get(i).yCanSlide && list.get(i).xCanSlide) {

                        list.get(i).realTimeRectF.top = list.get(list.get(i).position[1] * columnCount).realTimeRectF.top;
                        list.get(i).realTimeRectF.bottom = list.get(list.get(i).position[1] * columnCount).realTimeRectF.bottom;
                    }

                    //Y轴可拖动且X轴不可拖动部分
                    if (list.get(i).yCanSlide && !list.get(i).xCanSlide) {
                        if (list.get(i).realTimeRectF.top + offsetY < list.get(i).originalRectF.top) {
                            list.get(i).realTimeRectF.top = list.get(i).realTimeRectF.top + offsetY;
                            list.get(i).realTimeRectF.bottom = list.get(i).realTimeRectF.bottom + offsetY;
                        }
                    }
                } else {
                    //XY轴都可拖动部分纵向
                    if (list.get(i).yCanSlide && list.get(i).xCanSlide) {
                        list.get(i).realTimeRectF.top = list.get(list.get(i).position[1] * columnCount).realTimeRectF.top;
                        list.get(i).realTimeRectF.bottom = list.get(list.get(i).position[1] * columnCount).realTimeRectF.bottom;
                    }

                    //Y轴可拖动且X轴不可拖动部分
                    if (list.get(i).yCanSlide && !list.get(i).xCanSlide) {
                        //底部，用列表最后一个cell的位置来判断，不需要关心column,因为在同一行
                        if (list.get(list.size() - 1).realTimeRectF.bottom + offsetY > getParentWidthHeightSize()[1]) {
                            list.get(i).realTimeRectF.top = list.get(i).realTimeRectF.top + offsetY;
                            list.get(i).realTimeRectF.bottom = list.get(i).realTimeRectF.bottom + offsetY;
                        }
                    }
                }
            }
        }
        //columnCount+1：取标题下一个，表项可滑动cell的第一个
        isScrollToTop = list.get(columnCount + 1).realTimeRectF.top + offsetY > list.get(columnCount + 1).originalRectF.top;
        isScrollToBottom = list.get(list.size() - 1).realTimeRectF.bottom + offsetY + (autoScrollToBottomWhileScrollByUser ? (list.get(list.size() - 1).realTimeRectF.height()) : 0) < getParentWidthHeightSize()[1];
    }


    /**
     * 补偿Y轴文字高度位置偏移量
     */
    private int textYOffset = DensityUtil.dp2px(3);

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (onSimpleTableViewCallBack == null) {
            return;
        }
        //优先绘制XY轴都可拖动部分，保证其被不可拖动部分覆盖
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).xCanSlide && !list.get(i).isHeaderTitle) {

                paint.setColor(list.get(i).backgroundColor);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(list.get(i).realTimeRectF, paint);

                paint.setColor(list.get(i).lineColor);
                paint.setStrokeWidth(list.get(i).lineWidth);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(list.get(i).realTimeRectF, paint);

                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(list.get(i).textSize);
                paint.setColor(list.get(i).textColor);
                drawText(canvas, i);
            }
        }

        //Y轴可拖动且X轴不可拖动部分
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).xCanSlide && !list.get(i).isHeaderTitle) {

                paint.setColor(list.get(i).backgroundColor);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(list.get(i).realTimeRectF, paint);

                paint.setColor(list.get(i).lineColor);
                paint.setStrokeWidth(list.get(i).lineWidth);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(list.get(i).realTimeRectF, paint);

                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(list.get(i).textSize);
                paint.setColor(list.get(i).textColor);
                drawText(canvas, i);
            }

        }

        //头部标题可动部分
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isHeaderTitle && list.get(i).xCanSlide) {
                paint.setColor(list.get(i).backgroundColor);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(list.get(i).realTimeRectF, paint);

                paint.setColor(list.get(i).lineColor);
                paint.setStrokeWidth(list.get(i).lineWidth);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(list.get(i).realTimeRectF, paint);

                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(list.get(i).textSize);
                paint.setColor(list.get(i).textColor);
                drawText(canvas, i);

            }
        }

        //头部标题不可动部分
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isHeaderTitle && !list.get(i).xCanSlide) {
                paint.setColor(list.get(i).backgroundColor);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(list.get(i).realTimeRectF, paint);

                paint.setColor(list.get(i).lineColor);
                paint.setStrokeWidth(list.get(i).lineWidth);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(list.get(i).realTimeRectF, paint);

                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(list.get(i).textSize);
                paint.setColor(list.get(i).textColor);
                drawText(canvas, i);

            }
        }
    }

    private void drawText(Canvas canvas, int i) {

        if (list.get(i).suffix != null) {
            Suffix suffix = list.get(i).suffix;

            float fullWidth = DrawViewUtils.getTextWHF(paint, getValueByPosition(i))[0] + suffix.getTextSize(true)[0] + suffix.padding * 2;

            //绘制cell文字
            float x = list.get(i).realTimeRectF.left + list.get(i).realTimeRectF.width() / 2 - fullWidth / 2;
            float y = list.get(i).realTimeRectF.top + list.get(i).realTimeRectF.height() / 2 + list.get(i).size[1] / 2 - textYOffset;
            canvas.drawText(getValueByPosition(i), x, y, paint);


            suffix.rectF.left = x + DrawViewUtils.getTextWHF(paint, getValueByPosition(i))[0] + suffix.distance + suffix.padding - suffix.radius / 2;
            suffix.rectF.right = suffix.rectF.left + suffix.getTextSize(true)[0] + suffix.radius / 2;
            suffix.rectF.top = y - DrawViewUtils.getTextWHF(paint, getValueByPosition(i))[1] / 2 - textYOffset - suffix.radius / 2;
            suffix.rectF.bottom = suffix.rectF.top + suffix.getTextSize(true)[1] + suffix.radius / 2;


            //绘制后缀背景
            suffix.paint.setColor(suffix.backgroundColor);
            canvas.drawRoundRect(suffix.rectF, suffix.radius, suffix.radius, suffix.paint);
            //绘制后缀文字
            suffix.paint.setTextSize(suffix.textSize);
            suffix.paint.setTypeface(suffix.typeface);
            suffix.paint.setColor(suffix.textColor);

            canvas.drawText(suffix.text, suffix.rectF.left + suffix.rectF.width() / 2 - suffix.rectF.width() / 2 + suffix.padding / 2, suffix.rectF.top + suffix.rectF.height() - textYOffset + suffix.padding / 2 - suffix.radius / 2, suffix.paint);
        } else {
            //绘制cell文字
            float x = list.get(i).realTimeRectF.left + list.get(i).realTimeRectF.width() / 2 - list.get(i).size[0] / 2;
            float y = list.get(i).realTimeRectF.top + list.get(i).realTimeRectF.height() / 2 + list.get(i).size[1] / 2 - textYOffset;
            canvas.drawText(getValueByPosition(i), x, y, paint);
        }
    }

    /**
     * 补满一行
     */
    public void setFillUp(boolean fillUp) {
        isFillUp = fillUp;
    }

    public static boolean gotoNumber(int... array) {
        boolean con = false;
        boolean isBig = false;
        for (int i = 0; i < array.length - 1; i++) {
            if (i == 0) {
                isBig = array[i] - array[i + 1] == 1 ? true : false;
            }
            if (isBig) {
                con = array[i] - array[i + 1] == 1;
            } else {
                con = array[i] - array[i + 1] == -1;
            }
            if (!con) {
                return con;
            }
        }
        return con;
    }

    /**
     * 禁止拖动的队列
     *
     * @param disableColumn 建议设置连续的数组，如{0，1，2}
     */
    public void setDisableColumn(int[] disableColumn) {
        this.disableColumn = disableColumn;
    }

    /**
     * 设置数据，注意，content中请不要包含title，以免数据重复
     *
     * @param title   头部标题
     * @param content 表项内容
     */
    public void setData(List<SimpleTableViewBean> title, List<SimpleTableViewBean> content) {
        if (title == null || title.size() == 0 || content == null || content.size() == 0) {
            return;
        }
        list.clear();
        list.addAll(title);
        columnCount = title.size();
        list.addAll(content);
        lineCount = list.size() / columnCount + (list.size() % columnCount > 0 ? 1 : 0);
//        Log.e("SSSSS", lineCount * columnCount + "---" + list.size());
        if (isFillUp) {
            List<SimpleTableViewBean> temp = new ArrayList<>();
            for (int i = 0; i < lineCount * columnCount - list.size(); i++) {
                temp.add(new SimpleTableViewBean());
            }
            list.addAll(temp);
        }
    }


    /**
     * 获取cell值
     *
     * @param i 下标
     * @return 字符串
     */
    private String getValueByPosition(int i) {
        return onSimpleTableViewCallBack.onLabelValue(list.get(i).t) == null ? "" : onSimpleTableViewCallBack.onLabelValue(list.get(i).t);
    }


    /**
     * 队列中的下标转换为表中的坐标轴位置
     *
     * @param index   数据队列下标
     * @param convert 是否转换成正常下标（不从0开始）
     * @return
     */
    private int[] getPosition(int index) {
        int[] position = {0, 0};
        if (index > list.size() || index < 0) {
            return position;
        }
        //x轴
        position[0] = index % columnCount;
        //y轴
        position[1] = index / columnCount;
        return position;
    }


    /**
     * 获取同一行中最大字符宽度
     *
     * @param index
     * @return
     */
    private float getMaxTextWidthForColumn(int index) {
        int[] position = getPosition(index);
        float maxWidth = 0;
        if (onSimpleTableViewCallBack == null) {
            return maxWidth;
        }
        //获取同列文字最大宽度
        for (int i = 0; i < lineCount; i++) {
            if (position[0] * i < list.size()) {
                paint.setTextSize(list.get(i).textSize);
                maxWidth = Math.max(maxWidth, DrawViewUtils.getTextWHF(paint, getValueByPosition(i))[0]);
            }
        }
        return maxWidth;
    }


    /**
     * 获取父容器宽高
     */
    private int[] getParentWidthHeightSize() {
        int[] size = {0, 0};
        ViewGroup viewGroup = (ViewGroup) getParent();
        if (null != viewGroup) {
            size[0] = viewGroup.getWidth();
            size[1] = viewGroup.getHeight();
        }
        return size;
    }

    public enum Direction {
        LEFT,
        UP,
        RIGHT,
        DOWN,
        NORMAL,
        MAIN_UP_DOWN,
        MAIN_LEFT_RIGHT
    }

    public static class SimpleTableViewBean<T> {
        private List<Integer> sameValuePosition = new ArrayList<>();
        /**
         * 文字
         */
        private T t;
        /**
         * 后缀
         */
        private Suffix suffix;
        /**
         * 坐标
         */
        private int[] position = new int[2];
        /**
         * 字体大小
         */
        private float[] size = new float[2];
        /**
         * X轴方向是否可拖动
         */
        private boolean xCanSlide = true;
        /**
         * Y轴方向是否可拖动
         */
        private boolean yCanSlide = true;
        /**
         * 是否为头部标题
         */
        private boolean isHeaderTitle = false;
        /**
         * 文字颜色
         */
        private int textColor = Color.BLACK;
        /**
         * 文字尺寸
         */
        private float textSize = DensityUtil.sp2px(10);
        /**
         * 背景
         */
        private int backgroundColor = Color.WHITE;
        /**
         * 线条颜色
         */
        private int lineColor = Color.parseColor("#eeeeee");
        /**
         * 线条宽度
         */
        private int lineWidth = DensityUtil.dp2px(1);
        /**
         * 实时矩阵范围
         */
        private RectF realTimeRectF = new RectF();
        /**
         * 原始矩阵范围
         */
        private RectF originalRectF = new RectF();

        public SimpleTableViewBean(T t, int textColor, float textSize, int backgroundColor) {
            this.t = t;
            this.textColor = textColor;
            this.textSize = textSize;
            this.backgroundColor = backgroundColor;
        }

        public SimpleTableViewBean(T t, boolean isHeaderTitle, int backgroundColor) {
            this.t = t;
            this.isHeaderTitle = isHeaderTitle;
            this.backgroundColor = backgroundColor;
        }

        public SimpleTableViewBean(T t) {
            this.t = t;
        }

        public SimpleTableViewBean(T t, Suffix suffix) {
            this.t = t;
            this.suffix = suffix;
        }

        public SimpleTableViewBean() {
        }

        public SimpleTableViewBean setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public SimpleTableViewBean setTextSize(float textSize) {
            this.textSize = textSize;
            return this;
        }

        public SimpleTableViewBean setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public SimpleTableViewBean setLineColor(int lineColor) {
            this.lineColor = lineColor;
            return this;
        }

        public SimpleTableViewBean setLineWidth(int lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }
    }

    /**
     * 后缀
     */
    public static class Suffix {
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private RectF rectF = new RectF();
        /**
         * 文字颜色
         */
        private String text = "";
        /**
         * 文字颜色
         */
        private int textColor = Color.BLACK;
        /**
         * 文字尺寸
         */
        private float textSize = DensityUtil.sp2px(12);
        /**
         * 背景
         */
        private int backgroundColor = Color.WHITE;
        /**
         * 字体
         */
        private Typeface typeface = Typeface.DEFAULT;
        /**
         * 圆角背景
         */
        private float radius = DensityUtil.dp2px(2);
        /**
         * 文字颜色
         */
        private int distance = DensityUtil.dp2px(2);
        /**
         * 文字颜色
         */
        private int padding = DensityUtil.dp2px(2);

        public void setTextColor(int textColor) {
            this.textColor = textColor;
        }

        public void setTextSize(float textSize) {
            this.textSize = textSize;
        }

        public void setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public void setRadius(float radius) {
            this.radius = radius;
        }

        public void setTypeface(Typeface typeface) {
            this.typeface = typeface;
        }

        public Suffix(String text) {
            this.text = text;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }

        public float[] getTextSize(boolean withDistance) {
            paint.setTextSize(textSize);
            paint.setTypeface(typeface);
            float[] size = DrawViewUtils.getTextWHF(paint, text);
            size[0] = size[0] + (text != null && text.length() > 0 && withDistance ? distance : 0);
            return size;
        }
    }

    public interface OnSimpleTableViewCallBack<T> {
        /**
         * 设置文字标签
         *
         * @param t 模型
         * @return 字符串
         */
        String onLabelValue(T t);

        /**
         * 点击事件
         *
         * @param t        模型
         * @param position 坐标（从0开始计数，真实坐标请+1）
         */
        void onClickItem(T t, int[] position);

        /**
         * 开始测量尺寸
         *
         * @param width  宽度
         * @param height 高度
         */
        void onMeasureSize(int width, int height);
    }
}
