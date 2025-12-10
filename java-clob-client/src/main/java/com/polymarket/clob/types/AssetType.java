package com.polymarket.clob.types;

/**
 * Asset type enumeration
 */
public enum AssetType {
    COLLATERAL("COLLATERAL"),
    CONDITIONAL("CONDITIONAL");

    private final String value;

    AssetType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}

