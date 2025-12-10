package com.polymarket.clob.config;

import com.polymarket.clob.types.ContractConfig;

import java.util.Map;

/**
 * Contract configuration for different chains
 */
public final class Config {
    private Config() {}

    private static final Map<Integer, ContractConfig> CONFIG = Map.of(
        137, new ContractConfig(
            "0x4bFb41d5B3570DeFd03C39a9A4D8dE6Bd8B8982E",
            "0x2791Bca1f2de4661ED88A30C99A7a9449Aa84174",
            "0x4D97DCd97eC945f40cF65F87097ACe5EA0476045"
        ),
        80002, new ContractConfig(
            "0xdFE02Eb6733538f8Ea35D585af8DE5958AD99E40",
            "0x9c4e1703476e875070ee25b56a58b008cfb8fa78",
            "0x69308FB512518e39F9b16112fA8d994F4e2Bf8bB"
        )
    );

    private static final Map<Integer, ContractConfig> NEG_RISK_CONFIG = Map.of(
        137, new ContractConfig(
            "0xC5d563A36AE78145C45a50134d48A1215220f80a",
            "0x2791bca1f2de4661ed88a30c99a7a9449aa84174",
            "0x4D97DCd97eC945f40cF65F87097ACe5EA0476045"
        ),
        80002, new ContractConfig(
            "0xd91E80cF2E7be2e162c6513ceD06f1dD0dA35296",
            "0x9c4e1703476e875070ee25b56a58b008cfb8fa78",
            "0x69308FB512518e39F9b16112fA8d994F4e2Bf8bB"
        )
    );

    public static ContractConfig getContractConfig(int chainId, boolean negRisk) {
        Map<Integer, ContractConfig> configMap = negRisk ? NEG_RISK_CONFIG : CONFIG;
        ContractConfig config = configMap.get(chainId);
        if (config == null) {
            throw new IllegalArgumentException("Invalid chainID: " + chainId);
        }
        return config;
    }
}

