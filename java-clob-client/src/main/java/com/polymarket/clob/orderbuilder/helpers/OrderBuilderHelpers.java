package com.polymarket.clob.orderbuilder.helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Helper functions for order building calculations
 */
public final class OrderBuilderHelpers {
    private OrderBuilderHelpers() {}

    private static final int TOKEN_DECIMALS = 6;

    public static double roundDown(double x, int sigDigits) {
        double multiplier = Math.pow(10, sigDigits);
        return Math.floor(x * multiplier) / multiplier;
    }

    public static double roundNormal(double x, int sigDigits) {
        double multiplier = Math.pow(10, sigDigits);
        return Math.round(x * multiplier) / multiplier;
    }

    public static double roundUp(double x, int sigDigits) {
        double multiplier = Math.pow(10, sigDigits);
        return Math.ceil(x * multiplier) / multiplier;
    }

    public static long toTokenDecimals(double x) {
        double f = Math.pow(10, TOKEN_DECIMALS) * x;
        int decimalPlaces = decimalPlaces(f);
        if (decimalPlaces > 0) {
            f = roundNormal(f, 0);
        }
        return (long) f;
    }

    public static int decimalPlaces(double x) {
        BigDecimal bd = BigDecimal.valueOf(x);
        bd = bd.stripTrailingZeros();
        int scale = bd.scale();
        return Math.max(0, scale);
    }
}

