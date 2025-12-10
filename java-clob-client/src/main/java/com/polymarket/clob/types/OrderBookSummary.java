package com.polymarket.clob.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Complete orderbook summary
 */
public record OrderBookSummary(
    @JsonProperty("market")
    String market,
    @JsonProperty("asset_id")
    String assetId,
    @JsonProperty("timestamp")
    String timestamp,
    @JsonProperty("bids")
    List<OrderSummary> bids,
    @JsonProperty("asks")
    List<OrderSummary> asks,
    @JsonProperty("min_order_size")
    String minOrderSize,
    @JsonProperty("neg_risk")
    Boolean negRisk,
    @JsonProperty("tick_size")
    String tickSize,
    @JsonProperty("hash")
    String hash
) {}

