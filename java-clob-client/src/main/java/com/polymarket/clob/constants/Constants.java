package com.polymarket.clob.constants;

/**
 * Constants used throughout the CLOB client
 */
public final class Constants {
    private Constants() {}

    // Access levels
    public static final int L0 = 0;
    public static final int L1 = 1;
    public static final int L2 = 2;

    public static final String CREDENTIAL_CREATION_WARNING = """
        🚨🚨🚨
        Your credentials CANNOT be recovered after they've been created.
        Be sure to store them safely!
        🚨🚨🚨""";

    public static final String L1_AUTH_UNAVAILABLE = "A private key is needed to interact with this endpoint!";
    public static final String L2_AUTH_UNAVAILABLE = "API Credentials are needed to interact with this endpoint!";
    public static final String BUILDER_AUTH_UNAVAILABLE = "Builder API Credentials needed to interact with this endpoint!";
    
    public static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";
    
    public static final int AMOY = 80002;
    public static final int POLYGON = 137;
    
    public static final String END_CURSOR = "LTE=";
}

