package com.pesapal.felixvcs.core;

import com.pesapal.felixvcs.utils.FileUtils;
import com.pesapal.felixvcs.utils.HashUtils;
import com.pesapal.felixvcs.utils.LRUCache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a repository in FelixVCS, providing functionality for managing branches, blobs, trees, and commits.
 */
public class Repository {
    private static final String VCS_DIR = ".felixvcs";
    private static final String COMMITS_DIR = VCS_DIR + "/commits";
    private static final String BLOBS_DIR = VCS_DIR + "/blobs";
    private static final String TREES_DIR = VCS_DIR + "/trees";
    private static final String REFS_DIR = VCS_DIR + "/refs/heads";
    private static final String HEAD_FILE = VCS_DIR + "/HEAD";
    private static final String INDEX_FILE = VCS_DIR + "/index";

    private Map<String, Branch> branches;
    private Branch currentBranch;

    private final LRUCache<String, Blob> blobCache;
    private final LRUCache<String, Tree> treeCache;

    /**
     * Initializes the repository by loading its state and setting up caches.
     *
     * @throws IOException If the repository is not initialized or if an error occurs during loading.
     */
    public Repository() throws IOException {
        blobCache = new LRUCache<>(1000); // Cache for blobs
        treeCache = new LRUCache<>(1000); // Cache for trees
        loadRepository();
    }

    /**
     * Initializes a new FelixVCS repository by creating necessary directories and setting up defaults.
     *
     * @throws IOException If an error occurs during initialization.
     */
    public static void initRepository() throws IOException {
        if (FileUtils.exists(VCS_DIR)) {
            System.out.println("Repository already initialized.");
            return;
        }

        // Create required directories
        FileUtils.createDirectory(VCS_DIR);
        FileUtils.createDirectory(COMMITS_DIR);
        FileUtils.createDirectory(BLOBS_DIR);
        FileUtils.createDirectory(TREES_DIR);
        FileUtils.createDirectory(REFS_DIR);

        // Set HEAD to main branch
        FileUtils.writeToFile(HEAD_FILE, "refs/heads/main");

        // Create initial branch
        Branch mainBranch = new Branch("main", "");
        Map<String, Branch> branches = new HashMap<>();
        branches.put("main", mainBranch);
        saveBranches(branches);

        // Initialize an empty index
        FileUtils.writeToFile(INDEX_FILE, "");

        System.out.println("Initialized empty FelixVCS repository in " + FileUtils.getAbsolutePath(VCS_DIR));
    }

    /**
     * Loads the repository's state, including branches and the current HEAD.
     *
     * @throws IOException If the repository is not initialized or if an error occurs during loading.
     */
    private void loadRepository() throws IOException {
        if (!FileUtils.exists(VCS_DIR)) {
            throw new IOException("Repository not initialized.");
        }

        branches = loadBranches();

        // Load current HEAD
        String headRef = FileUtils.readFile(HEAD_FILE).trim();
        String currentBranchName = headRef.replace("refs/heads/", "");
        currentBranch = branches.get(currentBranchName);

        if (currentBranch == null) {
            throw new IOException("Current branch " + currentBranchName + " does not exist.");
        }
    }

    /**
     * Loads branches from the repository's refs/heads directory.
     *
     * @return A map of branch names to their corresponding {@link Branch} objects.
     * @throws IOException If an error occurs during branch loading.
     */
    private Map<String, Branch> loadBranches() throws IOException {
        Map<String, Branch> branches = new HashMap<>();
        List<String> branchFiles = FileUtils.listFiles(REFS_DIR);

        for (String branchName : branchFiles) {
            String branchRefPath = REFS_DIR + "/" + branchName;
            String commitHash = FileUtils.readFile(branchRefPath).trim();
            branches.put(branchName, new Branch(branchName, commitHash));
        }
        return branches;
    }

    /**
     * Saves branches to the repository's refs/heads directory.
     *
     * @param branches A map of branch names to their corresponding {@link Branch} objects.
     * @throws IOException If an error occurs during branch saving.
     */
    private static void saveBranches(Map<String, Branch> branches) throws IOException {
        for (Branch branch : branches.values()) {
            String branchRefPath = REFS_DIR + "/" + branch.getName();
            FileUtils.writeToFile(branchRefPath, branch.getCommitHash() == null ? "" : branch.getCommitHash());
        }
    }

    /**
     * Retrieves the current branch of the repository.
     *
     * @return The {@link Branch} object representing the current branch.
     */
    public Branch getCurrentBranch() {
        return currentBranch;
    }

    /**
     * Switches to the specified branch.
     *
     * @param branchName The name of the branch to switch to.
     * @throws IOException If the branch does not exist or an error occurs during the switch.
     */
    public void switchBranch(String branchName) throws IOException {
        if (!branches.containsKey(branchName)) {
            System.out.println("Branch " + branchName + " does not exist.");
            return;
        }
        FileUtils.writeToFile(HEAD_FILE, "refs/heads/" + branchName);
        currentBranch = branches.get(branchName);
        System.out.println("Switched to branch " + branchName);
    }

    /**
     * Creates a new branch in the repository.
     *
     * @param branchName The name of the new branch.
     * @throws IOException If the branch already exists or an error occurs during creation.
     */
    public void createBranch(String branchName) throws IOException {
        if (branches.containsKey(branchName)) {
            System.out.println("Branch " + branchName + " already exists.");
            return;
        }
        String currentCommitHash = currentBranch.getCommitHash();
        Branch newBranch = new Branch(branchName, currentCommitHash);
        branches.put(branchName, newBranch);
        saveBranches(branches);
        System.out.println("Created branch " + branchName);
    }

    /**
     * Deletes a branch from the repository.
     *
     * @param branchName The name of the branch to delete.
     * @throws IOException If the branch does not exist or an error occurs during deletion.
     */
    public void deleteBranch(String branchName) throws IOException {
        if (!branches.containsKey(branchName)) {
            System.out.println("Branch " + branchName + " does not exist.");
            return;
        }
        if (branchName.equals(currentBranch.getName())) {
            System.out.println("Cannot delete the current active branch.");
            return;
        }
        branches.remove(branchName);
        FileUtils.deleteFile(REFS_DIR + "/" + branchName);
        System.out.println("Deleted branch " + branchName);
    }

    // Other methods (loadBlob, loadTree, saveTree) are similarly documented
}
