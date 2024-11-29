package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.utils.FileUtils;
import com.pesapal.felixvcs.utils.HashUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class StashCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String STASH_DIR = VCS_DIR + "/stash";
    private static final String INDEX_FILE = VCS_DIR + "/index";

    public void execute(String[] args) throws IOException {
        if (!FileUtils.exists(VCS_DIR)) {
            System.out.println("Not a FelixVersionControl repository. Use 'init' to initialize.");
            return;
        }

        if (args.length == 0) {
            // Create a new stash
            createStash();
        } else if (args.length == 1) {
            switch (args[0]) {
                case "pop":
                    applyStash();
                    break;
                case "list":
                    listStashes();
                    break;
                default:
                    System.out.println("Unknown stash command: " + args[0]);
                    break;
            }
        } else {
            System.out.println("Usage:");
            System.out.println("  stash               # Create a new stash");
            System.out.println("  stash pop           # Apply the latest stash");
            System.out.println("  stash list          # List all stashes");
        }
    }

    private void createStash() throws IOException {
        // Load current index
        Map<String, String> indexEntries = loadIndex();

        if (indexEntries.isEmpty()) {
            System.out.println("No changes to stash.");
            return;
        }

        String stashMessage = "Stash on " + new Date();
        String stashId = HashUtils.sha1((stashMessage + new Date()).getBytes());

        // Serialize index
        String stashJson = mapToJson(indexEntries);

        // Save stash
        FileUtils.createDirectory(STASH_DIR);
        FileUtils.writeToFile(STASH_DIR + "/" + stashId, stashJson);

        // Clear the index
        FileUtils.writeToFile(INDEX_FILE, "");

        System.out.println("Saved working directory and index state as stash " + stashId);
    }

    private void applyStash() throws IOException {
        List<String> stashes = getStashes();

        if (stashes.isEmpty()) {
            System.out.println("No stashes to apply.");
            return;
        }

        String latestStash = stashes.get(stashes.size() - 1);
        String stashContent = FileUtils.readFile(STASH_DIR + "/" + latestStash);
        Map<String, String> stashIndex = jsonToMap(stashContent);

        // Merge stashIndex into current index
        Map<String, String> currentIndex = loadIndex();
        currentIndex.putAll(stashIndex);
        saveIndex(currentIndex);

        // Remove the applied stash
        Files.deleteIfExists(Paths.get(STASH_DIR + "/" + latestStash));

        System.out.println("Applied stash " + latestStash);
    }

    private void listStashes() throws IOException {
        List<String> stashes = getStashes();

        if (stashes.isEmpty()) {
            System.out.println("No stashes available.");
            return;
        }

        System.out.println("Stashes:");
        for (String stash : stashes) {
            System.out.println("  " + stash);
        }
    }

    private List<String> getStashes() throws IOException {
        if (!FileUtils.exists(STASH_DIR)) {
            return Collections.emptyList();
        }

        return Files.list(Paths.get(STASH_DIR))
                .map(path -> path.getFileName().toString())
                .sorted()
                .collect(Collectors.toList());
    }

    private Map<String, String> loadIndex() throws IOException {
        Map<String, String> indexEntries = new HashMap<>();
        if (FileUtils.exists(INDEX_FILE)) {
            String content = FileUtils.readFile(INDEX_FILE);
            String[] entries = content.split("\n");
            for (String entry : entries) {
                if (!entry.trim().isEmpty()) {
                    String[] parts = entry.split(":", 2);
                    if (parts.length == 2) {
                        indexEntries.put(parts[0], parts[1]);
                    }
                }
            }
        }
        return indexEntries;
    }

    private void saveIndex(Map<String, String> indexEntries) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : indexEntries.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
        }
        FileUtils.writeToFile(INDEX_FILE, sb.toString());
    }

    // Custom JSON serialization for a map
    private String mapToJson(Map<String, String> map) {
        StringBuilder jsonBuilder = new StringBuilder("{");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            jsonBuilder.append("\"").append(escapeJson(entry.getKey())).append("\":")
                    .append("\"").append(escapeJson(entry.getValue())).append("\",");
        }
        if (!map.isEmpty()) {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1); // Remove trailing comma
        }
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    // Custom JSON deserialization for a map
    private Map<String, String> jsonToMap(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
        }
        String[] entries = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String entry : entries) {
            String[] keyValue = entry.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
                String value = keyValue[1].trim().replaceAll("^\"|\"$", "");
                map.put(unescapeJson(key), unescapeJson(value));
            }
        }
        return map;
    }

    // Helper method to escape JSON special characters
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

    // Helper method to unescape JSON special characters
    private String unescapeJson(String str) {
        if (str == null) return "";
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
