---
description: Cancel auto-resume tracking for this workspace
allowed-tools: Bash
---

## Result

!`bash "${CLAUDE_PLUGIN_ROOT}/scripts/task-cancel.sh"`

## Your task

Relay the result above to the user. If a task was cancelled, confirm that
any pending auto-resume will stand down (the daemon re-reads state before
every action). Do not modify any files.
