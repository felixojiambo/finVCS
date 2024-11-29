package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class CloneCommand {
    private static final String VCS_DIR = ".felixvcs";

    public void execute(String sourcePath, String destinationPath) throws IOException {
        if (!FileUtils.exists(sourcePath + "/" + VCS_DIR)) {
            System.out.println("Source path is not a FelixVersionControl repository.");
            return;
        }

        if (FileUtils.exists(destinationPath + "/" + VCS_DIR)) {
            System.out.println("Destination path already has a FelixVersionControl repository.");
            return;
        }

        // Copy .felixvcs directory
        Path sourceVcs = Path.of(sourcePath, VCS_DIR);
        Path destinationVcs = Path.of(destinationPath, VCS_DIR);
        Files.walk(sourceVcs)
                .forEach(source -> {
                    Path destination = destinationVcs.resolve(sourceVcs.relativize(source));
                    try {
                        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        // Optionally, copy working directory files
        Files.walk(Path.of(sourcePath))
                .filter(path -> !path.startsWith(sourceVcs))
                .forEach(source -> {
                    Path destination = Path.of(destinationPath).resolve(sourcePath).relativize(source);
                    try {
                        if (Files.isDirectory(source)) {
                            Files.createDirectories(destination);
                        } else {
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        System.out.println("Cloned repository from " + sourcePath + " to " + destinationPath);
    }
}
