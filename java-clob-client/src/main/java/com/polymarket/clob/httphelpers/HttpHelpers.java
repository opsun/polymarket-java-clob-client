package com.polymarket.clob.httphelpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polymarket.clob.exceptions.PolyApiException;
import com.polymarket.clob.types.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * HTTP helper functions for API requests
 */
public final class HttpHelpers {
    private HttpHelpers() {}

    private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
            .proxy(ProxySelector.of(InetSocketAddress.createUnresolved("127.0.0.1", 7890)))
        .build();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Object get(String endpoint, Map<String, String> headers) {
        return request(endpoint, "GET", headers, null);
    }

    public static Object post(String endpoint, Map<String, String> headers, Object data) {
        return request(endpoint, "POST", headers, data);
    }

    public static Object delete(String endpoint, Map<String, String> headers, Object data) {
        return request(endpoint, "DELETE", headers, data);
    }

    private static Map<String, String> overloadHeaders(String method, Map<String, String> headers) {
        Map<String, String> result = headers != null ? new java.util.HashMap<>(headers) : new java.util.HashMap<>();
        result.put("User-Agent", "py_clob_client");
        result.put("Accept", "*/*");
        //result.put("Connection", "keep-alive");
        result.put("Content-Type", "application/json");

        if ("GET".equals(method)) {
            result.put("Accept-Encoding", "gzip");
        }

        return result;
    }

    private static Object request(String endpoint, String method, Map<String, String> headers, Object data) {
        try {
            Map<String, String> finalHeaders = overloadHeaders(method, headers);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .method(method, createBodyPublisher(data, finalHeaders));

            finalHeaders.forEach(requestBuilder::header);

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new PolyApiException(response.statusCode(), response.body());
            }

            try {
                String body = response.body();
                if (body == null || body.trim().isEmpty()) {
                    return body;
                }
                // Try to parse as JSON
                return objectMapper.readValue(body, Object.class);
            } catch (Exception e) {
                return response.body();
            }
        } catch (IOException | InterruptedException e) {
            throw new PolyApiException("Request exception: " + e.getMessage());
        }
    }

    private static HttpRequest.BodyPublisher createBodyPublisher(Object data, Map<String, String> headers) {
        if (data == null) {
            return HttpRequest.BodyPublishers.noBody();
        }

        try {
            if (data instanceof String) {
                // Pre-serialized body: send exact bytes
                // headers.put("Content-Length", String.valueOf(((String) data).length()));
                return HttpRequest.BodyPublishers.ofString((String) data, StandardCharsets.UTF_8);
            } else {
                String json = objectMapper.writeValueAsString(data);
                return HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }
    }

    public static String buildQueryParams(String url, String param, String val) {
        String separator = url.contains("?") ? "&" : "?";
        try {
            return url + separator + URLEncoder.encode(param, StandardCharsets.UTF_8) + 
                   "=" + URLEncoder.encode(val, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build query params", e);
        }
    }

    public static String addQueryTradeParams(String baseUrl, TradeParams params, String nextCursor) {
        String url = baseUrl;
        if (params != null) {
            url = url + "?";
            if (params.id() != null) {
                url = buildQueryParams(url, "id", params.id());
            }
            if (params.makerAddress() != null) {
                url = buildQueryParams(url, "maker_address", params.makerAddress());
            }
            if (params.market() != null) {
                url = buildQueryParams(url, "market", params.market());
            }
            if (params.assetId() != null) {
                url = buildQueryParams(url, "asset_id", params.assetId());
            }
            if (params.before() != null) {
                url = buildQueryParams(url, "before", String.valueOf(params.before()));
            }
            if (params.after() != null) {
                url = buildQueryParams(url, "after", String.valueOf(params.after()));
            }
            if (nextCursor != null) {
                url = buildQueryParams(url, "next_cursor", nextCursor);
            }
        }
        return url;
    }

    public static String addQueryOpenOrdersParams(String baseUrl, OpenOrderParams params, String nextCursor) {
        String url = baseUrl;
        if (params != null) {
            url = url + "?";
            if (params.market() != null) {
                url = buildQueryParams(url, "market", params.market());
            }
            if (params.assetId() != null) {
                url = buildQueryParams(url, "asset_id", params.assetId());
            }
            if (params.id() != null) {
                url = buildQueryParams(url, "id", params.id());
            }
            if (nextCursor != null) {
                url = buildQueryParams(url, "next_cursor", nextCursor);
            }
        }
        return url;
    }

    public static String dropNotificationsQueryParams(String baseUrl, DropNotificationParams params) {
        String url = baseUrl;
        if (params != null && params.ids() != null) {
            url = url + "?";
            String ids = String.join(",", params.ids());
            url = buildQueryParams(url, "ids", ids);
        }
        return url;
    }

    public static String addBalanceAllowanceParamsToUrl(String baseUrl, BalanceAllowanceParams params) {
        String url = baseUrl;
        if (params != null) {
            url = url + "?";
            if (params.assetType() != null) {
                url = buildQueryParams(url, "asset_type", params.assetType().toString());
            }
            if (params.tokenId() != null) {
                url = buildQueryParams(url, "token_id", params.tokenId());
            }
            if (params.signatureType() != -1) {
                url = buildQueryParams(url, "signature_type", String.valueOf(params.signatureType()));
            }
        }
        return url;
    }

    public static String addOrderScoringParamsToUrl(String baseUrl, OrderScoringParams params) {
        String url = baseUrl;
        if (params != null && params.orderId() != null) {
            url = url + "?";
            url = buildQueryParams(url, "order_id", params.orderId());
        }
        return url;
    }
}

