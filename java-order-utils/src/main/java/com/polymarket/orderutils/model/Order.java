package com.polymarket.orderutils.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * EIP712 compliant Order structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /**
     * Unique salt to ensure entropy
     */
    private BigInteger salt;

    /**
     * Maker of the order, i.e the source of funds for the order
     */
    private String maker;

    /**
     * Signer of the order
     */
    private String signer;

    /**
     * Address of the order taker. The zero address is used to indicate a public order
     */
    private String taker;

    /**
     * Token Id of the CTF ERC1155 asset to be bought or sold.
     * If BUY, this is the tokenId of the asset to be bought, i.e the makerAssetId
     * If SELL, this is the tokenId of the asset to be sold, i.e the takerAssetId
     */
    private BigInteger tokenId;

    /**
     * Maker amount, i.e the max amount of tokens to be sold
     */
    private BigInteger makerAmount;

    /**
     * Taker amount, i.e the minimum amount of tokens to be received
     */
    private BigInteger takerAmount;

    /**
     * Timestamp after which the order is expired
     */
    private BigInteger expiration;

    /**
     * Nonce used for onchain cancellations
     */
    private BigInteger nonce;

    /**
     * Fee rate, in basis points, charged to the order maker, charged on proceeds
     */
    private BigInteger feeRateBps;

    /**
     * The side of the order, BUY or SELL
     */
    private Integer side;

    /**
     * Signature type used by the Order
     */
    private Integer signatureType;

    /**
     * Convert order to a map representation
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("salt", salt.toString());
        map.put("maker", maker);
        map.put("signer", signer);
        map.put("taker", taker);
        map.put("tokenId", tokenId.toString());
        map.put("makerAmount", makerAmount.toString());
        map.put("takerAmount", takerAmount.toString());
        map.put("expiration", expiration.toString());
        map.put("nonce", nonce.toString());
        map.put("feeRateBps", feeRateBps.toString());
        map.put("side", side);
        map.put("signatureType", signatureType);
        return map;
    }
}
