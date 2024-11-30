# finVCS Documentation

![finVCS Logo](path/to/logo.png)

**finVCS** is a lightweight, Git-inspired version control system for tracking commits, managing branches, merging, and viewing diffs. Designed for simplicity and flexibility, it uses custom JSON serialization for efficiency. Ideal for developers seeking a minimal yet powerful tool for project versioning.

---

## Table of Contents

1. [Introduction](#introduction)
2. [Architecture and Design Decisions](#architecture-and-design-decisions)
3. [Installation](#installation)
4. [User Guide](#user-guide)
   - [Initializing a Repository](#initializing-a-repository)
   - [Adding Files](#adding-files)
   - [Committing Changes](#committing-changes)
   - [Viewing Commit History](#viewing-commit-history)
   - [Handling Ignored Files](#handling-ignored-files)
   - [Removing Files](#removing-files)
   - [Creating Tags](#creating-tags)
   - [Stashing Changes](#stashing-changes)
   - [Rebasing Branches](#rebasing-branches)
   - [Viewing Differences](#viewing-differences)
5. [Design Document](#design-document)
   - [Architecture Overview](#architecture-overview)
   - [Key Components](#key-components)
   - [Design Decisions](#design-decisions)
   - [Trade-offs and Challenges](#trade-offs-and-challenges)
6. [Documentation](#documentation)
   - [Code Documentation](#code-documentation)
   - [User Guide](#user-guide-1)
   - [Design Document](#design-document-1)
7. [Highlighting Interesting Aspects](#highlighting-interesting-aspects)
   - [Innovations](#innovations)
   - [Challenges Overcome](#challenges-overcome)
   - [Performance Optimizations](#performance-optimizations)
8. [Contributing](#contributing)
9. [License](#license)
10. [Contact](#contact)

---

## Introduction

**finVCS** is a custom-built version control system developed in Java, inspired by Git's core functionalities. It provides essential features such as committing changes, branching, merging, and viewing diffs, all while maintaining a lightweight footprint. By utilizing custom JSON serialization, finVCS ensures efficient storage and retrieval of versioned data, making it an excellent choice for developers who prefer a minimalistic yet effective version control solution.

---

## Architecture and Design Decisions

The architecture of finVCS is inspired by Git, providing a distributed version control system based on a file storage model. Below are the key architectural highlights:

### 1. File-Based Storage

- **Repository Data**: Stored in a hidden `.finVCS` directory, maintaining blobs, trees, commits, and references.
- **Advantages**:
  - Simplicity and portability.
  - Avoids external database dependencies.

### 2. Core Abstractions

- **Blob**: Represents individual file states. Handles binary and text files distinctly to ensure compatibility.
- **Tree**: Encapsulates a snapshot of the file system at a given commit, storing paths and their corresponding blob hashes.
- **Commit**: Represents a snapshot of a repository at a specific point in time, with metadata such as parent, author, timestamp, and message.
- **Branch**: Tracks the current state of the repository's progress, each tied to the latest commit.

### 3. Command-Based Execution

- **Modular Commands**: Commands like `init`, `add`, `commit`, `merge`, `rebase`, and `diff` follow a modular structure, allowing easy extension or modification without affecting unrelated features.
- **State Validation**: Each command validates the repository's state before execution, ensuring a consistent user experience.

### 4. Utility Layer

- **`FileUtils`**: Manages all file system operations with additional safety checks and utility functions for reading, writing, and traversing directories.
- **`HashUtils`**: Provides SHA-1 hashing to generate unique identifiers for blobs, trees, and commits.

### 5. Caching Mechanism

- **LRU Caches**: Integrated for frequently accessed blobs and trees to reduce disk I/O and improve runtime performance during operations like `log`, `diff`, and `merge`.

---

## Installation

### Prerequisites

- **Java 11 or Higher**: Ensure that Java is installed on your system. Verify the installation by running:

  ```bash
  java -version
  ```

- **Maven**: finVCS uses Maven for dependency management and building. Verify Maven installation with:

  ```bash
  mvn -v
  ```

### Steps to Install

1. **Clone the Repository**

   ```bash
   git clone https://github.com/felixojiambo/finVCS.git
   ```

2. **Navigate to the Project Directory**

   ```bash
   cd finVCS
   ```

3. **Build the Project**

   Use Maven to compile the project and run tests.

   ```bash
   mvn clean install
   ```

4. **Run the Application**

   After a successful build, execute the application using:

   ```bash
   java -jar target/finVCS-1.0-SNAPSHOT.jar [command] [options]
   ```

   Replace `[command]` and `[options]` with the desired VCS commands and their respective options.

---

## User Guide

### Initializing a Repository

Before tracking changes, initialize a new finVCS repository in your project directory.

```bash
java -jar finVCS.jar init
```

**Output:**

```
Initialized empty finVCS repository in /path/to/your/project/.finVCS
```

### Adding Files

Stage files to include them in the next commit.

```bash
java -jar finVCS.jar add <file-path>
```

**Example:**

```bash
java -jar finVCS.jar add src/Main.java
```

**Output:**

```
Added src/Main.java
```

### Committing Changes

Record the staged changes with a descriptive message.

```bash
java -jar finVCS.jar commit "Your commit message"
```

**Example:**

```bash
java -jar finVCS.jar commit "Implement user authentication"
```

**Output:**

```
[master a1b2c3d] Implement user authentication
```

### Viewing Commit History

Display a log of all commits made in the repository.

```bash
java -jar finVCS.jar log
```

**Output:**

```
commit a1b2c3d
Author: Felix Ojiambo <felix@example.com>
Date:   2024-04-25T10:15:30Z

    Implement user authentication

commit e4f5g6h
Author: Felix Ojiambo <felix@example.com>
Date:   2024-04-20T08:45:10Z

    Initial commit
```

### Handling Ignored Files

Specify patterns for files or directories to be ignored by finVCS.

1. **Create an Ignore File**

   Create a file named `.finVCS/ignore` in the root of your repository.

   **Example `.finVCS/ignore`:**

   ```
   # Ignore all log files
   *.log

   # Ignore temporary files
   temp/
   ```

2. **Effect**

   Files matching the patterns in the ignore file will be excluded from staging and commits.

### Removing Files

Unstage files and optionally remove them from the working directory.

```bash
java -jar finVCS.jar remove <file-path>
```

**Example:**

```bash
java -jar finVCS.jar remove src/Unused.java
```

**Output:**

```
Removed src/Unused.java
```

### Creating Tags

Label specific commits with meaningful names.

```bash
java -jar finVCS.jar tag <tag-name> <commit-hash>
```

**Example:**

```bash
java -jar finVCS.jar tag v1.0 a1b2c3d
```

**Output:**

```
Tag 'v1.0' created for commit a1b2c3d
```

### Stashing Changes

Temporarily save changes without committing them.

- **Stash Current Changes**

  ```bash
  java -jar finVCS.jar stash
  ```

  **Output:**

  ```
  Changes have been stashed.
  ```

- **Apply the Latest Stash**

  ```bash
  java -jar finVCS.jar stash pop
  ```

  **Output:**

  ```
  Applied the latest stash.
  ```

### Rebasing Branches

Integrate changes from one branch into another.

```bash
java -jar finVCS.jar rebase <target-branch>
```

**Example:**

```bash
java -jar finVCS.jar rebase main
```

**Output:**

```
Rebased current branch onto main.
```

### Viewing Differences

Compare changes between two commits.

```bash
java -jar finVCS.jar diff <commit1> <commit2>
```

**Example:**

```bash
java -jar finVCS.jar diff a1b2c3d e4f5g6h
```

**Output:**

```
Differences in src/Main.java:
- System.out.println("Hello, World!");
+ System.out.println("Hello, finVCS!");
```

---

## Design Document

### Architecture Overview

**finVCS** is architected around modular components that handle different aspects of version control. The primary components include:

- **Commands Module**: Handles user inputs and executes corresponding actions (e.g., `AddCommand`, `CommitCommand`).
- **Core Module**: Manages core functionalities like commits, trees, and blobs.
- **Utils Module**: Provides utility classes for file operations, hashing, and other common tasks.
- **Storage Module**: Responsible for persisting data structures (commits, trees, blobs) to the filesystem.

### Key Components

1. **Commands Module**

   - **AddCommand**: Stages files for inclusion in the next commit.
   - **CommitCommand**: Records staged changes as a new commit.
   - **LogCommand**: Displays the commit history.
   - **RemoveCommand**: Unstages and optionally deletes files.
   - **TagCommand**: Labels specific commits.
   - **StashCommand**: Temporarily saves changes.
   - **RebaseCommand**: Integrates changes from one branch to another.
   - **DiffCommand**: Shows differences between commits.

2. **Core Module**

   - **Commit**: Represents a single commit, containing metadata and a reference to the tree snapshot.
   - **Tree**: Represents the state of the repository at a specific commit, mapping file paths to blob hashes.
   - **Blob**: Stores the content of individual files.

3. **Utils Module**

   - **FileUtils**: Facilitates file operations relative to a base directory.
   - **HashUtils**: Provides methods for hashing content using SHA-1.

4. **Storage Module**

   - Organizes commits, trees, and blobs within the `.finVCS` directory structure.

### Design Decisions

1. **Modular Architecture**

   - **Reasoning**: Enhances maintainability, scalability, and testability by segregating functionalities into distinct modules.
   - **Benefit**: Facilitates easier debugging and future feature additions without impacting unrelated components.

2. **File-Based Storage**

   - **Reasoning**: Mimics the approach of established VCS tools like Git, leveraging the filesystem for storing versioned data.
   - **Benefit**: Simplifies data persistence and retrieval without the need for external databases.

3. **SHA-1 Hashing**

   - **Reasoning**: Utilizes SHA-1 for generating unique identifiers for commits, trees, and blobs.
   - **Benefit**: Ensures data integrity and facilitates efficient storage by avoiding duplicate content.

4. **Base Directory Handling in `FileUtils`**

   - **Reasoning**: Allows operations to be confined within a specified directory, enhancing test isolation and preventing path conflicts.
   - **Benefit**: Improves test reliability and ensures that the VCS operates correctly within different project directories.

### Trade-offs and Challenges

1. **Choosing SHA-1 Over Stronger Hash Functions**

   - **Trade-off**: While SHA-1 is faster and widely used in VCS tools, it's less secure compared to newer hash functions like SHA-256.
   - **Reasoning**: SHA-1 suffices for ensuring uniqueness and integrity in a version control context without exposing security vulnerabilities.

2. **File-Based Storage vs. Database Storage**

   - **Trade-off**: File-based storage is simpler and more aligned with existing VCS practices but may be less efficient for very large repositories.
   - **Reasoning**: Prioritized simplicity and alignment with industry standards, accepting potential performance limitations for exceptionally large projects.

3. **Handling Binary Files**

   - **Challenge**: Ensuring that binary files are correctly handled without corruption during staging and committing.
   - **Solution**: Implemented separate handling for binary files, avoiding text-based processing and preserving data integrity.

4. **Testing Isolation**

   - **Challenge**: Preventing tests from interfering with each other due to shared filesystem paths.
   - **Solution**: Refactored `FileUtils` to accept a base directory, ensuring that each test operates within its own isolated environment.

---

## Documentation

### Code Documentation

- **Comments**: Use inline comments to explain complex logic within the codebase. This practice aids in understanding the flow and purpose of intricate sections.

  ```java
  // Calculate the SHA-1 hash of the given content
  String blobHash = HashUtils.sha1(contentBytes);
  ```

- **JavaDoc Comments**: Apply JavaDoc comments for all classes and methods to provide clear descriptions of their functionalities, parameters, return values, and exceptions.

  ```java
  /**
   * Executes the add command for the specified file.
   *
   * @param filePathStr The file path to add, relative to baseDir.
   * @throws IOException If an I/O error occurs.
   */
  public void execute(String filePathStr) throws IOException {
      // Method implementation
  }
  ```

### User Guide

Refer to the [User Guide](#user-guide) section above for detailed instructions on how to install and use finVCS, including examples of common workflows.

### Design Document

Refer to the [Design Document](#design-document) section above for an in-depth explanation of finVCS's architecture, design decisions, trade-offs, and challenges encountered during development.

---

## Highlighting Interesting Aspects

### Innovations

1. **Custom Binary File Handling**

   - **Description**: Developed a mechanism to accurately detect and handle binary files during the add and commit processes, ensuring data integrity without relying on external libraries.
   - **Benefit**: Allows users to version control both text and binary files seamlessly, expanding the utility of finVCS.

2. **Flexible Ignore Patterns**

   - **Description**: Implemented a flexible pattern matching system in the `.finVCS/ignore` file, supporting wildcard characters and comments.
   - **Benefit**: Provides users with granular control over which files and directories are excluded from version tracking, enhancing usability.

3. **Buffered File I/O Operations**

   - **Description**: Utilized buffered streams for all file read and write operations within `FileUtils`, optimizing performance.
   - **Benefit**: Reduces I/O overhead, especially when handling large files or numerous file operations, leading to faster command executions.

### Challenges Overcome

1. **Test Environment Isolation**

   - **Challenge**: Ensuring that unit tests do not interfere with each other due to shared file paths or residual data.
   - **Solution**: Refactored `FileUtils` to operate within a specified base directory, allowing each test to run in an isolated temporary environment. This approach eliminated path conflicts and prevented `FileAlreadyExistsException` errors during testing.

2. **Binary File Integrity**

   - **Challenge**: Preventing data corruption when handling binary files during staging and committing.
   - **Solution**: Implemented separate methods for reading and writing binary files (`writeBinaryFile`) and introduced a binary file detection mechanism based on the presence of null bytes. This ensured that binary data remains unaltered throughout the VCS processes.

3. **Dynamic Author Retrieval**

   - **Challenge**: Accurately capturing the commit author's identity across different environments.
   - **Solution**: Leveraged system properties (`System.getProperty("user.name")`) to dynamically retrieve the current user's name. Provided a fallback mechanism to assign "Unknown Author" if the system property is unavailable, ensuring robustness.

### Performance Optimizations

1. **Efficient Staging Area with `HashMap`**

   - **Description**: Utilized `HashMap` data structures for managing the staging area and index, enabling constant-time lookups and updates.
   - **Benefit**: Enhances the performance of add and commit operations, especially in repositories with a large number of files.

2. **Buffered I/O in `FileUtils`**

   - **Description**: Employed buffered readers and writers for all file operations to minimize disk I/O operations.
   - **Benefit**: Significantly improves the speed of reading and writing files, particularly beneficial when dealing with large files or numerous file operations.

3. **Selective Blob Creation**

   - **Description**: Implemented checks to avoid creating duplicate blobs by verifying the existence of a blob before writing.
   - **Benefit**: Reduces unnecessary disk writes and storage consumption by preventing duplicate data, optimizing both performance and resource usage.

---

## Contributing

We welcome contributions from the community! Whether you're reporting bugs, suggesting features, or submitting pull requests, your involvement helps improve finVCS.

### Steps to Contribute

1. **Fork the Repository**

   Click the "Fork" button at the top-right corner of the repository page to create your own copy.

2. **Clone Your Fork**

   ```bash
   git clone https://github.com/felixojiambo/finVCS.git
   ```

3. **Create a New Branch**

   ```bash
   git checkout -b feature/YourFeatureName
   ```

4. **Make Your Changes**

   Implement your feature or bug fix. Ensure that your code adheres to the project's coding standards and passes all tests.

5. **Commit Your Changes**

   ```bash
   git commit -m "Add feature: YourFeatureName"
   ```

6. **Push to Your Fork**

   ```bash
   git push origin feature/YourFeatureName
   ```

7. **Create a Pull Request**

   Navigate to your forked repository on GitHub and click the "Compare & pull request" button. Provide a clear description of your changes and submit the pull request.

### Code of Conduct

Please adhere to our [Code of Conduct](CODE_OF_CONDUCT.md) to ensure a welcoming and respectful community for all contributors.

---

## License

This project is licensed under the [MIT License](LICENSE). See the [LICENSE](LICENSE) file for details.

---

## Contact

For questions, suggestions, or support, feel free to reach out:

- **Felix Ojiambo**  
  Email: [felix@example.com](mailto:felix@example.com)  
  GitHub: [@felixojiambo](https://github.com/felixojiambo)

---

## Trade-offs and Challenges

### 1. File-Based Storage vs. In-Memory State

- **Trade-off**: Opting for file-based storage ensured data persistence but introduced performance overhead during operations like `diff` and `merge` that require frequent file reads.
- **Resolution**: LRU caching mitigated the impact by storing frequently accessed blobs and trees in memory.

### 2. Conflict Detection in Merge

- **Challenge**: Comparing ancestor, current, and source trees for conflicts in `MergeCommand` required efficient handling of large file sets.
- **Resolution**: The design focuses on detecting conflicts at the file level, leaving conflict resolution to the user for simplicity.

### 3. Efficiency of `.ignore` Patterns

- **Trade-off**: Implementing a full glob-matching engine was complex, so the solution adopted regex-based matching for `.ignore` patterns.
- **Resolution**: While less flexible, regex provides a robust and understandable way to exclude files during `add` and `commit`.

### 4. Progress Reporting

- **Challenge**: Long-running operations such as `clone` and `commit` could leave users uncertain about their progress.
- **Resolution**: Integrated progress listeners that dynamically update progress, enhancing user experience without introducing external dependencies.

### 5. Commit History Traversal

- **Challenge**: The `LogCommand` required efficient traversal of parent-child commit relationships, even for deeply nested histories.
- **Solution**: Commit chains are traversed iteratively using parent references, reducing memory usage compared to recursive approaches.

### 6. Stash Management

- **Challenge**: Implementing stash functionality required careful merging of stash state with the current index.
- **Solution**: The stash state is saved as a JSON map, and conflicts are avoided by overwriting the index upon `stash pop`.

### 7. Branch Switching

- **Challenge**: Switching branches while preserving the working directory state posed a challenge for files not yet committed.
- **Solution**: Users are warned about uncommitted changes during branch switching to prevent accidental data loss.

---

## Novel Features

### 1. Binary vs. Text File Handling

- Files are automatically detected as binary or text during staging (`AddCommand`), ensuring that binary files are stored efficiently while text files are optimized for diff operations.

### 2. Custom Serialization

- Custom JSON serialization/deserialization is implemented for `Blob`, `Tree`, and `Commit` classes. This removes the need for external libraries and keeps dependencies minimal.

### 3. Hierarchical Tree Storage

- Trees are stored hierarchically based on their hash prefixes, reducing file system clutter and improving lookup efficiency during operations like `diff` and `merge`.

### 4. Conflict Detection

- A lightweight but effective conflict detection mechanism is integrated into `MergeCommand`. It compares file states across ancestor, current, and source branches, ensuring clear feedback to the user about which files have conflicts.

---

## Efficiency Improvements

### 1. LRU Caching

- Speeds up access to frequently accessed repository objects like blobs and trees, especially during operations like `log` and `diff`.

### 2. Exclusion-Based Copying

- `CloneCommand` uses an exclusion-based mechanism to skip `.finVCS` during the working directory copy. This reduces unnecessary overhead.

### 3. Optimized File Traversal

- Commands like `add` and `clone` use file tree traversal with filters, ensuring only necessary files are processed.

### 4. Progressive Feedback

- Commands such as `commit` and `clone` provide real-time feedback to users, improving perceived performance and usability.

---
