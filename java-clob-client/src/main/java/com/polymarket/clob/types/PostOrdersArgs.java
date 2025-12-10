package com.polymarket.clob.types;

import java.util.Map;

/**
 * Arguments for posting multiple orders
 */
public record PostOrdersArgs(
    Map<String, Object> order,
    OrderType orderType
) {
    public PostOrdersArgs(Map<String, Object> order) {
        this(order, OrderType.GTC);
    }
}

