package com.polymarket.clob.orderbuilder;

import com.polymarket.clob.config.Config;
import com.polymarket.clob.orderbuilder.constants.OrderSide;
import com.polymarket.clob.orderbuilder.helpers.OrderBuilderHelpers;
import com.polymarket.clob.signer.Signer;
import com.polymarket.clob.types.*;
import com.polymarket.orderutils.UtilsSigner;
import com.polymarket.orderutils.builders.UtilsOrderBuilder;
import com.polymarket.orderutils.model.OrderData;
import com.polymarket.orderutils.model.SignedOrder;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for creating and signing orders
 */
public class OrderBuilder {
    private static final Map<String, RoundConfig> ROUNDING_CONFIG = Map.of(
        TickSize.TICK_0_1, new RoundConfig(1, 2, 3),
        TickSize.TICK_0_01, new RoundConfig(2, 2, 4),
        TickSize.TICK_0_001, new RoundConfig(3, 2, 5),
        TickSize.TICK_0_0001, new RoundConfig(4, 2, 6)
    );

    private final Signer signer;
    private final int sigType;
    private final String funder;

    public OrderBuilder(Signer signer, Integer sigType, String funder) {
        this.signer = signer;
        // Default to EOA (0) if not specified
        this.sigType = (sigType != null) ? sigType : 0;
        // Default to signer address if funder not specified
        this.funder = (funder != null) ? funder : signer.address();
    }

    public OrderResult getOrderAmounts(String side, double size, double price, RoundConfig roundConfig) {
        double rawPrice = OrderBuilderHelpers.roundNormal(price, roundConfig.price());

        if (OrderSide.BUY.equals(side)) {
            double rawTakerAmt = OrderBuilderHelpers.roundDown(size, roundConfig.size());
            double rawMakerAmt = rawTakerAmt * rawPrice;
            
            if (OrderBuilderHelpers.decimalPlaces(rawMakerAmt) > roundConfig.amount()) {
                rawMakerAmt = OrderBuilderHelpers.roundUp(rawMakerAmt, roundConfig.amount() + 4);
                if (OrderBuilderHelpers.decimalPlaces(rawMakerAmt) > roundConfig.amount()) {
                    rawMakerAmt = OrderBuilderHelpers.roundDown(rawMakerAmt, roundConfig.amount());
                }
            }

            long makerAmount = OrderBuilderHelpers.toTokenDecimals(rawMakerAmt);
            long takerAmount = OrderBuilderHelpers.toTokenDecimals(rawTakerAmt);

            return new OrderResult(OrderSide.BUY, makerAmount, takerAmount);
        } else if (OrderSide.SELL.equals(side)) {
            double rawMakerAmt = OrderBuilderHelpers.roundDown(size, roundConfig.size());
            double rawTakerAmt = rawMakerAmt * rawPrice;
            
            if (OrderBuilderHelpers.decimalPlaces(rawTakerAmt) > roundConfig.amount()) {
                rawTakerAmt = OrderBuilderHelpers.roundUp(rawTakerAmt, roundConfig.amount() + 4);
                if (OrderBuilderHelpers.decimalPlaces(rawTakerAmt) > roundConfig.amount()) {
                    rawTakerAmt = OrderBuilderHelpers.roundDown(rawTakerAmt, roundConfig.amount());
                }
            }

            long makerAmount = OrderBuilderHelpers.toTokenDecimals(rawMakerAmt);
            long takerAmount = OrderBuilderHelpers.toTokenDecimals(rawTakerAmt);

            return new OrderResult(OrderSide.SELL, makerAmount, takerAmount);
        } else {
            throw new IllegalArgumentException("order_args.side must be '" + OrderSide.BUY + "' or '" + OrderSide.SELL + "'");
        }
    }

    public OrderResult getMarketOrderAmounts(String side, double amount, double price, RoundConfig roundConfig) {
        double rawPrice = OrderBuilderHelpers.roundNormal(price, roundConfig.price());

        if (OrderSide.BUY.equals(side)) {
            double rawMakerAmt = OrderBuilderHelpers.roundDown(amount, roundConfig.size());
            double rawTakerAmt = rawMakerAmt / rawPrice;
            
            if (OrderBuilderHelpers.decimalPlaces(rawTakerAmt) > roundConfig.amount()) {
                rawTakerAmt = OrderBuilderHelpers.roundUp(rawTakerAmt, roundConfig.amount() + 4);
                if (OrderBuilderHelpers.decimalPlaces(rawTakerAmt) > roundConfig.amount()) {
                    rawTakerAmt = OrderBuilderHelpers.roundDown(rawTakerAmt, roundConfig.amount());
                }
            }

            long makerAmount = OrderBuilderHelpers.toTokenDecimals(rawMakerAmt);
            long takerAmount = OrderBuilderHelpers.toTokenDecimals(rawTakerAmt);

            return new OrderResult(OrderSide.BUY, makerAmount, takerAmount);
        } else if (OrderSide.SELL.equals(side)) {
            double rawMakerAmt = OrderBuilderHelpers.roundDown(amount, roundConfig.size());
            double rawTakerAmt = rawMakerAmt * rawPrice;
            
            if (OrderBuilderHelpers.decimalPlaces(rawTakerAmt) > roundConfig.amount()) {
                rawTakerAmt = OrderBuilderHelpers.roundUp(rawTakerAmt, roundConfig.amount() + 4);
                if (OrderBuilderHelpers.decimalPlaces(rawTakerAmt) > roundConfig.amount()) {
                    rawTakerAmt = OrderBuilderHelpers.roundDown(rawTakerAmt, roundConfig.amount());
                }
            }

            long makerAmount = OrderBuilderHelpers.toTokenDecimals(rawMakerAmt);
            long takerAmount = OrderBuilderHelpers.toTokenDecimals(rawTakerAmt);

            return new OrderResult(OrderSide.SELL, makerAmount, takerAmount);
        } else {
            throw new IllegalArgumentException("order_args.side must be '" + OrderSide.BUY + "' or '" + OrderSide.SELL + "'");
        }
    }

    public Map<String, Object> createOrder(OrderArgs orderArgs, CreateOrderOptions options) {
        RoundConfig roundConfig = ROUNDING_CONFIG.get(options.tickSize());
        OrderResult result = getOrderAmounts(orderArgs.side(), orderArgs.size(), orderArgs.price(), roundConfig);

        ContractConfig contractConfig = Config.getContractConfig(signer.getChainId(), options.negRisk());

        // Create UtilsSigner and UtilsOrderBuilder
        UtilsSigner utilsSigner = new UtilsSigner(signer.getPrivateKey());
        UtilsOrderBuilder orderBuilder = new UtilsOrderBuilder(
                contractConfig.exchange(),
                signer.getChainId(),
                utilsSigner
        );

        // Convert side string to integer (0 = BUY, 1 = SELL)
        int sideValue = OrderSide.BUY.equals(result.side()) ? 0 : 1;

        // Build order data
        OrderData orderData = OrderData.builder()
                .maker(funder)
                .taker(orderArgs.taker())
                .tokenId(orderArgs.tokenId())
                .makerAmount(String.valueOf(result.makerAmount()))
                .takerAmount(String.valueOf(result.takerAmount()))
                .side(sideValue)
                .feeRateBps(String.valueOf(orderArgs.feeRateBps()))
                .nonce(String.valueOf(orderArgs.nonce()))
                .signer(signer.address())
                .expiration(String.valueOf(orderArgs.expiration()))
                .signatureType(sigType)
                .build();

        // Build and sign the order
        SignedOrder signedOrder = orderBuilder.buildSignedOrder(orderData);

        return signedOrder.toMap();
    }

    public Map<String, Object> createMarketOrder(MarketOrderArgs orderArgs, CreateOrderOptions options) {
        RoundConfig roundConfig = ROUNDING_CONFIG.get(options.tickSize());
        OrderResult result = getMarketOrderAmounts(orderArgs.side(), orderArgs.amount(), orderArgs.price(), roundConfig);

        ContractConfig contractConfig = Config.getContractConfig(signer.getChainId(), options.negRisk());

        // Create UtilsSigner and UtilsOrderBuilder
        UtilsSigner utilsSigner = new UtilsSigner(signer.getPrivateKey());
        UtilsOrderBuilder orderBuilder = new UtilsOrderBuilder(
                contractConfig.exchange(),
                signer.getChainId(),
                utilsSigner
        );

        // Convert side string to integer (0 = BUY, 1 = SELL)
        int sideValue = OrderSide.BUY.equals(result.side()) ? 0 : 1;

        // Build order data for market order (expiration is 0)
        OrderData orderData = OrderData.builder()
                .maker(funder)
                .taker(orderArgs.taker())
                .tokenId(orderArgs.tokenId())
                .makerAmount(String.valueOf(result.makerAmount()))
                .takerAmount(String.valueOf(result.takerAmount()))
                .side(sideValue)
                .feeRateBps(String.valueOf(orderArgs.feeRateBps()))
                .nonce(String.valueOf(orderArgs.nonce()))
                .signer(signer.address())
                .expiration("0")  // Market orders typically don't expire
                .signatureType(sigType)
                .build();

        // Build and sign the order
        SignedOrder signedOrder = orderBuilder.buildSignedOrder(orderData);

        return signedOrder.toMap();
    }

    public double calculateBuyMarketPrice(List<OrderSummary> positions, double amountToMatch, OrderType orderType) {
        if (positions == null || positions.isEmpty()) {
            throw new RuntimeException("no match");
        }

        double sum = 0;
        for (int i = positions.size() - 1; i >= 0; i--) {
            OrderSummary p = positions.get(i);
            sum += Double.parseDouble(p.size()) * Double.parseDouble(p.price());
            if (sum >= amountToMatch) {
                return Double.parseDouble(p.price());
            }
        }

        if (OrderType.FOK.equals(orderType)) {
            throw new RuntimeException("no match");
        }

        return Double.parseDouble(positions.get(0).price());
    }

    public double calculateSellMarketPrice(List<OrderSummary> positions, double amountToMatch, OrderType orderType) {
        if (positions == null || positions.isEmpty()) {
            throw new RuntimeException("no match");
        }

        double sum = 0;
        for (int i = positions.size() - 1; i >= 0; i--) {
            OrderSummary p = positions.get(i);
            sum += Double.parseDouble(p.size());
            if (sum >= amountToMatch) {
                return Double.parseDouble(p.price());
            }
        }

        if (OrderType.FOK.equals(orderType)) {
            throw new RuntimeException("no match");
        }

        return Double.parseDouble(positions.get(0).price());
    }

    public int getSigType() {
        return sigType;
    }

    /**
     * Result of order amount calculations
     */
    public record OrderResult(String side, long makerAmount, long takerAmount) {}
}

