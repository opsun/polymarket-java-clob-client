package com.polymarket.clob.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polymarket.clob.types.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility functions for order processing
 */
public final class Utilities {
    private Utilities() {}

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static OrderBookSummary parseRawOrderbookSummary(Map<String, Object> rawObs) {
        List<OrderSummary> bids = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> bidsList = (List<Map<String, Object>>) rawObs.get("bids");
        if (bidsList != null) {
            for (Map<String, Object> bid : bidsList) {
                bids.add(new OrderSummary(
                    (String) bid.get("price"),
                    (String) bid.get("size")
                ));
            }
        }

        List<OrderSummary> asks = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> asksList = (List<Map<String, Object>>) rawObs.get("asks");
        if (asksList != null) {
            for (Map<String, Object> ask : asksList) {
                asks.add(new OrderSummary(
                    (String) ask.get("price"),
                    (String) ask.get("size")
                ));
            }
        }

        return new OrderBookSummary(
            (String) rawObs.get("market"),
            (String) rawObs.get("asset_id"),
            (String) rawObs.get("timestamp"),
            bids,
            asks,
            (String) rawObs.get("min_order_size"),
            (Boolean) rawObs.get("neg_risk"),
            (String) rawObs.get("tick_size"),
            (String) rawObs.get("hash")
        );
    }

    public static String generateOrderbookSummaryHash(OrderBookSummary orderbook) {
        try {
            // Create a copy without hash
            OrderBookSummary orderbookWithoutHash = new OrderBookSummary(
                orderbook.market(),
                orderbook.assetId(),
                orderbook.timestamp(),
                orderbook.bids(),
                orderbook.asks(),
                orderbook.minOrderSize(),
                orderbook.negRisk(),
                orderbook.tickSize(),
                ""
            );

            String json = objectMapper.writeValueAsString(orderbookWithoutHash);
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate orderbook hash", e);
        }
    }

    public static Map<String, Object> orderToJson(Map<String, Object> order, String owner, OrderType orderType) {
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("owner", owner);
        result.put("orderType", orderType.getValue());
        return result;
    }

    public static boolean isTickSizeSmaller(String a, String b) {
        return Double.parseDouble(a) < Double.parseDouble(b);
    }

    public static boolean priceValid(double price, String tickSize) {
        double tick = Double.parseDouble(tickSize);
        return price >= tick && price <= (1 - tick);
    }
}

