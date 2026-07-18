---
description: Track this workspace as an auto-resumable task (critical|normal|low)
argument-hint: <critical|normal|low> <task description>
allowed-tools: Bash
---

## Result

!`bash "${CLAUDE_PLUGIN_ROOT}/scripts/task-start.sh" $ARGUMENTS`

## Your task

Relay the result above to the user verbatim in substance (you may tidy the
formatting). If it shows a usage error, explain the expected form briefly.
If tracking started, remind the user that this session should keep
PROGRESS.md up to date, since it is the context an auto-resumed session
reads first. Do not modify any files as part of this command.
