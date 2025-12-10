package com.polymarket.clob.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Summary of a single order in the orderbook
 */
public record OrderSummary(
    @JsonProperty("price")
    String price,
    @JsonProperty("size")
    String size
) {}

