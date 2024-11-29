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

    // Caches
    private final LRUCache<String, Blob> blobCache;
    private final LRUCache<String, Tree> treeCache;

    // Constructor
    public Repository() throws IOException {
        // Initialize caches with a capacity of 1000 entries each (adjust as needed)
        blobCache = new LRUCache<>(1000);
        treeCache = new LRUCache<>(1000);
        loadRepository();
    }

    // Initialize repository structure
    public static void initRepository() throws IOException {
        if (FileUtils.exists(VCS_DIR)) {
            System.out.println("Repository already initialized.");
            return;
        }

        // Create necessary directories
        FileUtils.createDirectory(VCS_DIR);
        FileUtils.createDirectory(COMMITS_DIR);
        FileUtils.createDirectory(BLOBS_DIR);
        FileUtils.createDirectory(TREES_DIR);
        FileUtils.createDirectory(REFS_DIR + "/heads");

        // Initialize HEAD to point to main branch
        FileUtils.writeToFile(HEAD_FILE, "refs/heads/main");

        // Create main branch with no commits yet
        Branch mainBranch = new Branch("main", "");
        Map<String, Branch> branches = new HashMap<>();
        branches.put("main", mainBranch);
        saveBranches(branches);

        // Initialize empty index
        FileUtils.writeToFile(INDEX_FILE, "");

        System.out.println("Initialized empty FelixVCS repository in " + FileUtils.getAbsolutePath(VCS_DIR));
    }

    // Load repository state
    private void loadRepository() throws IOException {
        if (!FileUtils.exists(VCS_DIR)) {
            throw new IOException("Repository not initialized.");
        }

        // Load branches
        branches = loadBranches();

        // Load HEAD
        String headRef = FileUtils.readFile(HEAD_FILE).trim();
        String currentBranchName = headRef.replace("refs/heads/", "");
        currentBranch = branches.get(currentBranchName);

        if (currentBranch == null) {
            throw new IOException("Current branch " + currentBranchName + " does not exist.");
        }
    }

    // Load branches from refs/heads
    private Map<String, Branch> loadBranches() throws IOException {
        Map<String, Branch> branches = new HashMap<>();

        // List all branch files within refs/heads
        List<String> branchFiles = FileUtils.listFiles(REFS_DIR);
        for (String branchName : branchFiles) {
            String branchRefPath = REFS_DIR + "/" + branchName;
            String commitHash = FileUtils.readFile(branchRefPath).trim();
            branches.put(branchName, new Branch(branchName, commitHash));
        }

        return branches;
    }

    // Save branches to refs/heads
    private static void saveBranches(Map<String, Branch> branches) throws IOException {
        for (Branch branch : branches.values()) {
            String branchRefPath = REFS_DIR + "/" + branch.getName();
            FileUtils.writeToFile(branchRefPath, branch.getCommitHash() == null ? "" : branch.getCommitHash());
        }
    }

    // Get current branch
    public Branch getCurrentBranch() {
        return currentBranch;
    }

    // Switch to another branch
    public void switchBranch(String branchName) throws IOException {
        if (!branches.containsKey(branchName)) {
            System.out.println("Branch " + branchName + " does not exist.");
            return;
        }

        // Update HEAD
        FileUtils.writeToFile(HEAD_FILE, "refs/heads/" + branchName);
        currentBranch = branches.get(branchName);

        System.out.println("Switched to branch " + branchName);
    }

    // Create a new branch
    public void createBranch(String branchName) throws IOException {
        if (branches.containsKey(branchName)) {
            System.out.println("Branch " + branchName + " already exists.");
            return;
        }

        // Point new branch to the current commit
        String currentCommitHash = currentBranch.getCommitHash();
        Branch newBranch = new Branch(branchName, currentCommitHash);
        branches.put(branchName, newBranch);

        // Save branches
        saveBranches(branches);

        System.out.println("Created branch " + branchName);
    }

    // Delete a branch
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
        String branchRefPath = REFS_DIR + "/" + branchName;
        FileUtils.deleteFile(branchRefPath);

        System.out.println("Deleted branch " + branchName);
    }

    // Load Blob from cache or disk
    public Blob loadBlob(String blobHash) throws IOException {
        Blob blob = blobCache.get(blobHash);
        if (blob != null) {
            return blob;
        }

        String blobPath = BLOBS_DIR + "/" + blobHash;
        if (!FileUtils.exists(blobPath)) {
            throw new IOException("Blob " + blobHash + " does not exist.");
        }

        // Determine if the blob is binary or text
        // Assuming a convention: binary blobs have a '.bin' suffix in their hash
        boolean isBinary = blobHash.endsWith(".bin");

        byte[] content;
        if (isBinary) {
            content = Files.readAllBytes(Paths.get(blobPath));
        } else {
            String contentStr = FileUtils.readFile(blobPath);
            content = contentStr.getBytes(StandardCharsets.UTF_8);
        }

        blob = new Blob(blobHash, content, isBinary);
        blobCache.put(blobHash, blob);
        return blob;
    }

    // Load Tree from cache or disk
    public Tree loadTree(String treeHash) throws IOException {
        Tree tree = treeCache.get(treeHash);
        if (tree != null) {
            return tree;
        }

        String treePath = getHierarchicalTreePath(treeHash);
        if (!FileUtils.exists(treePath)) {
            throw new IOException("Tree " + treeHash + " does not exist.");
        }

        String treeJson = FileUtils.readFile(treePath);
        tree = Tree.fromJson(treeJson);
        treeCache.put(treeHash, tree);
        return tree;
    }

    // Helper method to get hierarchical tree path based on hash
    private String getHierarchicalTreePath(String treeHash) {
        if (treeHash.length() < 4) {
            throw new IllegalArgumentException("Hash too short for hierarchical storage.");
        }
        String firstDir = treeHash.substring(0, 2);
        String secondDir = treeHash.substring(2, 4);
        return TREES_DIR + "/" + firstDir + "/" + secondDir + "/" + treeHash;
    }

    // Save Tree with hierarchical directories
    public void saveTree(Tree tree) throws IOException {
        String treeHash = tree.getHash();
        String treePath = getHierarchicalTreePath(treeHash);

        // Ensure the parent directories exist
        Path parentDir = Paths.get(treePath).getParent();
        if (!Files.exists(parentDir)) {
            FileUtils.createDirectory(parentDir.toString());
        }

        // Write the tree JSON if it doesn't already exist
        if (!FileUtils.exists(treePath)) {
            FileUtils.writeToFile(treePath, tree.toJson());
        }
    }

    // Additional repository management methods can be added here
}
