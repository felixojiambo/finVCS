package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.core.Commit;
import com.pesapal.felixvcs.utils.FileUtils;
import com.pesapal.felixvcs.utils.HashUtils;

import java.io.IOException;
import java.util.*;

public class RebaseCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String COMMITS_DIR = VCS_DIR + "/commits";
    private static final String REFS_DIR = VCS_DIR + "/refs/heads";
    private static final String HEAD_FILE = VCS_DIR + "/HEAD";

    public void execute(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: rebase <target-branch>");
            return;
        }

        String targetBranch = args[1];

        if (!FileUtils.exists(REFS_DIR + "/" + targetBranch)) {
            System.out.println("Target branch " + targetBranch + " does not exist.");
            return;
        }

        // Get current branch and commit
        String headRef = FileUtils.readFile(HEAD_FILE).trim();
        String currentBranch = headRef.replace("refs/heads/", "");
        String currentCommitHash = FileUtils.readFile(REFS_DIR + "/" + currentBranch).trim();

        // Get target branch commit
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

        // Apply each commit on top of target branch
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

            // Update parent for next commit
            newParent = newCommitHash;

            System.out.println("Rebased commit " + commitHash + " to " + newCommitHash);
        }

        // Update current branch to point to new commit
        FileUtils.writeToFile(REFS_DIR + "/" + currentBranch, newParent);

        System.out.println("Rebase completed successfully.");
    }

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

        Collections.reverse(commits); // From ancestor to current
        return commits;
    }

    private Commit loadCommit(String commitHash) throws IOException {
        String commitPath = COMMITS_DIR + "/" + commitHash;
        String commitJson = FileUtils.readFile(commitPath);
        return Commit.fromJson(commitJson);
    }
}
/*
* This is a simplified implementation of rebasing.
* Implementing interactive rebasing,
*  conflict resolution during rebasing,
* and other advanced features would require more comprehensive logic.
*
*
* */