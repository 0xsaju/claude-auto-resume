# Claude Auto-Resume Cockpit (VS Code)

Pure UI over [claude-auto-resume](https://github.com/0xsaju/claude-auto-resume):
a status bar item and a small action menu for the workspace's auto-resume
task. All reads come from `~/.claude/auto-resume/state.json`; all writes go
through the `claude-auto-resume` terminal CLI. The extension never spawns
or parses Claude Code itself.

## Features

- **Full-page dashboard** — clicking the activity-bar logo opens the
  dashboard as an editor tab: live countdown to the next resume, health
  chips, activity timeline, and a schedule composer covering all four
  decisions — project (any workspace, current preselected), session
  plates (which conversation to continue), custom resume prompt, and
  when/tier. One-click cancel with per-workspace cards for everything
  else being tracked.
- **Status bar** — live task state for the open workspace
  (`waiting · 20:00`, `resuming…`, `done`, `failed`, …), refreshed on
  state-file changes plus a 5-second fallback poll. Click it for the menu.
- **Menu / commands** — Schedule Resume (quick picks: auto / 30m / 1h /
  custom…), Show Status, Cancel Task, Open Log.
- **Onboarding** — if the terminal tool isn't installed, offers to run the
  one-command installer in an integrated terminal.

## Requirements

- VS Code 1.85+
- The `claude-auto-resume` terminal tool (the extension offers to install
  it on first activation). Auto-detected from PATH or `~/.local/bin`;
  override with the `claudeAutoResume.cliPath` setting.

## Running from source (not yet published)

1. Open this folder (`vscode-extension/`) in VS Code.
2. Press **F5** (Run Extension) — an Extension Development Host window
   opens with the cockpit active.
3. Open any workspace folder in that window; the `auto-resume` status bar
   item appears bottom-left.

To package a `.vsix` for manual install: `npx @vscode/vsce package`, then
"Extensions: Install from VSIX…".

## Design

See the main repo's `docs/ARCHITECTURE.md` (Cockpit section) and
`docs/DECISIONS.md` D21. Deliberately no bundler, no dependencies, plain
JavaScript — this is a thin shell and should stay one.
