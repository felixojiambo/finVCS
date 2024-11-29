package com.pesapal.felixvcs;
import com.pesapal.felixvcs.commands.*;

import java.util.Arrays;

public class MainApplication {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide a command.");
            return;
        }

        String command = args[0];

        switch (command) {
            case "init":
                InitCommand init = new InitCommand();
                try {
                    init.execute();
                } catch (Exception e) {
                    System.out.println("Error initializing repository: " + e.getMessage());
                }
                break;
            case "add":
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
                break;
            case "commit":
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
                break;
            case "log":
                LogCommand log = new LogCommand();
                try {
                    log.execute();
                } catch (Exception e) {
                    System.out.println("Error displaying log: " + e.getMessage());
                }
                break;
            case "branch":
                BranchCommand branch = new BranchCommand();
                try {
                    branch.execute(Arrays.copyOfRange(args, 1, args.length));
                } catch (Exception e) {
                    System.out.println("Error handling branch command: " + e.getMessage());
                }
                break;
            case "merge":
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
                break;
            case "diff":
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
                break;
            case "clone":
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
                break;
            case "remove":
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
                break;
            case "tag":
                TagCommand tag = new TagCommand();
                try {
                    tag.execute(Arrays.copyOfRange(args, 1, args.length));
                } catch (Exception e) {
                    System.out.println("Error handling tag command: " + e.getMessage());
                }
                break;
            case "stash":
                StashCommand stash = new StashCommand();
                try {
                    String[] stashArgs = Arrays.copyOfRange(args, 1, args.length);
                    stash.execute(stashArgs);
                } catch (Exception e) {
                    System.out.println("Error handling stash command: " + e.getMessage());
                }
                break;
            default:
                System.out.println("Unknown command: " + command);
        }
    }
}
