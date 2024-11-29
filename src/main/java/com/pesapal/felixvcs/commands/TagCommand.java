package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TagCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String TAGS_DIR = VCS_DIR + "/refs/tags";
    private static final String HEAD_FILE = VCS_DIR + "/HEAD";

    public void execute(String[] args) throws IOException {
        if (!FileUtils.exists(VCS_DIR)) {
            System.out.println("Not a FelixVersionControl repository. Use 'init' to initialize.");
            return;
        }

        if (args.length == 1) {
            // List all tags
            listTags();
        } else if (args.length == 2) {
            // Create a new tag
            String tagName = args[1];
            createTag(tagName);
        } else {
            System.out.println("Usage:");
            System.out.println("  tag               # List all tags");
            System.out.println("  tag <tag-name>    # Create a new tag");
        }
    }

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
            return;
        }

        System.out.println("Tags:");
        for (String tag : tags) {
            System.out.println("  " + tag);
        }
    }

    private void createTag(String tagName) throws IOException {
        String tagPath = TAGS_DIR + "/" + tagName;
        if (FileUtils.exists(tagPath)) {
            System.out.println("Tag " + tagName + " already exists.");
            return;
        }

        // Get current commit hash
        String headRef = FileUtils.readFile(HEAD_FILE).trim();
        String currentBranch = headRef.replace("refs/heads/", "");
        String currentCommitHash = FileUtils.readFile(VCS_DIR + "/refs/heads/" + currentBranch).trim();

        if (currentCommitHash.isEmpty()) {
            System.out.println("Current branch has no commits.");
            return;
        }

        // Create tag pointing to current commit
        FileUtils.writeToFile(tagPath, currentCommitHash);
        System.out.println("Tag " + tagName + " created at commit " + currentCommitHash);
    }
}
