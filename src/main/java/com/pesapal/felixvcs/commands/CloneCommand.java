package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.utils.FileUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CloneCommand {
    private static final String VCS_DIR = ".felixvcs";

    /**
     * Executes the clone operation by copying the repository data and the working directory.
     *
     * @param sourcePath      The path to the source repository.
     * @param destinationPath The path where the repository should be cloned.
     * @throws IOException If an I/O error occurs during cloning.
     */
    public void execute(String sourcePath, String destinationPath) throws IOException {
        String sourceVcsPath = sourcePath + "/" + VCS_DIR;
        String destinationVcsPath = destinationPath + "/" + VCS_DIR;

        // Validate source repository
        if (!FileUtils.exists(sourceVcsPath)) {
            System.out.println("Source path is not a FelixVersionControl repository.");
            return;
        }

        // Check if destination already has a repository
        if (FileUtils.exists(destinationVcsPath)) {
            System.out.println("Destination path already has a FelixVersionControl repository.");
            return;
        }

        // Define paths to exclude during working directory copy
        Set<String> excludePaths = new HashSet<>();
        excludePaths.add(VCS_DIR); // Exclude .felixvcs directory

        try {
            // Clone working directory files excluding .felixvcs
            System.out.println("Cloning working directory...");
            FileUtils.copyDirectory(sourcePath, destinationPath, excludePaths);

            // Clone .felixvcs directory
            System.out.println("Cloning repository data...");
            FileUtils.copyDirectory(sourceVcsPath, destinationVcsPath, Set.of());

            System.out.println("Successfully cloned repository from " + sourcePath + " to " + destinationPath);
        } catch (RuntimeException e) {
            System.err.println("Error during cloning: " + e.getMessage());
            // Optionally, implement rollback logic here
        }
    }
}
