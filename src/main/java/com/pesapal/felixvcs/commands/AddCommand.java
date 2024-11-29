package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.utils.FileUtils;
import com.pesapal.felixvcs.utils.HashUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

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

        // Read file content
        byte[] content = Files.readAllBytes(Paths.get(filePath));
        String blobHash = HashUtils.sha1(content);

        // Save blob
        String blobPath = BLOBS_DIR + "/" + blobHash;
        if (!FileUtils.exists(blobPath)) {
            FileUtils.writeToFile(blobPath, new String(content));
        }

        // Update index
        Set<String> indexEntries = loadIndex();
        indexEntries.add(filePath + ":" + blobHash);
        saveIndex(indexEntries);

        System.out.println("Added " + filePath);
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

    private Set<String> loadIndex() throws IOException {
        Set<String> indexEntries = new HashSet<>();
        if (FileUtils.exists(INDEX_FILE)) {
            String content = FileUtils.readFile(INDEX_FILE);
            String[] entries = content.split("\n");
            for (String entry : entries) {
                if (!entry.trim().isEmpty()) {
                    indexEntries.add(entry.trim());
                }
            }
        }
        return indexEntries;
    }

    private void saveIndex(Set<String> indexEntries) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String entry : indexEntries) {
            sb.append(entry).append("\n");
        }
        FileUtils.writeToFile(INDEX_FILE, sb.toString());
    }
}
