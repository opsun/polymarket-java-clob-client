## Polymarket CLOB Java order-utils

Java utilities for generating and signing orders from Polymarket's Exchange

This is a Java port of the [Python order-utils](https://github.com/Polymarket/py-order-utils) library.

### Project Structure

```
java-order-utils/
├── pom.xml
└── src/main/java/com/polymarket/orderutils/
    ├── Constants.java
    ├── Signer.java
    ├── builders/
    │   ├── BaseBuilder.java
    │   ├── OrderBuilder.java
    │   └── ValidationException.java
    ├── model/
    │   ├── Order.java
    │   ├── OrderData.java
    │   ├── Side.java
    │   ├── SignatureType.java
    │   └── SignedOrder.java
    └── utils/
        └── Utils.java
```

### Build

This project uses Maven. To build:

```bash
cd java-order-utils
mvn clean install
```

### Dependencies

- Java 11 or higher
- Web3j 4.10.3 - Ethereum client library
- Bouncy Castle - Cryptographic operations
- Jackson - JSON serialization
- Lombok - Reduces boilerplate code

### Usage

```java
import com.polymarket.orderutils.UtilsSigner;
import com.polymarket.orderutils.builders.UtilsOrderBuilder;
import com.polymarket.orderutils.model.OrderData;
import com.polymarket.orderutils.model.SignedOrder;
import com.polymarket.orderutils.model.Side;
import com.polymarket.orderutils.model.SignatureType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Example {
    public static void main(String[] args) throws Exception {
        // Configuration
        String exchangeAddress = "0x...";
        int chainId = 80002;
        String privateKey = "0x...";

        // Create signer and builder
        UtilsSigner signer = new UtilsSigner(privateKey);
        UtilsOrderBuilder builder = new UtilsOrderBuilder(exchangeAddress, chainId, signer);

        // Create order data
        OrderData orderData = OrderData.builder()
                .maker(signer.getAddress())
                .taker("0x0000000000000000000000000000000000000000") // Public order
                .tokenId("123456")
                .makerAmount("1000000000000000000") // 1.0 in wei
                .takerAmount("500000000000000000")  // 0.5 in wei
                .side(Side.BUY.getValue())
                .feeRateBps("100") // 1% fee
                .nonce("0")
                .expiration("0") // No expiration
                .signatureType(SignatureType.EOA)
                .build();

        // Build and sign the order
        SignedOrder signedOrder = builder.buildSignedOrder(orderData);

        // Convert to JSON for API submission
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(signedOrder.toMap());

        System.out.println("Signed Order JSON:");
        System.out.println(json);
    }
}
```

### Example Output

```json
{
  "salt": "1234567890",
  "maker": "0x1234567890abcdef1234567890abcdef12345678",
  "signer": "0x1234567890abcdef1234567890abcdef12345678",
  "taker": "0x0000000000000000000000000000000000000000",
  "tokenId": "123456",
  "makerAmount": "1000000000000000000",
  "takerAmount": "500000000000000000",
  "expiration": "0",
  "nonce": "0",
  "feeRateBps": "100",
  "side": "BUY",
  "signatureType": 0,
  "signature": "0xabcdef..."
}
```

### API Reference

#### Signer

Creates a signer for signing orders using a private key.

```java
Signer signer = new Signer("0x...private_key...");
String address = signer.getAddress();
```

#### OrderBuilder

Builds and signs orders.

```java
OrderBuilder builder = new OrderBuilder(exchangeAddress, chainId, signer);

// Build order
Order order = builder.buildOrder(orderData);

// Sign order
String signature = builder.buildOrderSignature(order);

// Build and sign in one step
SignedOrder signedOrder = builder.buildSignedOrder(orderData);
```

#### OrderData

Input data for creating orders. Use the builder pattern:

```java
OrderData orderData = OrderData.builder()
    .maker("0x...") // Required
    .tokenId("123") // Required
    .makerAmount("1000") // Required
    .takerAmount("500") // Required
    .side(Side.BUY.getValue()) // Required (0 = BUY, 1 = SELL)
    .feeRateBps("100") // Required
    .taker("0x...") // Optional, defaults to zero address
    .nonce("0") // Optional, defaults to "0"
    .expiration("0") // Optional, defaults to "0" (no expiration)
    .signer("0x...") // Optional, defaults to maker
    .signatureType(SignatureType.EOA) // Optional, defaults to EOA
    .build();
```

#### Side

Order side enum:

```java
Side.BUY.getValue()  // 0
Side.SELL.getValue() // 1
```

#### SignatureType

Signature type constants:

```java
SignatureType.EOA              // 0 - ECDSA EIP712 signatures signed by EOAs
SignatureType.POLY_PROXY       // 1 - EIP712 signatures for Polymarket Proxy wallets
SignatureType.POLY_GNOSIS_SAFE // 2 - EIP712 signatures for Polymarket Gnosis safes
```

### Differences from Python Version

1. **Type System**: Java uses strong typing with BigInteger for large numbers instead of Python's dynamic types
2. **Builder Pattern**: OrderData uses Lombok's builder pattern for cleaner construction
3. **Enums**: Side is implemented as a proper Java enum instead of Python constants
4. **Error Handling**: Uses checked exceptions and proper Java exception handling
5. **Dependencies**: Uses Web3j for Ethereum functionality instead of eth-account

### License

See LICENSE file for details.
