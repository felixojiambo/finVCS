package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.core.Commit;
import com.pesapal.felixvcs.utils.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LogCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String COMMITS_DIR = VCS_DIR + "/commits";
    private static final String HEAD_FILE = VCS_DIR + "/HEAD";

    private ObjectMapper objectMapper = new ObjectMapper();

    public void execute() throws IOException {
        // Check if repository is initialized
        if (!FileUtils.exists(VCS_DIR)) {
            System.out.println("Not a FelixVersionControl repository. Use 'init' to initialize.");
            return;
        }

        // Load current HEAD
        String headRef = FileUtils.readFile(HEAD_FILE).trim();
        String currentBranch = headRef.replace("refs/heads/", "");
        String commitHash = FileUtils.readFile(VCS_DIR + "/refs/heads/" + currentBranch).trim();

        if (commitHash.isEmpty()) {
            System.out.println("No commits yet.");
            return;
        }

        // Traverse commits
        while (!commitHash.isEmpty()) {
            String commitPath = COMMITS_DIR + "/" + commitHash;
            if (!FileUtils.exists(commitPath)) {
                System.out.println("Commit " + commitHash + " not found.");
                break;
            }

            String commitJson = FileUtils.readFile(commitPath);
            Commit commit = objectMapper.readValue(commitJson, Commit.class);

            // Display commit details
            System.out.println("Commit: " + commitHash);
            System.out.println("Author: " + commit.getAuthor());
            System.out.println("Date: " + commit.getTimestamp());
            System.out.println("\n    " + commit.getMessage() + "\n");

            // Move to parent commit
            commitHash = commit.getParent() != null ? commit.getParent() : "";
        }
    }
}
