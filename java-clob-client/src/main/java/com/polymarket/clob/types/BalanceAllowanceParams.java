package com.polymarket.clob.types;

/**
 * Parameters for balance and allowance queries
 */
public record BalanceAllowanceParams(
    AssetType assetType,
    String tokenId,
    int signatureType
) {
    public BalanceAllowanceParams() {
        this(null, null, -1);
    }
}

