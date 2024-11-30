package com.pesapal.felixvcs.core;

import com.pesapal.felixvcs.utils.HashUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a tree object in the version control system.
 * <p>
 * A tree is a mapping of file paths to blob hashes, representing the state of the file system
 * at a specific point in time. It is serialized and deserialized to and from JSON format.
 */
public class Tree {
    private Map<String, String> files; // Maps file paths to their corresponding blob hashes.

    /**
     * Default constructor.
     */
    public Tree() {
        this.files = new HashMap<>();
    }

    /**
     * Constructor to initialize a tree with a given set of files.
     *
     * @param files A map of file paths to blob hashes.
     */
    public Tree(Map<String, String> files) {
        this.files = files;
    }

    /**
     * Retrieves the files in the tree.
     *
     * @return A map of file paths to blob hashes.
     */
    public Map<String, String> getFiles() {
        return files;
    }

    /**
     * Sets the files in the tree.
     *
     * @param files A map of file paths to blob hashes.
     */
    public void setFiles(Map<String, String> files) {
        this.files = files;
    }

    /**
     * Computes the SHA-1 hash of the tree's serialized JSON representation.
     * <p>
     * This serves as a unique identifier for the tree.
     *
     * @return The SHA-1 hash of the tree.
     */
    public String getHash() {
        return HashUtils.sha1(toJson().getBytes());
    }

    /**
     * Serializes the tree to a JSON string.
     *
     * @return A JSON representation of the tree.
     */
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

    /**
     * Deserializes a tree object from a JSON string.
     *
     * @param json A JSON string representing a tree.
     * @return A Tree object.
     */
    public static Tree fromJson(String json) {
        Tree tree = new Tree();

        // Remove outer curly braces
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
        }

        // Extract the "files" object
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
            Map<String, String> files = new HashMap<>();
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

    /**
     * Escapes special characters in a string for JSON serialization.
     *
     * @param str The string to escape.
     * @return The escaped string.
     */
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

    /**
     * Unescapes special characters in a string for JSON deserialization.
     *
     * @param str The string to unescape.
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
}
