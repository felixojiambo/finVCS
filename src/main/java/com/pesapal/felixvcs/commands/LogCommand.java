package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.core.Commit;
import com.pesapal.felixvcs.utils.FileUtils;

import java.io.IOException;

/**
 * Command to display the commit history of the current branch.
 */
public class LogCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String COMMITS_DIR = VCS_DIR + "/commits";
    private static final String HEAD_FILE = VCS_DIR + "/HEAD";

    /**
     * Executes the log command to display the commit history.
     *
     * @throws IOException If an I/O error occurs while accessing the repository files.
     */
    public void execute() throws IOException {
        // Check if the repository is initialized
        if (!FileUtils.exists(VCS_DIR)) {
            System.out.println("Not a FelixVersionControl repository. Use 'init' to initialize.");
            return;
        }

        // Load the current branch from HEAD
        String headRef = FileUtils.readFile(HEAD_FILE).trim();
        String currentBranch = headRef.replace("refs/heads/", "");
        String commitHash = FileUtils.readFile(VCS_DIR + "/refs/heads/" + currentBranch).trim();

        if (commitHash.isEmpty()) {
            System.out.println("No commits yet.");
            return;
        }

        // Traverse and display commit history
        displayCommitHistory(commitHash);
    }

    /**
     * Traverses and displays the commit history starting from the given commit hash.
     *
     * @param commitHash The starting commit hash.
     * @throws IOException If an I/O error occurs while reading commit files.
     */
    private void displayCommitHistory(String commitHash) throws IOException {
        while (commitHash != null && !commitHash.isEmpty()) {
            String commitPath = COMMITS_DIR + "/" + commitHash;

            if (!FileUtils.exists(commitPath)) {
                System.out.println("Commit " + commitHash + " not found.");
                break;
            }

            // Load and parse the commit
            String commitJson = FileUtils.readFile(commitPath);
            Commit commit = Commit.fromJson(commitJson);

            // Print commit details
            printCommitDetails(commitHash, commit);

            // Move to the parent commit
            commitHash = commit.getParent();
        }
    }

    /**
     * Prints the details of a commit.
     *
     * @param commitHash The hash of the commit.
     * @param commit     The commit object containing details.
     */
    private void printCommitDetails(String commitHash, Commit commit) {
        System.out.println("Commit: " + commitHash);
        System.out.println("Author: " + commit.getAuthor());
        System.out.println("Date: " + commit.getTimestamp());
        System.out.println("\n    " + commit.getMessage() + "\n");
    }
}
