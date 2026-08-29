---
name: feature-refactor-or-interface-addition
description: Workflow command scaffold for feature-refactor-or-interface-addition in ghidra.
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /feature-refactor-or-interface-addition

Use this workflow when working on **feature-refactor-or-interface-addition** in `ghidra`.

## Goal

Introduces or refactors an interface or service, typically in the Debugger or Debugger-api modules, often followed by a merge commit.

## Common Files

- `Ghidra/Debug/Debugger-api/src/main/java/ghidra/app/services/DebuggerStaticMappingService.java`
- `Ghidra/Debug/Debugger-api/src/main/java/ghidra/debug/api/modules/DebuggerAddressTranslator.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/service/modules/DebuggerStaticMappingContext.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/service/modules/DebuggerStaticMappingServicePlugin.java`

## Suggested Sequence

1. Understand the current state and failure mode before editing.
2. Make the smallest coherent change that satisfies the workflow goal.
3. Run the most relevant verification for touched files.
4. Summarize what changed and what still needs review.

## Typical Commit Signals

- Create or refactor interface/service Java files.
- Update related implementation files.
- Commit the changes with a reference to the feature or refactor.
- Merge the branch into the main or patch branch.

## Notes

- Treat this as a scaffold, not a hard-coded script.
- Update the command if the workflow evolves materially.