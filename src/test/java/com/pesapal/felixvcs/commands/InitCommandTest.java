package com.pesapal.felixvcs.commands;

import com.pesapal.felixvcs.utils.FileUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InitCommandTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUpStreams() {
        // Redirect System.out to capture outputs
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStreams() {
        // Restore original System.out
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("Test initializing a new repository")
    void testInitRepository(@TempDir Path tempDir) throws IOException {
        // Initialize InitCommand with the temporary directory
        InitCommand initCommand = new InitCommand(tempDir);
        initCommand.execute();

        // Verify that .felixvcs directory and subdirectories are created
        Path vcsDir = tempDir.resolve(".felixvcs");
        assertTrue(Files.exists(vcsDir), ".felixvcs directory should exist.");
        assertTrue(Files.isDirectory(vcsDir), ".felixvcs should be a directory.");

        // Subdirectories
        Path commitsDir = vcsDir.resolve("commits");
        Path blobsDir = vcsDir.resolve("blobs");
        Path treesDir = vcsDir.resolve("trees");
        Path refsDir = vcsDir.resolve("refs/heads");
        assertTrue(Files.exists(commitsDir), "commits directory should exist.");
        assertTrue(Files.isDirectory(commitsDir), "commits should be a directory.");
        assertTrue(Files.exists(blobsDir), "blobs directory should exist.");
        assertTrue(Files.isDirectory(blobsDir), "blobs should be a directory.");
        assertTrue(Files.exists(treesDir), "trees directory should exist.");
        assertTrue(Files.isDirectory(treesDir), "trees should be a directory.");
        assertTrue(Files.exists(refsDir), "refs/heads directory should exist.");
        assertTrue(Files.isDirectory(refsDir), "refs/heads should be a directory.");

        // Files
        Path headFile = vcsDir.resolve("HEAD");
        Path masterRef = refsDir.resolve("master");
        Path indexFile = vcsDir.resolve("index");
        Path ignoreFile = vcsDir.resolve("ignore");

        assertTrue(Files.exists(headFile), "HEAD file should exist.");
        assertTrue(Files.exists(masterRef), "master branch reference should exist.");
        assertTrue(Files.exists(indexFile), "index file should exist.");
        assertTrue(Files.exists(ignoreFile), "ignore file should exist.");

        // Verify contents of HEAD
        String headContent = FileUtils.readFile(headFile.toString()).trim();
        assertEquals("refs/heads/master", headContent, "HEAD should point to refs/heads/master.");

        // Verify contents of master reference
        String masterContent = FileUtils.readFile(masterRef.toString()).trim();
        assertEquals("", masterContent, "master branch reference should be empty initially.");

        // Verify contents of index and ignore files are empty
        String indexContent = FileUtils.readFile(indexFile.toString()).trim();
        String ignoreContent = FileUtils.readFile(ignoreFile.toString()).trim();
        assertEquals("", indexContent, "index file should be empty initially.");
        assertEquals("", ignoreContent, "ignore file should be empty initially.");

        // Verify console output
        String expectedOutput = "Initialized empty FelixVersionControl repository in " + vcsDir.toAbsolutePath();
        String actualOutput = outContent.toString().trim();
        assertEquals(expectedOutput, actualOutput, "Console output should confirm initialization.");
    }

    @Test
    @DisplayName("Test initializing a repository when it already exists")
    void testInitRepositoryAlreadyExists(@TempDir Path tempDir) throws IOException {
        // Manually create .felixvcs directory
        Path vcsDir = tempDir.resolve(".felixvcs");
        Files.createDirectory(vcsDir);

        // Initialize InitCommand with the temporary directory
        InitCommand initCommand = new InitCommand(tempDir);
        initCommand.execute();

        // Verify that console output indicates repository already exists
        String expectedOutput = "Repository already initialized.";
        String actualOutput = outContent.toString().trim();
        assertEquals(expectedOutput, actualOutput, "Console should indicate repository is already initialized.");

        // Verify that existing .felixvcs directory is not altered (no new subdirectories or files)
        assertFalse(Files.exists(vcsDir.resolve("commits")), "commits directory should not be created again.");
        assertFalse(Files.exists(vcsDir.resolve("blobs")), "blobs directory should not be created again.");
        assertFalse(Files.exists(vcsDir.resolve("trees")), "trees directory should not be created again.");
        assertFalse(Files.exists(vcsDir.resolve("refs/heads")), "refs/heads directory should not be created again.");
        assertFalse(Files.exists(vcsDir.resolve("HEAD")), "HEAD file should not be created again.");
        assertFalse(Files.exists(vcsDir.resolve("refs/heads/master")), "master branch reference should not be created again.");
        assertFalse(Files.exists(vcsDir.resolve("index")), "index file should not be created again.");
        assertFalse(Files.exists(vcsDir.resolve("ignore")), "ignore file should not be created again.");
    }
}
