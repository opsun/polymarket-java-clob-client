package com.polymarket.clob.exceptions;

/**
 * Base exception for Polymarket CLOB client
 */
public class PolyException extends RuntimeException {
    private final String message;

    public PolyException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

