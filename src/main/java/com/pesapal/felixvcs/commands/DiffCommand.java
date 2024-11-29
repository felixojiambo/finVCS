package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.core.Commit;
import com.pesapal.felixvcs.core.Tree;
import com.pesapal.felixvcs.utils.FileUtils;

import java.io.IOException;
import java.util.*;

public class DiffCommand {
    private static final String VCS_DIR = ".felixvcs";
    private static final String COMMITS_DIR = VCS_DIR + "/commits";
    private static final String TREES_DIR = VCS_DIR + "/trees";

    public void execute(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: diff <commit1> <commit2>");
            return;
        }

        String commitHash1 = args[1];
        String commitHash2 = args[2];

        if (!FileUtils.exists(COMMITS_DIR + "/" + commitHash1)) {
            System.out.println("Commit " + commitHash1 + " does not exist.");
            return;
        }

        if (!FileUtils.exists(COMMITS_DIR + "/" + commitHash2)) {
            System.out.println("Commit " + commitHash2 + " does not exist.");
            return;
        }

        Tree tree1 = loadTree(commitHash1);
        Tree tree2 = loadTree(commitHash2);

        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(tree1.getFiles().keySet());
        allFiles.addAll(tree2.getFiles().keySet());

        for (String file : allFiles) {
            String blob1 = tree1.getFiles().get(file);
            String blob2 = tree2.getFiles().get(file);

            if (blob1 == null) {
                System.out.println("File added: " + file);
            } else if (blob2 == null) {
                System.out.println("File deleted: " + file);
            } else if (!blob1.equals(blob2)) {
                String content1 = FileUtils.readFile(TREES_DIR + "/" + blob1);
                String content2 = FileUtils.readFile(TREES_DIR + "/" + blob2);
                boolean isBinary = isBinaryContent(content1) || isBinaryContent(content2);
                List<String> diffs = generateDiff(content1, content2, isBinary);
                System.out.println("Differences in " + file + ":");
                for (String diff : diffs) {
                    System.out.println(diff);
                }
                System.out.println();
            }
        }
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

    private List<String> generateDiff(String content1, String content2, boolean isBinary) {
        List<String> diffs = new ArrayList<>();
        if (isBinary) {
            diffs.add("Binary files differ.");
            return diffs;
        }

        String[] lines1 = content1.split("\n");
        String[] lines2 = content2.split("\n");

        int max = Math.max(lines1.length, lines2.length);
        for (int i = 0; i < max; i++) {
            String line1 = i < lines1.length ? lines1[i] : "";
            String line2 = i < lines2.length ? lines2[i] : "";

            if (!line1.equals(line2)) {
                if (i < lines1.length) {
                    diffs.add("- " + line1);
                }
                if (i < lines2.length) {
                    diffs.add("+ " + line2);
                }
            }
        }

        return diffs;
    }

    private boolean isBinaryContent(String content) {
        // Simple check for binary content by searching for null characters
        return content.contains("\0");
    }
}
