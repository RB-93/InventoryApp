package com.rohit.examples.android.bookstore.Utility;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;

public class ErrorEditText extends TextInputEditText {
    public ErrorEditText(Context context) {
        super(context);
    }

    public ErrorEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ErrorEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setError(CharSequence error, Drawable icon) {
        setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
    }
}
