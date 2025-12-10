package com.polymarket.clob.types;

import java.util.Set;

/**
 * Tick size values
 */
public final class TickSize {
    private TickSize() {}

    public static final String TICK_0_1 = "0.1";
    public static final String TICK_0_01 = "0.01";
    public static final String TICK_0_001 = "0.001";
    public static final String TICK_0_0001 = "0.0001";

    public static final Set<String> VALID_TICKS = Set.of(
        TICK_0_1,
        TICK_0_01,
        TICK_0_001,
        TICK_0_0001
    );

    public static boolean isValid(String tickSize) {
        return VALID_TICKS.contains(tickSize);
    }
}

