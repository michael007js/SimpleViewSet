package com.sss.michael.simpleview.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.sss.michael.simpleview.R;


/**
 * @author Michael by 61642
 * @date 2025/8/19 13:29
 * @Description 一个按压位移的半异形圆角按钮
 */
public class ShadowAlienButtonView extends View {
    private boolean enableBadge = true;   //徽标绘制开关
    public static final int BADGE_LEFT = 0;
    public static final int BADGE_RIGHT = 1;
    private int badgePosition = BADGE_RIGHT;

    private CharSequence text = "按钮"; //默认显示文本
    private Layout.Alignment textAlignment = Layout.Alignment.ALIGN_NORMAL; //文字位置
    private int maxLines = 2; //文本最大行数，默认 2 行

    private float textSizePx;            //文本像素大小
    private int textColor = 0xff262626;  //文本颜色，深灰

    private float cornerRadiusPx;        //圆角半径
    private float minHeightPx;           //最小高度

    private float unCheckedStrokeWidthPx;         //常态边框宽度
    private float checkedStrokeWidthPx; //选中态边框宽度
    private float innerSeamWidthPx;      //选中时内部“高光缝”线宽

    private int unCheckedStrokeColor = 0x22000000; //未选中边框颜色
    private int checkedStrokeColor = 0xFFF59175; //选中时边框/徽标主色

    private int CheckedFillBgColor = Color.WHITE; //选中时填充色
    private int unCheckedFillBgColor = Color.WHITE;    //常态背景填充色

    private int disabledFillBgColor = 0xFFE5E5E5;
    private int disabledStrokeColor = Color.TRANSPARENT;
    private int disabledTextColor = 0xFFFFFFFF; //白字

    private int checkedBottomAlienColor = 0xFFF59175; //选中时底部异形圆角区域颜色
    private int unCheckedBottomAlienFillColor = 0xffe5e5e3; //未选中时底部异形圆角区域颜色

    //半阴影相关（只渲染下半部分）
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG); //阴影画笔，开启抗锯齿
    private final Path shadowPath = new Path(); //阴影路径
    private Shader shadowGradient; //阴影渐变着色器
    private float shadowGapPx = dp(3f);     //内容底部上抬间隙，用于容纳阴影
    private float shadowSize;               //阴影厚度（动态计算）
    private float shadowExtra;              //阴影外扩量（动态计算）

    //顶/底辅助色块（“轨道”），按下下压挡住下方色块，回弹露出上方色块
    private boolean enableAccents = true; //是否启用上下辅助色块
    private int topAccentColor = Color.TRANSPARENT; //顶部辅助色块颜色
    private int bottomAccentColor = Color.TRANSPARENT; //底部辅助色块颜色

    //按压动画
    private float pressOffsetPx = dp(3f);   //按下整体下移位移量
    private float pressProgress = 0f;       //按下进度 0→1
    private float releaseProgress = 1f;     //释放进度 0→1（与按下相反）
    private boolean isPressedInside = false; //触摸点是否仍在控件内部

    //选中态
    private boolean isSelected = false; //是否选中
    private float selectProgress = 0f;      //选中进度 0→1（带 Overshoot）

    //对勾动画
    private float checkProgress = 0f;       //对勾绘制进度 0→1

    //====== 画笔 ======
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG); //文本画笔
    private final Paint cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG); //卡片填充画笔
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG); //边框画笔
    private final Paint seamPaint = new Paint(Paint.ANTI_ALIAS_FLAG); //内侧高光缝画笔
    private final Paint badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG); //徽标（右侧圆）画笔
    private final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG); //对勾画笔

    //====== 路径/区域 ======
    private final RectF cardRect = new RectF(); //卡片实际内容区域（上抬，底部留出 shadowGap）
    private final RectF innerRect = new RectF(); //内部用于画高光缝的矩形

    //文本
    private Layout textLayout; //多行文本布局对象
    private CharSequence lastTextCache; //上次构建文本缓存（用于变更判断）

    //动画器
    private ValueAnimator pressAnimator; //按下动画器
    private ValueAnimator releaseAnimator; //释放动画器
    private ValueAnimator selectAnimator; //选中动画器
    private ValueAnimator deselectAnimator; //取消选中动画器
    private ValueAnimator checkAnimator; //对勾动画器

    public ShadowAlienButtonView(Context context) {
        this(context, null);
    }

    public ShadowAlienButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadowAlienButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //默认尺寸初始化
        cornerRadiusPx = dp(22f);
        minHeightPx = dp(40f);
        innerSeamWidthPx = dp(10f);


        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShadowAlienButtonView, defStyleAttr, 0);

        int align = typedArray.getInt(R.styleable.ShadowAlienButtonView_sab_textAlignment, 0);
        if (align == 1) textAlignment = Layout.Alignment.ALIGN_CENTER;
        else if (align == 2) textAlignment = Layout.Alignment.ALIGN_OPPOSITE;
        else textAlignment = Layout.Alignment.ALIGN_NORMAL;
        text = typedArray.getText(R.styleable.ShadowAlienButtonView_sab_text);
        badgePosition = typedArray.getInt(R.styleable.ShadowAlienButtonView_sab_badgePosition, BADGE_RIGHT);
        enableBadge = typedArray.getBoolean(R.styleable.ShadowAlienButtonView_sab_enableBadge, true);

        maxLines = typedArray.getInt(R.styleable.ShadowAlienButtonView_sab_maxLines, 2);
        textSizePx = typedArray.getDimension(R.styleable.ShadowAlienButtonView_sab_textSize, sp(16f));
        textColor = typedArray.getColor(R.styleable.ShadowAlienButtonView_sab_textColor, 0xff262626);

        unCheckedStrokeWidthPx = typedArray.getDimension(R.styleable.ShadowAlienButtonView_sab_unCheckedStrokeWidth, dp(1f));
        checkedStrokeWidthPx = typedArray.getDimension(R.styleable.ShadowAlienButtonView_sab_checkedStrokeWidth, dp(1f));

        unCheckedStrokeColor = typedArray.getColor(R.styleable.ShadowAlienButtonView_sab_unCheckedStrokeColor, 0xffe5e5e3);
        checkedStrokeColor = typedArray.getColor(R.styleable.ShadowAlienButtonView_sab_checkedStrokeColor, 0xFFF59175);

        CheckedFillBgColor = typedArray.getColor(R.styleable.ShadowAlienButtonView_sab_checkedFillBgColor, Color.WHITE);
        unCheckedFillBgColor = typedArray.getColor(R.styleable.ShadowAlienButtonView_sab_unCheckedFillBgColor, Color.WHITE);

        checkedBottomAlienColor = typedArray.getColor(R.styleable.ShadowAlienButtonView_sab_checkedBottomAlienColor, 0xFFF59175);
        unCheckedBottomAlienFillColor = typedArray.getColor(R.styleable.ShadowAlienButtonView_sab_unCheckedBottomAlienFillColor, 0xffe5e5e3);

        shadowGapPx = typedArray.getDimension(R.styleable.ShadowAlienButtonView_sab_shadowGap, dp(3f));
        pressOffsetPx = typedArray.getDimension(R.styleable.ShadowAlienButtonView_sab_pressOffset, dp(3f));

        typedArray.recycle();


        //文字画笔设置
        textPaint.setTextSize(textSizePx);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.LEFT);

        //填充画笔
        cardPaint.setStyle(Paint.Style.FILL);
        cardPaint.setColor(unCheckedFillBgColor);

        //边框画笔
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);

        //内侧高光缝画笔
        seamPaint.setStyle(Paint.Style.STROKE);
        seamPaint.setStrokeCap(Paint.Cap.ROUND);
        seamPaint.setStrokeJoin(Paint.Join.ROUND);
        seamPaint.setColor(Color.argb(72, 255, 255, 255));

        //徽标圆 + 对勾画笔
        badgePaint.setStyle(Paint.Style.FILL);
        badgePaint.setColor(checkedStrokeColor);

        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeCap(Paint.Cap.ROUND);
        tickPaint.setStrokeJoin(Paint.Join.ROUND);
        tickPaint.setColor(Color.WHITE);
        tickPaint.setStrokeWidth(Math.max(2f, dp(2f)));

        //阴影画笔
        shadowPaint.setStyle(Paint.Style.FILL);
        setLayerType(LAYER_TYPE_SOFTWARE, null);


        //动画相关
        pressAnimator = ValueAnimator.ofFloat(0f, 1f);
        pressAnimator.setDuration(100);
        pressAnimator.setInterpolator(new AccelerateInterpolator());
        pressAnimator.addUpdateListener(a -> {
            pressProgress = (float) a.getAnimatedValue();
            releaseProgress = 1f - pressProgress;
            invalidate();
        });

        releaseAnimator = ValueAnimator.ofFloat(0f, 1f);
        releaseAnimator.setDuration(100);
        releaseAnimator.setInterpolator(new DecelerateInterpolator());
        releaseAnimator.addUpdateListener(a -> {
            releaseProgress = (float) a.getAnimatedValue();
            pressProgress = 1f - releaseProgress;
            invalidate();
        });
        releaseAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                postDelayed(ShadowAlienButtonView.this::startCheckAnimation, 50);
            }
        });

        selectAnimator = ValueAnimator.ofFloat(0f, 1f);
        selectAnimator.setDuration(140);
        selectAnimator.setInterpolator(new OvershootInterpolator(1.25f));
        selectAnimator.addUpdateListener(a -> {
            selectProgress = (float) a.getAnimatedValue();
            invalidate();
        });

        deselectAnimator = ValueAnimator.ofFloat(1f, 0f);
        deselectAnimator.setDuration(140); //时长 140ms
        deselectAnimator.setInterpolator(new DecelerateInterpolator());
        deselectAnimator.addUpdateListener(a -> {
            selectProgress = (float) a.getAnimatedValue();
            invalidate();
        });

        checkAnimator = ValueAnimator.ofFloat(0f, 1f);
        checkAnimator.setDuration(180); //时长 180ms
        checkAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        checkAnimator.addUpdateListener(a -> {
            checkProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        setEnabled(true);
    }

    private void startCheckAnimation() {
        if (!isSelected) return; //仅选中状态才展示对勾
        checkProgress = 0f;
        if (checkAnimator != null) {
            checkAnimator.cancel();
            checkAnimator.start();
        }
    }

    private void cancelCheckAnimation() {
        if (checkAnimator != null) checkAnimator.cancel();
        checkProgress = 0f;
    }

    //========= 量化/布局 =========
    //计算左右内边距（与圆角相关）
    private float autoHPad() {
        return Math.max(dp(12f), cornerRadiusPx * 0.6f);
    }

    //右侧徽标（圆+对勾）预留宽度
    private float badgeReservePx() {
        //徽标半径
        float r = dp(12f);
        //右侧内边距
        float insetFromRight = dp(12f);
        //文本与徽标的间距
        float textGap = dp(8f);
        //总预留宽度（约 44dp）
        return insetFromRight + 2f * r + textGap;
    }

    //构建多行文本布局
    private void buildTextLayout(int width) {
        if (width <= 0) return;

        //左右两侧基准都是 cornerRadiusPx；哪侧放徽标，就把那一侧由 cornerRadiusPx 换成 badgeReservePx()
        float extraLeft = (enableBadge && badgePosition == BADGE_LEFT) ? (badgeReservePx() - cornerRadiusPx) : 0f;
        float extraRight = (enableBadge && badgePosition == BADGE_RIGHT) ? (badgeReservePx() - cornerRadiusPx) : 0f;
        //计算可用宽度
        int avail = (int) (
                width
                        - getPaddingLeft() - getPaddingRight()
                        - autoHPad() * 2f
                        - cornerRadiusPx * 2
                        - extraLeft - extraRight
        );
        //保底
        if (avail <= 0) avail = 1;
        if (text == null) text = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder b = StaticLayout.Builder.obtain(text, 0, text.length(), textPaint, avail)
                    //左对齐
                    .setAlignment(textAlignment)
                    .setIncludePad(false) //不包含额外上下内边距
                    //行间距（附加=0，倍数=1）
                    .setLineSpacing(0f, 1f)
                    //超出省略
                    .setEllipsize(TextUtils.TruncateAt.END)
                    //最大行数
                    .setMaxLines(maxLines)
                    //文本方向
                    .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_LTR);
            textLayout = b.build();
        } else {
            textLayout = new StaticLayout(text, textPaint, avail, textAlignment, 1f, 0f, false);
        }
        lastTextCache = text;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        buildTextLayout(width);
        //文本高度
        float textH = textLayout != null ? textLayout.getHeight() : 0f;
        //内容高度 = 上下内边距 + 文本 + 额外留白
        float contentH = getPaddingTop() + getPaddingBottom() + textH + dp(16f);
        //保证最小高度
        contentH = Math.max(minHeightPx, contentH);
        //顶/底辅助色块占位（让上下各露出一条）
        if (enableAccents) contentH += dp(12f); //如果启用轨道色块，高度再加 12dp
        int desiredH = (int) Math.ceil(contentH); //期望高度（向上取整）
        int resolvedW = resolveSize(width, widthMeasureSpec);
        int resolvedH = resolveSize(desiredH, heightMeasureSpec);
        setMeasuredDimension(resolvedW, resolvedH);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        buildTextLayout(w);
    }

    //对勾完整路径（两段线）
    Path tick = new Path();
    //动画时截取出来的子路径
    Path seg = new Path();
    //临时画笔
    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    RectF fgRect = new RectF();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //背景“轨道”色块（上/下）
        if (enableAccents) {
            float band = dp(6f);

            p.setStyle(Paint.Style.FILL);
            p.setColor(topAccentColor);
            //绘制顶部色带
            canvas.drawRect(0, 0, getWidth(), band, p);
            p.setColor(bottomAccentColor);
            //绘制底部色带
            canvas.drawRect(0, getHeight() - band, getWidth(), getHeight(), p);
        }


        //内容区域（上抬，底部留出 shadowGap）
        float left = getPaddingLeft() + autoHPad(); //左边界（含自适应内边距）
        float right = getWidth() - getPaddingRight() - autoHPad(); //右边界
        float top = getPaddingTop() + (enableAccents ? dp(6f) : 0f); //顶边（预留上色带）
        float bottom = getHeight() - getPaddingBottom() - (enableAccents ? dp(6f) : 0f); //底边（预留下色带）

        //设定卡片矩形（上抬：底部减去阴影空隙）
        cardRect.set(left, top, right, bottom - shadowGapPx);


        if (isEnabled()) {
            //是否处于按下样式
            boolean drawPressedStyle = pressProgress > 0.5f;
            //半阴影
            //绘制仅下半部分的阴影
            drawHalfShadow(canvas, cardRect, drawPressedStyle | isSelected);
            //填充底色
            int fillCol = isSelected ? blend(unCheckedFillBgColor, CheckedFillBgColor, selectProgress) : unCheckedFillBgColor;
            cardPaint.setColor(fillCol);
            //绘制圆角填充
            fgRect.set(cardRect);
            //按压位移（整体下移）
            float dy = pressOffsetPx * pressProgress;
            fgRect.top += dy;
            fgRect.bottom += dy;
            canvas.drawRoundRect(fgRect, cornerRadiusPx, cornerRadiusPx, cardPaint);

            canvas.save();
            canvas.translate(0, dy);

            //主描边（颜色&粗细插值；按下时使用 pressedBorderColor）
            float strokeW = drawPressedStyle ? Math.max(dp(2f), checkedStrokeWidthPx) : lerp(unCheckedStrokeWidthPx, checkedStrokeWidthPx, selectProgress); //线宽
            int strokeCol; //线色
            if (drawPressedStyle) {
                //按下时
                strokeCol = checkedStrokeColor;
            } else {
                //非按下
                //结合按下与选中进度
                float t = Math.max(0.2f * pressProgress, selectProgress);
                //边框色在两色间插值
                strokeCol = blend(unCheckedStrokeColor, checkedStrokeColor, t);
            }
            strokePaint.setStrokeWidth(strokeW);
            strokePaint.setColor(strokeCol);
            //描边样式（冗余设置保证一致性）
            strokePaint.setStyle(Paint.Style.STROKE);
            //绘制圆角边框
            canvas.drawRoundRect(cardRect, cornerRadiusPx, cornerRadiusPx, strokePaint);

            //内侧高光缝（仅选中且未按下）
            //只在未按下且存在选中进度时绘制
            if (!drawPressedStyle && selectProgress > 0f) {
                //拷贝外矩形
                innerRect.set(cardRect);
                //向内缩进边框宽度
                innerRect.inset(strokeW, strokeW);
                //线宽随选中进度变化
                seamPaint.setStrokeWidth(innerSeamWidthPx * selectProgress);
                //画内侧高光缝
                canvas.drawRoundRect(innerRect, Math.max(0, cornerRadiusPx - strokeW), Math.max(0, cornerRadiusPx - strokeW), seamPaint);
            }

            //文本
            if (textLayout != null) {
                textPaint.setColor(textColor);
                textPaint.setAlpha(255);
                textPaint.setShader(null);
                textPaint.setStyle(Paint.Style.FILL);

                //文本起点 X（留圆角和微调）
                //徽标在左侧时，文本需要让出徽标的预留宽度；在右侧时保持原始逻辑
                float tx = cardRect.left + dp(2f) + ((enableBadge && badgePosition == BADGE_LEFT) ? badgeReservePx() : cornerRadiusPx);
                //垂直居中 Y
                float ty = cardRect.top + (cardRect.height() - textLayout.getHeight()) / 2f;
                canvas.save();
                canvas.translate(tx, ty);
                textLayout.draw(canvas);
                canvas.restore();
            }

            //右侧徽标 + 对勾（选中 或 对勾在播时显示；按下时也可显示不受影响）
            //选中/动画中时显示徽标
            if (enableBadge && (isSelected || checkProgress > 0f)) {
                //徽标半径，随选中进度略微放大
                float r = dp(12f) * (0.92f + 0.08f * selectProgress);
                //圆心 X（距边侧留 12dp）
                float cx = (badgePosition == BADGE_RIGHT)
                        ? (cardRect.right - r - dp(12f))
                        : (cardRect.left + r + dp(12f));
                //圆心 Y（垂直居中）
                float cy = cardRect.centerY();

                //圆形徽标
                badgePaint.setColor(checkedStrokeColor);
                //绘制圆形徽标
                canvas.drawCircle(cx, cy, r, badgePaint);

                //对勾路径
                tick.moveTo(cx - r * 0.35f, cy + r * 0.05f); //对勾起点
                tick.lineTo(cx - r * 0.08f, cy + r * 0.32f); //对勾中点
                tick.lineTo(cx + r * 0.38f, cy - r * 0.30f); //对勾终点

                PathMeasure pm = new PathMeasure(tick, false);
                float len = pm.getLength(); //路径总长度
                //根据进度计算截取终点
                float stop = len * Math.max(checkProgress, selectProgress);
                //截取 0→stop 的路径段到 seg
                pm.getSegment(0, stop, seg, true);
                //对勾线宽随半径调整
                tickPaint.setStrokeWidth(Math.max(2f, r * 0.18f));
                //绘制渐现的对勾
                canvas.drawPath(seg, tickPaint);
            }

            canvas.restore();
        } else {
            // 不绘制上/下轨道色块、不绘制阴影、徽标、内缝、对勾、动画
            fgRect.set(cardRect);

            // 圆角：禁用态保持当前圆角，不做动态自适应也可
            float r = cornerRadiusPx;

            // 填充
            cardPaint.setColor(disabledFillBgColor);
            canvas.drawRoundRect(fgRect, r, r, cardPaint);

            // （可选）极淡描边
            if (Color.alpha(disabledStrokeColor) > 0) {
                strokePaint.setStyle(Paint.Style.STROKE);
                strokePaint.setStrokeWidth(unCheckedStrokeWidthPx);
                strokePaint.setColor(disabledStrokeColor);
                canvas.drawRoundRect(fgRect, r, r, strokePaint);
            }

            // 文本（白字）
            if (textLayout != null) {
                textPaint.setColor(disabledTextColor);
                textPaint.setShader(null);
                float tx = cardRect.left + dp(2f)
                        + ((enableBadge && badgePosition == BADGE_LEFT) ? badgeReservePx() : cornerRadiusPx);
                float ty = cardRect.top + (cardRect.height() - textLayout.getHeight()) / 2f;
                canvas.save();
                canvas.translate(tx, ty);
                textLayout.draw(canvas);
                canvas.restore();
            }
            return; // 禁止态到此结束
        }
    }

    //绘制下半部悬浮阴影
    private void drawHalfShadow(Canvas canvas, RectF fg, boolean b) {
        //内容高度
        float fgH = cardRect.height();
//        cornerRadiusPx = Math.min(fgH / (textLayout.getLineCount() == 1 ? 2f : 3f), cardRect.width() / (textLayout.getLineCount() == 1 ? 2f : 3f)); //根据行数自适应圆角
        //阴影厚度
        shadowSize = dp(3f); //基础阴影厚度
        //高度与圆角直径比
        float ratio = fgH / (cornerRadiusPx * 2f); //比例用于调节外扩
        //外扩系数
        float factor = 0.15f - Math.min(0.15f, 0.1f * (ratio - 1f)); //外扩随比例衰减
        //外扩像素值
        shadowExtra = cornerRadiusPx * factor;

        shadowPath.reset(); //重置路径
        shadowPath.moveTo(fg.left + cornerRadiusPx, fg.top); //顶边左圆角起点
        shadowPath.lineTo(fg.right - cornerRadiusPx, fg.top); //顶边直线
        shadowPath.quadTo(fg.right, fg.top, fg.right, fg.top + cornerRadiusPx); //右上圆角
        shadowPath.lineTo(fg.right, fg.bottom - cornerRadiusPx); //右边直线到底部圆角上方
        shadowPath.quadTo( //右下圆角延伸到阴影底
                fg.right,
                fg.bottom + shadowSize,
                fg.right - (cornerRadiusPx + shadowExtra),
                fg.bottom + shadowSize
        );
        shadowPath.lineTo(fg.left + (cornerRadiusPx + shadowExtra), fg.bottom + shadowSize); //底边阴影直线
        shadowPath.quadTo( //左下圆角阴影
                fg.left,
                fg.bottom + shadowSize,
                fg.left,
                fg.bottom - cornerRadiusPx
        );
        shadowPath.lineTo(fg.left, fg.top + cornerRadiusPx); //左边直线
        shadowPath.quadTo(fg.left, fg.top, fg.left + cornerRadiusPx, fg.top); //左上圆角
        shadowPath.close();

        canvas.save();
        //仅裁剪下半段区域
        float clipTop = Math.max(fg.top, fg.bottom - cornerRadiusPx);
        //裁剪出阴影可见区域
        canvas.clipRect(fg.left - shadowSize * 2f, clipTop, fg.right + shadowSize * 2f, fg.bottom + shadowSize);

        if (b) {
            shadowGradient = new LinearGradient(
                    0, fg.bottom - cornerRadiusPx * 0.6f,
                    0, fg.bottom + shadowSize,
                    new int[]{checkedBottomAlienColor, checkedBottomAlienColor, checkedBottomAlienColor},
                    new float[]{0f, 0.55f, 1f},
                    Shader.TileMode.CLAMP
            );
        } else {
            shadowGradient = new LinearGradient(
                    0, fg.bottom - cornerRadiusPx * 0.6f,
                    0, fg.bottom + shadowSize,
                    new int[]{unCheckedBottomAlienFillColor, unCheckedBottomAlienFillColor, unCheckedBottomAlienFillColor},
                    new float[]{0f, 0.55f, 1f},
                    Shader.TileMode.CLAMP
            );
        }


        shadowPaint.setShader(shadowGradient);
        canvas.drawPath(shadowPath, shadowPaint);
        canvas.restore();
    }

    @Override
    public void setEnabled(boolean enabled) {
        boolean changed = enabled != isEnabled();
        super.setEnabled(enabled);
        if (!changed) return;

        // 禁止时：全部动画停掉并复位到“未按下、无对勾”
        if (!enabled) {
            if (pressAnimator != null) pressAnimator.cancel();
            if (releaseAnimator != null) releaseAnimator.cancel();
            if (selectAnimator != null) selectAnimator.cancel();
            if (deselectAnimator != null) deselectAnimator.cancel();
            if (checkAnimator != null) checkAnimator.cancel();

            isPressedInside = false;
            pressProgress = 0f;
            releaseProgress = 1f;
            checkProgress = 0f;
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        if (!isEnabled()) return false; // 禁止态不响应触摸

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isPressedInside = true;
                getParent().requestDisallowInterceptTouchEvent(true);
                startPress();
                return true;
            case MotionEvent.ACTION_MOVE: {
                boolean inside = e.getX() >= 0 && e.getX() <= getWidth() && e.getY() >= 0 && e.getY() <= getHeight();
                if (isPressedInside != inside) {
                    isPressedInside = inside;
                    if (inside) startPress();
                    else cancelPress();
                }
                return true;
            }
            case MotionEvent.ACTION_CANCEL:
                cancelPress();
                return true;
            case MotionEvent.ACTION_UP: {
                boolean click = isPressedInside;
                endPress();
                if (click) {
                    setSelected(true);
                    performClick();
                }
                return true;
            }
        }
        return super.onTouchEvent(e);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    //开始按下动画
    private void startPress() {
        cancelCheckAnimation();
        if (releaseAnimator != null) releaseAnimator.cancel();
        if (pressAnimator != null) {
            pressAnimator.cancel();
            pressAnimator.start();
        }
        invalidate();
    }

    //结束按下，开始释放动画
    private void endPress() {
        if (pressAnimator != null) pressAnimator.cancel();
        if (releaseAnimator != null) {
            releaseAnimator.cancel();
            releaseAnimator.start();
        }
        invalidate();
    }

    //取消按压态（例如移出控件）
    private void cancelPress() {
        isPressedInside = false;
        endPress();
    }

    //设置文本内容
    public void setText(CharSequence t) {
        if (t == null) t = "";
        this.text = t; //赋值
        if (!TextUtils.equals(lastTextCache, t)) {
            buildTextLayout(getWidth());
            requestLayout();
            invalidate();
        }
    }

    //设置文本颜色
    public void setTextColor(int color) {
        this.textColor = color;
        textPaint.setColor(color);
        invalidate();
    }

    //设置最大行数
    public void setMaxLines(int lines) {
        lines = Math.max(1, lines);
        if (this.maxLines != lines) {
            this.maxLines = lines;
            buildTextLayout(getWidth());
            requestLayout();
            invalidate();
        }
    }

    //设置圆角（单位 dp）
    public void setCornerRadiusDp(float dpv) {
        this.cornerRadiusPx = dp(dpv);
        invalidate(); //重绘
    }

    //设置选中态颜色（边框/徽标色 和 填充色）
    public void setActiveColors(int stroke, int fill) {
        this.checkedStrokeColor = stroke;
        this.CheckedFillBgColor = fill;
        invalidate();
    }

    //设置圆形徽标位置（BADGE_LEFT / BADGE_RIGHT）
    public void setBadgePosition(int position) {
        if (position != BADGE_LEFT && position != BADGE_RIGHT) return;
        if (this.badgePosition != position) {
            this.badgePosition = position;
            //徽标在左/右会改变文本可用宽度与起点，需要重建布局并重绘
            buildTextLayout(getWidth());
            requestLayout();
            invalidate();
        }
    }

    //设置徽标是否绘制
    public void setBadgeEnabled(boolean enabled) {
        if (this.enableBadge != enabled) {
            this.enableBadge = enabled;
            // 关闭绘制时释放徽标占位，让文本可用宽度回收
            buildTextLayout(getWidth());
            requestLayout();
            invalidate();
        }
    }

    //外部设置选中状态
    public void setSelected(boolean selected) {
        if (this.isSelected == selected) return;
        this.isSelected = selected;
        if (selected) {
            if (deselectAnimator != null) deselectAnimator.cancel();
            if (selectAnimator != null) {
                selectAnimator.cancel();
                selectAnimator.start();
            }
        } else {
            if (selectAnimator != null) selectAnimator.cancel();
            if (deselectAnimator != null) {
                deselectAnimator.cancel();
                deselectAnimator.start();
            }
            cancelCheckAnimation();
        }
        invalidate();
    }

    //线性插值工具
    private static float lerp(float a, float b, float t) {
        return a + (b - a) * Math.max(0f, Math.min(1f, t)); //限制 t 在 [0,1] 后插值
    }

    //颜色插值工具（ARGB 通道分别插值）
    private static int blend(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t)); //限制 t 在 [0,1]
        int a = (int) (Color.alpha(c1) + (Color.alpha(c2) - Color.alpha(c1)) * t); //A 通道
        int r = (int) (Color.red(c1) + (Color.red(c2) - Color.red(c1)) * t); //R 通道
        int g = (int) (Color.green(c1) + (Color.green(c2) - Color.green(c1)) * t); //G 通道
        int b = (int) (Color.blue(c1) + (Color.blue(c2) - Color.blue(c1)) * t); //B 通道
        return Color.argb(a, r, g, b); //合成颜色
    }

    private static float dp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, ResourcesHolder.density());
    }

    private static float sp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, ResourcesHolder.density());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow(); //调用父类
        if (pressAnimator != null) pressAnimator.cancel();
        if (releaseAnimator != null) releaseAnimator.cancel();
        if (selectAnimator != null) selectAnimator.cancel();
        if (deselectAnimator != null) deselectAnimator.cancel();
        if (checkAnimator != null) checkAnimator.cancel();
        shadowGradient = null;
        textLayout = null;
    }

    private static class ResourcesHolder {
        private static android.util.DisplayMetrics metrics;

        static android.util.DisplayMetrics density() {
            if (metrics == null) {
                metrics = android.content.res.Resources.getSystem().getDisplayMetrics();
            }
            return metrics;
        }
    }
}