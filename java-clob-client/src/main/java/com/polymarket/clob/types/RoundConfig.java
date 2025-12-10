package com.polymarket.clob.types;

/**
 * Rounding configuration for order calculations
 */
public record RoundConfig(
    int price,
    int size,
    int amount
) {}

