package com.sss.michael.simpleview.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.sss.michael.simpleview.utils.DensityUtil;

import java.util.*;

/**
 * @author Michael by 61642
 * @date 2025/8/26 16:29
 * @Description 一个强大的弹幕自绘视图
 */
public class SimpleDanmuView extends View {

    private int contentPaddingLeftPx, contentPaddingTopPx, contentPaddingRightPx, contentPaddingBottomPx;
    //弹幕密度
    private float density = 0.6f;
    //最大弹幕轨道数量
    private int maxTrackCount = 5;
    //每一行（轨道）之间的上下间距
    private float trackSpacingPx;
    //气泡文字左右的内边距（水平 padding），保证文字和气泡边缘之间有留白
    private float bubblePaddingHorPx;
    //气泡文字上下的内边距（垂直 padding），保证文字和气泡边缘之间有留白
    private float bubblePaddingVerPx;
    //同一行两个弹幕之间的最小间距，避免文字或气泡重叠
    private float minGapPx;
    //新弹幕从屏幕右侧进入时的安全距离，防止刚出现就和已有弹幕重叠
    private float startRightSafePx;
    //弹幕移出屏幕后额外保留的距离，保证彻底不可见后再回收，避免闪烁
    private float offscreenLeftExtraPx;


    //速度，每秒最小像素
    private float speedPxPerSecMin = 80f;
    //速度，每秒最大像素
    private float speedPxPerSecMax = 220f;

    //碰撞规避开关
    private boolean collisionAvoidanceEnabled = true;
    //是否保持相同速度
    private boolean keepSpeedSame = true;
    //固定速度（当 keepSpeedSame 为 true 有效）
    private float fixedSpeedPxPerSec = 150f;

    private final Deque<DanmuItem> itemPool = new ArrayDeque<>();
    private int maxPoolSize = 80;
    private final Map<Integer, Paint> paintCache = new HashMap<>();


    private final DanmuStyle globalStyle = new DanmuStyle();
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    //挂起等待队列（还没上屏的弹幕）
    private final Deque<DanmuItem> pendingQueue = new ArrayDeque<>();
    //正在屏幕上播放的弹幕
    private final List<DanmuItem> activeItems = new ArrayList<>();
    //记录每个轨道的最后一条弹幕（用来做间距检查）。
    private final List<DanmuItem> lastItemPerTrack = new ArrayList<>();

    //可用轨道数量（容量）
    private int measuredTrackCapacity = 1;
    //上一帧的时间戳（毫秒）
    private long lastFrameTimeMs = -1;

    private final Random random = new Random();

    private OnDanmuClickListener onDanmuClickListener;

    public void setOnDanmuClickListener(OnDanmuClickListener onDanmuClickListener) {
        this.onDanmuClickListener = onDanmuClickListener;
    }

    public SimpleDanmuView(Context context) {
        this(context, null);
    }

    public SimpleDanmuView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        setClickable(true);

        globalStyle.textSizePx = DensityUtil.sp2px(16f);
        globalStyle.bubbleRadiusPx = DensityUtil.dp2px(8f);

        trackSpacingPx = DensityUtil.dp2px(8);
        bubblePaddingHorPx = DensityUtil.dp2px(10);
        bubblePaddingVerPx = DensityUtil.dp2px(5);
        minGapPx = DensityUtil.dp2px(48);
        startRightSafePx = DensityUtil.dp2px(8);
        offscreenLeftExtraPx = DensityUtil.dp2px(24);

        bubblePaint.setStyle(Paint.Style.FILL);
        bubblePaint.setColor(globalStyle.bubbleColor);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setAntiAlias(true);

        //预分配 lastItemPerTrack
        for (int i = 0; i < maxTrackCount; i++) lastItemPerTrack.add(null);
        applyBilibiliPreset();
//        for (int i = 0; i < 10; i++) {
//            addDanmu("我是弹幕" + i, null);
//        }
    }

    public void setCollisionAvoidanceEnabled(boolean enabled) {
        this.collisionAvoidanceEnabled = enabled;
    }

    public void setBubbleRadiusDp(float dpRadius) {
        globalStyle.bubbleRadiusPx = DensityUtil.dp2px(dpRadius);
        invalidate();
    }

    public void setKeepSpeedSame(boolean keep) {
        this.keepSpeedSame = keep;
    }

    public void setFixedSpeedPxPerSec(float pxPerSec) {
        if (pxPerSec <= 0) return;
        this.fixedSpeedPxPerSec = pxPerSec;
    }

    public void setFixedSpeedDpPerSec(float dpPerSec) {
        setFixedSpeedPxPerSec(DensityUtil.dp2px(dpPerSec));
    }

    public void setPoolMaxSize(int size) {
        if (size < 8) size = 8;
        this.maxPoolSize = size;
    }

    public void setDanmuStyle(@Nullable DanmuStyle style) {
        if (style == null) return;
        copyStyle(style, globalStyle);
        bubblePaint.setColor(globalStyle.bubbleColor);
        invalidate();
    }

    public void setDensity(float value01) {
        this.density = clamp01(value01);
        invalidate();
    }

    public void setMaxTrackCount(int max) {
        if (max < 1) max = 1;
        this.maxTrackCount = max;
        syncLastItemListSize();
        requestLayout();
    }

    public void setContentPadding(int leftPx, int topPx, int rightPx, int bottomPx) {
        contentPaddingLeftPx = Math.max(0, leftPx);
        contentPaddingTopPx = Math.max(0, topPx);
        contentPaddingRightPx = Math.max(0, rightPx);
        contentPaddingBottomPx = Math.max(0, bottomPx);
        requestLayout();
    }

    public void setTrackSpacing(float spacingDp) {
        trackSpacingPx = spacingDp <= 0 ? 0 : DensityUtil.dp2px(spacingDp);
        requestLayout();
    }

    public void setBubblePadding(float horizontalDp, float verticalDp) {
        bubblePaddingHorPx = Math.max(0, DensityUtil.dp2px(horizontalDp));
        bubblePaddingVerPx = Math.max(0, DensityUtil.dp2px(verticalDp));
        invalidate();
    }

    public void setMinGap(float gapDp) {
        minGapPx = Math.max(0, DensityUtil.dp2px(gapDp));
        invalidate();
    }

    public void setSpeedRange(float minPxPerSec, float maxPxPerSec) {
        if (minPxPerSec <= 0) minPxPerSec = 1f;
        if (maxPxPerSec < minPxPerSec) maxPxPerSec = minPxPerSec;
        speedPxPerSecMin = minPxPerSec;
        speedPxPerSecMax = maxPxPerSec;
    }

    //仿bilibili
    public void applyBilibiliPreset() {
        DanmuStyle s = new DanmuStyle();
        s.textColor = Color.WHITE;
        s.enableStroke = true;
        s.strokeColor = Color.BLACK;
        s.strokeWidthPx = DensityUtil.dp2px(1.8f);
        s.enableBubble = true;
        s.bubbleColor = 0x99000000;
        s.bubbleRadiusPx = DensityUtil.dp2px(12);
        s.bold = false;
        s.textSizePx = globalStyle.textSizePx;
        setDanmuStyle(s);

        setTrackSpacing(6);
        setBubblePadding(8, 4);
        setMinGap(36);
        setDensity(0.75f);
        setSpeedRange(120, 240);
    }


    public void addDanmu(String text, @Nullable DanmuStyle style) {
        if (text == null || text.length() == 0) return;

        DanmuItem item = itemPool.pollFirst();
        if (item == null) {
            item = new DanmuItem();
        }

        DanmuStyle s = (style == null) ? cloneStyle(globalStyle) : cloneStyle(style);
        item.text = text;
        item.style = s;

        Paint p = getPaintForStyle(s);
        item.cachedTextPaint = p;
        item.fontMetrics = p.getFontMetrics();
        item.textWidth = p.measureText(text);

        //speed
        if (s.speedPxPerSec != null) {
            item.speedPxPerSec = s.speedPxPerSec;
        } else if (keepSpeedSame) {
            item.speedPxPerSec = fixedSpeedPxPerSec;
        } else {
            item.speedPxPerSec = rand(speedPxPerSecMin, speedPxPerSecMax);
        }

        item.trackIndex = -1;
        item.x = getWidth() - contentPaddingRightPx + startRightSafePx;
        pendingQueue.offerLast(item);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //重新计算轨道容量
        float textHeight = getTextHeightWith(globalStyle.textSizePx);
        float trackHeight = textHeight + bubblePaddingVerPx * 2f;
        float contentHeight = Math.max(0, getHeight() - contentPaddingTopPx - contentPaddingBottomPx);
        if (trackHeight <= 0 || contentHeight <= 0) {
            measuredTrackCapacity = 1;
        } else {
            int possible = (int) Math.floor((contentHeight + trackSpacingPx) / (trackHeight + trackSpacingPx));
            measuredTrackCapacity = Math.max(1, Math.min(possible, maxTrackCount));
        }

        //同步最后一个项目列表大小
        syncLastItemListSize();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    //同步最后一个项目列表大小
    private void syncLastItemListSize() {
        if (lastItemPerTrack.size() < maxTrackCount) {
            while (lastItemPerTrack.size() < maxTrackCount) lastItemPerTrack.add(null);
        } else if (lastItemPerTrack.size() > maxTrackCount) {
            while (lastItemPerTrack.size() > maxTrackCount)
                lastItemPerTrack.remove(lastItemPerTrack.size() - 1);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long now = System.currentTimeMillis();
        if (lastFrameTimeMs < 0) lastFrameTimeMs = now;
        float dtSec = (now - lastFrameTimeMs) / 1000f;
        lastFrameTimeMs = now;

        int activeTrackTarget = Math.max(1, Math.min(measuredTrackCapacity, Math.round(lerp(1, measuredTrackCapacity, density))));
        float dynamicMinGap = collisionAvoidanceEnabled ? lerp(minGapPx * 1.3f, Math.max(DensityUtil.dp2px(12), minGapPx * 0.7f), density) : 0f;

        //更新位置并绘制
        //从后到前绘制（后加入的在上层）
        for (int i = 0; i < activeItems.size(); i++) {
            DanmuItem item = activeItems.get(i);

            //推进位置
            item.x -= item.speedPxPerSec * dtSec;

            //基线计算
            float textHeight = getTextHeightWith(globalStyle.textSizePx);
            float trackHeight = textHeight + bubblePaddingVerPx * 2f;
            float trackTop = contentPaddingTopPx + item.trackIndex * (trackHeight + trackSpacingPx);
            Paint.FontMetrics fm = item.fontMetrics;
            float baseline = trackTop + bubblePaddingVerPx - fm.ascent;

            //背景矩形
            float left = item.x - bubblePaddingHorPx;
            float top = baseline + fm.ascent - bubblePaddingVerPx;
            float right = item.x + item.textWidth + bubblePaddingHorPx;
            float bottom = baseline + fm.descent + bubblePaddingVerPx;
            item.bubbleRect.set(left, top, right, bottom);

            //背景（圆角自适应：优先使用样式指定值，但至少为高度的一半以获得胶囊效果，并不超过宽度一半）
            if (item.style.enableBubble) {
                bubblePaint.setColor(item.style.bubbleColor);

                float height = item.bubbleRect.height();
                float width = item.bubbleRect.width();

                //至少为样式提供的 bubbleRadiusPx，但如果高度更大则使用高度的一半以保证“完美圆角”(胶囊)
                float desiredRadius = Math.max(item.style.bubbleRadiusPx, height / 2f);
                //同时不要超过宽度的一半（以免形成不规则形状）
                float effectiveRadius = Math.min(desiredRadius, width / 2f);

                canvas.drawRoundRect(item.bubbleRect, effectiveRadius, effectiveRadius, bubblePaint);
            }

            //描边
            if (item.style.enableStroke) {
                strokePaint.setColor(item.style.strokeColor);
                strokePaint.setStrokeWidth(item.style.strokeWidthPx);
                strokePaint.setTextSize(item.cachedTextPaint.getTextSize());
                strokePaint.setFakeBoldText(item.cachedTextPaint.isFakeBoldText());
                canvas.drawText(item.text, item.x, baseline, strokePaint);
            }
            //文字
            canvas.drawText(item.text, item.x, baseline, item.cachedTextPaint);
        }

        //清理离场
        for (int i = activeItems.size() - 1; i >= 0; i--) {
            DanmuItem it = activeItems.get(i);
            if (it.bubbleRect.right < -offscreenLeftExtraPx) {
                //清理 lastItemPerTrack 引用
                if (it.trackIndex >= 0 && it.trackIndex < lastItemPerTrack.size()) {
                    DanmuItem lastRef = lastItemPerTrack.get(it.trackIndex);
                    if (lastRef == it) lastItemPerTrack.set(it.trackIndex, null);
                }
                activeItems.remove(i);
                if (onDanmuClickListener != null) {
                    onDanmuClickListener.onDanmuRemovedFromDisplay(it.text);
                }
                recycleItem(it);
            }
        }

        //尝试投放挂起等待队列中的弹幕
        if (pendingQueue.isEmpty()) {
            postInvalidateOnAnimation();
            return;
        }
        int tryCount = Math.min(6, pendingQueue.size());
        for (int t = 0; t < tryCount; t++) {
            DanmuItem cand = pendingQueue.peekFirst();
            if (cand == null) break;
            int track = findAvailableTrack(activeTrackTarget, cand, dynamicMinGap);
            if (track >= 0) {
                cand.trackIndex = track;
                cand.x = getWidth() - contentPaddingRightPx + startRightSafePx;
                lastItemPerTrack.set(track, cand);
                activeItems.add(cand);
                if (onDanmuClickListener != null) {
                    onDanmuClickListener.onDanmuAddedToDisplay(cand.text);
                }
                pendingQueue.pollFirst();
            } else {
                //没有可放轨道时跳出
                break;
            }
        }

        postInvalidateOnAnimation();
    }

    private int findAvailableTrack(int activeTrackTarget, DanmuItem candidate, float dynamicMinGap) {
        int usableTracks = Math.max(1, Math.min(activeTrackTarget, measuredTrackCapacity));
        float startX = getWidth() - contentPaddingRightPx + startRightSafePx;

        int bestIdx = -1;
        float bestRightMost = Float.MAX_VALUE;

        for (int i = 0; i < usableTracks; i++) {
            DanmuItem last = lastItemPerTrack.get(i);
            if (last == null) {
                return i;
            } else {
                float lastRight = last.x + last.textWidth + bubblePaddingHorPx * 2f;
                if (!collisionAvoidanceEnabled) {
                    if (lastRight < bestRightMost) {
                        bestRightMost = lastRight;
                        bestIdx = i;
                    }
                } else {
                    if (lastRight + dynamicMinGap <= startX) {
                        if (lastRight < bestRightMost) {
                            bestRightMost = lastRight;
                            bestIdx = i;
                        }
                    }
                }
            }
        }
        return bestIdx;
    }

    private void recycleItem(DanmuItem item) {
        if (item == null) return;
        //清理引用，便于 GC 回收一些大对象
        item.text = null;
        item.style = null;
        item.cachedTextPaint = null;
        item.fontMetrics = null;
        item.bubbleRect.setEmpty();
        item.trackIndex = -1;
        item.textWidth = 0f;
        item.speedPxPerSec = 0f;
        if (itemPool.size() < maxPoolSize) itemPool.offerFirst(item);
    }

    private void clearAndRecycleAll() {
        for (DanmuItem it : activeItems) {
            recycleItem(it);
        }
        activeItems.clear();

        for (DanmuItem it : pendingQueue) {
            recycleItem(it);
        }
        pendingQueue.clear();

        for (int i = 0; i < lastItemPerTrack.size(); i++) lastItemPerTrack.set(i, null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearAndRecycleAll();
        itemPool.clear();
        paintCache.clear();
    }


    private Paint getPaintForStyle(DanmuStyle s) {
        //样式哈希
        int hash = 17;
        hash = hash * 31 + s.textColor;
        hash = hash * 31 + Float.floatToIntBits(s.textSizePx);
        hash = hash * 31 + (s.bold ? 1 : 0);
        hash = hash * 31 + (s.enableBubble ? 1 : 0);
        hash = hash * 31 + s.bubbleColor;
        hash = hash * 31 + Float.floatToIntBits(s.bubbleRadiusPx);
        hash = hash * 31 + (s.enableStroke ? 1 : 0);
        hash = hash * 31 + s.strokeColor;
        hash = hash * 31 + Float.floatToIntBits(s.strokeWidthPx);
        Paint p = paintCache.get(hash);
        if (p != null) return p;
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setColor(s.textColor);
        p.setTextSize(s.textSizePx > 0 ? s.textSizePx : globalStyle.textSizePx);
        p.setFakeBoldText(s.bold);
        paintCache.put(hash, p);
        return p;
    }

    //记录当前被按下的弹幕对象
    //按下时，会检测点击到哪条弹幕：
    //在手指抬起时，会判断抬起位置是否还是同一条弹幕，如果是 → 触发点击回调。
    //防止手指滑动时误触其他弹幕。
    private DanmuItem pressedItem = null;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (onDanmuClickListener == null) return super.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                pressedItem = findHitItem(event.getX(), event.getY());
                return pressedItem != null || super.onTouchEvent(event);

            case MotionEvent.ACTION_MOVE:
                return pressedItem != null || super.onTouchEvent(event);

            case MotionEvent.ACTION_UP:

                if (pressedItem != null) {
                    DanmuItem hit = findHitItem(event.getX(), event.getY());
                    if (hit == pressedItem) {//防止手指滑动时误触其他弹幕。
                        int index = activeItems.indexOf(hit);
                        onDanmuClickListener.onDanmuClick(hit.text, hit.trackIndex, index, hit.style);
                    }
                    pressedItem = null;
                    return true;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                pressedItem = null;
                break;
        }
        return super.onTouchEvent(event);
    }

    private DanmuItem findHitItem(float x, float y) {
        for (int i = activeItems.size() - 1; i >= 0; i--) {
            DanmuItem it = activeItems.get(i);
            if (it.bubbleRect.contains(x, y)) return it;
        }
        return null;
    }

    private float rand(float a, float b) {
        return a + (b - a) * random.nextFloat();
    }

    private float clamp01(float x) {
        return x < 0 ? 0 : (x > 1 ? 1 : x);
    }

    private float lerp(float a, float b, float t01) {
        t01 = clamp01(t01);
        return a + (b - a) * t01;
    }

    private float getTextHeightWith(float sizePx) {
        Paint p = new Paint();
        p.setTextSize(sizePx);
        Paint.FontMetrics fm = p.getFontMetrics();
        return fm.descent - fm.ascent;
    }

    private static void copyStyle(DanmuStyle src, DanmuStyle dst) {
        if (src == null || dst == null) return;
        dst.textColor = src.textColor;
        dst.textSizePx = src.textSizePx;
        dst.bold = src.bold;
        dst.enableBubble = src.enableBubble;
        dst.bubbleColor = src.bubbleColor;
        dst.bubbleRadiusPx = src.bubbleRadiusPx;
        dst.enableStroke = src.enableStroke;
        dst.strokeColor = src.strokeColor;
        dst.strokeWidthPx = src.strokeWidthPx;
        dst.speedPxPerSec = src.speedPxPerSec;
    }

    private static DanmuStyle cloneStyle(DanmuStyle s) {
        DanmuStyle c = new DanmuStyle();
        copyStyle(s, c);
        return c;
    }


    private static class DanmuItem {
        String text;
        DanmuStyle style;
        float x;
        int trackIndex;
        float textWidth;
        RectF bubbleRect = new RectF();
        float speedPxPerSec;
        Paint cachedTextPaint;
        Paint.FontMetrics fontMetrics;
    }

    public static class DanmuStyle {
        public int textColor = Color.WHITE;
        public float textSizePx = 0f; //<=0 使用全局
        public boolean bold = false;

        public boolean enableBubble = true;
        public int bubbleColor = 0xCC000000;
        public float bubbleRadiusPx = 18f; //基准圆角（dp->px 后）

        public boolean enableStroke = true;
        public int strokeColor = Color.BLACK;
        public float strokeWidthPx = 2.2f;

        public Float speedPxPerSec = null; //若非 null，覆盖全局速度
    }

    public interface OnDanmuClickListener {
        void onDanmuClick(String text, int trackIndex, int indexWithinActive, DanmuStyle style);

        void onDanmuAddedToDisplay(String danmuText);

        void onDanmuRemovedFromDisplay(String danmuText);
    }

}
