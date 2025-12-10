package com.polymarket.clob.types;

import com.polymarket.clob.constants.Constants;

/**
 * Arguments for creating a limit order
 */
public record OrderArgs(
    String tokenId,        // TokenID of the Conditional token asset being traded
    double price,          // Price used to create the order
    double size,           // Size in terms of the ConditionalToken
    String side,           // Side of the order
    int feeRateBps,        // Fee rate, in basis points, charged to the order maker
    long nonce,            // Nonce used for onchain cancellations
    long expiration,       // Timestamp after which the order is expired
    String taker           // Address of the order taker. Zero address indicates public order
) {
    public OrderArgs(String tokenId, double price, double size, String side) {
        this(tokenId, price, size, side, 0, 0, 0, Constants.ZERO_ADDRESS);
    }

    public OrderArgs(String tokenId, double price, double size, String side, 
                     int feeRateBps, long nonce, long expiration) {
        this(tokenId, price, size, side, feeRateBps, nonce, expiration, Constants.ZERO_ADDRESS);
    }
}

