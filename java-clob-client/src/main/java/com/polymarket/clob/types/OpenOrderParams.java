package com.polymarket.clob.types;

/**
 * Parameters for open order queries
 */
public record OpenOrderParams(
    String id,
    String market,
    String assetId
) {
    public OpenOrderParams() {
        this(null, null, null);
    }
}

