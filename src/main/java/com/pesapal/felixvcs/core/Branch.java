package com.pesapal.felixvcs.core;

/**
 * Represents a branch in the version control system.
 * Each branch points to a specific commit identified by a hash.
 */
public class Branch {
    private final String name;     // Name of the branch (e.g., "main", "feature-x")
    private String commitHash;     // Hash of the commit the branch points to

    /**
     * Constructs a new Branch object.
     *
     * @param name       The name of the branch.
     * @param commitHash The commit hash the branch points to.
     */
    public Branch(String name, String commitHash) {
        this.name = name;
        this.commitHash = commitHash;
    }

    /**
     * Gets the name of the branch.
     *
     * @return The branch name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the commit hash the branch points to.
     *
     * @return The commit hash.
     */
    public String getCommitHash() {
        return commitHash;
    }

    /**
     * Sets the commit hash for the branch.
     *
     * @param commitHash The new commit hash.
     */
    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }

    /**
     * Converts the branch object to its JSON representation.
     *
     * @return A JSON string representing the branch.
     */
    public String toJson() {
        StringBuilder jsonBuilder = new StringBuilder("{");
        jsonBuilder.append("\"name\":\"").append(escapeJson(name)).append("\",");
        jsonBuilder.append("\"commitHash\":\"").append(escapeJson(commitHash)).append("\"");
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    /**
     * Creates a Branch object from its JSON representation.
     *
     * @param json The JSON string representing the branch.
     * @return A Branch object.
     */
    public static Branch fromJson(String json) {
        String name = extractJsonValue(json, "name");
        String commitHash = extractJsonValue(json, "commitHash");
        return new Branch(unescapeJson(name), unescapeJson(commitHash));
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
            // For string values
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
