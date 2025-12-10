package com.polymarket.orderutils.model;

/**
 * Order sides
 */
public enum Side {
    BUY(0),
    SELL(1);

    private final int value;

    Side(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Side fromValue(int value) {
        for (Side side : Side.values()) {
            if (side.value == value) {
                return side;
            }
        }
        throw new IllegalArgumentException("Invalid side value: " + value);
    }
}
