package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles the removal of files from the version control system.
 * This includes untracking files and optionally deleting them from the working directory.
 */
public class RemoveCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String INDEX_FILE = VCS_DIR + "/index";

    /**
     * Executes the remove command to untrack and optionally delete a file.
     *
     * @param filePath The path of the file to remove.
     * @throws IOException If an I/O error occurs during execution.
     */
    public void execute(String filePath) throws IOException {
        // Check if repository is initialized
        if (!FileUtils.exists(VCS_DIR)) {
            System.out.println("Not a FelixVersionControl repository. Use 'init' to initialize.");
            return;
        }

        // Load the current index
        Set<String> indexEntries = loadIndex();
        String entryToRemove = null;

        // Locate the file in the index
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

        // Remove the file entry from the index
        indexEntries.remove(entryToRemove);
        saveIndex(indexEntries);

        // Delete the file from the working directory
        Files.deleteIfExists(Paths.get(filePath));

        System.out.println("Removed " + filePath);
    }

    /**
     * Loads the index file and retrieves a set of tracked file entries.
     *
     * @return A set of file entries from the index.
     * @throws IOException If an I/O error occurs while reading the index file.
     */
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

    /**
     * Saves the updated index back to the index file.
     *
     * @param indexEntries The updated set of file entries to save.
     * @throws IOException If an I/O error occurs while writing to the index file.
     */
    private void saveIndex(Set<String> indexEntries) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String entry : indexEntries) {
            sb.append(entry).append("\n");
        }
        FileUtils.writeToFile(INDEX_FILE, sb.toString());
    }
}
