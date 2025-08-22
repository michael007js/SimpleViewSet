package com.sss.michael.simpleview.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.sss.michael.simpleview.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael by 61642
 * @date 2025/8/22 10:51
 * @Description 一个功能强大的气泡布局，支持箭头、圆角、渐变、阴影、描边等效果
 * <p>
 * 主要特性：
 * - 支持四个方向的箭头显示，可自定义大小、位置和形状
 * - 支持圆角矩形背景，可自定义圆角半径（同时支持四个角分别设置，四个角的圆角优先级大于cornerRadiusPx字段，如果分别设置了四个角的圆角，则覆盖cornerRadiusPx）
 * - 支持纯色填充和线性渐变填充（多点颜色和位置可配置）
 * - 支持iOS风格的柔和阴影效果（渐变开启时自动禁用以避免冲突）
 * - 支持描边效果，包括虚线描边
 * - 支持透明度调节
 * - 自动处理内边距，为箭头和阴影预留空间
 * <p>
 * 注意事项：
 * 1. 渐变填充与阴影冲突：当启用渐变填充（useFillGradient=true）时，iOS柔和阴影将自动禁用，
 * 因为Shader和ShadowLayer在Android中不能同时正常工作
 * 2. 渐变颜色配置：支持通过字符串配置多点渐变颜色，格式为逗号分隔的颜色值，
 * 支持：#RRGGBB、#AARRGGBB、0xRRGGBB、颜色名称等格式
 * 示例："#FF0000,#00FF00,#0000FF" 或 "red,green,blue"
 * 3. 渐变位置配置：支持通过字符串配置渐变位置，格式为逗号分隔的0-1之间的浮点数，
 * 示例："0.0,0.5,1.0"
 * 4. 阴影效果：默认提供iOS风格的环绕阴影，可通过setUniformShadow()方法设置，
 * 建议使用低透明度的颜色值（如#33000000）获得更自然的效果
 * 5. 箭头自动避让：默认启用autoInsetPadding，会自动为箭头预留空间，
 * 如需手动控制内边距，可设置为false
 * 6. 性能考虑：阴影效果需要使用软件层渲染（LAYER_TYPE_SOFTWARE），
 * 在大量使用时需注意性能影响
 * <p>
 * 使用示例（XML）：
 * <com.sss.michael.simpleview.view.SimplePowerBubbleFrameLayout
 * android:layout_width="wrap_content"
 * android:layout_height="wrap_content"
 * app:bfl_cornerRadius="8dp"
 * app:bfl_fillColor="#FFFFFF"
 * app:bfl_arrowSide="top"
 * app:bfl_arrowWidth="16dp"
 * app:bfl_arrowHeight="8dp"
 * app:bfl_shadowRadius="8dp"
 * app:bfl_shadowColor="#33000000"
 * app:bfl_useGradient="true"
 * app:bfl_gradientColors="#FF0000,#00FF00,#0000FF"
 * app:bfl_gradientPositions="0.0,0.5,1.0">
 * <p>
 * 使用示例（代码）：
 * bubble.setUniformShadow(dp(8), Color.parseColor("#33000000"), dp(4));
 * bubble.setFillGradient(new int[]{Color.RED, Color.GREEN, Color.BLUE},
 * new float[]{0f, 0.5f, 1f}, 0f);
 */
@SuppressLint("ObsoleteSdkInt")
public class SimplePowerBubbleFrameLayout extends FrameLayout {

    public static final int SIDE_TOP = 0;
    public static final int SIDE_BOTTOM = 1;
    public static final int SIDE_LEFT = 2;
    public static final int SIDE_RIGHT = 3;

    public static final int GRAVITY_START = 0;
    public static final int GRAVITY_CENTER = 1;
    public static final int GRAVITY_END = 2;

    // 箭头形状类型
    public static final int ARROW_SHAPE_TRIANGLE = 0;
    public static final int ARROW_SHAPE_ROUNDED_TRIANGLE = 1;

    // 绘制用 Paint / Path
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path rectPath = new Path();
    private final Path triPath = new Path();
    private final Path unionPath = new Path();

    // 基础视觉属性
    private float cornerRadiusPx = dp(12);
    // 支持对四个角单独配置，未配置时为 NaN -> 回退到 cornerRadiusPx
    private float cornerRadiusTopLeftPx = Float.NaN;
    private float cornerRadiusTopRightPx = Float.NaN;
    private float cornerRadiusBottomRightPx = Float.NaN;
    private float cornerRadiusBottomLeftPx = Float.NaN;

    // 顶部/底部：base宽度；左/右：base高度
    private float arrowBasePx = dp(12);
    // 箭头深度（从边缘向外）
    private float arrowDepthPx = dp(8);
    //箭头圆角
    private float arrowCornerRadiusPx = 0;
    private int arrowSide = SIDE_TOP;
    // 0..1，<0 使用 gravity
    private float arrowPercent = -1f;
    private int arrowGravity = GRAVITY_END;
    private int arrowShape = ARROW_SHAPE_ROUNDED_TRIANGLE;

    // 背景 / 渐变（保留之前功能）
    private int fillColor = 0xFF333333;
    //渐变开启后阴影不生效
    private boolean useFillGradient = false;
    // 渐变多点颜色 只要设置了就覆盖bfl_gradientStartColor与bfl_gradientEndColor
    private int[] fillGradientColors = null;
    // 渐变多点位置（0..1）
    private float[] fillGradientPositions = null;
    private float fillGradientAngleDeg = 0f;
    private Shader fillShader = null;

    // 描边
    private int strokeColor = 0x00000000;
    private float strokeWidthPx = 0f;
    //使用渐变，渐变开启后阴影不生效
    private boolean useStrokeGradient = false;
    private Shader strokeShader = null;

    // 虚线
    private float dashWidthPx = 0f, dashGapPx = 0f, dashPhasePx = 0f;

    // 透明度（0..1）
    private float backgroundAlpha = 1f;

    // 阴影（外部阴影）
    // shadowRadiusPx: 用户期望的模糊半径（越大越模糊）
    // shadowSoftnessPx: 额外的“扩散/柔和度”，与 radius 累加构成 Paint#setShadowLayer 的最终 radius
    // shadowDxPx / shadowDyPx: 偏移。**如果为 0，我们会得到环绕（四周）均匀阴影**；如果你显式设置非 0，则表现为带方向的阴影
    private float shadowRadiusPx = 0f;
    // 默认 4dp 的额外柔和量，使阴影更发散（更像 iOS）
    private float shadowSoftnessPx = dp(4);
    private float shadowDxPx = 0f, shadowDyPx = 0f;
    // 默认更淡一些
    private int shadowColor = 0x33000000;

    // 内容 padding（由 setPadding 映射到这里）
    private int contentPadL = 0, contentPadT = 0, contentPadR = 0, contentPadB = 0;
    // 是否自动为箭头让位
    private boolean autoInsetPadding = true;
    private boolean hideArrow = false;

    // 计算出的 shadow 外扩量（由 onMeasure 计算）
    private int shadowPadLeft = 0, shadowPadTop = 0, shadowPadRight = 0, shadowPadBottom = 0;
    // last 用于差量偏移子 view，避免重复累加
    private int lastShadowOffsetLeft = 0, lastShadowOffsetTop = 0;

    public SimplePowerBubbleFrameLayout(Context context) {
        this(context, null);
    }

    public SimplePowerBubbleFrameLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimplePowerBubbleFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SimplePowerBubbleFrameLayout);
            cornerRadiusPx = a.getDimension(R.styleable.SimplePowerBubbleFrameLayout_bfl_cornerRadius, cornerRadiusPx);

            // 读取单角配置（如果 attrs 中存在这些属性）
            if (a.hasValue(R.styleable.SimplePowerBubbleFrameLayout_bfl_cornerRadiusTopLeft))
                cornerRadiusTopLeftPx = a.getDimension(R.styleable.SimplePowerBubbleFrameLayout_bfl_cornerRadiusTopLeft, Float.NaN);
            if (a.hasValue(R.styleable.SimplePowerBubbleFrameLayout_bfl_cornerRadiusTopRight))
                cornerRadiusTopRightPx = a.getDimension(R.styleable.SimplePowerBubbleFrameLayout_bfl_cornerRadiusTopRight, Float.NaN);
            if (a.hasValue(R.styleable.SimplePowerBubbleFrameLayout_bfl_cornerRadiusBottomRight))
                cornerRadiusBottomRightPx = a.getDimension(R.styleable.SimplePowerBubbleFrameLayout_bfl_cornerRadiusBottomRight, Float.NaN);
            if (a.hasValue(R.styleable.SimplePowerBubbleFrameLayout_bfl_cornerRadiusBottomLeft))
                cornerRadiusBottomLeftPx = a.getDimension(R.styleable.SimplePowerBubbleFrameLayout_bfl_cornerRadiusBottomLeft, Float.NaN);

            // 向后兼容：把 arrowWidth/arrowHeight 映射到 base/depth
            float aw = a.getDimension(R.styleable.SimplePowerBubbleFrameLayout_bfl_arrowWidth, arrowBasePx);
            float ah = a.getDimension(R.styleable.SimplePowerBubbleFrameLayout_bfl_arrowHeight, arrowDepthPx);
            arrowBasePx = aw;
            arrowDepthPx = ah;
            arrowCornerRadiusPx = a.getDimension(R.styleable.SimplePowerBubbleFrameLayout_bfl_arrowCornerRadius, arrowCornerRadiusPx);
            arrowSide = a.getInt(R.styleable.SimplePowerBubbleFrameLayout_bfl_arrowSide, arrowSide);
            arrowPercent = a.getFloat(R.styleable.SimplePowerBubbleFrameLayout_bfl_arrowPositionPercent, arrowPercent);
            arrowGravity = a.getInt(R.styleable.SimplePowerBubbleFrameLayout_bfl_arrowGravity, arrowGravity);

            fillColor = a.getColor(R.styleable.SimplePowerBubbleFrameLayout_bfl_fillColor, fillColor);
            strokeColor = a.getColor(R.styleable.SimplePowerBubbleFrameLayout_bfl_strokeColor, strokeColor);
            strokeWidthPx = a.getDimension(R.styleable.SimplePowerBubbleFrameLayout_bfl_strokeWidth, strokeWidthPx);
            backgroundAlpha = clamp01(a.getFloat(R.styleable.SimplePowerBubbleFrameLayout_bfl_backgroundAlpha, backgroundAlpha));
            autoInsetPadding = a.getBoolean(R.styleable.SimplePowerBubbleFrameLayout_bfl_autoInsetPadding, true);
            hideArrow = a.getBoolean(R.styleable.SimplePowerBubbleFrameLayout_bfl_hideArrow, hideArrow);

            // dash
            dashWidthPx = a.getDimension(R.styleable.SimplePowerBubbleFrameLayout_bfl_dashWidth, 0f);
            dashGapPx = a.getDimension(R.styleable.SimplePowerBubbleFrameLayout_bfl_dashGap, 0f);
            dashPhasePx = a.getDimension(R.styleable.SimplePowerBubbleFrameLayout_bfl_dashPhase, 0f);

            // 阴影（xml 支持）
            shadowRadiusPx = a.getDimension(R.styleable.SimplePowerBubbleFrameLayout_bfl_shadowRadius, shadowRadiusPx);
            shadowDxPx = a.getDimension(R.styleable.SimplePowerBubbleFrameLayout_bfl_shadowDx, shadowDxPx);
            shadowDyPx = a.getDimension(R.styleable.SimplePowerBubbleFrameLayout_bfl_shadowDy, shadowDyPx);
            shadowColor = a.getColor(R.styleable.SimplePowerBubbleFrameLayout_bfl_shadowColor, shadowColor);
            shadowSoftnessPx = a.getDimension(R.styleable.SimplePowerBubbleFrameLayout_bfl_shadowSoftness, shadowSoftnessPx);

            // content padding（旧 API 兼容）
            int cpAll = a.getDimensionPixelSize(R.styleable.SimplePowerBubbleFrameLayout_bfl_contentPadding, 0);
            contentPadL = a.getDimensionPixelSize(R.styleable.SimplePowerBubbleFrameLayout_bfl_contentPaddingLeft, cpAll);
            contentPadT = a.getDimensionPixelSize(R.styleable.SimplePowerBubbleFrameLayout_bfl_contentPaddingTop, cpAll);
            contentPadR = a.getDimensionPixelSize(R.styleable.SimplePowerBubbleFrameLayout_bfl_contentPaddingRight, cpAll);
            contentPadB = a.getDimensionPixelSize(R.styleable.SimplePowerBubbleFrameLayout_bfl_contentPaddingBottom, cpAll);

            // 简单的两色线性渐变支持（xml）
            useFillGradient = a.getBoolean(R.styleable.SimplePowerBubbleFrameLayout_bfl_useGradient, false);
            if (useFillGradient) {
                int sc = a.getColor(R.styleable.SimplePowerBubbleFrameLayout_bfl_gradientStartColor, fillColor);
                int ec = a.getColor(R.styleable.SimplePowerBubbleFrameLayout_bfl_gradientEndColor, fillColor);
                fillGradientColors = new int[]{sc, ec};
                fillGradientPositions = null;
                fillGradientAngleDeg = a.getFloat(R.styleable.SimplePowerBubbleFrameLayout_bfl_gradientAngle, 0f);
            }

            // 从字符串属性读取多点渐变颜色和位置（用英文逗号分隔）
            String colorsStr = a.getString(R.styleable.SimplePowerBubbleFrameLayout_bfl_gradientColors);
            if (colorsStr != null && colorsStr.trim().length() > 0) {
                int[] parsed = parseColorString(colorsStr);
                if (parsed != null && parsed.length > 0) fillGradientColors = parsed;
            }

            String positionsStr = a.getString(R.styleable.SimplePowerBubbleFrameLayout_bfl_gradientPositions);
            if (positionsStr != null && positionsStr.trim().length() > 0) {
                float[] parsedPos = parseFloatPositionsString(positionsStr);
                if (parsedPos != null && parsedPos.length > 0) fillGradientPositions = parsedPos;
            }

            a.recycle();
        }

        // Paint 初始设置
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(strokeWidthPx);

        // 虚线效果
        updateDashEffect();

        // 阴影：采用 effectiveRadius = radius + softness（使阴影更柔和、扩散更广）
        // 但如果启用了渐变（useFillGradient），为了视觉一致以及避免软件层混合问题，禁用 iOS 风格柔和阴影
        if (useFillGradient) {
            // 清除 shadow layer，优先使用硬件层渲染以便 Shader 正常工作
            fillPaint.clearShadowLayer();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setLayerType(LAYER_TYPE_HARDWARE, null);
            } else {
                setLayerType(LAYER_TYPE_SOFTWARE, null);
            }
        } else {
            if (getShadowEffectiveRadius() > 0f) {
                // setShadowLayer 需要软件层绘制
                setLayerType(LAYER_TYPE_SOFTWARE, null);
                fillPaint.setShadowLayer(getShadowEffectiveRadius(), shadowDxPx, shadowDyPx, shadowColor);
            }
        }

        setWillNotDraw(false);
        applyEffectivePadding();
    }

    /**
     * 计算最终用于 setShadowLayer 的 radius（radius + softness）
     */
    private float getShadowEffectiveRadius() {
        return shadowRadiusPx + shadowSoftnessPx;
    }

    /**
     * 更新虚线 PathEffect（有 dash 参数时）
     */
    private void updateDashEffect() {
        if (strokeWidthPx > 0 && dashWidthPx > 0 && dashGapPx > 0) {
            strokePaint.setPathEffect(new DashPathEffect(new float[]{dashWidthPx, dashGapPx}, dashPhasePx));
        } else {
            strokePaint.setPathEffect(null);
        }
    }

    /**
     * 把 contentPad + strokeInset + arrowInset 合并并 setPadding 给子 View
     * 把 contentPad + strokeInset +（箭头让位）合并成实际 padding，应用到子 View。
     * 注意：shadowPad（阴影外扩）不在这里加入；shadow 通过偏移子 View（onLayout）实现，
     * 这样子 View 的 margin/padding 保持语义一致（不受阴影影响）。
     */
    private void applyEffectivePadding() {
        int strokeInset = (int) Math.ceil(strokeWidthPx / 2f);
        int l = contentPadL + strokeInset;
        int r = contentPadR + strokeInset;
        int t = contentPadT + strokeInset;
        int b = contentPadB + strokeInset;

        if (!hideArrow && autoInsetPadding) {
            if (arrowSide == SIDE_TOP) t += Math.ceil(arrowDepthPx);
            else if (arrowSide == SIDE_BOTTOM) b += Math.ceil(arrowDepthPx);
            else if (arrowSide == SIDE_LEFT) l += Math.ceil(arrowDepthPx);
            else if (arrowSide == SIDE_RIGHT) r += Math.ceil(arrowDepthPx);
        }

        super.setPadding(l, t, r, b);
    }

    private void updateFillShader(int w, int h) {
        fillShader = null;
        fillPaint.setShader(null);
        if (!useFillGradient || fillGradientColors == null || fillGradientColors.length == 0)
            return;

        float angleRad = (float) Math.toRadians(fillGradientAngleDeg % 360f);
        float dx = (float) Math.cos(angleRad), dy = (float) Math.sin(angleRad);
        float cx = w / 2f, cy = h / 2f;
        float halfDiag = (float) (Math.hypot(w, h) / 2f);
        float x0 = cx - dx * halfDiag, y0 = cy - dy * halfDiag;
        float x1 = cx + dx * halfDiag, y1 = cy + dy * halfDiag;

        // positions 可能为 null（表示均匀分布）
        fillShader = new LinearGradient(x0, y0, x1, y1, applyAlphaToColors(fillGradientColors, backgroundAlpha), fillGradientPositions, Shader.TileMode.CLAMP);
        fillPaint.setShader(fillShader);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateFillShader(w, h);
        updateStrokeShader(w, h);
    }

    private void updateStrokeShader(int w, int h) {
        strokeShader = null;
        strokePaint.setShader(null);
        if (!useStrokeGradient) return;
        // 这里的逻辑和 fill 的类似：你可以扩展为多点渐变、径向、扫描等
        // 暂时使用与 fill 相同的 colors/positions（若需要独立属性，再扩展）
        if (fillGradientColors == null || fillGradientColors.length == 0) return;
        float angleRad = (float) Math.toRadians(fillGradientAngleDeg % 360f);
        float dx = (float) Math.cos(angleRad), dy = (float) Math.sin(angleRad);
        float cx = w / 2f, cy = h / 2f;
        float halfDiag = (float) (Math.hypot(w, h) / 2f);
        float x0 = cx - dx * halfDiag, y0 = cy - dy * halfDiag;
        float x1 = cx + dx * halfDiag, y1 = cy + dy * halfDiag;
        strokeShader = new LinearGradient(x0, y0, x1, y1, applyAlphaToColors(fillGradientColors, backgroundAlpha), fillGradientPositions, Shader.TileMode.CLAMP);
        strokePaint.setShader(strokeShader);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 先让 FrameLayout 测量子 View（尊重 child 的 margin / layoutParams）
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int measuredW = getMeasuredWidth();
        int measuredH = getMeasuredHeight();

        // 计算阴影需要的外扩量（环绕式：四边都至少扩展 effRadius）
        int effRadius = (int) Math.ceil(getShadowEffectiveRadius());

        // 如果用户指定了偏移 dx/dy，我们仍然考虑偏移对某些边的额外需求（向右偏移需要右侧更多空间）
        int extraL = effRadius + (int) Math.ceil(Math.max(0f, -shadowDxPx));
        int extraR = effRadius + (int) Math.ceil(Math.max(0f, shadowDxPx));
        int extraT = effRadius + (int) Math.ceil(Math.max(0f, -shadowDyPx));
        int extraB = effRadius + (int) Math.ceil(Math.max(0f, shadowDyPx));

        // 期望的最终尺寸
        int desiredW = measuredW + extraL + extraR;
        int desiredH = measuredH + extraT + extraB;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int finalW;
        if (widthMode == MeasureSpec.EXACTLY) finalW = widthSize;
        else if (widthMode == MeasureSpec.AT_MOST) finalW = Math.min(desiredW, widthSize);
        else finalW = desiredW;

        int finalH;
        if (heightMode == MeasureSpec.EXACTLY) finalH = heightSize;
        else if (heightMode == MeasureSpec.AT_MOST) finalH = Math.min(desiredH, heightSize);
        else finalH = desiredH;

        // 分配可用的 extra 空间（优先保证左/上）
        int availableExtraW = Math.max(0, finalW - measuredW);
        shadowPadLeft = Math.min(extraL, availableExtraW);
        shadowPadRight = availableExtraW - shadowPadLeft;

        int availableExtraH = Math.max(0, finalH - measuredH);
        shadowPadTop = Math.min(extraT, availableExtraH);
        shadowPadBottom = availableExtraH - shadowPadTop;

        setMeasuredDimension(finalW, finalH);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // 差量偏移，避免重复累加
        int dx = shadowPadLeft - lastShadowOffsetLeft;
        int dy = shadowPadTop - lastShadowOffsetTop;

        if (dx != 0 || dy != 0) {
            final int cnt = getChildCount();
            for (int i = 0; i < cnt; i++) {
                View c = getChildAt(i);
                if (c.getVisibility() == View.GONE) continue;
                c.offsetLeftAndRight(dx);
                c.offsetTopAndBottom(dy);
            }
            lastShadowOffsetLeft = shadowPadLeft;
            lastShadowOffsetTop = shadowPadTop;
        }
    }

    RectF rect = new RectF();
    float[] radii = new float[8];

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // shader
        if (useFillGradient && fillShader == null) updateFillShader(getWidth(), getHeight());
        if (!useFillGradient || fillGradientColors == null || fillGradientColors.length == 0) {
            fillPaint.setShader(null);
            fillPaint.setColor(applyAlphaToColor(fillColor, backgroundAlpha));
        } else {
            fillPaint.setShader(fillShader);
        }

        final float halfStroke = strokeWidthPx > 0 ? strokeWidthPx / 2f : 0f;

        // 绘制区域以 shadowPad 为基准（子 view 已向右/下偏移 shadowPadLeft/Top）
        float left = shadowPadLeft + halfStroke;
        float top = shadowPadTop + halfStroke;
        float right = getWidth() - shadowPadRight - halfStroke;
        float bottom = getHeight() - shadowPadBottom - halfStroke;

        // 为箭头预留绘制空间（子 view 的 padding 已由 applyEffectivePadding 处理）
        if (!hideArrow) {
            if (arrowSide == SIDE_TOP) top += arrowDepthPx;
            else if (arrowSide == SIDE_BOTTOM) bottom -= arrowDepthPx;
            else if (arrowSide == SIDE_LEFT) left += arrowDepthPx;
            else if (arrowSide == SIDE_RIGHT) right -= arrowDepthPx;
        }

        rectPath.reset();
        rect.set(left, top, right, bottom);

        // 支持四角不同的圆角：若某角未设置（NaN），回退到 uniform cornerRadiusPx
        float tl = Float.isNaN(cornerRadiusTopLeftPx) ? cornerRadiusPx : cornerRadiusTopLeftPx;
        float tr = Float.isNaN(cornerRadiusTopRightPx) ? cornerRadiusPx : cornerRadiusTopRightPx;
        float br = Float.isNaN(cornerRadiusBottomRightPx) ? cornerRadiusPx : cornerRadiusBottomRightPx;
        float bl = Float.isNaN(cornerRadiusBottomLeftPx) ? cornerRadiusPx : cornerRadiusBottomLeftPx;

        // 限制半径不超过矩形一半
        float maxR = Math.min(rect.width() / 2f, rect.height() / 2f);
        tl = Math.max(0f, Math.min(tl, maxR));
        tr = Math.max(0f, Math.min(tr, maxR));
        br = Math.max(0f, Math.min(br, maxR));
        bl = Math.max(0f, Math.min(bl, maxR));

        // top-left
        radii[0] = tl;
        radii[1] = tl;

        // top-right
        radii[2] = tr;
        radii[3] = tr;

        // bottom-right
        radii[4] = br;
        radii[5] = br;

        // bottom-left
        radii[6] = bl;
        radii[7] = bl;

        rectPath.addRoundRect(rect, radii, Path.Direction.CW);

        triPath.reset();
        unionPath.reset();
        boolean merged = false;

        // 构造箭头路径（顶部/底部/左/右）
        if (!hideArrow && arrowDepthPx > 0 && arrowBasePx > 0) {
            if (arrowSide == SIDE_TOP || arrowSide == SIDE_BOTTOM) {
                float usable = rect.right - rect.left - 2f * Math.max(Math.max(tl, tr), Math.max(bl, br));
                float cx;
                if (arrowPercent >= 0f)
                    cx = rect.left + Math.max(tl, tr) + clamp01(arrowPercent) * usable;
                else {
                    int layout = getLayoutDirection();
                    boolean isRtl = (layout == LAYOUT_DIRECTION_RTL);
                    int g = arrowGravity;
                    if (g == GRAVITY_START) g = isRtl ? GRAVITY_END : GRAVITY_START;
                    if (g == GRAVITY_END) g = isRtl ? GRAVITY_START : GRAVITY_END;
                    if (g == GRAVITY_CENTER) cx = (rect.left + rect.right) / 2f;
                    else if (g == GRAVITY_START)
                        cx = rect.left + Math.max(tl, bl) + arrowBasePx / 2f;
                    else cx = rect.right - Math.max(tr, br) - arrowBasePx / 2f;
                }
                float minCx = rect.left + Math.max(tl, bl) + arrowBasePx / 2f;
                float maxCx = rect.right - Math.max(tr, br) - arrowBasePx / 2f;
                cx = Math.max(minCx, Math.min(maxCx, cx));
                float baseY = (arrowSide == SIDE_TOP) ? rect.top : rect.bottom;
                float tipY = (arrowSide == SIDE_TOP) ? (shadowPadTop + halfStroke) : (getHeight() - shadowPadBottom - halfStroke);

                if (arrowShape == ARROW_SHAPE_TRIANGLE) {
                    triPath.moveTo(cx - arrowBasePx / 2f, baseY);
                    triPath.lineTo(cx + arrowBasePx / 2f, baseY);
                    triPath.lineTo(cx, tipY);
                    triPath.close();
                } else {
                    buildRoundedTriangle(triPath,
                            cx - arrowBasePx / 2f, baseY,
                            cx + arrowBasePx / 2f, baseY,
                            cx, tipY,
                            arrowCornerRadiusPx);
                }
            } else {
                float usable = rect.bottom - rect.top - 2f * Math.max(Math.max(tl, tr), Math.max(bl, br));
                float cy;
                if (arrowPercent >= 0f)
                    cy = rect.top + Math.max(tl, tr) + clamp01(arrowPercent) * usable;
                else {
                    int g = arrowGravity;
                    if (g == GRAVITY_CENTER) cy = (rect.top + rect.bottom) / 2f;
                    else if (g == GRAVITY_START)
                        cy = rect.top + Math.max(tl, tr) + arrowBasePx / 2f;
                    else cy = rect.bottom - Math.max(bl, br) - arrowBasePx / 2f;
                }
                float minCy = rect.top + Math.max(tl, tr) + arrowBasePx / 2f;
                float maxCy = rect.bottom - Math.max(bl, br) - arrowBasePx / 2f;
                cy = Math.max(minCy, Math.min(maxCy, cy));

                float baseX, tipX;
                if (arrowSide == SIDE_LEFT) {
                    baseX = rect.left;
                    tipX = shadowPadLeft + halfStroke;
                    if (arrowShape == ARROW_SHAPE_TRIANGLE) {
                        triPath.moveTo(baseX, cy - arrowBasePx / 2f);
                        triPath.lineTo(baseX, cy + arrowBasePx / 2f);
                        triPath.lineTo(tipX, cy);
                        triPath.close();
                    } else {
                        buildRoundedTriangle(triPath,
                                baseX, cy - arrowBasePx / 2f,
                                baseX, cy + arrowBasePx / 2f,
                                tipX, cy,
                                arrowCornerRadiusPx);
                    }
                } else { // RIGHT
                    baseX = rect.right;
                    tipX = getWidth() - shadowPadRight - halfStroke;
                    if (arrowShape == ARROW_SHAPE_TRIANGLE) {
                        triPath.moveTo(baseX, cy + arrowBasePx / 2f);
                        triPath.lineTo(baseX, cy - arrowBasePx / 2f);
                        triPath.lineTo(tipX, cy);
                        triPath.close();
                    } else {
                        buildRoundedTriangle(triPath,
                                baseX, cy + arrowBasePx / 2f,
                                baseX, cy - arrowBasePx / 2f,
                                tipX, cy,
                                arrowCornerRadiusPx);
                    }
                }
            }

            // 合并路径 -> 优先 Path.op（KitKat+），失败或低版本 fallback addPath（保证只 draw 一次）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    merged = unionPath.op(rectPath, triPath, Path.Op.UNION);
                } catch (Throwable ex) {
                    unionPath.reset();
                    unionPath.addPath(rectPath);
                    unionPath.addPath(triPath);
                    merged = true;
                }
            } else {
                unionPath.reset();
                unionPath.addPath(rectPath);
                unionPath.addPath(triPath);
                merged = true;
            }
        }

        // 一次性绘制合并路径，避免 shadow 被重复绘制
        if (merged) {
            canvas.drawPath(unionPath, fillPaint);
        } else {
            canvas.drawPath(rectPath, fillPaint);
            if (!triPath.isEmpty()) canvas.drawPath(triPath, fillPaint);
        }

        // 描边（可能是渐变或纯色）
        if (!useStrokeGradient) {
            strokePaint.setShader(null);
            strokePaint.setColor(applyAlphaToColor(strokeColor, backgroundAlpha));
        } else {
            // 已在 updateStrokeShader 中准备好 strokeShader
            strokePaint.setShader(strokeShader);
        }

        if (strokeWidthPx > 0 && Color.alpha(strokeColor) != 0) {
            strokePaint.setStrokeWidth(strokeWidthPx);
            if (merged) {
                canvas.drawPath(unionPath, strokePaint);
            } else {
                canvas.drawPath(rectPath, strokePaint);
                if (!triPath.isEmpty()) canvas.drawPath(triPath, strokePaint);
            }
        }
    }

    /**
     * 圆角三角（与之前实现相同）
     */
    private static void buildRoundedTriangle(Path out,
                                             float ax, float ay,
                                             float bx, float by,
                                             float tx, float ty,
                                             float r) {
        PointF A = new PointF(ax, ay), B = new PointF(bx, by), T = new PointF(tx, ty);
        PointF[] P = new PointF[]{A, B, T};
        out.reset();

        for (int i = 0; i < 3; i++) {
            PointF prev = P[(i + 2) % 3];
            PointF cur = P[i];
            PointF next = P[(i + 1) % 3];

            float vx1 = prev.x - cur.x, vy1 = prev.y - cur.y;
            float vx2 = next.x - cur.x, vy2 = next.y - cur.y;
            float len1 = (float) Math.hypot(vx1, vy1), len2 = (float) Math.hypot(vx2, vy2);
            if (len1 == 0 || len2 == 0) continue;

            float ux1 = vx1 / len1, uy1 = vy1 / len1;
            float ux2 = vx2 / len2, uy2 = vy2 / len2;
            float dot = ux1 * ux2 + uy1 * uy2;
            dot = Math.max(-1f, Math.min(1f, dot));
            double theta = Math.acos(dot);

            float cut = (float) (r / Math.tan(theta / 2.0));
            cut = Math.min(cut, Math.min(len1, len2) * 0.45f);

            PointF p1 = new PointF(cur.x + ux1 * cut, cur.y + uy1 * cut);
            PointF p2 = new PointF(cur.x + ux2 * cut, cur.y + uy2 * cut);

            if (i == 0) out.moveTo(p1.x, p1.y);
            else out.lineTo(p1.x, p1.y);

            out.quadTo(cur.x, cur.y, p2.x, p2.y);
        }
        out.close();
    }

    /**
     * 设置统一圆角，会清除任何单角的自定义值，恢复统一半径模式
     */
    public void setCornerRadius(float px) {
        this.cornerRadiusPx = px;
        // 清除单独角的自定义值，回退到统一半径
        this.cornerRadiusTopLeftPx = Float.NaN;
        this.cornerRadiusTopRightPx = Float.NaN;
        this.cornerRadiusBottomRightPx = Float.NaN;
        this.cornerRadiusBottomLeftPx = Float.NaN;
        invalidate();
    }

    /**
     * 同时设置四个角的圆角（顺序：top-left, top-right, bottom-right, bottom-left）
     * 调用此方法后，将使用逐角半径；如需恢复统一半径，请调用 setCornerRadius(px)
     */
    public void setCornerRadii(float topLeftPx, float topRightPx, float bottomRightPx, float bottomLeftPx) {
        this.cornerRadiusTopLeftPx = Math.max(0f, topLeftPx);
        this.cornerRadiusTopRightPx = Math.max(0f, topRightPx);
        this.cornerRadiusBottomRightPx = Math.max(0f, bottomRightPx);
        this.cornerRadiusBottomLeftPx = Math.max(0f, bottomLeftPx);
        invalidate();
    }

    public void setCornerRadiusTopLeft(float px) {
        this.cornerRadiusTopLeftPx = Math.max(0f, px);
        invalidate();
    }

    public void setCornerRadiusTopRight(float px) {
        this.cornerRadiusTopRightPx = Math.max(0f, px);
        invalidate();
    }

    public void setCornerRadiusBottomRight(float px) {
        this.cornerRadiusBottomRightPx = Math.max(0f, px);
        invalidate();
    }

    public void setCornerRadiusBottomLeft(float px) {
        this.cornerRadiusBottomLeftPx = Math.max(0f, px);
        invalidate();
    }

    /**
     * 设置箭头大小（base 沿边的长度，depth 为箭头向外深度）
     */
    public void setArrowSize(float basePx, float depthPx) {
        this.arrowBasePx = basePx;
        this.arrowDepthPx = depthPx;
        applyEffectivePadding();
        requestLayout();
        invalidate();
    }

    public void setArrowCornerRadius(float px) {
        this.arrowCornerRadiusPx = px;
        invalidate();
    }

    public void setArrowSide(int side) {
        this.arrowSide = side;
        applyEffectivePadding();
        requestLayout();
        invalidate();
    }

    public void setArrowPositionPercent(float percent) {
        this.arrowPercent = percent;
        invalidate();
    }

    public void setArrowGravity(int gravity) {
        this.arrowGravity = gravity;
        invalidate();
    }

    public void setArrowShape(int shape) {
        this.arrowShape = shape;
        invalidate();
    }

    public void setHideArrow(boolean hide) {
        if (this.hideArrow == hide) return;
        this.hideArrow = hide;
        applyEffectivePadding();
        requestLayout();
        invalidate();
    }

    public boolean isArrowHidden() {
        return hideArrow;
    }

    public void setFillColor(int color) {
        this.fillColor = color;
        useFillGradient = false;
        fillPaint.setShader(null);
        invalidate();
    }

    public void setDash(float dashW, float dashGap, float phase) {
        this.dashWidthPx = dashW;
        this.dashGapPx = dashGap;
        this.dashPhasePx = phase;
        updateDashEffect();
        invalidate();
    }

    public void setStroke(int color, float widthPx) {
        this.strokeColor = color;
        this.strokeWidthPx = widthPx;
        strokePaint.setStrokeWidth(widthPx);
        applyEffectivePadding();
        updateDashEffect();
        invalidate();
    }

    public void setContentPadding(int l, int t, int r, int b) {
        this.contentPadL = Math.max(0, l);
        this.contentPadT = Math.max(0, t);
        this.contentPadR = Math.max(0, r);
        this.contentPadB = Math.max(0, b);
        applyEffectivePadding();
        requestLayout();
    }

    public void setAutoInsetPadding(boolean enable) {
        this.autoInsetPadding = enable;
        applyEffectivePadding();
        requestLayout();
    }

    /**
     * 普通的 setShadow（保留兼容）。如果 dx/dy 都为 0，则表现为环绕阴影；否则表现为偏移阴影。
     * 注意：如果当前启用了 useFillGradient（在 XML 中 bfl_useGradient 或代码中 setUseFillGradient(true)），
     * 我们**不会**在 Paint 上设置柔和的 shadow layer（按你的要求：渐变开启时 iOS 柔和阴影不生效）。
     */
    public void setShadow(float radiusPx, float dxPx, float dyPx, int color) {
        this.shadowRadiusPx = Math.max(0f, radiusPx);
        this.shadowDxPx = dxPx;
        this.shadowDyPx = dyPx;
        this.shadowColor = color;

        if (useFillGradient) {
            // 渐变开启时禁用柔和 shadow（仅记录参数，不在 Paint 上生效）
            fillPaint.clearShadowLayer();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                setLayerType(LAYER_TYPE_HARDWARE, null);
        } else {
            if (getShadowEffectiveRadius() > 0f) {
                setLayerType(LAYER_TYPE_SOFTWARE, null);
                fillPaint.setShadowLayer(getShadowEffectiveRadius(), shadowDxPx, shadowDyPx, shadowColor);
            } else {
                fillPaint.clearShadowLayer();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    setLayerType(LAYER_TYPE_HARDWARE, null);
            }
        }
        requestLayout();
        invalidate();
    }

    /**
     * 设置iOS风格阴影
     *
     * @param radiusPx   模糊半径（建议 6-12dp 之间用于轻柔效果）
     * @param color      阴影颜色（建议使用低 alpha，例如 #33000000）
     * @param softnessPx 额外扩散（使阴影更柔和，默认 4dp）
     */
    public void setUniformShadow(float radiusPx, int color, float softnessPx) {
        this.shadowRadiusPx = Math.max(0f, radiusPx);
        this.shadowSoftnessPx = Math.max(0f, softnessPx);
        this.shadowDxPx = 0f;
        this.shadowDyPx = 0f;
        this.shadowColor = color;

        if (useFillGradient) {
            // 渐变开启时禁用柔和 shadow
            fillPaint.clearShadowLayer();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                setLayerType(LAYER_TYPE_HARDWARE, null);
        } else {
            if (getShadowEffectiveRadius() > 0f) {
                setLayerType(LAYER_TYPE_SOFTWARE, null);
                // 注意：dx/dy 为 0 -> 环绕阴影
                fillPaint.setShadowLayer(getShadowEffectiveRadius(), 0f, 0f, shadowColor);
            } else {
                fillPaint.clearShadowLayer();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    setLayerType(LAYER_TYPE_HARDWARE, null);
            }
        }
        requestLayout();
        invalidate();
    }

    public void setShadowSoftness(float softnessPx) {
        this.shadowSoftnessPx = Math.max(0f, softnessPx);
        if (useFillGradient) {
            // 渐变开启时不生效
            fillPaint.clearShadowLayer();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                setLayerType(LAYER_TYPE_HARDWARE, null);
        } else {
            if (getShadowEffectiveRadius() > 0f) {
                setLayerType(LAYER_TYPE_SOFTWARE, null);
                fillPaint.setShadowLayer(getShadowEffectiveRadius(), shadowDxPx, shadowDyPx, shadowColor);
            } else {
                fillPaint.clearShadowLayer();
            }
        }
        requestLayout();
        invalidate();
    }

    // setPadding 映射到 content padding（保持 FrameLayout 语义）
    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        setContentPadding(left, top, right, bottom);
    }

    // -------------------- 工具方法 --------------------
    private int applyAlphaToColor(int color, float factor) {
        int baseA = Color.alpha(color);
        int outA = Math.round(baseA * clamp01(factor));
        return (color & 0x00FFFFFF) | (outA << 24);
    }

    // 给颜色数组应用整体透明度 factor（0..1），返回新数组
    private int[] applyAlphaToColors(int[] colors, float factor) {
        if (colors == null) return null;
        int[] out = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            out[i] = applyAlphaToColor(colors[i], factor);
        }
        return out;
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }


    /**
     * 解析颜色字符串（逗号分隔），支持：
     * - #RRGGBB 或 #AARRGGBB
     * - 0xRRGGBB / 0xAARRGGBB
     * - 颜色名（Color.parseColor 支持的）
     */
    private int[] parseColorString(String s) {
        if (s == null) return null;
        String[] parts = s.split(",");
        List<Integer> list = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (t.length() == 0) continue;
            try {
                // 优先尝试 Color.parseColor（支持 #hex 和 color names）
                int c = Color.parseColor(t);
                list.add(c);
                continue;
            } catch (IllegalArgumentException ignored) {
            }
            try {
                // 支持 0xAARRGGBB 或 0xRRGGBB
                String tmp = t.toLowerCase();
                if (tmp.startsWith("0x")) tmp = tmp.substring(2);
                long v = Long.parseLong(tmp, 16);
                // 根据长度判断是否带 alpha
                if (tmp.length() <= 6) {
                    // RRGGBB -> 加上不透明 alpha
                    v = v | 0xFF000000L;
                }
                list.add((int) v);
            } catch (Exception ex) {
                // 解析失败 -> 忽略（不加入）
            }
        }
        if (list.isEmpty()) return null;
        int[] out = new int[list.size()];
        for (int i = 0; i < list.size(); i++) out[i] = list.get(i);
        return out;
    }

    /**
     * 解析 positions 字符串（逗号分隔），并 clamp 到 [0,1]
     */
    private float[] parseFloatPositionsString(String s) {
        if (s == null) return null;
        String[] parts = s.split(",");
        List<Float> list = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (t.length() == 0) continue;
            try {
                float v = Float.parseFloat(t);
                v = Math.max(0f, Math.min(1f, v));
                list.add(v);
            } catch (Exception ex) {
                // 忽略无法解析的项
            }
        }
        if (list.isEmpty()) return null;
        float[] out = new float[list.size()];
        for (int i = 0; i < list.size(); i++) out[i] = list.get(i);
        return out;
    }
}
