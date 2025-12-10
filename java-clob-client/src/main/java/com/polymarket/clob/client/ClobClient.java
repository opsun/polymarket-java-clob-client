package com.polymarket.clob.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polymarket.clob.config.Config;
import com.polymarket.clob.constants.Constants;
import com.polymarket.clob.constants.Endpoints;
import com.polymarket.clob.exceptions.PolyApiException;
import com.polymarket.clob.exceptions.PolyException;
import com.polymarket.clob.headers.Headers;
import com.polymarket.clob.httphelpers.HttpHelpers;
import com.polymarket.clob.orderbuilder.OrderBuilder;
import com.polymarket.clob.signer.Signer;
import com.polymarket.clob.types.*;
import com.polymarket.clob.utilities.Utilities;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main client for interacting with the Polymarket CLOB API
 */
public class ClobClient {
    private final String host;
    private final Integer chainId;
    private final Signer signer;
    private ApiCreds creds;
    private int mode;
    private OrderBuilder builder;

    // Local cache
    private final Map<String, String> tickSizes = new HashMap<>();
    private final Map<String, Boolean> negRisk = new HashMap<>();
    private final Map<String, Integer> feeRates = new HashMap<>();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Initializes the CLOB client
     * The client can be started in 3 modes:
     * 1) Level 0: Requires only the CLOB host url - Allows access to open CLOB endpoints
     * 2) Level 1: Requires the host, chain_id and a private key - Allows access to L1 authenticated endpoints
     * 3) Level 2: Requires the host, chain_id, a private key, and Credentials - Allows access to all endpoints
     */
    public ClobClient(String host) {
        this(host, null, null, null, null, null);
    }

    public ClobClient(String host, Integer chainId, String key) {
        this(host, chainId, key, null, null, null);
    }

    public ClobClient(String host, Integer chainId, String key, ApiCreds creds, 
                      Integer signatureType, String funder) {
        this.host = host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
        this.chainId = chainId;
        this.signer = (key != null && chainId != null) ? new Signer(key, chainId) : null;
        this.creds = creds;
        this.mode = getClientMode();

        if (this.signer != null) {
            this.builder = new OrderBuilder(this.signer, signatureType, funder);
        }
    }

    private int getClientMode() {
        if (signer != null && creds != null) {
            return Constants.L2;
        }
        if (signer != null) {
            return Constants.L1;
        }
        return Constants.L0;
    }

    public String getAddress() {
        return signer != null ? signer.address() : null;
    }

    public String getCollateralAddress() {
        if (chainId == null) return null;
        var contractConfig = Config.getContractConfig(chainId, false);
        return contractConfig != null ? contractConfig.collateral() : null;
    }

    public String getConditionalAddress() {
        if (chainId == null) return null;
        var contractConfig = Config.getContractConfig(chainId, false);
        return contractConfig != null ? contractConfig.conditionalTokens() : null;
    }

    public String getExchangeAddress(boolean negRisk) {
        if (chainId == null) return null;
        var contractConfig = Config.getContractConfig(chainId, negRisk);
        return contractConfig != null ? contractConfig.exchange() : null;
    }

    public Object getOk() {
        return HttpHelpers.get(host + "/", null);
    }

    public Object getServerTime() {
        return HttpHelpers.get(host + Endpoints.TIME, null);
    }

    public ApiCreds createApiKey(Integer nonce) {
        assertLevel1Auth();

        String endpoint = host + Endpoints.CREATE_API_KEY;
        Map<String, String> headers = Headers.createLevel1Headers(signer, nonce);

        Object response = HttpHelpers.post(endpoint, headers, null);
        Map<String, Object> credsRaw = parseMapResponse(response, "Failed to parse created CLOB creds");

        try {
            return new ApiCreds(
                (String) credsRaw.get("apiKey"),
                (String) credsRaw.get("secret"),
                (String) credsRaw.get("passphrase")
            );
        } catch (Exception e) {
            throw new RuntimeException("Couldn't parse created CLOB creds", e);
        }
    }

    public ApiCreds deriveApiKey(Integer nonce) {
        assertLevel1Auth();

        String endpoint = host + Endpoints.DERIVE_API_KEY;
        Map<String, String> headers = Headers.createLevel1Headers(signer, nonce);

        Object response = HttpHelpers.get(endpoint, headers);
        Map<String, Object> credsRaw = parseMapResponse(response, "Failed to parse derived CLOB creds");

        try {
            return new ApiCreds(
                (String) credsRaw.get("apiKey"),
                (String) credsRaw.get("secret"),
                (String) credsRaw.get("passphrase")
            );
        } catch (Exception e) {
            throw new RuntimeException("Couldn't parse derived CLOB creds", e);
        }
    }

    public ApiCreds createOrDeriveApiCreds(Integer nonce) {
        try {
            return createApiKey(nonce);
        } catch (Exception e) {
            return deriveApiKey(nonce);
        }
    }

    public void setApiCreds(ApiCreds creds) {
        this.creds = creds;
        this.mode = getClientMode();
    }

    public Object getApiKeys() {
        assertLevel2Auth();

        RequestArgs requestArgs = new RequestArgs("GET", Endpoints.GET_API_KEYS);
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        return HttpHelpers.get(host + Endpoints.GET_API_KEYS, headers);
    }

    public Object getClosedOnlyMode() {
        assertLevel2Auth();

        RequestArgs requestArgs = new RequestArgs("GET", Endpoints.CLOSED_ONLY);
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        return HttpHelpers.get(host + Endpoints.CLOSED_ONLY, headers);
    }

    public Object deleteApiKey() {
        assertLevel2Auth();

        RequestArgs requestArgs = new RequestArgs("DELETE", Endpoints.DELETE_API_KEY);
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        return HttpHelpers.delete(host + Endpoints.DELETE_API_KEY, headers, null);
    }

    public ReadonlyApiKeyResponse createReadonlyApiKey() {
        assertLevel2Auth();

        RequestArgs requestArgs = new RequestArgs("POST", Endpoints.CREATE_READONLY_API_KEY);
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) HttpHelpers.post(
            host + Endpoints.CREATE_READONLY_API_KEY, headers, null
        );
        
        try {
            return new ReadonlyApiKeyResponse((String) response.get("apiKey"));
        } catch (Exception e) {
            throw new RuntimeException("Couldn't parse readonly API key response", e);
        }
    }

    public Object getReadonlyApiKeys() {
        assertLevel2Auth();

        RequestArgs requestArgs = new RequestArgs("GET", Endpoints.GET_READONLY_API_KEYS);
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        return HttpHelpers.get(host + Endpoints.GET_READONLY_API_KEYS, headers);
    }

    public boolean deleteReadonlyApiKey(String key) {
        assertLevel2Auth();

        Map<String, Object> body = Map.of("key", key);
        String serialized;
        try {
            serialized = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize body", e);
        }

        RequestArgs requestArgs = new RequestArgs(
            "DELETE",
            Endpoints.DELETE_READONLY_API_KEY,
            body,
            serialized
        );
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        
        HttpHelpers.delete(host + Endpoints.DELETE_READONLY_API_KEY, headers, serialized);
        return true;
    }

    public Object validateReadonlyApiKey(String address, String key) {
        return HttpHelpers.get(
            host + Endpoints.VALIDATE_READONLY_API_KEY + "?address=" + address + "&key=" + key,
            null
        );
    }

    public Object getMidpoint(String tokenId) {
        return HttpHelpers.get(host + Endpoints.MID_POINT + "?token_id=" + tokenId, null);
    }

    public Object getMidpoints(List<BookParams> params) {
        List<Map<String, String>> body = params.stream()
            .map(p -> Map.of("token_id", p.tokenId()))
            .collect(Collectors.toList());
        return HttpHelpers.post(host + Endpoints.MID_POINTS, null, body);
    }

    public Object getPrice(String tokenId, String side) {
        return HttpHelpers.get(host + Endpoints.PRICE + "?token_id=" + tokenId + "&side=" + side, null);
    }

    public Object getPrices(List<BookParams> params) {
        List<Map<String, String>> body = params.stream()
            .map(p -> {
                Map<String, String> map = new HashMap<>();
                map.put("token_id", p.tokenId());
                map.put("side", p.side());
                return map;
            })
            .collect(Collectors.toList());
        return HttpHelpers.post(host + Endpoints.GET_PRICES, null, body);
    }

    public Object getSpread(String tokenId) {
        return HttpHelpers.get(host + Endpoints.GET_SPREAD + "?token_id=" + tokenId, null);
    }

    public Object getSpreads(List<BookParams> params) {
        List<Map<String, String>> body = params.stream()
            .map(p -> Map.of("token_id", p.tokenId()))
            .collect(Collectors.toList());
        return HttpHelpers.post(host + Endpoints.GET_SPREADS, null, body);
    }

    public String getTickSize(String tokenId) {
        if (tickSizes.containsKey(tokenId)) {
            return tickSizes.get(tokenId);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) HttpHelpers.get(
            host + Endpoints.GET_TICK_SIZE + "?token_id=" + tokenId, null
        );
        String tickSize = String.valueOf(result.get("minimum_tick_size"));
        tickSizes.put(tokenId, tickSize);
        return tickSize;
    }

    public boolean getNegRisk(String tokenId) {
        if (negRisk.containsKey(tokenId)) {
            return negRisk.get(tokenId);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) HttpHelpers.get(
            host + Endpoints.GET_NEG_RISK + "?token_id=" + tokenId, null
        );
        Boolean resultValue = (Boolean) result.get("neg_risk");
        negRisk.put(tokenId, resultValue);
        return resultValue;
    }

    public int getFeeRateBps(String tokenId) {
        if (feeRates.containsKey(tokenId)) {
            return feeRates.get(tokenId);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) HttpHelpers.get(
            host + Endpoints.GET_FEE_RATE + "?token_id=" + tokenId, null
        );
        Integer feeRate = result.get("base_fee") != null ? 
            ((Number) result.get("base_fee")).intValue() : 0;
        feeRates.put(tokenId, feeRate);
        return feeRate;
    }

    private String resolveTickSize(String tokenId, String tickSize) {
        String minTickSize = getTickSize(tokenId);
        if (tickSize != null) {
            if (Utilities.isTickSizeSmaller(tickSize, minTickSize)) {
                throw new IllegalArgumentException(
                    "invalid tick size (" + tickSize + "), minimum for the market is " + minTickSize
                );
            }
        } else {
            tickSize = minTickSize;
        }
        return tickSize;
    }

    private int resolveFeeRate(String tokenId, Integer userFeeRate) {
        int marketFeeRateBps = getFeeRateBps(tokenId);
        if (marketFeeRateBps > 0 && userFeeRate != null && userFeeRate > 0 && 
            !userFeeRate.equals(marketFeeRateBps)) {
            throw new IllegalArgumentException(
                "invalid user provided fee rate: (" + userFeeRate + 
                "), fee rate for the market must be " + marketFeeRateBps
            );
        }
        return marketFeeRateBps;
    }

    public Map<String, Object> createOrder(OrderArgs orderArgs, PartialCreateOrderOptions options) {
        assertLevel1Auth();

        String tickSize = resolveTickSize(
            orderArgs.tokenId(),
            options != null ? options.tickSize() : null
        );

        if (!Utilities.priceValid(orderArgs.price(), tickSize)) {
            throw new IllegalArgumentException(
                "price (" + orderArgs.price() + "), min: " + tickSize + 
                " - max: " + (1 - Double.parseDouble(tickSize))
            );
        }

        boolean negRiskValue = (options != null && options.negRisk() != null) ?
            options.negRisk() : getNegRisk(orderArgs.tokenId());

        int feeRateBps = resolveFeeRate(orderArgs.tokenId(), orderArgs.feeRateBps());
        OrderArgs updatedOrderArgs = new OrderArgs(
            orderArgs.tokenId(),
            orderArgs.price(),
            orderArgs.size(),
            orderArgs.side(),
            feeRateBps,
            orderArgs.nonce(),
            orderArgs.expiration(),
            orderArgs.taker()
        );

        CreateOrderOptions createOptions = new CreateOrderOptions(tickSize, negRiskValue);
        return builder.createOrder(updatedOrderArgs, createOptions);
    }

    public Map<String, Object> createMarketOrder(
        MarketOrderArgs orderArgs,
        PartialCreateOrderOptions options
    ) {
        assertLevel1Auth();

        String tickSize = resolveTickSize(
            orderArgs.tokenId(),
            options != null ? options.tickSize() : null
        );

        double price = orderArgs.price();
        if (price <= 0) {
            price = calculateMarketPrice(
                orderArgs.tokenId(),
                orderArgs.side(),
                orderArgs.amount(),
                orderArgs.orderType()
            );
        }

        if (!Utilities.priceValid(price, tickSize)) {
            throw new IllegalArgumentException(
                "price (" + price + "), min: " + tickSize + 
                " - max: " + (1 - Double.parseDouble(tickSize))
            );
        }

        boolean negRiskValue = (options != null && options.negRisk() != null) ?
            options.negRisk() : getNegRisk(orderArgs.tokenId());

        int feeRateBps = resolveFeeRate(orderArgs.tokenId(), orderArgs.feeRateBps());
        MarketOrderArgs updatedOrderArgs = new MarketOrderArgs(
            orderArgs.tokenId(),
            orderArgs.amount(),
            orderArgs.side(),
            price,
            feeRateBps,
            orderArgs.nonce(),
            orderArgs.taker(),
            orderArgs.orderType()
        );

        CreateOrderOptions createOptions = new CreateOrderOptions(tickSize, negRiskValue);
        return builder.createMarketOrder(updatedOrderArgs, createOptions);
    }

    // Continued in next part due to length...
    
    private void assertLevel1Auth() {
        if (mode < Constants.L1) {
            throw new PolyException(Constants.L1_AUTH_UNAVAILABLE);
        }
    }

    private void assertLevel2Auth() {
        if (mode < Constants.L2) {
            throw new PolyException(Constants.L2_AUTH_UNAVAILABLE);
        }
    }

    public double calculateMarketPrice(String tokenId, String side, double amount, OrderType orderType) {
        OrderBookSummary book = getOrderBook(tokenId);
        if (book == null) {
            throw new RuntimeException("no orderbook");
        }
        if ("BUY".equals(side)) {
            if (book.asks() == null || book.asks().isEmpty()) {
                throw new RuntimeException("no match");
            }
            return builder.calculateBuyMarketPrice(book.asks(), amount, orderType);
        } else {
            if (book.bids() == null || book.bids().isEmpty()) {
                throw new RuntimeException("no match");
            }
            return builder.calculateSellMarketPrice(book.bids(), amount, orderType);
        }
    }

    public OrderBookSummary getOrderBook(String tokenId) {
        Object response = HttpHelpers.get(
            host + Endpoints.GET_ORDER_BOOK + "?token_id=" + tokenId, null
        );

        // Handle the response based on its actual type
        if (response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rawObs = (Map<String, Object>) response;
            return Utilities.parseRawOrderbookSummary(rawObs);
        } else if (response instanceof String) {
            // If the response is a String, try to parse it as JSON
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> rawObs = objectMapper.readValue((String) response, Map.class);
                return Utilities.parseRawOrderbookSummary(rawObs);
            } catch (Exception e) {
                throw new PolyApiException("Failed to parse order book response: " + e.getMessage());
            }
        } else {
            throw new PolyApiException("Unexpected response type: " +
                (response != null ? response.getClass().getName() : "null"));
        }
    }

    public List<OrderBookSummary> getOrderBooks(List<BookParams> params) {
        List<Map<String, String>> body = params.stream()
            .map(p -> Map.of("token_id", p.tokenId()))
            .collect(Collectors.toList());

        Object response = HttpHelpers.post(host + Endpoints.GET_ORDER_BOOKS, null, body);

        // Handle the response based on its actual type
        if (response instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawObsList = (List<Map<String, Object>>) response;
            return rawObsList.stream()
                .map(Utilities::parseRawOrderbookSummary)
                .collect(Collectors.toList());
        } else if (response instanceof String) {
            // If the response is a String, try to parse it as JSON
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> rawObsList = objectMapper.readValue(
                    (String) response,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
                );
                return rawObsList.stream()
                    .map(Utilities::parseRawOrderbookSummary)
                    .collect(Collectors.toList());
            } catch (Exception e) {
                throw new PolyApiException("Failed to parse order books response: " + e.getMessage());
            }
        } else {
            throw new PolyApiException("Unexpected response type: " +
                (response != null ? response.getClass().getName() : "null"));
        }
    }

    public Object postOrder(Map<String, Object> order, OrderType orderType) {
        assertLevel2Auth();
        
        Map<String, Object> body = Utilities.orderToJson(order, creds.apiKey(), orderType);
        String serialized;
        try {
            serialized = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize order", e);
        }

        RequestArgs requestArgs = new RequestArgs(
            "POST",
            Endpoints.POST_ORDER,
            body,
            serialized
        );
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        return HttpHelpers.post(host + Endpoints.POST_ORDER, headers, serialized);
    }

    public Object postOrders(List<PostOrdersArgs> args) {
        assertLevel2Auth();
        
        List<Map<String, Object>> body = args.stream()
            .map(arg -> Utilities.orderToJson(arg.order(), creds.apiKey(), arg.orderType()))
            .collect(Collectors.toList());
        
        String serialized;
        try {
            serialized = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize orders", e);
        }

        RequestArgs requestArgs = new RequestArgs(
            "POST",
            Endpoints.POST_ORDERS,
            body,
            serialized
        );
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        return HttpHelpers.post(host + Endpoints.POST_ORDERS, headers, serialized);
    }

    public Object cancel(String orderId) {
        assertLevel2Auth();
        
        Map<String, Object> body = Map.of("orderID", orderId);
        String serialized;
        try {
            serialized = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize body", e);
        }

        RequestArgs requestArgs = new RequestArgs(
            "DELETE",
            Endpoints.CANCEL,
            body,
            serialized
        );
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        return HttpHelpers.delete(host + Endpoints.CANCEL, headers, serialized);
    }

    public Object cancelOrders(List<String> orderIds) {
        assertLevel2Auth();
        
        String serialized;
        try {
            serialized = objectMapper.writeValueAsString(orderIds);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize orderIds", e);
        }

        RequestArgs requestArgs = new RequestArgs(
            "DELETE",
            Endpoints.CANCEL_ORDERS,
            orderIds,
            serialized
        );
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        return HttpHelpers.delete(host + Endpoints.CANCEL_ORDERS, headers, serialized);
    }

    public Object cancelAll() {
        assertLevel2Auth();
        
        RequestArgs requestArgs = new RequestArgs("DELETE", Endpoints.CANCEL_ALL);
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        return HttpHelpers.delete(host + Endpoints.CANCEL_ALL, headers, null);
    }

    public Object cancelMarketOrders(String market, String assetId) {
        assertLevel2Auth();
        
        Map<String, Object> body = new HashMap<>();
        if (market != null) body.put("market", market);
        if (assetId != null) body.put("asset_id", assetId);
        
        String serialized;
        try {
            serialized = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize body", e);
        }

        RequestArgs requestArgs = new RequestArgs(
            "DELETE",
            Endpoints.CANCEL_MARKET_ORDERS,
            body,
            serialized
        );
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        return HttpHelpers.delete(host + Endpoints.CANCEL_MARKET_ORDERS, headers, serialized);
    }

    public List<Object> getOrders(OpenOrderParams params, String nextCursor) {
        assertLevel2Auth();
        
        RequestArgs requestArgs = new RequestArgs("GET", Endpoints.ORDERS);
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);

        List<Object> results = new ArrayList<>();
        String cursor = nextCursor != null ? nextCursor : "MA==";
        
        while (!Constants.END_CURSOR.equals(cursor)) {
            String url = HttpHelpers.addQueryOpenOrdersParams(
                host + Endpoints.ORDERS, params, cursor
            );

            Object rawResponse = HttpHelpers.get(url, headers);
            Map<String, Object> response = parseMapResponse(rawResponse, "Failed to parse open orders response");
            cursor = (String) response.get("next_cursor");

            @SuppressWarnings("unchecked")
            List<Object> data = (List<Object>) response.get("data");
            if (data != null) {
                results.addAll(data);
            }
        }
        
        return results;
    }

    public Object getOrder(String orderId) {
        assertLevel2Auth();
        
        String endpoint = Endpoints.GET_ORDER + orderId;
        RequestArgs requestArgs = new RequestArgs("GET", endpoint);
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        return HttpHelpers.get(host + endpoint, headers);
    }

    public List<Object> getTrades(TradeParams params, String nextCursor) {
        assertLevel2Auth();
        
        RequestArgs requestArgs = new RequestArgs("GET", Endpoints.TRADES);
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);

        List<Object> results = new ArrayList<>();
        String cursor = nextCursor != null ? nextCursor : "MA==";
        
        while (!Constants.END_CURSOR.equals(cursor)) {
            String url = HttpHelpers.addQueryTradeParams(
                host + Endpoints.TRADES, params, cursor
            );

            Object rawResponse = HttpHelpers.get(url, headers);
            Map<String, Object> response = parseMapResponse(rawResponse, "Failed to parse trades response");
            cursor = (String) response.get("next_cursor");

            @SuppressWarnings("unchecked")
            List<Object> data = (List<Object>) response.get("data");
            if (data != null) {
                results.addAll(data);
            }
        }
        
        return results;
    }

    public Object getLastTradePrice(String tokenId) {
        return HttpHelpers.get(
            host + Endpoints.GET_LAST_TRADE_PRICE + "?token_id=" + tokenId, null
        );
    }

    public Object getLastTradesPrices(List<BookParams> params) {
        List<Map<String, String>> body = params.stream()
            .map(p -> Map.of("token_id", p.tokenId()))
            .collect(Collectors.toList());
        return HttpHelpers.post(host + Endpoints.GET_LAST_TRADES_PRICES, null, body);
    }

    public Object getNotifications() {
        assertLevel2Auth();
        
        RequestArgs requestArgs = new RequestArgs("GET", Endpoints.GET_NOTIFICATIONS);
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        String url = host + Endpoints.GET_NOTIFICATIONS + "?signature_type=" + builder.getSigType();
        return HttpHelpers.get(url, headers);
    }

    public Object dropNotifications(DropNotificationParams params) {
        assertLevel2Auth();
        
        RequestArgs requestArgs = new RequestArgs("DELETE", Endpoints.DROP_NOTIFICATIONS);
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        String url = HttpHelpers.dropNotificationsQueryParams(
            host + Endpoints.DROP_NOTIFICATIONS, params
        );
        return HttpHelpers.delete(url, headers, null);
    }

    public Object getBalanceAllowance(BalanceAllowanceParams params) {
        assertLevel2Auth();
        
        RequestArgs requestArgs = new RequestArgs("GET", Endpoints.GET_BALANCE_ALLOWANCE);
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        
        BalanceAllowanceParams finalParams = params;
        if (finalParams.signatureType() == -1) {
            finalParams = new BalanceAllowanceParams(
                finalParams.assetType(),
                finalParams.tokenId(),
                builder.getSigType()
            );
        }
        
        String url = HttpHelpers.addBalanceAllowanceParamsToUrl(
            host + Endpoints.GET_BALANCE_ALLOWANCE, finalParams
        );
        return HttpHelpers.get(url, headers);
    }

    public Object updateBalanceAllowance(BalanceAllowanceParams params) {
        assertLevel2Auth();
        
        RequestArgs requestArgs = new RequestArgs("GET", Endpoints.UPDATE_BALANCE_ALLOWANCE);
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        
        BalanceAllowanceParams finalParams = params;
        if (finalParams.signatureType() == -1) {
            finalParams = new BalanceAllowanceParams(
                finalParams.assetType(),
                finalParams.tokenId(),
                builder.getSigType()
            );
        }
        
        String url = HttpHelpers.addBalanceAllowanceParamsToUrl(
            host + Endpoints.UPDATE_BALANCE_ALLOWANCE, finalParams
        );
        return HttpHelpers.get(url, headers);
    }

    public Object isOrderScoring(OrderScoringParams params) {
        assertLevel2Auth();
        
        RequestArgs requestArgs = new RequestArgs("GET", Endpoints.IS_ORDER_SCORING);
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        String url = HttpHelpers.addOrderScoringParamsToUrl(
            host + Endpoints.IS_ORDER_SCORING, params
        );
        return HttpHelpers.get(url, headers);
    }

    public Object areOrdersScoring(OrdersScoringParams params) {
        assertLevel2Auth();
        
        String serialized;
        try {
            serialized = objectMapper.writeValueAsString(params.orderIds());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize orderIds", e);
        }

        RequestArgs requestArgs = new RequestArgs(
            "POST",
            Endpoints.ARE_ORDERS_SCORING,
            params.orderIds(),
            serialized
        );
        Map<String, String> headers = Headers.createLevel2Headers(signer, creds, requestArgs);
        return HttpHelpers.post(host + Endpoints.ARE_ORDERS_SCORING, headers, serialized);
    }

    public Object getSamplingMarkets(String nextCursor) {
        String cursor = nextCursor != null ? nextCursor : "MA==";
        return HttpHelpers.get(
            host + Endpoints.GET_SAMPLING_MARKETS + "?next_cursor=" + cursor, null
        );
    }

    public Object getSamplingSimplifiedMarkets(String nextCursor) {
        String cursor = nextCursor != null ? nextCursor : "MA==";
        return HttpHelpers.get(
            host + Endpoints.GET_SAMPLING_SIMPLIFIED_MARKETS + "?next_cursor=" + cursor, null
        );
    }

    public Object getMarkets(String nextCursor) {
        String cursor = nextCursor != null ? nextCursor : "MA==";
        return HttpHelpers.get(host + Endpoints.GET_MARKETS + "?next_cursor=" + cursor, null);
    }

    public Object getSimplifiedMarkets(String nextCursor) {
        String cursor = nextCursor != null ? nextCursor : "MA==";
        return HttpHelpers.get(
            host + Endpoints.GET_SIMPLIFIED_MARKETS + "?next_cursor=" + cursor, null
        );
    }

    public Object getMarket(String conditionId) {
        return HttpHelpers.get(host + Endpoints.GET_MARKET + conditionId, null);
    }

    public Object getMarketTradesEvents(String conditionId) {
        return HttpHelpers.get(host + Endpoints.GET_MARKET_TRADES_EVENTS + conditionId, null);
    }

    // Helper method to parse Map response from HTTP requests
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMapResponse(Object response, String errorMessage) {
        if (response instanceof Map) {
            return (Map<String, Object>) response;
        } else if (response instanceof String) {
            try {
                return objectMapper.readValue((String) response, Map.class);
            } catch (Exception e) {
                throw new PolyApiException(errorMessage + ": " + e.getMessage());
            }
        } else {
            throw new PolyApiException("Unexpected response type: " +
                (response != null ? response.getClass().getName() : "null"));
        }
    }

    public String getOrderBookHash(OrderBookSummary orderbook) {
        return Utilities.generateOrderbookSummaryHash(orderbook);
    }
}

