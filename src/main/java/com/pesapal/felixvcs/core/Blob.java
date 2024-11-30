package com.pesapal.felixvcs.core;

import com.pesapal.felixvcs.utils.HashUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Represents a Blob object in the version control system.
 * A Blob is a basic unit of storage, encapsulating file content (binary or text).
 */
public class Blob {
    private final String hash;     // Unique SHA-1 hash of the Blob's content
    private final byte[] content; // The content stored in the Blob
    private final boolean isBinary; // Indicates if the Blob represents binary data

    /**
     * Constructs a new Blob and computes its hash.
     *
     * @param content  The content of the Blob.
     * @param isBinary True if the content is binary, false otherwise.
     */
    public Blob(byte[] content, boolean isBinary) {
        this.content = content;
        this.isBinary = isBinary;
        this.hash = HashUtils.sha1(content);
    }

    /**
     * Constructs an existing Blob object with a predefined hash.
     * Typically used for deserialization or loading from storage.
     *
     * @param hash     The SHA-1 hash of the Blob.
     * @param content  The content of the Blob.
     * @param isBinary True if the content is binary, false otherwise.
     */
    public Blob(String hash, byte[] content, boolean isBinary) {
        this.hash = hash;
        this.content = content;
        this.isBinary = isBinary;
    }

    // Getters

    /**
     * Returns the hash of the Blob.
     *
     * @return The SHA-1 hash as a String.
     */
    public String getHash() {
        return hash;
    }

    /**
     * Returns the content of the Blob.
     *
     * @return A byte array representing the content.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Indicates whether the Blob represents binary data.
     *
     * @return True if the Blob is binary, false otherwise.
     */
    public boolean isBinary() {
        return isBinary;
    }

    // Custom JSON Serialization and Deserialization

    /**
     * Converts the Blob object to its JSON representation.
     * For binary content, it uses Base64 encoding.
     *
     * @return A JSON string representing the Blob.
     */
    public String toJson() {
        StringBuilder jsonBuilder = new StringBuilder("{");
        jsonBuilder.append("\"hash\":\"").append(escapeJson(hash)).append("\",");
        jsonBuilder.append("\"isBinary\":").append(isBinary).append(",");
        if (isBinary) {
            // Encode binary content as Base64
            String encodedContent = Base64.getEncoder().encodeToString(content);
            jsonBuilder.append("\"content\":\"").append(encodedContent).append("\"");
        } else {
            // Encode text content directly
            String textContent = new String(content, StandardCharsets.UTF_8);
            jsonBuilder.append("\"content\":\"").append(escapeJson(textContent)).append("\"");
        }
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    /**
     * Creates a Blob object from its JSON representation.
     * Decodes Base64 for binary content and parses text content directly.
     *
     * @param json The JSON string representing the Blob.
     * @return A Blob object.
     */
    public static Blob fromJson(String json) {
        String hash = extractJsonValue(json, "hash");
        String isBinaryStr = extractJsonValue(json, "isBinary");
        boolean isBinary = Boolean.parseBoolean(isBinaryStr);
        String contentStr = extractJsonValue(json, "content");

        byte[] content;
        if (isBinary) {
            // Decode Base64 content
            content = Base64.getDecoder().decode(contentStr);
        } else {
            // Decode UTF-8 text content
            content = unescapeJson(contentStr).getBytes(StandardCharsets.UTF_8);
        }

        return new Blob(hash, content, isBinary);
    }

    // Helper Methods

    /**
     * Escapes special characters in a string for safe inclusion in JSON.
     *
     * @param str The input string.
     * @return The escaped string.
     */
    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("/", "\\/")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Unescapes special characters in a JSON string.
     *
     * @param str The escaped JSON string.
     * @return The unescaped string.
     */
    private static String unescapeJson(String str) {
        if (str == null) return null;
        return str.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\/", "/")
                .replace("\\b", "\b")
                .replace("\\f", "\f")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    /**
     * Extracts the value of a key from a JSON string.
     *
     * @param json The JSON string.
     * @param key  The key whose value is to be extracted.
     * @return The value associated with the key, or null if the key is not present.
     */
    private static String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        start += pattern.length();
        char firstChar = json.charAt(start);
        if (firstChar == '\"') {
            int end = json.indexOf('\"', start + 1);
            return json.substring(start + 1, end);
        } else {
            // For boolean or null
            int end = json.indexOf(',', start);
            if (end == -1) {
                end = json.indexOf('}', start);
            }
            return json.substring(start, end).trim();
        }
    }
}
