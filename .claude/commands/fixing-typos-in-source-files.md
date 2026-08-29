---
name: fixing-typos-in-source-files
description: Workflow command scaffold for fixing-typos-in-source-files in ghidra.
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /fixing-typos-in-source-files

Use this workflow when working on **fixing-typos-in-source-files** in `ghidra`.

## Goal

Fixes minor typographical errors in source code files, often in Java files related to UI or API.

## Common Files

- `Ghidra/Features/VersionTracking/src/main/java/ghidra/feature/vt/api/markuptype/VTMarkupType.java`
- `Ghidra/Features/Base/src/main/java/ghidra/plugins/importer/batch/BatchImportDialog.java`

## Suggested Sequence

1. Understand the current state and failure mode before editing.
2. Make the smallest coherent change that satisfies the workflow goal.
3. Run the most relevant verification for touched files.
4. Summarize what changed and what still needs review.

## Typical Commit Signals

- Identify the typo in the source file.
- Edit the file to correct the typo.
- Commit the change with a message referencing the typo fix.

## Notes

- Treat this as a scaffold, not a hard-coded script.
- Update the command if the workflow evolves materially.