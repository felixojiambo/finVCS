package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BranchCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String REFS_DIR = VCS_DIR + "/refs/heads";
    private static final String HEAD_FILE = VCS_DIR + "/HEAD";

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

    private void listBranches() throws IOException {
        List<String> branches = Files.list(Paths.get(REFS_DIR))
                .map(path -> path.getFileName().toString())
                .toList();

        String headRef = FileUtils.readFile(HEAD_FILE).trim();
        String currentBranch = headRef.replace("refs/heads/", "");

        for (String branch : branches) {
            if (branch.equals(currentBranch)) {
                System.out.println("* " + branch);
            } else {
                System.out.println("  " + branch);
            }
        }
    }

    private void createBranch(String branchName) throws IOException {
        String headRef = FileUtils.readFile(HEAD_FILE).trim();
        String currentBranch = headRef.replace("refs/heads/", "");
        String commitHash = FileUtils.readFile(REFS_DIR + "/" + currentBranch).trim();

        String newBranchPath = REFS_DIR + "/" + branchName;
        if (FileUtils.exists(newBranchPath)) {
            System.out.println("Branch " + branchName + " already exists.");
            return;
        }

        FileUtils.writeToFile(newBranchPath, commitHash);
        System.out.println("Branch " + branchName + " created at commit " + commitHash);
    }
}
