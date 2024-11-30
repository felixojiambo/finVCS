package com.pesapal.felixvcs.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing operations.
 * <p>
 * Provides a method to compute SHA-1 hashes for data.
 */
public class HashUtils {

    /**
     * Computes the SHA-1 hash of the given data.
     *
     * @param data The data to hash, represented as a byte array.
     * @return The SHA-1 hash of the data, represented as a hexadecimal string.
     * @throws RuntimeException If the SHA-1 algorithm is not available on the platform.
     */
    public static String sha1(byte[] data) {
        try {
            // Obtain an instance of the SHA-1 message digest.
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            // Compute the hash bytes.
            byte[] hashBytes = digest.digest(data);
            // Convert the hash bytes to a hexadecimal string.
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // Handle the unlikely case where SHA-1 is not supported.
            throw new RuntimeException("SHA-1 algorithm not found.", e);
        }
    }

    /**
     * Converts a byte array into a hexadecimal string.
     *
     * @param bytes The byte array to convert.
     * @return A string representing the hexadecimal value of the byte array.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            // Convert each byte to a two-character hexadecimal string.
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
