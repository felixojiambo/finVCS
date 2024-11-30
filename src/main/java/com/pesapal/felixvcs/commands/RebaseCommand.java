package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.core.Commit;
import com.pesapal.felixvcs.utils.FileUtils;
import com.pesapal.felixvcs.utils.HashUtils;

import java.io.IOException;
import java.util.*;

/**
 * Handles the rebase operation, which reapplies commits from the current branch on top of another branch.
 */
public class RebaseCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String COMMITS_DIR = VCS_DIR + "/commits";
    private static final String REFS_DIR = VCS_DIR + "/refs/heads";
    private static final String HEAD_FILE = VCS_DIR + "/HEAD";

    /**
     * Executes the rebase command to reapply commits from the current branch onto the target branch.
     *
     * @param args Arguments provided by the user. Expects <target-branch> as the second argument.
     * @throws IOException If an I/O error occurs during rebasing.
     */
    public void execute(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: rebase <target-branch>");
            return;
        }

        String targetBranch = args[1];

        // Validate target branch
        if (!FileUtils.exists(REFS_DIR + "/" + targetBranch)) {
            System.out.println("Target branch " + targetBranch + " does not exist.");
            return;
        }

        // Get current branch and commit hash
        String headRef = FileUtils.readFile(HEAD_FILE).trim();
        String currentBranch = headRef.replace("refs/heads/", "");
        String currentCommitHash = FileUtils.readFile(REFS_DIR + "/" + currentBranch).trim();

        // Get target branch commit hash
        String targetCommitHash = FileUtils.readFile(REFS_DIR + "/" + targetBranch).trim();

        // Find common ancestor
        String commonAncestor = findCommonAncestor(currentCommitHash, targetCommitHash);
        if (commonAncestor == null) {
            System.out.println("No common ancestor found.");
            return;
        }

        // Get list of commits to rebase
        List<String> commitsToRebase = getCommitsAfter(commonAncestor, currentCommitHash);
        if (commitsToRebase.isEmpty()) {
            System.out.println("No commits to rebase.");
            return;
        }

        // Reapply commits on top of the target branch
        String newParent = targetCommitHash;
        for (String commitHash : commitsToRebase) {
            Commit originalCommit = loadCommit(commitHash);

            Commit newCommit = new Commit();
            newCommit.setTree(originalCommit.getTree());
            newCommit.setParent(newParent);
            newCommit.setMessage(originalCommit.getMessage());
            newCommit.setTimestamp(new Date().toString());
            newCommit.setAuthor(originalCommit.getAuthor());

            String commitJson = newCommit.toJson();
            String newCommitHash = HashUtils.sha1(commitJson.getBytes());

            // Save new commit
            String newCommitPath = COMMITS_DIR + "/" + newCommitHash;
            FileUtils.writeToFile(newCommitPath, commitJson);

            // Update parent for the next commit
            newParent = newCommitHash;

            System.out.println("Rebased commit " + commitHash + " to " + newCommitHash);
        }

        // Update the current branch to point to the new commit
        FileUtils.writeToFile(REFS_DIR + "/" + currentBranch, newParent);

        System.out.println("Rebase completed successfully.");
    }

    /**
     * Finds the common ancestor commit between two branches.
     *
     * @param commit1 The hash of the first commit.
     * @param commit2 The hash of the second commit.
     * @return The hash of the common ancestor commit, or null if none is found.
     * @throws IOException If an I/O error occurs during commit traversal.
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
     * Retrieves all ancestor commits for a given commit.
     *
     * @param commitHash The hash of the commit.
     * @return A set of ancestor commit hashes.
     * @throws IOException If an I/O error occurs during commit traversal.
     */
    private Set<String> getAllAncestors(String commitHash) throws IOException {
        Set<String> ancestors = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(commitHash);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current == null || current.isEmpty() || ancestors.contains(current)) {
                continue;
            }
            ancestors.add(current);
            String commitPath = COMMITS_DIR + "/" + current;
            if (FileUtils.exists(commitPath)) {
                String commitJson = FileUtils.readFile(commitPath);
                Commit commit = Commit.fromJson(commitJson);
                if (commit.getParent() != null) {
                    queue.add(commit.getParent());
                }
            }
        }

        return ancestors;
    }

    /**
     * Retrieves a list of commits to rebase from the current branch, starting after the common ancestor.
     *
     * @param ancestor   The hash of the common ancestor commit.
     * @param commitHash The hash of the latest commit in the current branch.
     * @return A list of commit hashes to rebase, in order from oldest to newest.
     * @throws IOException If an I/O error occurs during commit traversal.
     */
    private List<String> getCommitsAfter(String ancestor, String commitHash) throws IOException {
        List<String> commits = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(commitHash);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(ancestor)) {
                break;
            }
            commits.add(current);
            String commitPath = COMMITS_DIR + "/" + current;
            if (FileUtils.exists(commitPath)) {
                String commitJson = FileUtils.readFile(commitPath);
                Commit commit = Commit.fromJson(commitJson);
                if (commit.getParent() != null) {
                    queue.add(commit.getParent());
                }
            }
        }

        Collections.reverse(commits); // Reverse to get commits from ancestor to current
        return commits;
    }

    /**
     * Loads a commit object from its hash.
     *
     * @param commitHash The hash of the commit to load.
     * @return The loaded Commit object.
     * @throws IOException If an I/O error occurs during commit loading.
     */
    private Commit loadCommit(String commitHash) throws IOException {
        String commitPath = COMMITS_DIR + "/" + commitHash;
        String commitJson = FileUtils.readFile(commitPath);
        return Commit.fromJson(commitJson);
    }
}
