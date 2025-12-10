package com.polymarket.orderutils.model;

import com.polymarket.orderutils.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Input data to generate orders
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderData {

    /**
     * Maker of the order, i.e the source of funds for the order
     */
    private String maker;

    /**
     * Address of the order taker. The zero address is used to indicate a public order
     */
    @Builder.Default
    private String taker = Constants.ZERO_ADDRESS;

    /**
     * Token Id of the CTF ERC1155 asset to be bought or sold.
     * If BUY, this is the tokenId of the asset to be bought, i.e the makerAssetId
     * If SELL, this is the tokenId of the asset to be sold, i.e the takerAssetId
     */
    private String tokenId;

    /**
     * Maker amount, i.e the max amount of tokens to be sold
     */
    private String makerAmount;

    /**
     * Taker amount, i.e the minimum amount of tokens to be received
     */
    private String takerAmount;

    /**
     * The side of the order, BUY or SELL
     */
    private Integer side;

    /**
     * Fee rate, in basis points, charged to the order maker, charged on proceeds
     */
    private String feeRateBps;

    /**
     * Nonce used for onchain cancellations
     */
    @Builder.Default
    private String nonce = "0";

    /**
     * Signer of the order. Optional, if it is not present the signer is the maker of the order.
     */
    private String signer;

    /**
     * Timestamp after which the order is expired.
     * Optional, if it is not present the value is '0' (no expiration)
     */
    @Builder.Default
    private String expiration = "0";

    /**
     * Signature type used by the Order. Default value 'EOA'
     */
    @Builder.Default
    private Integer signatureType = SignatureType.EOA;
}
