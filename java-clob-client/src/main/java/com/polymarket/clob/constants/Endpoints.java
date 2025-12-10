package com.polymarket.clob.constants;

/**
 * API endpoint paths
 */
public final class Endpoints {
    private Endpoints() {}

    public static final String TIME = "/time";
    public static final String CREATE_API_KEY = "/auth/api-key";
    public static final String GET_API_KEYS = "/auth/api-keys";
    public static final String DELETE_API_KEY = "/auth/api-key";
    public static final String DERIVE_API_KEY = "/auth/derive-api-key";
    public static final String CLOSED_ONLY = "/auth/ban-status/closed-only";

    // Readonly API Key endpoints
    public static final String CREATE_READONLY_API_KEY = "/auth/readonly-api-key";
    public static final String GET_READONLY_API_KEYS = "/auth/readonly-api-keys";
    public static final String DELETE_READONLY_API_KEY = "/auth/readonly-api-key";
    public static final String VALIDATE_READONLY_API_KEY = "/auth/validate-readonly-api-key";

    public static final String TRADES = "/data/trades";
    public static final String GET_ORDER_BOOK = "/book";
    public static final String GET_ORDER_BOOKS = "/books";
    public static final String GET_ORDER = "/data/order/";
    public static final String ORDERS = "/data/orders";
    public static final String POST_ORDER = "/order";
    public static final String POST_ORDERS = "/orders";
    public static final String CANCEL = "/order";
    public static final String CANCEL_ORDERS = "/orders";
    public static final String CANCEL_ALL = "/cancel-all";
    public static final String CANCEL_MARKET_ORDERS = "/cancel-market-orders";
    public static final String MID_POINT = "/midpoint";
    public static final String MID_POINTS = "/midpoints";
    public static final String PRICE = "/price";
    public static final String GET_PRICES = "/prices";
    public static final String GET_SPREAD = "/spread";
    public static final String GET_SPREADS = "/spreads";
    public static final String GET_LAST_TRADE_PRICE = "/last-trade-price";
    public static final String GET_LAST_TRADES_PRICES = "/last-trades-prices";
    public static final String GET_NOTIFICATIONS = "/notifications";
    public static final String DROP_NOTIFICATIONS = "/notifications";
    public static final String GET_BALANCE_ALLOWANCE = "/balance-allowance";
    public static final String UPDATE_BALANCE_ALLOWANCE = "/balance-allowance/update";
    public static final String IS_ORDER_SCORING = "/order-scoring";
    public static final String ARE_ORDERS_SCORING = "/orders-scoring";
    public static final String GET_TICK_SIZE = "/tick-size";
    public static final String GET_NEG_RISK = "/neg-risk";
    public static final String GET_FEE_RATE = "/fee-rate";
    public static final String GET_SAMPLING_SIMPLIFIED_MARKETS = "/sampling-simplified-markets";
    public static final String GET_SAMPLING_MARKETS = "/sampling-markets";
    public static final String GET_SIMPLIFIED_MARKETS = "/simplified-markets";
    public static final String GET_MARKETS = "/markets";
    public static final String GET_MARKET = "/markets/";
    public static final String GET_MARKET_TRADES_EVENTS = "/live-activity/events/";

    public static final String GET_BUILDER_TRADES = "/builder/trades";
}

