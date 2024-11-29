package com.pesapal.felixvcs.core;

public class Branch {
    private final String name;
    private String commitHash;

    // Constructor
    public Branch(String name, String commitHash) {
        this.name = name;
        this.commitHash = commitHash;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getCommitHash() {
        return commitHash;
    }

    // Setter for commitHash
    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }

    // Custom JSON Serialization
    public String toJson() {
        StringBuilder jsonBuilder = new StringBuilder("{");
        jsonBuilder.append("\"name\":\"").append(escapeJson(name)).append("\",");
        jsonBuilder.append("\"commitHash\":\"").append(escapeJson(commitHash)).append("\"");
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    // Custom JSON Deserialization
    public static Branch fromJson(String json) {
        String name = extractJsonValue(json, "name");
        String commitHash = extractJsonValue(json, "commitHash");
        return new Branch(unescapeJson(name), unescapeJson(commitHash));
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
            // For non-string values
            int end = json.indexOf(',', start);
            if (end == -1) {
                end = json.indexOf('}', start);
            }
            return json.substring(start, end).trim();
        }
    }
}
