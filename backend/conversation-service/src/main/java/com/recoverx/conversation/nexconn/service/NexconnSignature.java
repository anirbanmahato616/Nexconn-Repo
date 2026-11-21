package com.recoverx.conversation.nexconn.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class NexconnSignature {
    private static final char[] ALPHANUM = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private final SecureRandom random = new SecureRandom();

    public String nonce() {
        StringBuilder value = new StringBuilder(18);
        for (int i = 0; i < 18; i++) value.append(ALPHANUM[random.nextInt(ALPHANUM.length)]);
        return value.toString();
    }

    public String sign(String secret, String nonce, String timestamp) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1")
                    .digest((secret + nonce + timestamp).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not create Nexconn signature", ex);
        }
    }

    public boolean matches(String expected, String actual) {
        if (expected == null || actual == null) return false;
        return MessageDigest.isEqual(expected.toLowerCase().getBytes(StandardCharsets.UTF_8),
                actual.toLowerCase().getBytes(StandardCharsets.UTF_8));
    }
}
