package com.huonggiang.utils;

import java.util.Locale;

/**
 * Shared currency formatting helpers for VND amounts.
 */
public final class CurrencyUtils {

    private CurrencyUtils() { /* utility class */ }

    /**
     * Formats a double amount as a dot-separated integer string, e.g. 1.234.567
     * (no currency suffix — append via string resource str_amount_format if needed).
     */
    public static String formatNumber(double amount) {
        long rounded = Math.round(amount);
        return String.format(Locale.US, "%,d", rounded).replace(",", ".");
    }

    /**
     * Formats a double amount as "1.234.567 đ" (VND style).
     */
    public static String formatVnd(double amount) {
        return formatNumber(amount) + " đ";
    }
}
