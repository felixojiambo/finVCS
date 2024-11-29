package com.pesapal.felixvcs.core;

import com.pesapal.felixvcs.utils.HashUtils;

import java.util.Map;

public class Tree {
    private Map<String, String> files; // filePath -> blobHash

    // Default Constructor
    public Tree() {}

    // Constructor with files
    public Tree(Map<String, String> files) {
        this.files = files;
    }

    // Getters and Setters
    public Map<String, String> getFiles() {
        return files;
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }

    /**
     * Computes the SHA-1 hash of the tree's serialized JSON representation.
     * This serves as a unique identifier for the tree.
     *
     * @return The SHA-1 hash of the tree.
     */
    public String getHash() {
        return HashUtils.sha1(toJson().getBytes());
    }

    // Custom JSON Serialization
    public String toJson() {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"files\":{");
        for (Map.Entry<String, String> entry : files.entrySet()) {
            jsonBuilder.append("\"")
                    .append(escapeJson(entry.getKey()))
                    .append("\":\"")
                    .append(escapeJson(entry.getValue()))
                    .append("\",");
        }
        if (!files.isEmpty()) {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1); // Remove trailing comma
        }
        jsonBuilder.append("}}");
        return jsonBuilder.toString();
    }

    // Custom JSON Deserialization
    public static Tree fromJson(String json) {
        Tree tree = new Tree();
        // Remove outer curly braces
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
        }

        // Find the "files" object
        String filesJson = null;
        String[] fields = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String field : fields) {
            String[] keyValue = field.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
            if (keyValue.length != 2) continue;

            String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
            String value = keyValue[1].trim();
            if (key.equals("files")) {
                if (value.startsWith("{") && value.endsWith("}")) {
                    filesJson = value.substring(1, value.length() - 1);
                }
                break;
            }
        }

        if (filesJson != null) {
            Map<String, String> files = new java.util.HashMap<>();
            String[] fileEntries = filesJson.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String fileEntry : fileEntries) {
                String[] kv = fileEntry.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
                if (kv.length != 2) continue;
                String filePath = kv[0].trim().replaceAll("^\"|\"$", "");
                String blobHash = kv[1].trim().replaceAll("^\"|\"$", "");
                files.put(unescapeJson(filePath), unescapeJson(blobHash));
            }
            tree.setFiles(files);
        }

        return tree;
    }

    // Helper method to escape special JSON characters
    private String escapeJson(String str) {
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

    // Helper method to unescape special JSON characters
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
}
