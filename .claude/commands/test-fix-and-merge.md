---
name: test-fix-and-merge
description: Workflow command scaffold for test-fix-and-merge in ghidra.
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /test-fix-and-merge

Use this workflow when working on **test-fix-and-merge** in `ghidra`.

## Goal

Fixes failing or problematic tests, often in SystemEmulation or DebuggerIntegration, followed by merging the fix into a patch branch.

## Common Files

- `Ghidra/Features/SystemEmulation/src/test/java/ghidra/pcode/emu/sys/EmuAmd64SyscallUseropLibraryTest.java`
- `Ghidra/Test/DebuggerIntegrationTest/src/test.slow/java/agent/lldb/rmi/LldbHooksTest.java`

## Suggested Sequence

1. Understand the current state and failure mode before editing.
2. Make the smallest coherent change that satisfies the workflow goal.
3. Run the most relevant verification for touched files.
4. Summarize what changed and what still needs review.

## Typical Commit Signals

- Identify failing or outdated test.
- Edit the test file to fix the issue.
- Commit the fix with a message referencing the test.
- Merge the fix into the patch branch.

## Notes

- Treat this as a scaffold, not a hard-coded script.
- Update the command if the workflow evolves materially.