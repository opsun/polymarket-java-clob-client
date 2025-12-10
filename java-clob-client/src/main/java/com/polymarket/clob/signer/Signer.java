package com.polymarket.clob.signer;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

/**
 * Signer for cryptographic operations
 */
public class Signer {
    private final String privateKey;
    private final Credentials credentials;
    private final int chainId;

    public Signer(String privateKey, int chainId) {
        if (privateKey == null || chainId == 0) {
            throw new IllegalArgumentException("Private key and chain ID are required");
        }

        // Remove 0x prefix if present
        String cleanKey = privateKey.startsWith("0x") ? privateKey.substring(2) : privateKey;
        
        this.privateKey = cleanKey;
        BigInteger privateKeyInt = new BigInteger(cleanKey, 16);
        ECKeyPair keyPair = ECKeyPair.create(privateKeyInt);
        this.credentials = Credentials.create(keyPair);
        this.chainId = chainId;
    }

    public String address() {
        return credentials.getAddress();
    }

    public int getChainId() {
        return chainId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * Signs a message hash
     */
    public String sign(String messageHash) {
        // Remove 0x prefix if present
        String cleanHash = messageHash.startsWith("0x") ? messageHash.substring(2) : messageHash;
        BigInteger hashInt = new BigInteger(cleanHash, 16);
        
        org.web3j.crypto.Sign.SignatureData signatureData = 
            org.web3j.crypto.Sign.signMessage(hashInt.toByteArray(), credentials.getEcKeyPair(), false);
        
        byte[] r = signatureData.getR();
        byte[] s = signatureData.getS();
        byte v = signatureData.getV()[0];
        
        byte[] signature = new byte[65];
        System.arraycopy(r, 0, signature, 0, 32);
        System.arraycopy(s, 0, signature, 32, 32);
        signature[64] = v;
        
        return "0x" + Numeric.toHexString(signature).substring(2);
    }
}

