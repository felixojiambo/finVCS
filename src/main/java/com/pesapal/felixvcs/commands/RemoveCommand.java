package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.utils.FileUtils;
import com.pesapal.felixvcs.utils.HashUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class RemoveCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String BLOBS_DIR = VCS_DIR + "/blobs";
    private static final String INDEX_FILE = VCS_DIR + "/index";

    public void execute(String filePath) throws IOException {
        // Check if repository is initialized
        if (!FileUtils.exists(VCS_DIR)) {
            System.out.println("Not a FelixVersionControl repository. Use 'init' to initialize.");
            return;
        }

        // Check if file exists in index
        Set<String> indexEntries = loadIndex();
        String entryToRemove = null;
        for (String entry : indexEntries) {
            if (entry.startsWith(filePath + ":")) {
                entryToRemove = entry;
                break;
            }
        }

        if (entryToRemove == null) {
            System.out.println("File " + filePath + " is not tracked.");
            return;
        }

        // Remove from index
        indexEntries.remove(entryToRemove);
        saveIndex(indexEntries);

        // Delete the file from working directory
        Files.deleteIfExists(Paths.get(filePath));

        System.out.println("Removed " + filePath);
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
