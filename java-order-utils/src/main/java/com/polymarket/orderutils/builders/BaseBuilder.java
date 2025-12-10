package com.polymarket.orderutils.builders;

import com.polymarket.orderutils.UtilsSigner;
import com.polymarket.orderutils.model.Order;
import com.polymarket.orderutils.utils.Utils;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * Base builder for order construction
 */
public abstract class BaseBuilder {

    protected final String contractAddress;
    protected final UtilsSigner signer;
    protected final int chainId;
    protected final EIP712Domain domainSeparator;
    protected final Supplier<Long> saltGenerator;

    // EIP712 Type Hashes
    private static final String EIP712_DOMAIN_TYPEHASH =
        "EIP712Domain(string name,string version,uint256 chainId,address verifyingContract)";

    private static final String ORDER_TYPEHASH =
        "Order(uint256 salt,address maker,address signer,address taker,uint256 tokenId," +
        "uint256 makerAmount,uint256 takerAmount,uint256 expiration,uint256 nonce," +
        "uint256 feeRateBps,uint8 side,uint8 signatureType)";

    /**
     * EIP712 Domain structure
     */
    public static class EIP712Domain {
        public final String name;
        public final String version;
        public final BigInteger chainId;
        public final String verifyingContract;

        public EIP712Domain(String name, String version, BigInteger chainId, String verifyingContract) {
            this.name = name;
            this.version = version;
            this.chainId = chainId;
            this.verifyingContract = verifyingContract;
        }
    }

    protected BaseBuilder(
        String exchangeAddress,
        int chainId,
        UtilsSigner signer,
        Supplier<Long> saltGenerator
    ) {
        this.contractAddress = Utils.normalizeAddress(exchangeAddress);
        this.signer = signer;
        this.chainId = chainId;
        this.domainSeparator = getDomainSeparator(chainId, this.contractAddress);
        this.saltGenerator = saltGenerator;
    }

    /**
     * Get the EIP712 domain separator
     */
    protected EIP712Domain getDomainSeparator(int chainId, String verifyingContract) {
        return new EIP712Domain(
            "Polymarket CTF Exchange",
            "1",
            BigInteger.valueOf(chainId),
            verifyingContract
        );
    }

    /**
     * Create an EIP712 struct hash for the order
     */
    protected String createStructHash(Order order) {
        byte[] domainSeparatorHash = hashDomain(domainSeparator);
        byte[] orderHash = hashOrder(order);

        // EIP712 message hash: keccak256("\x19\x01" ‖ domainSeparator ‖ structHash)
        byte[] prefix = new byte[]{0x19, 0x01};
        byte[] message = new byte[prefix.length + domainSeparatorHash.length + orderHash.length];

        System.arraycopy(prefix, 0, message, 0, prefix.length);
        System.arraycopy(domainSeparatorHash, 0, message, prefix.length, domainSeparatorHash.length);
        System.arraycopy(orderHash, 0, message, prefix.length + domainSeparatorHash.length, orderHash.length);

        return Utils.prependZx(Numeric.toHexStringNoPrefix(Hash.sha3(message)));
    }

    /**
     * Hash the EIP712 domain
     */
    private byte[] hashDomain(EIP712Domain domain) {
        byte[] typeHash = Hash.sha3(EIP712_DOMAIN_TYPEHASH.getBytes(StandardCharsets.UTF_8));
        byte[] nameHash = Hash.sha3(domain.name.getBytes(StandardCharsets.UTF_8));
        byte[] versionHash = Hash.sha3(domain.version.getBytes(StandardCharsets.UTF_8));

        // encode(domainSeparator : 𝕊) = keccak256(typeHash ‖ nameHash ‖ versionHash ‖ chainId ‖ verifyingContract)
        byte[] encoded = new byte[32 * 5];

        System.arraycopy(typeHash, 0, encoded, 0, 32);
        System.arraycopy(nameHash, 0, encoded, 32, 32);
        System.arraycopy(versionHash, 0, encoded, 64, 32);
        System.arraycopy(Numeric.toBytesPadded(domain.chainId, 32), 0, encoded, 96, 32);

        // Address needs to be padded to 32 bytes (left-padded with zeros)
        // Convert address to BigInteger and pad to 32 bytes
        byte[] addressBytes = Numeric.hexStringToByteArray(domain.verifyingContract);
        byte[] addressPadded = Numeric.toBytesPadded(new BigInteger(1, addressBytes), 32);
        System.arraycopy(addressPadded, 0, encoded, 128, 32);

        return Hash.sha3(encoded);
    }

    /**
     * Hash the Order structure
     */
    private byte[] hashOrder(Order order) {
        byte[] typeHash = Hash.sha3(ORDER_TYPEHASH.getBytes(StandardCharsets.UTF_8));

        // Encode order struct
        byte[] encoded = new byte[32 * 13];

        System.arraycopy(typeHash, 0, encoded, 0, 32);
        System.arraycopy(Numeric.toBytesPadded(order.getSalt(), 32), 0, encoded, 32, 32);
        //System.arraycopy(Numeric.toBytesPadded(order.getSalt(), 32), 0, encoded, 32, 32);
        //System.arraycopy(Numeric.toBytesPadded(new BigInteger(order.getSalt().substring(2), 16), 32), 0, encoded, 64, 32);
        System.arraycopy(Numeric.toBytesPadded(new BigInteger(order.getMaker().substring(2), 16), 32), 0, encoded, 64, 32);
        System.arraycopy(Numeric.toBytesPadded(new BigInteger(order.getSigner().substring(2), 16), 32), 0, encoded, 96, 32);
        System.arraycopy(Numeric.toBytesPadded(new BigInteger(order.getTaker().substring(2), 16), 32), 0, encoded, 128, 32);
        System.arraycopy(Numeric.toBytesPadded(order.getTokenId(), 32), 0, encoded, 160, 32);
        System.arraycopy(Numeric.toBytesPadded(order.getMakerAmount(), 32), 0, encoded, 192, 32);
        System.arraycopy(Numeric.toBytesPadded(order.getTakerAmount(), 32), 0, encoded, 224, 32);
        System.arraycopy(Numeric.toBytesPadded(order.getExpiration(), 32), 0, encoded, 256, 32);
        System.arraycopy(Numeric.toBytesPadded(order.getNonce(), 32), 0, encoded, 288, 32);
        System.arraycopy(Numeric.toBytesPadded(order.getFeeRateBps(), 32), 0, encoded, 320, 32);
        System.arraycopy(Numeric.toBytesPadded(BigInteger.valueOf(order.getSide()), 32), 0, encoded, 352, 32);
        System.arraycopy(Numeric.toBytesPadded(BigInteger.valueOf(order.getSignatureType()), 32), 0, encoded, 384, 32);

        return Hash.sha3(encoded);
    }

    /**
     * Sign the struct hash
     */
    protected String sign(String structHash) {
        return signer.sign(structHash);
    }
}
