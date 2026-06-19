```markdown
# ghidra Development Patterns

> Auto-generated skill from repository analysis

## Overview
This skill teaches the core development patterns, coding conventions, and common workflows used in the `ghidra` repository, a large Java-based reverse engineering framework. You'll learn how to maintain code consistency, follow established commit and merge practices, and use suggested commands to streamline your workflow.

## Coding Conventions

- **File Naming:**  
  Use PascalCase for Java files.  
  _Example:_  
  ```
  BatchImportDialog.java
  VTMarkupType.java
  ```

- **Import Style:**  
  Use relative imports within the Java package structure.  
  _Example:_  
  ```java
  import ghidra.plugins.importer.batch.BatchImportDialog;
  ```

- **Export Style:**  
  Use named exports (Java's public classes and interfaces).  
  _Example:_  
  ```java
  public class BatchImportDialog { ... }
  ```

- **Commit Messages:**  
  - Mixed types (features, fixes, refactors, merges)
  - No strict prefix, but concise (~50 characters)
  - Reference the purpose (e.g., "Fix typo in VTMarkupType.java")

## Workflows

### Fixing Typos in Source Files
**Trigger:** When a typo is found in code or documentation  
**Command:** `/fix-typo`

1. Identify the typo in the source file.
2. Edit the file to correct the typo.
3. Commit the change with a message referencing the typo fix.

_Example:_  
```java
// Before
public void intialize() { ... }

// After
public void initialize() { ... }
```
_Commit message:_  
```
Fix typo: intialize -> initialize in BatchImportDialog.java
```

---

### Test Fix and Merge
**Trigger:** When a test fails or needs updating due to code changes  
**Command:** `/fix-test`

1. Identify the failing or outdated test.
2. Edit the test file to fix the issue.
3. Commit the fix with a message referencing the test.
4. Merge the fix into the patch branch.

_Example:_  
```java
@Test
public void testSyscall() {
    // Fix incorrect assertion
    assertEquals(expected, actual);
}
```
_Commit message:_  
```
Fix test: correct assertion in EmuAmd64SyscallUseropLibraryTest.java
```

---

### Feature Refactor or Interface Addition
**Trigger:** When a new interface is needed or an existing service is refactored  
**Command:** `/add-interface`

1. Create or refactor interface/service Java files.
2. Update related implementation files.
3. Commit the changes with a reference to the feature or refactor.
4. Merge the branch into the main or patch branch.

_Example:_  
```java
public interface DebuggerAddressTranslator {
    Address translate(Address input);
}
```
_Commit message:_  
```
Add DebuggerAddressTranslator interface for modularity
```

---

### Compression or Serialization Improvement
**Trigger:** When compression or serialization performance, limits, or bugs need addressing  
**Command:** `/improve-compression`

1. Edit Java files related to compression or serialization.
2. Update or add relevant documentation (e.g., README).
3. Commit the changes.
4. Merge the branch into the main or patch branch.

_Example:_  
```java
// Improved buffer size for zlib compression
private static final int BUFFER_SIZE = 8192;
```
_Commit message:_  
```
Improve zlib compression buffer size in ZLIB.java
```

---

### Merge Feature or Fix Branch
**Trigger:** When a feature or fix branch is ready to be integrated  
**Command:** `/merge-branch`

1. Complete feature or fix in a dedicated branch.
2. Open a merge request or perform the merge.
3. Resolve any conflicts.
4. Commit the merge with a message referencing the branch.

_Commit message:_  
```
Merge feature/refactor-serialization into main
```

## Testing Patterns

- **Framework:** Unknown (no explicit test framework detected)
- **Test File Pattern:** `*.test.ts` (Note: Most tests are Java-based, but some TypeScript test patterns exist)
- **Location:** Java test files are placed in `src/test/java/...`
- **Example:**  
  ```java
  @Test
  public void testFunctionality() {
      // test logic here
  }
  ```

## Commands

| Command          | Purpose                                                      |
|------------------|-------------------------------------------------------------|
| /fix-typo        | Fix minor typographical errors in source or documentation    |
| /fix-test        | Fix or update failing/problematic tests                      |
| /add-interface   | Add or refactor interfaces/services for modularity           |
| /improve-compression | Improve or fix compression/serialization code            |
| /merge-branch    | Merge a feature or fix branch into main or patch branch      |
```
