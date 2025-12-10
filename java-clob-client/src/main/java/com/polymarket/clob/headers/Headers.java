package com.polymarket.clob.headers;

import com.polymarket.clob.signer.Signer;
import com.polymarket.clob.signing.eip712.EIP712Signer;
import com.polymarket.clob.signing.hmac.HmacSigner;
import com.polymarket.clob.types.ApiCreds;
import com.polymarket.clob.types.RequestArgs;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Header creation for different authentication levels
 */
public final class Headers {
    private Headers() {}

    private static final String POLY_ADDRESS = "POLY_ADDRESS";
    private static final String POLY_SIGNATURE = "POLY_SIGNATURE";
    private static final String POLY_TIMESTAMP = "POLY_TIMESTAMP";
    private static final String POLY_NONCE = "POLY_NONCE";
    private static final String POLY_API_KEY = "POLY_API_KEY";
    private static final String POLY_PASSPHRASE = "POLY_PASSPHRASE";

    /**
     * Creates Level 1 Poly headers for a request
     */
    public static Map<String, String> createLevel1Headers(Signer signer, Integer nonce) {
        long timestamp = Instant.now().getEpochSecond();
        int n = (nonce != null) ? nonce : 0;

        String signature = EIP712Signer.signClobAuthMessage(signer, timestamp, n);

        Map<String, String> headers = new HashMap<>();
        headers.put(POLY_ADDRESS, signer.address());
        headers.put(POLY_SIGNATURE, signature);
        headers.put(POLY_TIMESTAMP, String.valueOf(timestamp));
        headers.put(POLY_NONCE, String.valueOf(n));

        return headers;
    }

    /**
     * Creates Level 2 Poly headers for a request
     */
    public static Map<String, String> createLevel2Headers(
        Signer signer,
        ApiCreds creds,
        RequestArgs requestArgs
    ) {
        long timestamp = Instant.now().getEpochSecond();

        // Prefer the pre-serialized body string for deterministic signing if available
        String bodyForSig = (requestArgs.serializedBody() != null)
            ? requestArgs.serializedBody()
            : (requestArgs.body() != null ? requestArgs.body().toString() : null);

        String hmacSig = HmacSigner.buildHmacSignature(
            creds.apiSecret(),
            timestamp,
            requestArgs.method(),
            requestArgs.requestPath(),
            bodyForSig
        );

        Map<String, String> headers = new HashMap<>();
        headers.put(POLY_ADDRESS, signer.address());
        headers.put(POLY_SIGNATURE, hmacSig);
        headers.put(POLY_TIMESTAMP, String.valueOf(timestamp));
        headers.put(POLY_API_KEY, creds.apiKey());
        headers.put(POLY_PASSPHRASE, creds.apiPassphrase());

        return headers;
    }

    /**
     * Enriches L2 headers with builder headers
     */
    public static Map<String, String> enrichL2HeadersWithBuilderHeaders(
        Map<String, String> headers,
        Map<String, String> builderHeaders
    ) {
        Map<String, String> enriched = new HashMap<>(headers);
        enriched.putAll(builderHeaders);
        return enriched;
    }
}

