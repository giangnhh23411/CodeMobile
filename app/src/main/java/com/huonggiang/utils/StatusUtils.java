package com.huonggiang.utils;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.huonggiang.k23411teapp.R;
import com.huonggiang.models.OrderStatus;

/**
 * Shared helper for rendering order-status pill badges on any TextView.
 */
public final class StatusUtils {

    private StatusUtils() { /* utility class */ }

    /** Applies pill background + text color + label to {@code badge}. */
    public static void applyBadge(TextView badge, OrderStatus status, Context ctx) {
        badge.setText(label(status, ctx));
        badge.setTextColor(ContextCompat.getColor(ctx, textColor(status)));

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(32f);
        bg.setColor(ContextCompat.getColor(ctx, bgColor(status)));
        badge.setBackground(bg);
    }

    public static String label(OrderStatus s, Context ctx) {
        switch (s) {
            case COMPLETED:   return ctx.getString(R.string.str_order_status_completed);
            case NOT_PAYMENT: return ctx.getString(R.string.str_order_status_not_payment);
            case ON_LOGISTIC: return ctx.getString(R.string.str_order_status_on_logistic);
            case COMPLAIN:    return ctx.getString(R.string.str_order_status_complain);
            default:          return ctx.getString(R.string.str_order_status_all);
        }
    }

    private static int textColor(OrderStatus s) {
        switch (s) {
            case COMPLETED:   return R.color.status_completed;
            case NOT_PAYMENT: return R.color.status_not_payment;
            case ON_LOGISTIC: return R.color.status_on_logistic;
            case COMPLAIN:    return R.color.status_complain;
            default:          return R.color.grey;
        }
    }

    private static int bgColor(OrderStatus s) {
        switch (s) {
            case COMPLETED:   return R.color.status_completed_bg;
            case NOT_PAYMENT: return R.color.status_not_payment_bg;
            case ON_LOGISTIC: return R.color.status_on_logistic_bg;
            case COMPLAIN:    return R.color.status_complain_bg;
            default:          return R.color.grey;
        }
    }
}
