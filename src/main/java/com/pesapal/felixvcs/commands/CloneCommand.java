package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.utils.FileUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

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
            Path sourcePathObj = Paths.get(sourcePath);
            Path destinationPathObj = Paths.get(destinationPath);

            // Step 1: Count total files to copy
            long totalFiles = countFiles(sourcePathObj, excludePaths);
            if (totalFiles == 0) totalFiles = 1; // Prevent division by zero

            // Step 2: Copy files with progress
            copyDirectoryWithProgress(sourcePathObj, destinationPathObj, excludePaths, totalFiles);

            // Clone .felixvcs directory
            System.out.println("\nCloning repository data...");
            Path sourceVcsPathObj = Paths.get(sourceVcsPath);
            Path destinationVcsPathObj = Paths.get(destinationVcsPath);

            long vcsTotalFiles = countFiles(sourceVcsPathObj, Set.of());
            if (vcsTotalFiles == 0) vcsTotalFiles = 1;

            copyDirectoryWithProgress(sourceVcsPathObj, destinationVcsPathObj, Set.of(), vcsTotalFiles);

            System.out.println("\nSuccessfully cloned repository from " + sourcePath + " to " + destinationPath);
        } catch (RuntimeException e) {
            System.err.println("Error during cloning: " + e.getMessage());
            // Optionally, implement rollback logic here
        }
    }

    /**
     * Counts the total number of files to be copied, excluding specified paths.
     *
     * @param source          The source directory path.
     * @param excludePathsSet Set of relative paths to exclude.
     * @return The total number of files to copy.
     * @throws IOException If an I/O error occurs during counting.
     */
    private long countFiles(Path source, Set<String> excludePathsSet) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        Path relativePath = source.relativize(path);
                        for (String exclude : excludePathsSet) {
                            if (relativePath.toString().startsWith(exclude)) {
                                return false; // Exclude this file
                            }
                        }
                        return true;
                    })
                    .count();
        }
    }

    /**
     * Copies a directory from source to destination while displaying progress.
     *
     * @param source          The source directory path.
     * @param destination     The destination directory path.
     * @param excludePathsSet Set of relative paths to exclude.
     * @param totalFiles      The total number of files to copy.
     */
    private void copyDirectoryWithProgress(Path source, Path destination, Set<String> excludePathsSet, long totalFiles) {
        final long[] copiedFiles = {0};

        try (Stream<Path> paths = Files.walk(source)) {
            paths.forEach(src -> {
                Path relativePath = source.relativize(src);
                String relativePathStr = relativePath.toString();

                // Check if the path should be excluded
                for (String exclude : excludePathsSet) {
                    if (relativePathStr.startsWith(exclude)) {
                        return; // Skip this path
                    }
                }

                Path dest = destination.resolve(relativePath);
                try {
                    if (Files.isDirectory(src)) {
                        if (!Files.exists(dest)) {
                            Files.createDirectories(dest);
                        }
                    } else {
                        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                        copiedFiles[0]++;
                        // Update progress
                        int progress = (int) ((copiedFiles[0] * 100) / totalFiles);
                        System.out.print("\rProgress: " + progress + "%");
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error copying " + src + " to " + dest, e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Error during directory copy with progress.", e);
        }
    }
}
