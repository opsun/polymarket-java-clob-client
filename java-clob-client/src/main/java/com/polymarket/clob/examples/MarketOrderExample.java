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
        String privateKey = "PK";
        String funder = "0x7375302DabFb662-XXXXX";

        ApiCreds apiCreds = new ApiCreds(
                "019b01ad-4e92-7ee1-b46f-XXXXX",
                "r23QBWwHlZ6YvAylEB1qsmZiG0l-XXXXXX",
                "3a8bdda8f5954414e4818f3f34cc68cb98e-XXXXXXXXXXX"
        );

        ClobClient client = new ClobClient(
            host,
            chainId,
            privateKey,
                apiCreds,
            1,
            funder
        );
        
        //client.setApiCreds(client.createOrDeriveApiCreds(null));

        // Create a market buy order (buy by $ amount)
        MarketOrderArgs marketOrderArgs = new MarketOrderArgs(
            "66092157504251629935997977921922342118422379224694016109322920213524447483721",
            25.0,  // $25 amount to buy
            OrderSide.BUY
        );
        
        Map<String, Object> signedOrder = client.createMarketOrder(marketOrderArgs, null);
        Object response = client.postOrder(signedOrder, OrderType.FOK);
        
        System.out.println("Market order posted: " + response);
    }
}

