package com.polymarket.orderutils.utils;

import org.web3j.crypto.Keys;

import java.time.Instant;
import java.util.Random;

/**
 * Utility functions for order processing
 */
public class Utils {

    private static final double MAX_INT = Math.pow(2, 32);
    private static final Random random = new Random();

    /**
     * Normalize a string by converting to lowercase and removing punctuation
     */
    public static String normalize(String s) {
        if (s == null) {
            return null;
        }
        String lowered = s.toLowerCase();
        return lowered.replaceAll("[\\p{Punct}]", "");
    }

    /**
     * Normalize an Ethereum address to checksum format
     */
    public static String normalizeAddress(String address) {
        if (address == null) {
            return null;
        }
        try {
            return Keys.toChecksumAddress(address);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Ethereum address: " + address, e);
        }
    }

    /**
     * Generate a pseudo-random seed
     */
    public static long generateSeed() {
        long now = Instant.now().getEpochSecond();
        return Math.round(now * random.nextDouble());
    }

    /**
     * Prepend "0x" to the input string if it is missing
     */
    public static String prependZx(String inStr) {
        if (inStr == null) {
            return null;
        }
        String s = inStr;
        if (s.length() > 2 && !s.startsWith("0x")) {
            s = "0x" + s;
        }
        return s;
    }

    private Utils() {
        // Private constructor to prevent instantiation
    }
}
