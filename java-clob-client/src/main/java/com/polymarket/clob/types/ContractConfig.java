package com.polymarket.clob.types;

/**
 * Contract configuration for a chain
 */
public record ContractConfig(
    String exchange,           // The exchange contract responsible for matching orders
    String collateral,         // The ERC20 token used as collateral
    String conditionalTokens   // The ERC1155 conditional tokens contract
) {}

