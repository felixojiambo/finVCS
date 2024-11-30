package com.pesapal.felixvcs;

import com.pesapal.felixvcs.commands.*;

import java.util.Arrays;

/**
 * Entry point for the Felix Version Control System (FelixVCS).
 * This class handles command-line arguments and dispatches
 * them to the appropriate command implementation.
 */
public class MainApplication {

    /**
     * Main method that processes command-line arguments and executes the appropriate command.
     *
     * @param args Command-line arguments where the first argument is the command name,
     *             and subsequent arguments are command-specific parameters.
     */
    public static void main(String[] args) {
        // Check if any arguments are provided
        if (args.length == 0) {
            System.out.println("Please provide a command.");
            return;
        }

        // Extract the command name
        String command = args[0];

        // Switch case to handle different commands
        switch (command) {
            case "init":
                handleInitCommand();
                break;

            case "add":
                handleAddCommand(args);
                break;

            case "commit":
                handleCommitCommand(args);
                break;

            case "log":
                handleLogCommand();
                break;

            case "branch":
                handleBranchCommand(args);
                break;

            case "merge":
                handleMergeCommand(args);
                break;

            case "diff":
                handleDiffCommand(args);
                break;

            case "clone":
                handleCloneCommand(args);
                break;

            case "remove":
                handleRemoveCommand(args);
                break;

            case "tag":
                handleTagCommand(args);
                break;

            case "stash":
                handleStashCommand(args);
                break;

            case "rebase":
                handleRebaseCommand(args);
                break;

            default:
                // Print an error message for unknown commands
                System.out.println("Unknown command: " + command);
        }
    }

    /**
     * Handles the "init" command to initialize a new repository.
     */
    private static void handleInitCommand() {
        InitCommand init = new InitCommand();
        try {
            init.execute();
        } catch (Exception e) {
            System.out.println("Error initializing repository: " + e.getMessage());
        }
    }

    /**
     * Handles the "add" command to stage a file for committing.
     *
     * @param args Command-line arguments.
     */
    private static void handleAddCommand(String[] args) {
        if (args.length < 2) {
            System.out.println("Please provide a file to add.");
            return;
        }
        String filePath = args[1];
        AddCommand add = new AddCommand();
        try {
            add.execute(filePath);
        } catch (Exception e) {
            System.out.println("Error adding file: " + e.getMessage());
        }
    }

    /**
     * Handles the "commit" command to save changes in the repository.
     *
     * @param args Command-line arguments.
     */
    private static void handleCommitCommand(String[] args) {
        if (args.length < 2) {
            System.out.println("Please provide a commit message.");
            return;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        CommitCommand commit = new CommitCommand();
        try {
            commit.execute(message);
        } catch (Exception e) {
            System.out.println("Error committing changes: " + e.getMessage());
        }
    }

    /**
     * Handles the "log" command to display the commit history.
     */
    private static void handleLogCommand() {
        LogCommand log = new LogCommand();
        try {
            log.execute();
        } catch (Exception e) {
            System.out.println("Error displaying log: " + e.getMessage());
        }
    }

    /**
     * Handles the "branch" command to manage branches.
     *
     * @param args Command-line arguments.
     */
    private static void handleBranchCommand(String[] args) {
        BranchCommand branch = new BranchCommand();
        try {
            branch.execute(Arrays.copyOfRange(args, 1, args.length));
        } catch (Exception e) {
            System.out.println("Error handling branch command: " + e.getMessage());
        }
    }

    /**
     * Handles the "merge" command to merge branches.
     *
     * @param args Command-line arguments.
     */
    private static void handleMergeCommand(String[] args) {
        if (args.length < 2) {
            System.out.println("Please provide a source branch to merge.");
            return;
        }
        String sourceBranch = args[1];
        MergeCommand merge = new MergeCommand();
        try {
            merge.execute(sourceBranch);
        } catch (Exception e) {
            System.out.println("Error merging branches: " + e.getMessage());
        }
    }

    /**
     * Handles the "diff" command to compare changes between commits.
     *
     * @param args Command-line arguments.
     */
    private static void handleDiffCommand(String[] args) {
        if (args.length < 3) {
            System.out.println("Please provide two commits to diff.");
            return;
        }
        String[] diffArgs = Arrays.copyOfRange(args, 1, args.length);
        DiffCommand diff = new DiffCommand();
        try {
            diff.execute(diffArgs);
        } catch (Exception e) {
            System.out.println("Error executing diff: " + e.getMessage());
        }
    }

    /**
     * Handles the "clone" command to copy an existing repository to a new location.
     *
     * @param args Command-line arguments.
     */
    private static void handleCloneCommand(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: clone <source-repo-path> <destination-path>");
            return;
        }
        String sourcePath = args[1];
        String destinationPath = args[2];
        CloneCommand clone = new CloneCommand();
        try {
            clone.execute(sourcePath, destinationPath);
        } catch (Exception e) {
            System.out.println("Error cloning repository: " + e.getMessage());
        }
    }

    /**
     * Handles the "remove" command to unstage a file or remove it from the repository.
     *
     * @param args Command-line arguments.
     */
    private static void handleRemoveCommand(String[] args) {
        if (args.length < 2) {
            System.out.println("Please provide a file to remove.");
            return;
        }
        String removeFilePath = args[1];
        RemoveCommand remove = new RemoveCommand();
        try {
            remove.execute(removeFilePath);
        } catch (Exception e) {
            System.out.println("Error removing file: " + e.getMessage());
        }
    }

    /**
     * Handles the "tag" command to create or manage tags.
     *
     * @param args Command-line arguments.
     */
    private static void handleTagCommand(String[] args) {
        TagCommand tag = new TagCommand();
        try {
            tag.execute(Arrays.copyOfRange(args, 1, args.length));
        } catch (Exception e) {
            System.out.println("Error handling tag command: " + e.getMessage());
        }
    }

    /**
     * Handles the "stash" command to temporarily save changes.
     *
     * @param args Command-line arguments.
     */
    private static void handleStashCommand(String[] args) {
        StashCommand stash = new StashCommand();
        try {
            String[] stashArgs = Arrays.copyOfRange(args, 1, args.length);
            stash.execute(stashArgs);
        } catch (Exception e) {
            System.out.println("Error handling stash command: " + e.getMessage());
        }
    }

    /**
     * Handles the "rebase" command to reapply commits on top of another branch.
     *
     * @param args Command-line arguments.
     */
    private static void handleRebaseCommand(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: rebase <target-branch>");
            return;
        }
        RebaseCommand rebase = new RebaseCommand();
        try {
            rebase.execute(Arrays.copyOfRange(args, 1, args.length));
        } catch (Exception e) {
            System.out.println("Error handling rebase command: " + e.getMessage());
        }
    }
}
