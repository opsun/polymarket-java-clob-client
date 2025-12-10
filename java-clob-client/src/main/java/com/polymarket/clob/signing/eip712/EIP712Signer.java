package com.polymarket.clob.signing.eip712;

import com.polymarket.clob.signer.Signer;

/**
 * EIP712 message signer for CLOB authentication
 */
public final class EIP712Signer {
    private EIP712Signer() {}

    public static String signClobAuthMessage(Signer signer, long timestamp, int nonce) {
        String address = signer.address();
        String timestampStr = String.valueOf(timestamp);
        String nonceStr = String.valueOf(nonce);
        String message = "This message attests that I control the given wallet";

        String authStructHash = EIP712.hashClobAuth(
            address,
            timestampStr,
            nonceStr,
            message,
            signer.getChainId()
        );

        // Prepend 0x if not present
        if (!authStructHash.startsWith("0x")) {
            authStructHash = "0x" + authStructHash;
        }

        String signature = signer.sign(authStructHash);
        
        // Ensure signature has 0x prefix
        if (!signature.startsWith("0x")) {
            signature = "0x" + signature;
        }

        return signature;
    }
}

