package com.pesapal.felixvcs.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class FileUtils {
    public static boolean exists(String path) {
        return Files.exists(Paths.get(path));
    }

    public static void createDirectory(String path) throws IOException {
        Files.createDirectories(Paths.get(path));
    }

    public static void writeToFile(String path, String content) throws IOException {
        Path filePath = Paths.get(path);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(content);
        }
    }

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

    public static String getAbsolutePath(String path) {
        return new File(path).getAbsolutePath();
    }
    private static Map<String, String> commitCache = new HashMap<>();

    public static String readCommit(String commitHash) throws IOException {
        if (commitCache.containsKey(commitHash)) {
            return commitCache.get(commitHash);
        }
        String commitPath = ".felixvcs/commits/" + commitHash;
        String commitJson = readFile(commitPath);
        commitCache.put(commitHash, commitJson);
        return commitJson;
    }
}
