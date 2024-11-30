package com.pesapal.felixvcs.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileUtils {
    /**
     * Checks if the given path exists.
     *
     * @param path The file or directory path.
     * @return True if the path exists, false otherwise.
     */
    public static boolean exists(String path) {
        return Files.exists(Paths.get(path));
    }

    /**
     * Creates a directory (and any necessary parent directories) at the specified path.
     *
     * @param path The directory path to create.
     * @throws IOException If an I/O error occurs.
     */
    public static void createDirectory(String path) throws IOException {
        Files.createDirectories(Paths.get(path));
    }

    /**
     * Writes text content to a file, creating or truncating it as necessary.
     *
     * @param path    The file path.
     * @param content The text content to write.
     * @throws IOException If an I/O error occurs.
     */
    public static void writeToFile(String path, String content) throws IOException {
        Path filePath = Paths.get(path);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(content);
        }
    }

    /**
     * Writes binary content to a file, creating or truncating it as necessary.
     *
     * @param path    The file path.
     * @param content The binary content to write.
     * @throws IOException If an I/O error occurs.
     */
    public static void writeBinaryFile(String path, byte[] content) throws IOException {
        Path filePath = Paths.get(path);
        Files.write(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Reads text content from a file.
     *
     * @param path The file path.
     * @return The text content of the file.
     * @throws IOException If an I/O error occurs.
     */
    public static String readFile(String path) throws IOException {
        Path filePath = Paths.get(path);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Gets the absolute path of the given relative path.
     *
     * @param path The relative path.
     * @return The absolute path as a string.
     */
    public static String getAbsolutePath(String path) {
        return new File(path).getAbsolutePath();
    }

    /**
     * Deletes the file at the specified path.
     *
     * @param path The file path to delete.
     * @throws IOException If an I/O error occurs or the file does not exist.
     */
    public static void deleteFile(String path) throws IOException {
        Path filePath = Paths.get(path);
        if (!Files.exists(filePath)) {
            throw new IOException("File " + path + " does not exist and cannot be deleted.");
        }
        Files.delete(filePath);
    }

    /**
     * Lists all files (not directories) within the specified directory.
     *
     * @param directoryPath The directory path.
     * @return A list of file names within the directory.
     * @throws IOException If an I/O error occurs or the path is not a directory.
     */
    public static List<String> listFiles(String directoryPath) throws IOException {
        Path dirPath = Paths.get(directoryPath);
        if (!Files.exists(dirPath)) {
            throw new IOException("Directory " + directoryPath + " does not exist.");
        }
        if (!Files.isDirectory(dirPath)) {
            throw new IOException("Path " + directoryPath + " is not a directory.");
        }

        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    fileNames.add(entry.getFileName().toString());
                }
            }
        }
        return fileNames;
    }

    /**
     * Recursively copies a directory from sourcePath to destinationPath.
     * Excludes any paths specified in the excludePaths set.
     *
     * @param sourcePath      The source directory path.
     * @param destinationPath The destination directory path.
     * @param excludePaths    A set of relative paths to exclude from copying.
     * @throws IOException If an I/O error occurs during copying.
     */
    public static void copyDirectory(String sourcePath, String destinationPath, Set<String> excludePaths) throws IOException {
        Path source = Paths.get(sourcePath);
        Path destination = Paths.get(destinationPath);

        if (!Files.exists(source)) {
            throw new IOException("Source directory " + sourcePath + " does not exist.");
        }
        if (!Files.isDirectory(source)) {
            throw new IOException("Source path " + sourcePath + " is not a directory.");
        }

        // Create destination directory if it doesn't exist
        if (!Files.exists(destination)) {
            Files.createDirectories(destination);
        }

        // Walk through the file tree and copy files/directories
        Files.walk(source)
                .forEach(src -> {
                    Path relativePath = source.relativize(src);
                    String relativePathStr = relativePath.toString();

                    // Check if the path should be excluded
                    for (String exclude : excludePaths) {
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
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Error copying " + src + " to " + dest, e);
                    }
                });
    }
}