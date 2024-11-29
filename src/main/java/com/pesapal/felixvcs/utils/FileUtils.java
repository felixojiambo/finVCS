package com.pesapal.felixvcs.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {
    public static boolean exists(String path) {
        return Files.exists(Paths.get(path));
    }

    public static void createDirectory(String path) throws IOException {
        Files.createDirectories(Paths.get(path));
    }

    public static void writeToFile(String path, String content) throws IOException {
        Files.write(Paths.get(path), content.getBytes());
    }

    public static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    public static String getAbsolutePath(String path) {
        return new File(path).getAbsolutePath();
    }
}
