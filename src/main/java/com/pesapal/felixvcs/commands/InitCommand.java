package com.pesapal.felixvcs.commands;
import com.pesapal.felixvcs.utils.FileUtils;
import java.io.IOException;

public class InitCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String COMMITS_DIR = VCS_DIR + "/commits";
    private static final String BLOBS_DIR = VCS_DIR + "/blobs";
    private static final String TREES_DIR = VCS_DIR + "/trees";
    private static final String REFS_DIR = VCS_DIR + "/refs/heads";
    private static final String HEAD_FILE = VCS_DIR + "/HEAD";
    private static final String INDEX_FILE = VCS_DIR + "/index";
    private static final String IGNORE_FILE = VCS_DIR + "/ignore";
    public void execute() throws IOException {
        if (FileUtils.exists(VCS_DIR)) {
            System.out.println("Repository already initialized.");
            return;
        }

        // Create directories
        FileUtils.createDirectory(VCS_DIR);
        FileUtils.createDirectory(COMMITS_DIR);
        FileUtils.createDirectory(BLOBS_DIR);
        FileUtils.createDirectory(TREES_DIR);
        FileUtils.createDirectory(REFS_DIR);

        // Initialize HEAD to point to master
        FileUtils.writeToFile(HEAD_FILE, "refs/heads/master");

        // Initialize master branch with no commits
        FileUtils.writeToFile(REFS_DIR + "/master", "");

        // Initialize empty index
        FileUtils.writeToFile(INDEX_FILE, "");

        // Initialize empty ignore file
        FileUtils.writeToFile(IGNORE_FILE, "");

        System.out.println("Initialized empty FelixVersionControl repository in " + FileUtils.getAbsolutePath(VCS_DIR));
    }
}
