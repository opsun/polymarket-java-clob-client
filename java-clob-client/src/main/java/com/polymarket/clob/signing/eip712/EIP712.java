package com.polymarket.clob.signing.eip712;

import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.generated.Uint256;

/**
 * EIP712 signing utilities
 */
public final class EIP712 {
    private EIP712() {}

    private static final String EIP712_DOMAIN_TYPE = "EIP712Domain(string name,string version,uint256 chainId)";
    private static final String CLOB_AUTH_TYPE = "ClobAuth(address address,string timestamp,uint256 nonce,string message)";

    private static final byte[] EIP712_DOMAIN_TYPEHASH = Hash.sha3(EIP712_DOMAIN_TYPE.getBytes(StandardCharsets.UTF_8));
    private static final byte[] CLOB_AUTH_TYPEHASH = Hash.sha3(CLOB_AUTH_TYPE.getBytes(StandardCharsets.UTF_8));

    public static String hashClobAuth(String address, String timestamp, String nonce, String message, int chainId) {
        byte[] domainSeparator = Numeric.hexStringToByteArray(hashDomain(chainId));
        byte[] structHash = Numeric.hexStringToByteArray(hashStruct(address, timestamp, nonce, message));

        byte[] data = new byte[2 + domainSeparator.length + structHash.length];
        data[0] = 0x19;
        data[1] = 0x01;
        System.arraycopy(domainSeparator, 0, data, 2, domainSeparator.length);
        System.arraycopy(structHash, 0, data, 2 + domainSeparator.length, structHash.length);
        return Numeric.toHexString(Hash.sha3(data));
    }

    private static String hashDomain(int chainId) {
        byte[] nameHash = Hash.sha3("ClobAuthDomain".getBytes(StandardCharsets.UTF_8));
        byte[] versionHash = Hash.sha3("1".getBytes(StandardCharsets.UTF_8));
        byte[] chainIdEncoded = Numeric.toBytesPadded(BigInteger.valueOf(chainId), 32);

        byte[] data = new byte[32 * 4];
        System.arraycopy(EIP712_DOMAIN_TYPEHASH, 0, data, 0, 32);
        System.arraycopy(nameHash, 0, data, 32, 32);
        System.arraycopy(versionHash, 0, data, 64, 32);
        System.arraycopy(chainIdEncoded, 0, data, 96, 32);

        return Numeric.toHexString(Hash.sha3(data));
    }

    private static String hashStruct(String address, String timestamp, String nonce, String message) {
        byte[] addressBytes = Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(address));
        byte[] addressEncoded = new byte[32];
        System.arraycopy(addressBytes, 0, addressEncoded, 12, 20);

        byte[] timestampHash = Hash.sha3(timestamp.getBytes(StandardCharsets.UTF_8));
        byte[] messageHash = Hash.sha3(message.getBytes(StandardCharsets.UTF_8));

        byte[] nonceEncoded = Numeric.toBytesPadded(new BigInteger(nonce), 32);

        byte[] data = new byte[32 * 5];
        System.arraycopy(CLOB_AUTH_TYPEHASH, 0, data, 0, 32);
        System.arraycopy(addressEncoded, 0, data, 32, 32);
        System.arraycopy(timestampHash, 0, data, 64, 32);
        System.arraycopy(nonceEncoded, 0, data, 96, 32);
        System.arraycopy(messageHash, 0, data, 128, 32);

        return Numeric.toHexString(Hash.sha3(data));
    }
}

