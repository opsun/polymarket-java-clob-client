package com.polymarket.clob.types;

/**
 * Partial options for creating an order (all optional)
 */
public record PartialCreateOrderOptions(
    String tickSize,
    Boolean negRisk
) {
    public PartialCreateOrderOptions() {
        this(null, null);
    }
}

