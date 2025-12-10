package com.polymarket.orderutils;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

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
     * Sign an EIP712 struct hash
     *
     * @param structHash The EIP712 struct hash to sign
     * @return The signature as a hex string
     */
    public String sign(String structHash) {
        byte[] hashBytes = Numeric.hexStringToByteArray(structHash);

        Sign.SignatureData signature = Sign.signMessage(
            hashBytes,
            credentials.getEcKeyPair(),
            false
        );

        // Concatenate r, s, and v into a single byte array
        byte[] r = signature.getR();
        byte[] s = signature.getS();
        byte[] v = signature.getV();

        byte[] result = new byte[r.length + s.length + v.length];
        System.arraycopy(r, 0, result, 0, r.length);
        System.arraycopy(s, 0, result, r.length, s.length);
        System.arraycopy(v, 0, result, r.length + s.length, v.length);

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
