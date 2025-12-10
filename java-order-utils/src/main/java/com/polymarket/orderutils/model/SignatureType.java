package com.polymarket.orderutils.model;

/**
 * Signature types for orders
 */
public class SignatureType {

    /**
     * ECDSA EIP712 signatures signed by EOAs
     */
    public static final int EOA = 0;

    /**
     * EIP712 signatures signed by EOAs that own Polymarket Proxy wallets
     */
    public static final int POLY_PROXY = 1;

    /**
     * EIP712 signatures signed by EOAs that own Polymarket Gnosis safes
     */
    public static final int POLY_GNOSIS_SAFE = 2;

    private SignatureType() {
        // Private constructor to prevent instantiation
    }
}
