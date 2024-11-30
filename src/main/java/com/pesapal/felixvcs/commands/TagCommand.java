package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Handles operations related to tags in the version control system.
 * A tag is a named reference to a specific commit.
 */
public class TagCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String TAGS_DIR = VCS_DIR + "/refs/tags";
    private static final String HEAD_FILE = VCS_DIR + "/HEAD";

    /**
     * Executes the tag command based on the provided arguments.
     *
     * @param args The command-line arguments.
     *             - No arguments: List all tags.
     *             - Single argument: Create a tag with the provided name.
     * @throws IOException If an I/O error occurs.
     */
    public void execute(String[] args) throws IOException {
        if (!FileUtils.exists(VCS_DIR)) {
            System.out.println("Not a FelixVersionControl repository. Use 'init' to initialize.");
            return;
        }

        if (args.length == 1) {
            listTags(); // List all existing tags
        } else if (args.length == 2) {
            String tagName = args[1];
            createTag(tagName); // Create a new tag
        } else {
            printUsage(); // Print usage instructions
        }
    }

    /**
     * Lists all existing tags in the repository.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void listTags() throws IOException {
        if (!FileUtils.exists(TAGS_DIR)) {
            System.out.println("No tags available.");
            return;
        }

        List<String> tags = Files.list(Paths.get(TAGS_DIR))
                .map(path -> path.getFileName().toString())
                .toList();

        if (tags.isEmpty()) {
            System.out.println("No tags available.");
        } else {
            System.out.println("Tags:");
            tags.forEach(tag -> System.out.println("  " + tag));
        }
    }

    /**
     * Creates a new tag pointing to the current commit.
     *
     * @param tagName The name of the tag to create.
     * @throws IOException If an I/O error occurs.
     */
    private void createTag(String tagName) throws IOException {
        String tagPath = TAGS_DIR + "/" + tagName;

        // Check if the tag already exists
        if (FileUtils.exists(tagPath)) {
            System.out.println("Tag " + tagName + " already exists.");
            return;
        }

        // Get the current commit hash
        String headRef = FileUtils.readFile(HEAD_FILE).trim();
        String currentBranch = headRef.replace("refs/heads/", "");
        String currentCommitHash = FileUtils.readFile(VCS_DIR + "/refs/heads/" + currentBranch).trim();

        if (currentCommitHash.isEmpty()) {
            System.out.println("Current branch has no commits.");
            return;
        }

        // Create the tag
        FileUtils.writeToFile(tagPath, currentCommitHash);
        System.out.println("Tag " + tagName + " created at commit " + currentCommitHash);
    }

    /**
     * Prints usage instructions for the tag command.
     */
    private void printUsage() {
        System.out.println("Usage:");
        System.out.println("  tag               # List all tags");
        System.out.println("  tag <tag-name>    # Create a new tag");
    }
}
