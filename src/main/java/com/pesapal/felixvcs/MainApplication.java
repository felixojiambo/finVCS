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
            default:
                System.out.println("Unknown command: " + command);
        }
    }
}
