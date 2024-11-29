package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.core.Commit;
import com.pesapal.felixvcs.core.Tree;
import com.pesapal.felixvcs.utils.FileUtils;
import com.pesapal.felixvcs.utils.HashUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CommitCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String COMMITS_DIR = VCS_DIR + "/commits";
    private static final String BLOBS_DIR = VCS_DIR + "/blobs";
    private static final String TREES_DIR = VCS_DIR + "/trees";
    private static final String HEAD_FILE = VCS_DIR + "/HEAD";
    private static final String INDEX_FILE = VCS_DIR + "/index";

    public void execute(String message) throws IOException {
        // Check if repository is initialized
        if (!FileUtils.exists(VCS_DIR)) {
            System.out.println("Not a FelixVersionControl repository. Use 'init' to initialize.");
            return;
        }

        // Load current HEAD
        String headRef = FileUtils.readFile(HEAD_FILE).trim();
        String currentBranch = headRef.replace("refs/heads/", "");
        String parentCommitHash = FileUtils.readFile(VCS_DIR + "/refs/heads/" + currentBranch).trim();

        // Load staged files
        Map<String, String> stagedFiles = loadIndex();

        if (stagedFiles.isEmpty()) {
            System.out.println("No changes added to commit.");
            return;
        }

        // Build tree object
        Tree tree = new Tree(stagedFiles);
        String treeJson = tree.toJson();
        String treeHash = HashUtils.sha1(treeJson.getBytes());

        // Save tree
        String treePath = TREES_DIR + "/" + treeHash;
        if (!FileUtils.exists(treePath)) {
            FileUtils.writeToFile(treePath, treeJson);
        }

        // Create commit object
        Commit commit = new Commit();
        commit.setTree(treeHash);
        commit.setParent(parentCommitHash.isEmpty() ? null : parentCommitHash);
        commit.setMessage(message);

        String timestamp = DateTimeFormatter.ISO_INSTANT
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
        commit.setTimestamp(timestamp);

        // Dynamic author retrieval
        String author = System.getenv("USER_NAME");
        if (author == null || author.isEmpty()) {
            author = "Unknown Author"; // Or implement another dynamic retrieval method
        }
        commit.setAuthor(author);

        // Serialize commit
        String commitJson = commit.toJson();
        String commitHash = HashUtils.sha1(commitJson.getBytes());

        // Save commit
        String commitPath = COMMITS_DIR + "/" + commitHash;
        FileUtils.writeToFile(commitPath, commitJson);

        // Update current branch
        FileUtils.writeToFile(VCS_DIR + "/refs/heads/" + currentBranch, commitHash);

        // Clear index
        FileUtils.writeToFile(INDEX_FILE, "");

        System.out.println("[" + currentBranch + " " + commitHash + "] " + message);
    }

    private Map<String, String> loadIndex() throws IOException {
        Map<String, String> stagedFiles = new HashMap<>();
        if (FileUtils.exists(INDEX_FILE)) {
            String content = FileUtils.readFile(INDEX_FILE);
            String[] entries = content.split("\n");
            for (String entry : entries) {
                if (!entry.trim().isEmpty()) {
                    String[] parts = entry.split(":");
                    if (parts.length == 2) {
                        stagedFiles.put(parts[0], parts[1]);
                    }
                }
            }
        }
        return stagedFiles;
    }
}
