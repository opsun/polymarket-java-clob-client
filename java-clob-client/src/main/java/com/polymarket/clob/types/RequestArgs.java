package com.polymarket.clob.types;

/**
 * Request arguments for signing
 */
public record RequestArgs(
    String method,
    String requestPath,
    Object body,
    String serializedBody
) {
    public RequestArgs(String method, String requestPath) {
        this(method, requestPath, null, null);
    }

    public RequestArgs(String method, String requestPath, Object body, String serializedBody) {
        this.method = method;
        this.requestPath = requestPath;
        this.body = body;
        this.serializedBody = serializedBody;
    }
}

