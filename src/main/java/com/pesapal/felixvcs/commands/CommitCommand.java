package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.core.Commit;
import com.pesapal.felixvcs.core.Tree;
import com.pesapal.felixvcs.utils.FileUtils;
import com.pesapal.felixvcs.utils.HashUtils;
import com.pesapal.felixvcs.utils.ProgressListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the commit operation in the FelixVersionControl repository.
 * Commits changes from the staging area (index) and records them in the repository.
 */
public class CommitCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String COMMITS_DIR = VCS_DIR + "/commits";
    private static final String TREES_DIR = VCS_DIR + "/trees";
    private static final String HEAD_FILE = VCS_DIR + "/HEAD";
    private static final String INDEX_FILE = VCS_DIR + "/index";

    /**
     * Executes the commit operation.
     *
     * @param message The commit message describing the changes.
     * @throws IOException If an I/O error occurs during the commit process.
     */
    public void execute(String message) throws IOException {
        // Ensure the repository is initialized
        if (!FileUtils.exists(VCS_DIR)) {
            System.out.println("Not a FelixVersionControl repository. Use 'init' to initialize.");
            return;
        }

        // Load HEAD reference and parent commit
        String headRef = FileUtils.readFile(HEAD_FILE).trim();
        String currentBranch = headRef.replace("refs/heads/", "");
        String parentCommitHash = FileUtils.readFile(VCS_DIR + "/refs/heads/" + currentBranch).trim();

        // Load staged files from the index
        Map<String, String> stagedFiles = loadIndex();

        if (stagedFiles.isEmpty()) {
            System.out.println("No changes added to commit.");
            return;
        }

        // Detect deletions by comparing with the previous tree
        Map<String, String> previousTree = parentCommitHash.isEmpty() ? new HashMap<>() : loadTree(parentCommitHash).getFiles();
        for (String file : previousTree.keySet()) {
            if (!stagedFiles.containsKey(file)) {
                stagedFiles.put(file, null); // Mark as deleted
            }
        }

        // Build the tree object and update progress
        Tree tree = buildTree(stagedFiles);

        // Save the tree and generate its hash
        String treeJson = tree.toJson();
        String treeHash = HashUtils.sha1(treeJson.getBytes());
        saveTree(treeHash, treeJson);

        // Create the commit object
        Commit commit = createCommit(treeHash, parentCommitHash, message);

        // Serialize and save the commit
        String commitJson = commit.toJson();
        String commitHash = HashUtils.sha1(commitJson.getBytes());
        saveCommit(commitHash, commitJson);

        // Update the branch reference to the new commit
        FileUtils.writeToFile(VCS_DIR + "/refs/heads/" + currentBranch, commitHash);

        // Clear the staging area (index)
        FileUtils.writeToFile(INDEX_FILE, "");

        System.out.println("\n[" + currentBranch + " " + commitHash + "] " + message);
    }

    /**
     * Loads the staged files from the index.
     *
     * @return A map of file paths to their blob hashes.
     * @throws IOException If an I/O error occurs during reading.
     */
    private Map<String, String> loadIndex() throws IOException {
        Map<String, String> stagedFiles = new HashMap<>();
        if (FileUtils.exists(INDEX_FILE)) {
            String content = FileUtils.readFile(INDEX_FILE);
            String[] entries = content.split("\n");
            for (String entry : entries) {
                if (!entry.trim().isEmpty()) {
                    String[] parts = entry.split(":", 2);
                    if (parts.length == 2) {
                        stagedFiles.put(parts[0], parts[1]);
                    }
                }
            }
        }
        return stagedFiles;
    }

    /**
     * Loads a tree object based on the commit hash.
     *
     * @param commitHash The commit hash.
     * @return The Tree object associated with the commit.
     * @throws IOException If an I/O error occurs during reading.
     */
    private Tree loadTree(String commitHash) throws IOException {
        String commitPath = COMMITS_DIR + "/" + commitHash;
        String commitJson = FileUtils.readFile(commitPath);
        Commit commit = Commit.fromJson(commitJson);
        String treeHash = commit.getTree();
        String treePath = TREES_DIR + "/" + treeHash;
        String treeJson = FileUtils.readFile(treePath);
        return Tree.fromJson(treeJson);
    }

    /**
     * Builds a tree object from staged files and displays progress.
     *
     * @param stagedFiles A map of file paths to their blob hashes.
     * @return The constructed Tree object.
     */
    private Tree buildTree(Map<String, String> stagedFiles) {
        Tree tree = new Tree();
        Map<String, String> filesInTree = new HashMap<>();
        long totalFiles = stagedFiles.size();
        long processedFiles = 0;

        ProgressListener listener = (completed, total) -> {
            int progress = (int) ((completed * 100) / total);
            System.out.print("\rProcessing files: " + progress + "%");
        };

        for (Map.Entry<String, String> entry : stagedFiles.entrySet()) {
            String filePath = entry.getKey();
            String blobHash = entry.getValue();

            if (blobHash != null) {
                filesInTree.put(filePath, blobHash);
            }

            processedFiles++;
            listener.update(processedFiles, totalFiles);
        }

        tree.setFiles(filesInTree);
        return tree;
    }

    /**
     * Saves the tree to the repository.
     *
     * @param treeHash The hash of the tree.
     * @param treeJson The JSON representation of the tree.
     * @throws IOException If an I/O error occurs during saving.
     */
    private void saveTree(String treeHash, String treeJson) throws IOException {
        String treePath = TREES_DIR + "/" + treeHash;
        if (!FileUtils.exists(treePath)) {
            FileUtils.writeToFile(treePath, treeJson);
        }
    }

    /**
     * Creates a commit object with the given parameters.
     *
     * @param treeHash          The hash of the tree.
     * @param parentCommitHash  The hash of the parent commit (if any).
     * @param message           The commit message.
     * @return A new Commit object.
     */
    private Commit createCommit(String treeHash, String parentCommitHash, String message) {
        Commit commit = new Commit();
        commit.setTree(treeHash);
        commit.setParent(parentCommitHash.isEmpty() ? null : parentCommitHash);
        commit.setMessage(message);

        String timestamp = DateTimeFormatter.ISO_INSTANT
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
        commit.setTimestamp(timestamp);

        String author = System.getProperty("user.name");
        commit.setAuthor(author == null || author.isEmpty() ? "Unknown Author" : author);

        return commit;
    }

    /**
     * Saves a commit to the repository.
     *
     * @param commitHash The hash of the commit.
     * @param commitJson The JSON representation of the commit.
     * @throws IOException If an I/O error occurs during saving.
     */
    private void saveCommit(String commitHash, String commitJson) throws IOException {
        String commitPath = COMMITS_DIR + "/" + commitHash;
        FileUtils.writeToFile(commitPath, commitJson);
    }
}
