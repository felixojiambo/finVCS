package com.pesapal.felixvcs;
import com.pesapal.felixvcs.commands.InitCommand;
import com.pesapal.felixvcs.commands.AddCommand;
import com.pesapal.felixvcs.commands.CommitCommand;
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

            default:
                System.out.println("Unknown command: " + command);
        }
    }
}
