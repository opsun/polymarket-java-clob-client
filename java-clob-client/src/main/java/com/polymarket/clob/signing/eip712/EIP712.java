package com.polymarket.clob.signing.eip712;

import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * EIP712 signing utilities
 */
public final class EIP712 {
    private EIP712() {}

    private static final String EIP712_DOMAIN_TYPE = "EIP712Domain(string name,string version,uint256 chainId)";
    private static final String CLOB_AUTH_TYPE = "ClobAuth(string address,string timestamp,string nonce,string message)";

    public static String getClobAuthDomain(int chainId) {
        return EIP712_DOMAIN_TYPE + "ClobAuthDomain" + "1" + Numeric.toHexStringWithPrefix(java.math.BigInteger.valueOf(chainId));
    }

    public static String hashClobAuth(String address, String timestamp, String nonce, String message, int chainId) {
        // This is a simplified version. In a production implementation,
        // you would need to properly implement EIP712 encoding
        // For now, we'll use a placeholder that matches the Python implementation's structure
        
        String domainSeparator = hashDomain(chainId);
        String structHash = hashStruct(address, timestamp, nonce, message);
        
        // EIP712 encoding: "\x19\x01" + domainSeparator + structHash
        byte[] prefix = new byte[]{0x19, 0x01};
        byte[] domainBytes = Numeric.hexStringToByteArray(domainSeparator);
        byte[] structBytes = Numeric.hexStringToByteArray(structHash);
        
        byte[] data = new byte[2 + 32 + 32];
        System.arraycopy(prefix, 0, data, 0, 2);
        System.arraycopy(domainBytes, 0, data, 2, 32);
        System.arraycopy(structBytes, 0, data, 34, 32);
        
        return Numeric.toHexString(Hash.sha3(data));
    }

    private static String hashDomain(int chainId) {
        // Simplified domain hash - in production, properly encode EIP712Domain
        String domainString = "ClobAuthDomain" + "1" + chainId;
        return Numeric.toHexString(Hash.sha3(domainString.getBytes(StandardCharsets.UTF_8)));
    }

    private static String hashStruct(String address, String timestamp, String nonce, String message) {
        // Simplified struct hash - in production, properly encode the struct
        String structString = address + timestamp + nonce + message;
        return Numeric.toHexString(Hash.sha3(structString.getBytes(StandardCharsets.UTF_8)));
    }
}

