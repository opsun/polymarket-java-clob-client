package com.polymarket.clob.types;

/**
 * Parameters for trade queries
 */
public record TradeParams(
    String id,
    String makerAddress,
    String market,
    String assetId,
    Long before,
    Long after
) {
    public TradeParams() {
        this(null, null, null, null, null, null);
    }
}

