package com.pesapal.felixvcs.core;

import java.util.Map;

public class Tree {
    private Map<String, String> files;

    public Tree() {}

    public Tree(Map<String, String> files) {
        this.files = files;
    }

    public Map<String, String> getFiles() {
        return files;
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }

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

    private String escapeJson(String str) {
        return str.replace("\"", "\\\"")
                .replace("\\", "\\\\")
                .replace("/", "\\/")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
