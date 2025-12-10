package com.polymarket.clob.exceptions;

import java.util.Map;

/**
 * Exception thrown when API requests fail
 */
public class PolyApiException extends PolyException {
    private final Integer statusCode;
    private final Object errorMsg;

    public PolyApiException(Integer statusCode, Object errorMsg) {
        super("PolyApiException[status_code=" + statusCode + ", error_message=" + errorMsg + "]");
        this.statusCode = statusCode;
        this.errorMsg = errorMsg;
    }

    public PolyApiException(String errorMsg) {
        super("PolyApiException[error_message=" + errorMsg + "]");
        this.statusCode = null;
        this.errorMsg = errorMsg;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public Object getErrorMsg() {
        return errorMsg;
    }

    @Override
    public String toString() {
        return "PolyApiException[status_code=" + statusCode + ", error_message=" + errorMsg + "]";
    }
}

