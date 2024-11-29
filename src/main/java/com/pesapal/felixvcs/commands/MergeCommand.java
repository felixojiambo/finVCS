package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.core.Commit;
import com.pesapal.felixvcs.core.Tree;
import com.pesapal.felixvcs.utils.FileUtils;
import com.pesapal.felixvcs.utils.HashUtils;
import java.io.IOException;
import java.util.*;

public class MergeCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String COMMITS_DIR = VCS_DIR + "/commits";
    private static final String REFS_DIR = VCS_DIR + "/refs/heads";
    private static final String HEAD_FILE = VCS_DIR + "/HEAD";
    private static final String BLOBS_DIR = VCS_DIR + "/blobs";
    private static final String TREES_DIR = VCS_DIR + "/trees";

    public void execute(String sourceBranch) throws IOException {
        // Check if repository is initialized
        if (!FileUtils.exists(VCS_DIR)) {
            System.out.println("Not a FelixVersionControl repository. Use 'init' to initialize.");
            return;
        }

        // Load current HEAD
        String headRef = FileUtils.readFile(HEAD_FILE).trim();
        String currentBranch = headRef.replace("refs/heads/", "");
        String currentCommitHash = FileUtils.readFile(REFS_DIR + "/" + currentBranch).trim();

        // Load source branch commit
        String sourceCommitHash = FileUtils.readFile(REFS_DIR + "/" + sourceBranch).trim();
        if (sourceCommitHash.isEmpty()) {
            System.out.println("Source branch " + sourceBranch + " has no commits.");
            return;
        }

        // Find common ancestor
        String commonAncestor = findCommonAncestor(currentCommitHash, sourceCommitHash);
        if (commonAncestor == null) {
            System.out.println("No common ancestor found.");
            return;
        }

        // Load trees
        Tree ancestorTree = loadTree(commonAncestor);
        Tree currentTree = loadTree(currentCommitHash);
        Tree sourceTree = loadTree(sourceCommitHash);

        // Detect conflicts
        Set<String> conflicts = detectConflicts(ancestorTree, currentTree, sourceTree);

        if (!conflicts.isEmpty()) {
            System.out.println("Merge conflicts detected in the following files:");
            for (String file : conflicts) {
                System.out.println(" - " + file);
            }
            System.out.println("Please resolve conflicts manually.");
            return;
        }

        // No conflicts; perform merge by updating current branch to source commit
        FileUtils.writeToFile(REFS_DIR + "/" + currentBranch, sourceCommitHash);
        System.out.println("Merged branch " + sourceBranch + " into " + currentBranch + " successfully.");
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
        Set<String> ancestors = new LinkedHashSet<>();
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

    private Tree loadTree(String commitHash) throws IOException {
        String commitPath = COMMITS_DIR + "/" + commitHash;
        String commitJson = FileUtils.readFile(commitPath);
        Commit commit = Commit.fromJson(commitJson);
        String treeHash = commit.getTree();
        String treePath = TREES_DIR + "/" + treeHash;
        String treeJson = FileUtils.readFile(treePath);
        return Tree.fromJson(treeJson);
    }

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

            if (Objects.equals(currentBlob, sourceBlob)) {
                continue; // No conflict
            }

            if (!Objects.equals(ancestorBlob, currentBlob) && !Objects.equals(ancestorBlob, sourceBlob)) {
                conflicts.add(file);
            }
        }

        return conflicts;
    }
}
