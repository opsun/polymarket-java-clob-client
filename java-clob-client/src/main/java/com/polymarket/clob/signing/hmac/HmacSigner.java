package com.polymarket.clob.signing.hmac;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * HMAC signature builder for Level 2 authentication
 */
public final class HmacSigner {
    private HmacSigner() {}

    public static String buildHmacSignature(
        String secret,
        long timestamp,
        String method,
        String requestPath,
        String body
    ) {
        try {
            byte[] base64Secret = Base64.getUrlDecoder().decode(secret);
            SecretKeySpec secretKeySpec = new SecretKeySpec(base64Secret, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);

            StringBuilder message = new StringBuilder();
            message.append(timestamp);
            message.append(method);
            message.append(requestPath);
            
            if (body != null && !body.isEmpty()) {
                // Replace single quotes with double quotes to match Go/TypeScript behavior
                String bodyStr = body.replace("'", "\"");
                message.append(bodyStr);
            }

            byte[] hash = mac.doFinal(message.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC signature", e);
        }
    }
}

