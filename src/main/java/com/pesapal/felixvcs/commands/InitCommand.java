package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InitCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String COMMITS_DIR = VCS_DIR + "/commits";
    private static final String BLOBS_DIR = VCS_DIR + "/blobs";
    private static final String TREES_DIR = VCS_DIR + "/trees";
    private static final String REFS_DIR = VCS_DIR + "/refs/heads";
    private static final String HEAD_FILE = VCS_DIR + "/HEAD";
    private static final String INDEX_FILE = VCS_DIR + "/index";
    private static final String IGNORE_FILE = VCS_DIR + "/ignore";

    private final Path baseDir;

    /**
     * Constructs an InitCommand with a specified base directory.
     *
     * @param baseDir The directory where the repository should be initialized.
     */
    public InitCommand(Path baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Default constructor that initializes the repository in the current working directory.
     */
    public InitCommand() {
        this(Paths.get(System.getProperty("user.dir")));
    }

    /**
     * Executes the initialization of the FelixVersionControl repository.
     *
     * @throws IOException If an I/O error occurs during initialization.
     */
    public void execute() throws IOException {
        Path vcsPath = baseDir.resolve(VCS_DIR);
        if (FileUtils.exists(vcsPath.toString())) {
            System.out.println("Repository already initialized.");
            return;
        }

        // Create directories
        FileUtils.createDirectory(vcsPath.toString());
        FileUtils.createDirectory(vcsPath.resolve("commits").toString());
        FileUtils.createDirectory(vcsPath.resolve("blobs").toString());
        FileUtils.createDirectory(vcsPath.resolve("trees").toString());
        FileUtils.createDirectory(vcsPath.resolve("refs/heads").toString());

        // Initialize HEAD to point to master
        FileUtils.writeToFile(vcsPath.resolve("HEAD").toString(), "refs/heads/master");

        // Initialize master branch with no commits
        FileUtils.writeToFile(vcsPath.resolve("refs/heads/master").toString(), "");

        // Initialize empty index
        FileUtils.writeToFile(vcsPath.resolve("index").toString(), "");

        // Initialize empty ignore file
        FileUtils.writeToFile(vcsPath.resolve("ignore").toString(), "");

        System.out.println("Initialized empty FelixVersionControl repository in " + vcsPath.toAbsolutePath());
    }
}
