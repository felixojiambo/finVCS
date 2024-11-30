package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Command to initialize a new FelixVersionControl repository.
 * <p>
 * This command sets up the necessary directory structure and configuration files
 * for managing a repository.
 */
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

        // Check if repository already exists
        if (FileUtils.exists(vcsPath.toString())) {
            System.out.println("Repository already initialized.");
            return;
        }

        // Create directory structure for the repository
        createRepositoryStructure(vcsPath);

        // Initialize HEAD to point to the "master" branch
        initializeHeadFile(vcsPath);

        // Create master branch with no commits
        initializeBranch(vcsPath);

        // Initialize empty index and ignore files
        initializeEmptyFiles(vcsPath);

        // Print success message
        System.out.println("Initialized empty FelixVersionControl repository in " + vcsPath.toAbsolutePath());
    }

    /**
     * Creates the necessary directory structure for the repository.
     *
     * @param vcsPath The base path for the repository.
     * @throws IOException If an I/O error occurs during directory creation.
     */
    private void createRepositoryStructure(Path vcsPath) throws IOException {
        FileUtils.createDirectory(vcsPath.toString());
        FileUtils.createDirectory(vcsPath.resolve("commits").toString());
        FileUtils.createDirectory(vcsPath.resolve("blobs").toString());
        FileUtils.createDirectory(vcsPath.resolve("trees").toString());
        FileUtils.createDirectory(vcsPath.resolve("refs/heads").toString());
    }

    /**
     * Initializes the HEAD file to point to the "master" branch.
     *
     * @param vcsPath The base path for the repository.
     * @throws IOException If an I/O error occurs while writing to the HEAD file.
     */
    private void initializeHeadFile(Path vcsPath) throws IOException {
        FileUtils.writeToFile(vcsPath.resolve("HEAD").toString(), "refs/heads/master");
    }

    /**
     * Creates an initial empty "master" branch.
     *
     * @param vcsPath The base path for the repository.
     * @throws IOException If an I/O error occurs while creating the branch file.
     */
    private void initializeBranch(Path vcsPath) throws IOException {
        FileUtils.writeToFile(vcsPath.resolve("refs/heads/master").toString(), "");
    }

    /**
     * Initializes the index and ignore files as empty.
     *
     * @param vcsPath The base path for the repository.
     * @throws IOException If an I/O error occurs while creating the files.
     */
    private void initializeEmptyFiles(Path vcsPath) throws IOException {
        FileUtils.writeToFile(vcsPath.resolve("index").toString(), "");
        FileUtils.writeToFile(vcsPath.resolve("ignore").toString(), "");
    }
}
