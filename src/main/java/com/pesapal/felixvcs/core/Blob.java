package com.pesapal.felixvcs.core;

import com.pesapal.felixvcs.utils.HashUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Blob {
    private final String hash;
    private final byte[] content;
    private final boolean isBinary;

    // Constructor for new Blob
    public Blob(byte[] content, boolean isBinary) {
        this.content = content;
        this.isBinary = isBinary;
        this.hash = HashUtils.sha1(content);
    }

    // Constructor for existing Blob (used during deserialization)
    public Blob(String hash, byte[] content, boolean isBinary) {
        this.hash = hash;
        this.content = content;
        this.isBinary = isBinary;
    }

    // Getters
    public String getHash() {
        return hash;
    }

    public byte[] getContent() {
        return content;
    }

    public boolean isBinary() {
        return isBinary;
    }

    // Custom JSON Serialization
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

    // Custom JSON Deserialization
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
