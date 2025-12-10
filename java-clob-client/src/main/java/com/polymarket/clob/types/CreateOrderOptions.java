package com.polymarket.clob.types;

/**
 * Options for creating an order
 */
public record CreateOrderOptions(
    String tickSize,
    boolean negRisk
) {}

