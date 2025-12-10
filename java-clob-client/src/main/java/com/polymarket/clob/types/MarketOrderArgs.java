package com.polymarket.clob.types;

import com.polymarket.clob.constants.Constants;

/**
 * Arguments for creating a market order
 */
public record MarketOrderArgs(
    String tokenId,        // TokenID of the Conditional token asset being traded
    double amount,         // BUY orders: $$$ Amount to buy, SELL orders: Shares to sell
    String side,           // Side of the order
    double price,          // Price used to create the order (can be 0 for auto-calculation)
    int feeRateBps,        // Fee rate, in basis points, charged to the order maker
    long nonce,            // Nonce used for onchain cancellations
    String taker,          // Address of the order taker. Zero address indicates public order
    OrderType orderType    // Order type (FOK, GTC, etc.)
) {
    public MarketOrderArgs(String tokenId, double amount, String side) {
        this(tokenId, amount, side, 0, 0, 0, Constants.ZERO_ADDRESS, OrderType.FOK);
    }
}

