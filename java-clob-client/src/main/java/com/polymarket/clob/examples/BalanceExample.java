package com.polymarket.clob.examples;

import com.polymarket.clob.client.ClobClient;
import com.polymarket.clob.orderbuilder.constants.OrderSide;
import com.polymarket.clob.types.*;

import java.util.Map;

public class BalanceExample {

    public static void main(String[] args) {
        String host = "https://clob.polymarket.com";
        int chainId = 137;
        String privateKey = "pk";
        String funder = "funer";

        ApiCreds apiCreds = new ApiCreds(
                "06f15283-7422-fd5d-d9ec-XXXXX",
                "JodYp1WM1B5AvGVb4yJcW-XXXXXXX",
                "544e5baeaea83d1c59e549d-XXXXX"
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
        //client.setApiCreds(apiCreds);
        BalanceAllowanceParams params = new BalanceAllowanceParams(AssetType.COLLATERAL, null, 1);
        Object allowance = client.getBalanceAllowance(params);

        System.out.println("Order posted: " + allowance);
    }
}
