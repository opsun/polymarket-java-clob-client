package com.polymarket.orderutils.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Order with signature
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignedOrder {

    private Order order;
    private String signature;

    /**
     * Convert signed order to a map representation suitable for API submission
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("salt", order.getSalt());
        map.put("maker", order.getMaker());
        map.put("signer", order.getSigner());
        map.put("taker", order.getTaker());
        map.put("tokenId", order.getTokenId().toString());
        map.put("makerAmount", order.getMakerAmount().toString());
        map.put("takerAmount", order.getTakerAmount().toString());
        map.put("expiration", order.getExpiration().toString());
        map.put("nonce", order.getNonce().toString());
        map.put("feeRateBps", order.getFeeRateBps().toString());

        // Convert side to string representation
        if (order.getSide() == 0) {
            map.put("side", "BUY");
        } else {
            map.put("side", "SELL");
        }

        map.put("signatureType", order.getSignatureType());
        map.put("signature", signature);

        return map;
    }
}
