package com.sss.michael.simpleview.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.sss.michael.simpleview.R;

/**
 * @author Michael by 61642
 * @date 2025/4/30 13:35
 * @Description 一个简单的脱密手机号输入框
 */
public class SimpleEditTextWithMaskedPhone extends AppCompatEditText implements TextWatcher, Runnable, TransformationMethod {
    //真实手机号
    private String realPhone = "";
    //脱敏模式
    private boolean isMasked = true;
    //内部文本变化
    private boolean isInternalChange = false;
    //按钮图标
    private final Drawable showDrawable, hideDrawable;

    public SimpleEditTextWithMaskedPhone(Context context, AttributeSet attrs) {
        super(context, attrs);
        setInputType(InputType.TYPE_CLASS_PHONE);
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        addTextChangedListener(this);

        float scale = 0.8f;

        showDrawable = ContextCompat.getDrawable(context, R.mipmap.icon_edit_shown);
        if (showDrawable != null) {
            showDrawable.setBounds(0, 0, (int) (showDrawable.getIntrinsicWidth() * scale), (int) (showDrawable.getIntrinsicHeight() * scale));
        }

        hideDrawable = ContextCompat.getDrawable(context, R.mipmap.icon_edit_hide);
        if (hideDrawable != null) {
            hideDrawable.setBounds(0, 0, (int) (hideDrawable.getIntrinsicWidth() * scale), (int) (hideDrawable.getIntrinsicHeight() * scale));
        }
        setCompoundDrawables(null, null, isMasked ? hideDrawable : showDrawable, null);
        post(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) {
            Drawable right = getCompoundDrawables()[2];
            if (right != null && e.getX() > getWidth() - getTotalPaddingRight()) {
                isMasked = !isMasked;
                post(this);
                return true;
            }
        }
        return super.onTouchEvent(e);
    }

    @Override
    public void run() {
        isInternalChange = true;
        setText(realPhone);
        setSelection(realPhone.length());
        setTransformationMethod(isMasked ? this : null);
        isInternalChange = false;
        setCompoundDrawables(null, null, isMasked ? hideDrawable : showDrawable, null);
    }

    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return new CharSequence() {
            @Override
            public int length() {
                return source.length();
            }

            @Override
            public char charAt(int i) {
                return (i >= 3 && i < 7) ? '*' : source.charAt(i);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                char[] buf = new char[end - start];
                for (int i = start; i < end; i++) {
                    buf[i - start] = charAt(i);
                }
                return new String(buf);
            }
        };
    }

    @Override
    public void onFocusChanged(View v, CharSequence s, boolean f, int d, Rect r) {
    }

    public String getRealPhone() {
        return realPhone;
    }

    public void setRealPhone(String phone) {
        realPhone = phone == null ? "" : phone;
        post(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!isInternalChange) {
            realPhone = s.toString();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeTextChangedListener(this);
    }
}