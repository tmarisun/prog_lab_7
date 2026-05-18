package org.example.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PasswordHasher {

    private PasswordHasher() {}

    public static String sha384Hex(String password) {
        if (password == null) {
            throw new IllegalArgumentException("password is null");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-384");
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-384 not available", e);
        }
    }
}
