package com.polymarket.clob.examples;

import com.polymarket.clob.client.ClobClient;
import com.polymarket.clob.orderbuilder.constants.OrderSide;
import com.polymarket.clob.types.*;

import java.util.Map;

/**
 * Market order example
 */
public class MarketOrderExample {
    public static void main(String[] args) {
        String host = "https://clob.polymarket.com";
        int chainId = 137;
        String privateKey = "<your-private-key>";
        String funder = "<your-funder-address>";

        ClobClient client = new ClobClient(
            host,
            chainId,
            privateKey,
            null,
            1,
            funder
        );
        
        client.setApiCreds(client.createOrDeriveApiCreds(null));

        // Create a market buy order (buy by $ amount)
        MarketOrderArgs marketOrderArgs = new MarketOrderArgs(
            "<token-id>",
            25.0,  // $25 amount to buy
            OrderSide.BUY
        );
        
        Map<String, Object> signedOrder = client.createMarketOrder(marketOrderArgs, null);
        Object response = client.postOrder(signedOrder, OrderType.FOK);
        
        System.out.println("Market order posted: " + response);
    }
}

