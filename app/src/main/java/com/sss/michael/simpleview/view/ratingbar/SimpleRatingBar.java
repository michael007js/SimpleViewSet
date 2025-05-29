package com.sss.michael.simpleview.view.ratingbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;


import com.sss.michael.simpleview.R;
import com.sss.michael.simpleview.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 一个简单的 RatingBar
 * xmlns:app="http://schemas.android.com/apk/res-auto"
 * android:id="@+id/simpleRatingBar"
 * android:layout_width="wrap_content"
 * android:layout_height="wrap_content"
 * app:srb_numStars="3"
 * app:srb_minimumStars="1"
 * app:srb_rating="2"
 * app:srb_starWidth="30dp"
 * app:srb_starHeight="30dp"
 * app:srb_starPadding="15dp"
 * app:srb_stepSize="0.5"
 * app:srb_isIndicator="false"
 * app:srb_clickable="true"
 * app:srb_scrollable="true"
 * app:srb_clearRatingEnabled="true"
 * app:srb_drawableEmpty="@drawable/start_empty"
 * app:srb_drawableFilled="@drawable/star_filled"
 */

public class SimpleRatingBar extends LinearLayout {

    public interface OnRatingChangeListener {
        void onRatingChange(SimpleRatingBar ratingBar, float rating, boolean fromUser);
    }

    private int mNumStars;
    private int mPadding = 0;
    private int mStarWidth;
    private int mStarHeight;
    private float mMinimumStars = 0;
    private float mRating = -1;
    private float mStepSize = 1f;
    private float mPreviousRating = 0;

    private boolean mIsIndicator = false;
    private boolean mIsScrollable = true;
    private boolean mIsClickable = true;
    private boolean mClearRatingEnabled = true;

    private float mStartX;
    private float mStartY;

    private Drawable mEmptyDrawable;
    private Drawable mFilledDrawable;


    protected List<PartialView> mPartialViews;
    private OnRatingChangeListener mOnRatingChangeListener;

    public void setOnRatingChangeListener(OnRatingChangeListener onRatingChangeListener) {
        mOnRatingChangeListener = onRatingChangeListener;
    }

    public SimpleRatingBar(Context context) {
        this(context, null);
    }

    /* Call by xml layout */
    public SimpleRatingBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * @param context      context
     * @param attrs        attributes from XML => app:mainText="mainText"
     * @param defStyleAttr attributes from default style (Application theme or activity theme)
     */
    public SimpleRatingBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleRatingBar);
        final float rating = typedArray.getFloat(R.styleable.SimpleRatingBar_srb_rating, 0);
        mNumStars = typedArray.getInt(R.styleable.SimpleRatingBar_srb_numStars, mNumStars);
        mStepSize = typedArray.getFloat(R.styleable.SimpleRatingBar_srb_stepSize, mStepSize);
        mMinimumStars = typedArray.getFloat(R.styleable.SimpleRatingBar_srb_minimumStars, mMinimumStars);
        mPadding = typedArray.getDimensionPixelSize(R.styleable.SimpleRatingBar_srb_starPadding, mPadding);
        mStarWidth = typedArray.getDimensionPixelSize(R.styleable.SimpleRatingBar_srb_starWidth, 0);
        mStarHeight = typedArray.getDimensionPixelSize(R.styleable.SimpleRatingBar_srb_starHeight, 0);
        mEmptyDrawable = typedArray.hasValue(R.styleable.SimpleRatingBar_srb_drawableEmpty) ? ContextCompat.getDrawable(context, typedArray.getResourceId(R.styleable.SimpleRatingBar_srb_drawableEmpty, View.NO_ID)) : null;
        mFilledDrawable = typedArray.hasValue(R.styleable.SimpleRatingBar_srb_drawableFilled) ? ContextCompat.getDrawable(context, typedArray.getResourceId(R.styleable.SimpleRatingBar_srb_drawableFilled, View.NO_ID)) : null;
        mIsIndicator = typedArray.getBoolean(R.styleable.SimpleRatingBar_srb_isIndicator, mIsIndicator);
        mIsScrollable = typedArray.getBoolean(R.styleable.SimpleRatingBar_srb_scrollable, mIsScrollable);
        mIsClickable = typedArray.getBoolean(R.styleable.SimpleRatingBar_srb_clickable, mIsClickable);
        mClearRatingEnabled = typedArray.getBoolean(R.styleable.SimpleRatingBar_srb_clearRatingEnabled, mClearRatingEnabled);
        typedArray.recycle();


        if (mStarWidth <= 0) {
            mStarWidth = DensityUtil.dp2px(30);
        }
        if (mStarHeight <= 0) {
            mStarHeight = DensityUtil.dp2px(30);
        }
        if (mNumStars <= 0) {
            mNumStars = 5;
        }

        if (mPadding < 0) {
            mPadding = 0;
        }

        if (mEmptyDrawable == null) {
            mEmptyDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.icon_star_hollow);
        }

        if (mFilledDrawable == null) {
            mFilledDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.icon_star_solid);
        }

        if (mStepSize > 1.0f) {
            mStepSize = 1.0f;
        } else if (mStepSize < 0.1f) {
            mStepSize = 0.1f;
        }

        mMinimumStars = RatingBarUtils.getValidMinimumStars(mMinimumStars, mNumStars, mStepSize);


        mPartialViews = new ArrayList<>();
        for (int i = 1; i <= mNumStars; i++) {
            PartialView partialView = new PartialView(getContext(), i, mStarWidth, mStarHeight, mPadding);
            partialView.setFilledDrawable(mFilledDrawable);
            partialView.setEmptyDrawable(mEmptyDrawable);
            addView(partialView);

            mPartialViews.add(partialView);
        }
        setRating(rating, false);
    }

    /**
     * Retain this method to let other RatingBar can custom their decrease animation.
     */
    protected void emptyRatingBar() {
        fillRatingBar(0);
    }

    /**
     * Use {maxIntOfRating} because if the rating is 3.5
     * the view which id is 3 also need to be filled.
     */
    protected void fillRatingBar(final float rating) {
        for (PartialView partialView : mPartialViews) {
            int ratingViewId = (int) partialView.getTag();
            double maxIntOfRating = Math.ceil(rating);

            if (ratingViewId > maxIntOfRating) {
                partialView.setEmpty();
                continue;
            }

            if (ratingViewId == maxIntOfRating) {
                partialView.setPartialFilled(rating);
            } else {
                partialView.setFilled();
            }
        }
    }

    public void setNumStars(int numStars) {
        if (numStars <= 0) {
            return;
        }

        mPartialViews.clear();
        removeAllViews();

        mNumStars = numStars;
    }

    public void setRating(float rating, boolean fromUser) {
        if (rating > mNumStars) {
            rating = mNumStars;
        }

        if (rating < mMinimumStars) {
            rating = mMinimumStars;
        }

        if (mRating == rating) {
            return;
        }
        float stepAbidingRating;

        if (mIsClickable) {
            // Respect Step size. So if the defined step size is 0.5, and we're attributing it a 4.7 rating,
            // it should actually be set to `4.5` rating.
            stepAbidingRating = Double.valueOf(Math.floor(rating / mStepSize)).floatValue() * mStepSize;
        } else {
            float offset = 0.15f;
            float percent = rating % 1;
            int starCount = (int) (rating / 1);
            if (percent > 0.5f) {
                percent -= offset;
            } else if (percent == 0) {
                percent = 0;
            } else {
                percent += offset;
            }
            stepAbidingRating = starCount + percent;
        }


        mRating = stepAbidingRating;
        if (mOnRatingChangeListener != null) {
            mOnRatingChangeListener.onRatingChange(this, mRating, fromUser);
        }

        fillRatingBar(mRating);
    }

    public float getRating() {
        return mRating;
    }

    // Unit is pixel
    public void setStarWidth(@IntRange(from = 0) int starWidth) {
        mStarWidth = starWidth;
        for (PartialView partialView : mPartialViews) {
            partialView.setStarWidth(starWidth);
        }
    }

    // Unit is pixel
    public void setStarHeight(@IntRange(from = 0) int starHeight) {
        mStarHeight = starHeight;
        for (PartialView partialView : mPartialViews) {
            partialView.setStarHeight(starHeight);
        }
    }

    public void setStarPadding(int ratingPadding) {
        if (ratingPadding < 0) {
            return;
        }
        mPadding = ratingPadding;
        for (PartialView partialView : mPartialViews) {
            partialView.setPadding(mPadding, mPadding, mPadding, mPadding);
        }
    }

    public int getStarPadding() {
        return mPadding;
    }

    public void setEmptyDrawableRes(@DrawableRes int res) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), res);
        if (drawable != null) {
            setEmptyDrawable(drawable);
        }
    }

    public void setEmptyDrawable(@NonNull Drawable drawable) {
        mEmptyDrawable = drawable;
        for (PartialView partialView : mPartialViews) {
            partialView.setEmptyDrawable(drawable);
        }
    }

    public void setFilledDrawableRes(@DrawableRes int res) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), res);
        if (drawable != null) {
            setFilledDrawable(drawable);
        }
    }

    public void setFilledDrawable(@NonNull Drawable drawable) {
        mFilledDrawable = drawable;
        for (PartialView partialView : mPartialViews) {
            partialView.setFilledDrawable(drawable);
        }
    }


    public void setMinimumStars(@FloatRange(from = 0.0) float minimumStars) {
        mMinimumStars = RatingBarUtils.getValidMinimumStars(minimumStars, mNumStars, mStepSize);
    }

    public void setIsIndicator(boolean indicator) {
        mIsIndicator = indicator;
    }

    public void setScrollable(boolean scrollable) {
        mIsScrollable = scrollable;
    }

    public void setClickable(boolean clickable) {
        this.mIsClickable = clickable;
    }

    public void setClearRatingEnabled(boolean enabled) {
        this.mClearRatingEnabled = enabled;
    }

    public void setStepSize(@FloatRange(from = 0.1, to = 1.0) float stepSize) {
        this.mStepSize = stepSize;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsIndicator) {
            return false;
        }
        if (!mIsClickable) {
            return false;
        }

        float eventX = event.getX();
        float eventY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = eventX;
                mStartY = eventY;
                mPreviousRating = mRating;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsScrollable) {
                    return false;
                }

                for (PartialView partialView : mPartialViews) {
                    if (eventX < partialView.getWidth() / 10f + (mMinimumStars * partialView.getWidth())) {
                        setRating(mMinimumStars, true);
                        return false;
                    }

                    if (!isPositionInRatingView(eventX, partialView)) {
                        continue;
                    }

                    float rating = RatingBarUtils.calculateRating(partialView, mStepSize, eventX);

                    if (mRating != rating) {
                        setRating(rating, true);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!RatingBarUtils.isClickEvent(mStartX, mStartY, event)) {
                    return false;
                }

                for (PartialView partialView : mPartialViews) {
                    if (!isPositionInRatingView(eventX, partialView)) {
                        continue;
                    }

                    float rating = mStepSize == 1 ? (int) partialView.getTag() : RatingBarUtils.calculateRating(partialView, mStepSize, eventX);

                    if (mPreviousRating == rating && mClearRatingEnabled) {
                        setRating(mMinimumStars, true);
                    } else {
                        setRating(rating, true);
                    }
                    break;
                }
        }

        getParent().requestDisallowInterceptTouchEvent(true);
        return true;
    }

    private boolean isPositionInRatingView(float eventX, View ratingView) {
        return eventX > ratingView.getLeft() && eventX < ratingView.getRight();
    }

}