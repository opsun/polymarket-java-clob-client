package com.polymarket.orderutils;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

/**
 * Signs orders using a private key
 */
public class UtilsSigner {

    private final String privateKey;
    private final Credentials credentials;

    /**
     * Create a new Signer with the given private key
     *
     * @param privateKey The private key (with or without 0x prefix)
     */
    public UtilsSigner(String privateKey) {
        // Remove 0x prefix if present
        String key = privateKey.startsWith("0x") ? privateKey.substring(2) : privateKey;
        this.privateKey = key;
        this.credentials = Credentials.create(key);
    }

    /**
     * Sign an EIP712 struct hash (already hashed data)
     * This matches Python's Account._sign_hash behavior
     *
     * @param structHash The EIP712 struct hash to sign (already keccak256 hashed)
     * @return The signature as a hex string
     */
    public String sign(String structHash) {
        // Remove 0x prefix if present
        String cleanHash = structHash.startsWith("0x") ? structHash.substring(2) : structHash;
        byte[] hashBytes = Numeric.hexStringToByteArray(cleanHash);

        // Sign the already-hashed data directly using ECDSA
        // This matches Python's Account._sign_hash behavior
        ECDSASignature signature = credentials.getEcKeyPair().sign(hashBytes);

        // Calculate v value (recovery id + 27)
        // We need to find the correct recovery id
        int recId = -1;
        for (int i = 0; i < 4; i++) {
            BigInteger publicKey = Sign.recoverFromSignature(i, signature, hashBytes);
            if (publicKey != null && publicKey.equals(credentials.getEcKeyPair().getPublicKey())) {
                recId = i;
                break;
            }
        }

        if (recId == -1) {
            throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
        }

        int v = recId + 27;

        // Convert to byte arrays (32 bytes each for r and s)
        byte[] r = Numeric.toBytesPadded(signature.r, 32);
        byte[] s = Numeric.toBytesPadded(signature.s, 32);
        byte[] vByte = new byte[]{(byte) v};

        // Concatenate r + s + v
        byte[] result = new byte[65];
        System.arraycopy(r, 0, result, 0, 32);
        System.arraycopy(s, 0, result, 32, 32);
        System.arraycopy(vByte, 0, result, 64, 1);

        return Numeric.toHexString(result);
    }

    /**
     * Get the address associated with this signer
     *
     * @return The Ethereum address
     */
    public String getAddress() {
        return credentials.getAddress();
    }

    /**
     * Get the credentials
     *
     * @return The Web3j Credentials object
     */
    public Credentials getCredentials() {
        return credentials;
    }
}
