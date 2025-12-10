# Polymarket Java CLOB Client

Java client for the Polymarket Central Limit Order Book (CLOB).

## Requirements

- **Java 21+**
- **Private key** that owns funds on Polymarket
- Optional: a **proxy/funder address** if you use an email or smart-contract wallet

## Installation

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.polymarket</groupId>
    <artifactId>java-clob-client</artifactId>
    <version>0.30.0</version>
</dependency>
```

## Usage

### Quickstart (read-only)

```java
import com.polymarket.clob.client.ClobClient;

ClobClient client = new ClobClient("https://clob.polymarket.com");
var ok = client.getOk();
var time = client.getServerTime();
System.out.println(ok + ", " + time);
```

### Start trading (EOA)

```java
import com.polymarket.clob.client.ClobClient;

String host = "https://clob.polymarket.com";
int chainId = 137;
String privateKey = "<your-private-key>";
String funder = "<your-funder-address>";

ClobClient client = new ClobClient(
    host,
    chainId,
    privateKey,
    null,
    1,  // signature_type: 1 for email/Magic wallet signatures
    funder
);
client.setApiCreds(client.createOrDeriveApiCreds(null));
```

### Place a limit order

```java
import com.polymarket.clob.client.ClobClient;
import com.polymarket.clob.types.OrderArgs;
import com.polymarket.clob.types.OrderType;
import com.polymarket.clob.orderbuilder.constants.OrderSide;

OrderArgs orderArgs = new OrderArgs(
    "<token-id>",
    0.01,
    5.0,
    OrderSide.BUY
);
var signedOrder = client.createOrder(orderArgs, null);
var resp = client.postOrder(signedOrder, OrderType.GTC);
System.out.println(resp);
```

## Notes

- To discover token IDs, use the Markets API Explorer: [Get Markets](https://docs.polymarket.com/developers/gamma-markets-api/get-markets).
- Prices are in dollars from 0.00 to 1.00. Shares are whole or fractional units of the outcome token.

