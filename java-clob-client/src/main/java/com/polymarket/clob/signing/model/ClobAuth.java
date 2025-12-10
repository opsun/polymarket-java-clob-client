package com.polymarket.clob.signing.model;

import java.util.List;

/**
 * EIP712 struct for CLOB authentication message
 */
public record ClobAuth(
    String address,
    String timestamp,
    String nonce,
    String message
) {
    public static final String DOMAIN_NAME = "ClobAuthDomain";
    public static final String VERSION = "1";
    public static final String MESSAGE_TO_SIGN = "This message attests that I control the given wallet";

    public String getEIP712TypeName() {
        return "ClobAuth";
    }

    public List<EIP712Field> getEIP712Fields() {
        return List.of(
            new EIP712Field("address", "address", address),
            new EIP712Field("timestamp", "string", timestamp),
            new EIP712Field("nonce", "uint256", nonce),
            new EIP712Field("message", "string", message)
        );
    }

    public record EIP712Field(String name, String type, String value) {}
}

