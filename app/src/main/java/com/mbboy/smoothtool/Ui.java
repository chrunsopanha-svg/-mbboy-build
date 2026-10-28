package com.mbboy.smoothtool;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Tiny view helpers so the whole app needs no XML layouts and no libraries. */
final class Ui {
    static final int BG = 0xFF11151A;
    static final int CARD = 0xFF1B222B;
    static final int ORANGE = 0xFFFF7A00;
    static final int TEXT = 0xFFE8EAED;
    static final int MUTED = 0xFF9AA3AD;

    private Ui() {}

    static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    static GradientDrawable rounded(Context c, int color, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    static TextView text(Context c, String s, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }

    static TextView button(Context c, String label, int bgColor, float sp, int padDp, View.OnClickListener l) {
        TextView b = text(c, label, sp, bgColor == ORANGE ? 0xFF1B1B1B : TEXT, bgColor == ORANGE);
        b.setGravity(Gravity.CENTER_VERTICAL);
        int p = dp(c, padDp);
        b.setPadding(p, p, p, p);
        b.setBackground(rounded(c, bgColor, 10));
        b.setClickable(true);
        b.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(c, 6);
        b.setLayoutParams(lp);
        return b;
    }
}
