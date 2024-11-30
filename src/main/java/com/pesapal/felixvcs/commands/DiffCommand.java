package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.core.Commit;
import com.pesapal.felixvcs.core.Tree;
import com.pesapal.felixvcs.utils.FileUtils;

import java.io.IOException;
import java.util.*;

/**
 * Command to compare the differences between two commits in a FelixVersionControl repository.
 * This command identifies added, deleted, and modified files between two commits.
 */
public class DiffCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String COMMITS_DIR = VCS_DIR + "/commits";
    private static final String TREES_DIR = VCS_DIR + "/trees";

    /**
     * Executes the diff command to compare two commits.
     *
     * @param args Command-line arguments (expects two commit hashes).
     * @throws IOException If an I/O error occurs during execution.
     */
    public void execute(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: diff <commit1> <commit2>");
            return;
        }

        String commitHash1 = args[1];
        String commitHash2 = args[2];

        // Validate that the commits exist
        if (!validateCommit(commitHash1) || !validateCommit(commitHash2)) {
            return;
        }

        // Load trees for the commits
        Tree tree1 = loadTree(commitHash1);
        Tree tree2 = loadTree(commitHash2);

        // Generate and display the differences
        displayDiffs(tree1, tree2);
    }

    /**
     * Validates the existence of a commit.
     *
     * @param commitHash The commit hash to validate.
     * @return True if the commit exists, false otherwise.
     * @throws IOException If an I/O error occurs during validation.
     */
    private boolean validateCommit(String commitHash) throws IOException {
        if (!FileUtils.exists(COMMITS_DIR + "/" + commitHash)) {
            System.out.println("Commit " + commitHash + " does not exist.");
            return false;
        }
        return true;
    }

    /**
     * Loads the tree object associated with a given commit hash.
     *
     * @param commitHash The commit hash.
     * @return The Tree object representing the file structure of the commit.
     * @throws IOException If an I/O error occurs during tree loading.
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
     * Displays the differences between two trees.
     *
     * @param tree1 The first tree (source commit).
     * @param tree2 The second tree (target commit).
     * @throws IOException If an I/O error occurs during file reading.
     */
    private void displayDiffs(Tree tree1, Tree tree2) throws IOException {
        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(tree1.getFiles().keySet());
        allFiles.addAll(tree2.getFiles().keySet());

        for (String file : allFiles) {
            String blob1 = tree1.getFiles().get(file);
            String blob2 = tree2.getFiles().get(file);

            if (blob1 == null) {
                System.out.println("File added: " + file);
            } else if (blob2 == null) {
                System.out.println("File deleted: " + file);
            } else if (!blob1.equals(blob2)) {
                displayFileDiff(file, blob1, blob2);
            }
        }
    }

    /**
     * Displays the differences between two versions of a file.
     *
     * @param fileName The name of the file.
     * @param blob1    The blob hash of the file in the first tree.
     * @param blob2    The blob hash of the file in the second tree.
     * @throws IOException If an I/O error occurs during file reading.
     */
    private void displayFileDiff(String fileName, String blob1, String blob2) throws IOException {
        String content1 = FileUtils.readFile(TREES_DIR + "/" + blob1);
        String content2 = FileUtils.readFile(TREES_DIR + "/" + blob2);

        boolean isBinary = isBinaryContent(content1) || isBinaryContent(content2);
        List<String> diffs = generateDiff(content1, content2, isBinary);

        System.out.println("Differences in " + fileName + ":");
        for (String diff : diffs) {
            System.out.println(diff);
        }
        System.out.println();
    }

    /**
     * Generates a diff between two file contents.
     *
     * @param content1 The content of the file in the first tree.
     * @param content2 The content of the file in the second tree.
     * @param isBinary True if the file is binary, false otherwise.
     * @return A list of differences.
     */
    private List<String> generateDiff(String content1, String content2, boolean isBinary) {
        List<String> diffs = new ArrayList<>();
        if (isBinary) {
            diffs.add("Binary files differ.");
            return diffs;
        }

        String[] lines1 = content1.split("\n");
        String[] lines2 = content2.split("\n");

        int max = Math.max(lines1.length, lines2.length);
        for (int i = 0; i < max; i++) {
            String line1 = i < lines1.length ? lines1[i] : "";
            String line2 = i < lines2.length ? lines2[i] : "";

            if (!line1.equals(line2)) {
                if (!line1.isEmpty()) {
                    diffs.add("- " + line1);
                }
                if (!line2.isEmpty()) {
                    diffs.add("+ " + line2);
                }
            }
        }

        return diffs;
    }

    /**
     * Determines if a file content is binary.
     *
     * @param content The file content to check.
     * @return True if the content is binary, false otherwise.
     */
    private boolean isBinaryContent(String content) {
        return content.contains("\0");
    }
}
