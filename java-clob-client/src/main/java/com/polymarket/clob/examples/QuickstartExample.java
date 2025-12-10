package com.polymarket.clob.examples;

import com.polymarket.clob.client.ClobClient;

/**
 * Quickstart example - Read-only operations
 */
public class QuickstartExample {
    public static void main(String[] args) {
        ClobClient client = new ClobClient("https://clob.polymarket.com");
        
        Object ok = client.getOk();
        Object time = client.getServerTime();
        
        System.out.println("OK: " + ok);
        System.out.println("Server Time: " + time);
    }
}

