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
        String privateKey = "pk";
        String funder = "0xC73b9bFd587758e-XXXXX";

        ApiCreds apiCreds = new ApiCreds(
                "06f15283-7422-fd5d-d9ec-XXXXXXXX",
                "JodYp1WM1B5AvGVb4yJcW-XXXXXXXXXX",
                "544e5baeaea83d1c59e549d56a9e845ea8-XXXXXXXXXXXXX"
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
//        OrderArgs orderArgs = new OrderArgs(
//            "47492224027673272587268533486557465128360198441334379196064037756821146202",  // Get a token ID from the Markets API
//            0.5,          // Price
//            5.0,           // Size
//            OrderSide.BUY
//        );
//
//        Map<String, Object> signedOrder = client.createOrder(orderArgs, null);
//        Object response = client.postOrder(signedOrder, OrderType.GTC);
        Object response = client.getOrder("0x6a642a5f9f9ac6e8b5ab6f3543122a2a0b68d84fd03db2b5fde0452946292d04");
        System.out.println("Order posted: " + response);
    }
}

