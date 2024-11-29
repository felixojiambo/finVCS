package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.utils.FileUtils;
import com.pesapal.felixvcs.utils.HashUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class AddCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String BLOBS_DIR = VCS_DIR + "/blobs";
    private static final String INDEX_FILE = VCS_DIR + "/index";
    private static final String IGNORE_FILE = VCS_DIR + "/ignore";

    public void execute(String filePath) throws IOException {
        // Check if repository is initialized
        if (!FileUtils.exists(VCS_DIR)) {
            System.out.println("Not a FelixVersionControl repository. Use 'init' to initialize.");
            return;
        }

        // Check if file exists
        if (!Files.exists(Paths.get(filePath))) {
            System.out.println("File " + filePath + " does not exist.");
            return;
        }

        // Check if file is ignored
        if (isIgnored(filePath)) {
            System.out.println("File " + filePath + " is ignored.");
            return;
        }

        // Determine if the file is binary
        boolean isBinary = isBinaryFile(filePath);

        if (isBinary) {
            handleBinaryFile(filePath);
        } else {
            handleTextFile(filePath);
        }
    }

    private boolean isIgnored(String filePath) throws IOException {
        if (!FileUtils.exists(IGNORE_FILE)) {
            return false;
        }

        String ignorePatterns = FileUtils.readFile(IGNORE_FILE);
        String[] patterns = ignorePatterns.split("\n");
        for (String pattern : patterns) {
            pattern = pattern.trim();
            if (pattern.isEmpty() || pattern.startsWith("#")) {
                continue;
            }
            if (filePath.matches(globToRegex(pattern))) {
                return true;
            }
        }
        return false;
    }

    private String globToRegex(String glob) {
        StringBuilder regex = new StringBuilder("^");
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            switch (c) {
                case '*':
                    regex.append(".*");
                    break;
                case '?':
                    regex.append(".");
                    break;
                case '.':
                    regex.append("\\.");
                    break;
                case '/':
                    regex.append("\\/");
                    break;
                default:
                    regex.append(c);
            }
        }
        regex.append("$");
        return regex.toString();
    }

    private void handleBinaryFile(String filePath) throws IOException {
        System.out.println("Adding binary file " + filePath);
        byte[] content = Files.readAllBytes(Paths.get(filePath));
        String blobHash = HashUtils.sha1(content);

        // Save binary blob
        String blobPath = BLOBS_DIR + "/" + blobHash;
        if (!FileUtils.exists(blobPath)) {
            FileUtils.writeBinaryFile(blobPath, content);
        }

        // Update index
        updateIndex(filePath, blobHash);
    }

    private void handleTextFile(String filePath) throws IOException {
        byte[] content = Files.readAllBytes(Paths.get(filePath));
        String blobHash = HashUtils.sha1(content);

        // Save text blob
        String blobPath = BLOBS_DIR + "/" + blobHash;
        if (!FileUtils.exists(blobPath)) {
            FileUtils.writeToFile(blobPath, new String(content));
        }

        // Update index
        updateIndex(filePath, blobHash);

        System.out.println("Added " + filePath);
    }

    private void updateIndex(String filePath, String blobHash) throws IOException {
        Map<String, String> indexEntries = loadIndex();
        indexEntries.put(filePath, blobHash);
        saveIndex(indexEntries);
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

    private boolean isBinaryFile(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        int binaryThreshold = 512; // Number of bytes to check

        for (int i = 0; i < Math.min(bytes.length, binaryThreshold); i++) {
            byte b = bytes[i];
            if (b == 0) {
                return true; // Null byte detected, likely binary
            }
        }
        return false;
    }
}
