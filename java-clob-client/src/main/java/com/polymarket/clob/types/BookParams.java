package com.polymarket.clob.types;

/**
 * Parameters for book queries
 */
public record BookParams(
    String tokenId,
    String side
) {
    public BookParams(String tokenId) {
        this(tokenId, "");
    }
}

