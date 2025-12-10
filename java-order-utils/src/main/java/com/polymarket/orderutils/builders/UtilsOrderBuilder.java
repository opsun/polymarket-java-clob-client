package com.polymarket.orderutils.builders;

import com.polymarket.orderutils.UtilsSigner;
import com.polymarket.orderutils.model.*;
import com.polymarket.orderutils.utils.Utils;

import java.math.BigInteger;
import java.util.function.Supplier;

/**
 * Builder for creating and signing orders
 */
public class UtilsOrderBuilder extends BaseBuilder {

    /**
     * Create a new OrderBuilder
     *
     * @param exchangeAddress The address of the exchange contract
     * @param chainId         The chain ID
     * @param signer          The signer for signing orders
     */
    public UtilsOrderBuilder(String exchangeAddress, int chainId, UtilsSigner signer) {
        this(exchangeAddress, chainId, signer, Utils::generateSeed);
    }

    /**
     * Create a new OrderBuilder with a custom salt generator
     *
     * @param exchangeAddress The address of the exchange contract
     * @param chainId         The chain ID
     * @param signer          The signer for signing orders
     * @param saltGenerator   Custom salt generator function
     */
    public UtilsOrderBuilder(
        String exchangeAddress,
        int chainId,
        UtilsSigner signer,
        Supplier<Long> saltGenerator
    ) {
        super(exchangeAddress, chainId, signer, saltGenerator);
    }

    /**
     * Build an order from order data
     *
     * @param data The order input data
     * @return The constructed Order
     * @throws ValidationException if the order data is invalid
     */
    public Order buildOrder(OrderData data) {
        if (!validateInputs(data)) {
            throw new ValidationException("Invalid order inputs");
        }

        // Set default signer to maker if not provided
        if (data.getSigner() == null) {
            data.setSigner(data.getMaker());
        }

        // Verify signer matches
        if (!data.getSigner().equalsIgnoreCase(signer.getAddress())) {
            throw new ValidationException("Signer does not match");
        }

        // Set default expiration if not provided
        if (data.getExpiration() == null) {
            data.setExpiration("0");
        }

        // Set default signature type if not provided
        if (data.getSignatureType() == null) {
            data.setSignatureType(SignatureType.EOA);
        }

        return Order.builder()
            //.salt(BigInteger.valueOf(1271679755))
            .salt(BigInteger.valueOf(saltGenerator.get()))
            .maker(Utils.normalizeAddress(data.getMaker()))
            .signer(Utils.normalizeAddress(data.getSigner()))
            .taker(Utils.normalizeAddress(data.getTaker()))
            .tokenId(new BigInteger(data.getTokenId()))
            .makerAmount(new BigInteger(data.getMakerAmount()))
            .takerAmount(new BigInteger(data.getTakerAmount()))
            .expiration(new BigInteger(data.getExpiration()))
            .nonce(new BigInteger(data.getNonce()))
            .feeRateBps(new BigInteger(data.getFeeRateBps()))
            .side(data.getSide())
            .signatureType(data.getSignatureType())
            .build();
    }

    /**
     * Build a signature for an order
     *
     * @param order The order to sign
     * @return The signature as a hex string
     */
    public String buildOrderSignature(Order order) {
        return Utils.prependZx(sign(createStructHash(order)));
    }

    /**
     * Build and sign an order
     *
     * @param data The order input data
     * @return The signed order
     * @throws ValidationException if the order data is invalid
     */
    public SignedOrder buildSignedOrder(OrderData data) {
        Order order = buildOrder(data);
        String signature = buildOrderSignature(order);
        return new SignedOrder(order, signature);
    }

    /**
     * Validate order input data
     *
     * @param data The order data to validate
     * @return true if valid, false otherwise
     */
    private boolean validateInputs(OrderData data) {
        try {
            // Check required fields
            if (data.getMaker() == null) return false;
            if (data.getTokenId() == null) return false;
            if (data.getMakerAmount() == null) return false;
            if (data.getTakerAmount() == null) return false;
            if (data.getSide() == null) return false;

            // Validate side
            if (data.getSide() != 0 && data.getSide() != 1) return false;

            // Validate feeRateBps
            if (data.getFeeRateBps() == null) return false;
            if (!isNumeric(data.getFeeRateBps())) return false;
            if (new BigInteger(data.getFeeRateBps()).compareTo(BigInteger.ZERO) < 0) return false;

            // Validate nonce
            if (data.getNonce() == null) return false;
            if (!isNumeric(data.getNonce())) return false;
            if (new BigInteger(data.getNonce()).compareTo(BigInteger.ZERO) < 0) return false;

            // Validate expiration
            if (data.getExpiration() == null) return false;
            if (!isNumeric(data.getExpiration())) return false;
            if (new BigInteger(data.getExpiration()).compareTo(BigInteger.ZERO) < 0) return false;

            // Validate signature type
            if (data.getSignatureType() == null) return false;
            if (data.getSignatureType() != SignatureType.EOA &&
                data.getSignatureType() != SignatureType.POLY_GNOSIS_SAFE &&
                data.getSignatureType() != SignatureType.POLY_PROXY) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a string is numeric
     */
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            new BigInteger(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
