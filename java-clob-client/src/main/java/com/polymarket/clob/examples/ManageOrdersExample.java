package com.polymarket.clob.examples;

import com.polymarket.clob.client.ClobClient;
import com.polymarket.clob.types.*;

import java.util.List;
import java.util.Map;

/**
 * Example for managing orders
 */
public class ManageOrdersExample {
    public static void main(String[] args) {
        String host = "https://clob.polymarket.com";
        int chainId = 137;
        String privateKey = "PK";
        String funder = "0x7375302DabFb662DE2Ba-XXXXX";

        ClobClient client = new ClobClient(
            host,
            chainId,
            privateKey,
            null,
            1,
            funder
        );
        
        client.setApiCreds(client.createOrDeriveApiCreds(null));

        // Get open orders
        List<Object> openOrders = client.getOrders(new OpenOrderParams(), "MA==");
        System.out.println("Open orders: " + openOrders.size());

        // Cancel a specific order
        if (!openOrders.isEmpty()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> firstOrder = (Map<String, Object>) openOrders.get(0);
            String orderId = (String) firstOrder.get("id");
            if (orderId != null) {
                client.cancel(orderId);
                System.out.println("Cancelled order: " + orderId);
            }
        }

        // Cancel all orders
        client.cancelAll();
        System.out.println("Cancelled all orders");
    }
}

