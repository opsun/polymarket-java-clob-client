package com.polymarket.clob.examples;

import com.polymarket.clob.client.ClobClient;
import com.polymarket.clob.orderbuilder.constants.OrderSide;
import com.polymarket.clob.types.*;

import java.util.Map;

/**
 * Trading example - Creating and posting orders
 */
public class TradingExample {
    public static void main(String[] args) {
        String host = "https://clob.polymarket.com";
        int chainId = 137;
        String privateKey = "";
        String funder = "";

        ApiCreds apiCreds = new ApiCreds(
                "019b01ad-4e92-7ee1-b46f-bd1848f9d5eb",
                "",
                ""
        );
        ClobClient client = new ClobClient(
            host,
            chainId,
            privateKey,
                apiCreds,
            1,  // signature_type: 1 for email/Magic wallet signatures
            funder
        );
        
        // Create or derive API credentials
        // ApiCreds creds = client.createOrDeriveApiCreds(null);
        client.setApiCreds(apiCreds);

        // Place a limit order
        OrderArgs orderArgs = new OrderArgs(
            "54775592769957176366862252981703555601401893078926681487143337065519380300006",  // Get a token ID from the Markets API
            0.01,          // Price
            5.0,           // Size
            OrderSide.BUY
        );
        
        Map<String, Object> signedOrder = client.createOrder(orderArgs, null);
        Object response = client.postOrder(signedOrder, OrderType.GTC);
        
        System.out.println("Order posted: " + response);
    }
}

