package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Handles operations related to branches in the FelixVersionControl repository.
 * This includes listing existing branches and creating new branches.
 */
public class BranchCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String REFS_DIR = VCS_DIR + "/refs/heads";
    private static final String HEAD_FILE = VCS_DIR + "/HEAD";

    /**
     * Executes the branch command based on the provided arguments.
     *
     * @param args The command-line arguments.
     * @throws IOException If an I/O error occurs during execution.
     */
    public void execute(String[] args) throws IOException {
        if (!FileUtils.exists(VCS_DIR)) {
            System.out.println("Not a FelixVersionControl repository. Use 'init' to initialize.");
            return;
        }

        if (args.length == 1) {
            // List all branches
            listBranches();
        } else if (args.length == 2) {
            // Create a new branch
            String branchName = args[1];
            createBranch(branchName);
        } else {
            System.out.println("Usage:");
            System.out.println("  branch               # List all branches");
            System.out.println("  branch <branch-name> # Create a new branch");
        }
    }

    /**
     * Lists all branches in the repository, indicating the current active branch.
     *
     * @throws IOException If an I/O error occurs while listing branches.
     */
    private void listBranches() throws IOException {
        // Retrieve all branch files in the refs/heads directory
        List<String> branches = Files.list(Paths.get(REFS_DIR))
                .map(path -> path.getFileName().toString())
                .toList();

        // Identify the current branch from the HEAD file
        String headRef = FileUtils.readFile(HEAD_FILE).trim();
        String currentBranch = headRef.replace("refs/heads/", "");

        // Display branches, marking the active one with '*'
        for (String branch : branches) {
            if (branch.equals(currentBranch)) {
                System.out.println("* " + branch); // Active branch
            } else {
                System.out.println("  " + branch); // Inactive branches
            }
        }
    }

    /**
     * Creates a new branch pointing to the current HEAD commit.
     *
     * @param branchName The name of the new branch.
     * @throws IOException If an I/O error occurs during branch creation.
     */
    private void createBranch(String branchName) throws IOException {
        // Read the current branch from HEAD
        String headRef = FileUtils.readFile(HEAD_FILE).trim();
        String currentBranch = headRef.replace("refs/heads/", "");

        // Retrieve the latest commit hash of the current branch
        String commitHash = FileUtils.readFile(REFS_DIR + "/" + currentBranch).trim();

        // Construct the path for the new branch reference
        String newBranchPath = REFS_DIR + "/" + branchName;

        // Check if the branch already exists
        if (FileUtils.exists(newBranchPath)) {
            System.out.println("Branch " + branchName + " already exists.");
            return;
        }

        // Write the current commit hash to the new branch file
        FileUtils.writeToFile(newBranchPath, commitHash);

        System.out.println("Branch " + branchName + " created at commit " + commitHash);
    }
}
