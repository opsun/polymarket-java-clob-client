package com.polymarket.clob.types;

/**
 * API credentials for Level 2 authentication
 */
public record ApiCreds(
    String apiKey,
    String apiSecret,
    String apiPassphrase
) {}

