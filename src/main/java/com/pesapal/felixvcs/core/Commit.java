package com.pesapal.felixvcs.core;

/**
 * Represents a commit in the version control system.
 * A commit includes metadata such as tree, parent, message, timestamp, and author.
 */
public class Commit {
    private String tree;       // The hash of the associated tree structure
    private String parent;     // The hash of the parent commit, if any
    private String message;    // Commit message
    private String timestamp;  // The timestamp of the commit
    private String author;     // The author of the commit

    // Getters and Setters

    /**
     * Gets the tree hash associated with the commit.
     *
     * @return The tree hash.
     */
    public String getTree() {
        return tree;
    }

    /**
     * Sets the tree hash associated with the commit.
     *
     * @param tree The tree hash.
     */
    public void setTree(String tree) {
        this.tree = tree;
    }

    /**
     * Gets the parent commit hash.
     *
     * @return The parent commit hash, or null if this is the initial commit.
     */
    public String getParent() {
        return parent;
    }

    /**
     * Sets the parent commit hash.
     *
     * @param parent The parent commit hash.
     */
    public void setParent(String parent) {
        this.parent = parent;
    }

    /**
     * Gets the commit message.
     *
     * @return The commit message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the commit message.
     *
     * @param message The commit message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the timestamp of the commit.
     *
     * @return The commit timestamp.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of the commit.
     *
     * @param timestamp The commit timestamp.
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the author of the commit.
     *
     * @return The author.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author of the commit.
     *
     * @param author The author.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    // JSON Serialization and Deserialization

    /**
     * Converts the commit object to its JSON representation.
     *
     * @return A JSON string representing the commit.
     */
    public String toJson() {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"tree\":\"").append(escapeJson(tree)).append("\",");
        jsonBuilder.append("\"parent\":").append(parent != null ? "\"" + escapeJson(parent) + "\"" : "null").append(",");
        jsonBuilder.append("\"message\":\"").append(escapeJson(message)).append("\",");
        jsonBuilder.append("\"timestamp\":\"").append(escapeJson(timestamp)).append("\",");
        jsonBuilder.append("\"author\":\"").append(escapeJson(author)).append("\"");
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    /**
     * Creates a commit object from its JSON representation.
     *
     * @param json The JSON string.
     * @return The commit object.
     */
    public static Commit fromJson(String json) {
        Commit commit = new Commit();

        // Remove outer curly braces
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
        }

        // Split fields by commas, respecting nested structures
        String[] fields = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String field : fields) {
            String[] keyValue = field.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
            if (keyValue.length != 2) continue;

            String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
            String value = keyValue[1].trim();

            // Remove quotes if present
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            } else if (value.equals("null")) {
                value = null;
            }

            // Assign the value to the appropriate field
            switch (key) {
                case "tree":
                    commit.setTree(unescapeJson(value));
                    break;
                case "parent":
                    commit.setParent(unescapeJson(value));
                    break;
                case "message":
                    commit.setMessage(unescapeJson(value));
                    break;
                case "timestamp":
                    commit.setTimestamp(unescapeJson(value));
                    break;
                case "author":
                    commit.setAuthor(unescapeJson(value));
                    break;
                default:
                    // Ignore unknown fields
                    break;
            }
        }

        return commit;
    }

    // Helper Methods

    /**
     * Escapes special characters for JSON strings.
     *
     * @param str The input string.
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
     * Unescapes special characters in JSON strings.
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
}
