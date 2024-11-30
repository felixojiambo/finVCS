package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.core.Commit;
import com.pesapal.felixvcs.core.Tree;
import com.pesapal.felixvcs.utils.FileUtils;

import java.io.IOException;
import java.util.*;

/**
 * Handles the merging of two branches, resolving any conflicts where necessary.
 */
public class MergeCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String COMMITS_DIR = VCS_DIR + "/commits";
    private static final String REFS_DIR = VCS_DIR + "/refs/heads";
    private static final String HEAD_FILE = VCS_DIR + "/HEAD";
    private static final String TREES_DIR = VCS_DIR + "/trees";

    /**
     * Executes the merge operation by combining the changes from a source branch into the current branch.
     *
     * @param sourceBranch The name of the branch to merge into the current branch.
     * @throws IOException If an I/O error occurs during the merge process.
     */
    public void execute(String sourceBranch) throws IOException {
        if (!FileUtils.exists(VCS_DIR)) {
            System.out.println("Not a FelixVersionControl repository. Use 'init' to initialize.");
            return;
        }

        // Load current HEAD and branch details
        String headRef = FileUtils.readFile(HEAD_FILE).trim();
        String currentBranch = headRef.replace("refs/heads/", "");
        String currentCommitHash = FileUtils.readFile(REFS_DIR + "/" + currentBranch).trim();

        // Load source branch details
        if (!FileUtils.exists(REFS_DIR + "/" + sourceBranch)) {
            System.out.println("Branch " + sourceBranch + " does not exist.");
            return;
        }
        String sourceCommitHash = FileUtils.readFile(REFS_DIR + "/" + sourceBranch).trim();
        if (sourceCommitHash.isEmpty()) {
            System.out.println("Source branch " + sourceBranch + " has no commits.");
            return;
        }

        // Find the common ancestor commit
        String commonAncestor = findCommonAncestor(currentCommitHash, sourceCommitHash);
        if (commonAncestor == null) {
            System.out.println("No common ancestor found between branches.");
            return;
        }

        // Load trees for ancestor, current branch, and source branch
        Tree ancestorTree = loadTree(commonAncestor);
        Tree currentTree = loadTree(currentCommitHash);
        Tree sourceTree = loadTree(sourceCommitHash);

        // Detect conflicts between trees
        Set<String> conflicts = detectConflicts(ancestorTree, currentTree, sourceTree);
        if (!conflicts.isEmpty()) {
            System.out.println("Merge conflicts detected in the following files:");
            conflicts.forEach(file -> System.out.println(" - " + file));
            System.out.println("Please resolve conflicts manually.");
            return;
        }

        // No conflicts; merge by updating the current branch to point to the source branch's commit
        FileUtils.writeToFile(REFS_DIR + "/" + currentBranch, sourceCommitHash);
        System.out.println("Merged branch " + sourceBranch + " into " + currentBranch + " successfully.");
    }

    /**
     * Finds the common ancestor commit between two commit hashes.
     *
     * @param commit1 The first commit hash.
     * @param commit2 The second commit hash.
     * @return The hash of the common ancestor commit, or null if none is found.
     * @throws IOException If an I/O error occurs during the search.
     */
    private String findCommonAncestor(String commit1, String commit2) throws IOException {
        Set<String> ancestors1 = getAllAncestors(commit1);
        Set<String> ancestors2 = getAllAncestors(commit2);

        for (String ancestor : ancestors1) {
            if (ancestors2.contains(ancestor)) {
                return ancestor;
            }
        }
        return null;
    }

    /**
     * Retrieves all ancestor commits for a given commit hash.
     *
     * @param commitHash The commit hash.
     * @return A set of all ancestor commit hashes.
     * @throws IOException If an I/O error occurs during traversal.
     */
    private Set<String> getAllAncestors(String commitHash) throws IOException {
        Set<String> ancestors = new LinkedHashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(commitHash);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current == null || ancestors.contains(current)) {
                continue;
            }
            ancestors.add(current);

            String commitPath = COMMITS_DIR + "/" + current;
            if (FileUtils.exists(commitPath)) {
                Commit commit = Commit.fromJson(FileUtils.readFile(commitPath));
                if (commit.getParent() != null) {
                    queue.add(commit.getParent());
                }
            }
        }
        return ancestors;
    }

    /**
     * Loads the tree object associated with a given commit hash.
     *
     * @param commitHash The commit hash.
     * @return The tree object associated with the commit.
     * @throws IOException If an I/O error occurs while loading the tree.
     */
    private Tree loadTree(String commitHash) throws IOException {
        Commit commit = Commit.fromJson(FileUtils.readFile(COMMITS_DIR + "/" + commitHash));
        String treeHash = commit.getTree();
        String treeJson = FileUtils.readFile(TREES_DIR + "/" + treeHash);
        return Tree.fromJson(treeJson);
    }

    /**
     * Detects conflicts between the ancestor tree, current branch tree, and source branch tree.
     *
     * @param ancestor The tree from the common ancestor.
     * @param current  The tree from the current branch.
     * @param source   The tree from the source branch.
     * @return A set of file paths that have conflicts.
     */
    private Set<String> detectConflicts(Tree ancestor, Tree current, Tree source) {
        Set<String> conflicts = new HashSet<>();
        Set<String> allFiles = new HashSet<>();

        allFiles.addAll(ancestor.getFiles().keySet());
        allFiles.addAll(current.getFiles().keySet());
        allFiles.addAll(source.getFiles().keySet());

        for (String file : allFiles) {
            String ancestorBlob = ancestor.getFiles().get(file);
            String currentBlob = current.getFiles().get(file);
            String sourceBlob = source.getFiles().get(file);

            if (!Objects.equals(currentBlob, sourceBlob) &&
                    !Objects.equals(ancestorBlob, currentBlob) &&
                    !Objects.equals(ancestorBlob, sourceBlob)) {
                conflicts.add(file);
            }
        }
        return conflicts;
    }
}
