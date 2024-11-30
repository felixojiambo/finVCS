package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.utils.FileUtils;
import com.pesapal.felixvcs.utils.HashUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles stashing functionality in the version control system.
 * Stashing temporarily saves changes in the working directory and index
 * for later application, allowing the user to work on a clean slate.
 */
public class StashCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String STASH_DIR = VCS_DIR + "/stash";
    private static final String INDEX_FILE = VCS_DIR + "/index";

    /**
     * Executes the stash command based on the provided arguments.
     *
     * @param args Command-line arguments:
     *             - No arguments: Creates a new stash.
     *             - "pop": Applies the latest stash.
     *             - "list": Lists all stashes.
     * @throws IOException If an I/O error occurs during execution.
     */
    public void execute(String[] args) throws IOException {
        if (!FileUtils.exists(VCS_DIR)) {
            System.out.println("Not a FelixVersionControl repository. Use 'init' to initialize.");
            return;
        }

        if (args.length == 0) {
            createStash();
        } else if (args.length == 1) {
            switch (args[0]) {
                case "pop" -> applyStash();
                case "list" -> listStashes();
                default -> printUsage(args[0]);
            }
        } else {
            printUsage(null);
        }
    }

    /**
     * Creates a new stash by saving the current index state and clearing it.
     *
     * @throws IOException If an I/O error occurs during stash creation.
     */
    private void createStash() throws IOException {
        Map<String, String> indexEntries = loadIndex();

        if (indexEntries.isEmpty()) {
            System.out.println("No changes to stash.");
            return;
        }

        String stashMessage = "Stash created on " + new Date();
        String stashId = HashUtils.sha1((stashMessage + new Date()).getBytes());

        String stashJson = mapToJson(indexEntries);

        FileUtils.createDirectory(STASH_DIR);
        FileUtils.writeToFile(STASH_DIR + "/" + stashId, stashJson);

        FileUtils.writeToFile(INDEX_FILE, "");

        System.out.println("Saved working directory and index state as stash " + stashId);
    }

    /**
     * Applies the latest stash and removes it from the stash directory.
     *
     * @throws IOException If an I/O error occurs during stash application.
     */
    private void applyStash() throws IOException {
        List<String> stashes = getStashes();

        if (stashes.isEmpty()) {
            System.out.println("No stashes to apply.");
            return;
        }

        String latestStash = stashes.get(stashes.size() - 1);
        String stashContent = FileUtils.readFile(STASH_DIR + "/" + latestStash);
        Map<String, String> stashIndex = jsonToMap(stashContent);

        Map<String, String> currentIndex = loadIndex();
        currentIndex.putAll(stashIndex);
        saveIndex(currentIndex);

        Files.deleteIfExists(Paths.get(STASH_DIR + "/" + latestStash));

        System.out.println("Applied stash " + latestStash);
    }

    /**
     * Lists all available stashes.
     *
     * @throws IOException If an I/O error occurs during stash listing.
     */
    private void listStashes() throws IOException {
        List<String> stashes = getStashes();

        if (stashes.isEmpty()) {
            System.out.println("No stashes available.");
        } else {
            System.out.println("Stashes:");
            stashes.forEach(stash -> System.out.println("  " + stash));
        }
    }

    /**
     * Retrieves a sorted list of all stashes.
     *
     * @return A list of stash IDs.
     * @throws IOException If an I/O error occurs.
     */
    private List<String> getStashes() throws IOException {
        if (!FileUtils.exists(STASH_DIR)) {
            return Collections.emptyList();
        }

        return Files.list(Paths.get(STASH_DIR))
                .map(path -> path.getFileName().toString())
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Loads the current index state into a map.
     *
     * @return A map of file paths to their hashes.
     * @throws IOException If an I/O error occurs during index loading.
     */
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

    /**
     * Saves the current index state from a map.
     *
     * @param indexEntries The map of file paths to their hashes.
     * @throws IOException If an I/O error occurs during index saving.
     */
    private void saveIndex(Map<String, String> indexEntries) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : indexEntries.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
        }
        FileUtils.writeToFile(INDEX_FILE, sb.toString());
    }

    /**
     * Converts a map to a JSON string.
     *
     * @param map The map to serialize.
     * @return A JSON representation of the map.
     */
    private String mapToJson(Map<String, String> map) {
        StringBuilder jsonBuilder = new StringBuilder("{");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            jsonBuilder.append("\"").append(escapeJson(entry.getKey())).append("\":")
                    .append("\"").append(escapeJson(entry.getValue())).append("\",");
        }
        if (!map.isEmpty()) {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
        }
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    /**
     * Converts a JSON string to a map.
     *
     * @param json The JSON string to deserialize.
     * @return A map representation of the JSON.
     */
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

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String unescapeJson(String str) {
        return str.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private void printUsage(String command) {
        System.out.println("Unknown stash command: " + command);
        System.out.println("Usage:");
        System.out.println("  stash               # Create a new stash");
        System.out.println("  stash pop           # Apply the latest stash");
        System.out.println("  stash list          # List all stashes");
    }
}
