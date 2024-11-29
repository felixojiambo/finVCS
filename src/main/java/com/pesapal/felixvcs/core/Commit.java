package com.pesapal.felixvcs.core;

public class Commit {
    private String tree;
    private String parent;
    private String message;
    private String timestamp;
    private String author;

    // Getters and Setters

    public String getTree() {
        return tree;
    }

    public void setTree(String tree) {
        this.tree = tree;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public String getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(String timestamp){
        this.timestamp = timestamp;
    }

    public String getAuthor(){
        return author;
    }

    public void setAuthor(String author){
        this.author = author;
    }

    // Custom JSON Serialization
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

    // Custom JSON Deserialization
    public static Commit fromJson(String json) {
        Commit commit = new Commit();
        // Remove curly braces and split by commas
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
        }

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
                    // Unknown field; ignore or handle as needed
                    break;
            }
        }
        return commit;
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
