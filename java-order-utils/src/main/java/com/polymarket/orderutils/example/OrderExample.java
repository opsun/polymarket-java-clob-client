package com.polymarket.orderutils.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polymarket.orderutils.UtilsSigner;
import com.polymarket.orderutils.builders.UtilsOrderBuilder;
import com.polymarket.orderutils.model.OrderData;
import com.polymarket.orderutils.model.Side;
import com.polymarket.orderutils.model.SignatureType;
import com.polymarket.orderutils.model.SignedOrder;

/**
 * Example usage of the Java Order Utils library
 */
public class OrderExample {

    public static void main(String[] args) throws Exception {
        // Configuration
        String exchangeAddress = "0x4bFb41d5B3570DeFd03C39a9A4D8dE6Bd8B8982E";
        int chainId = 80002; // Polygon Amoy testnet
        String privateKey = "0xYOUR_PRIVATE_KEY_HERE"; // Replace with your private key

        // Create signer and builder
        UtilsSigner signer = new UtilsSigner(privateKey);
        UtilsOrderBuilder builder = new UtilsOrderBuilder(exchangeAddress, chainId, signer);

        System.out.println("Signer address: " + signer.getAddress());

        // Create order data
        OrderData orderData = OrderData.builder()
            .maker(signer.getAddress())
            .taker("0x0000000000000000000000000000000000000000") // Public order
            .tokenId("123456789")
            .makerAmount("1000000000000000000") // 1.0 tokens in wei
            .takerAmount("500000000000000000")  // 0.5 tokens in wei
            .side(Side.BUY.getValue())
            .feeRateBps("100") // 1% fee (100 basis points)
            .nonce("0")
            .expiration("0") // No expiration
            .signatureType(SignatureType.EOA)
            .build();

        // Build and sign the order
        System.out.println("\nBuilding and signing order...");
        SignedOrder signedOrder = builder.buildSignedOrder(orderData);

        // Convert to JSON for API submission
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(signedOrder.toMap());

        System.out.println("\nSigned Order JSON:");
        System.out.println(json);

        // Example: Sell order
        System.out.println("\n\n--- SELL ORDER EXAMPLE ---\n");

        OrderData sellOrderData = OrderData.builder()
            .maker(signer.getAddress())
            .taker("0x0000000000000000000000000000000000000000")
            .tokenId("987654321")
            .makerAmount("2000000000000000000") // 2.0 tokens
            .takerAmount("1500000000000000000") // 1.5 tokens
            .side(Side.SELL.getValue())
            .feeRateBps("50") // 0.5% fee
            .nonce("1")
            .expiration(String.valueOf(System.currentTimeMillis() / 1000 + 86400)) // Expires in 24 hours
            .signatureType(SignatureType.EOA)
            .build();

        SignedOrder signedSellOrder = builder.buildSignedOrder(sellOrderData);
        String sellJson = mapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(signedSellOrder.toMap());

        System.out.println("Signed Sell Order JSON:");
        System.out.println(sellJson);
    }
}
