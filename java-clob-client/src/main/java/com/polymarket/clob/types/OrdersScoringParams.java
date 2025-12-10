package com.polymarket.clob.types;

import java.util.List;

/**
 * Parameters for checking if multiple orders are scoring
 */
public record OrdersScoringParams(
    List<String> orderIds
) {}

