package com.polymarket.clob.types;

/**
 * Order type enumeration
 */
public enum OrderType {
    GTC("GTC"),
    FOK("FOK"),
    GTD("GTD"),
    FAK("FAK");

    private final String value;

    OrderType(String value) {
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

