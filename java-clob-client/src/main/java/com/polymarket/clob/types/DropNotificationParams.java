package com.polymarket.clob.types;

import java.util.List;

/**
 * Parameters for dropping notifications
 */
public record DropNotificationParams(
    List<String> ids
) {
    public DropNotificationParams() {
        this(null);
    }
}

