# Claude Auto-Resume Cockpit (VS Code)

Pure UI over [claude-auto-resume](https://github.com/0xsaju/claude-auto-resume):
a status bar item and a small action menu for the workspace's auto-resume
task. All reads come from `~/.claude/auto-resume/state.json`; all writes go
through the `claude-auto-resume` terminal CLI. The extension never spawns
or parses Claude Code itself.

## Features

- **Onboarding / setup** — first run opens a setup checklist (terminal CLI
  installed · detection hooks registered · Claude Code detected · state
  file healthy) with inline **Install** and **Register** actions; once
  everything is green it hands off to the dashboard. Reachable later via
  the header "Setup" link.
- **Full-page dashboard** — clicking the activity-bar logo opens the
  dashboard as an editor tab: a schedule composer for the current
  workspace (which conversation to continue, a resume prompt prefilled to
  the default, an AM/PM time picker plus Auto-detect / 30m / 1h / 2h,
  importance tier), a list of scheduled resumes with live countdowns, an
  Other-workspaces picker that opens the same composer for any project,
  an activity timeline, a collapsible CLI reference, and an About row.
- **Status bar** — the tool's live state for the open workspace
  (`waiting · resumes 8:30 PM`, `auto · reset ~1:01 PM`, `resuming…`,
  `done`, `failed`, …). Hovering shows a rich tool-status card (resume
  time, pinned session, attempts, Open-dashboard / Cancel). Refreshed on
  state-file changes plus a 5-second fallback poll.
- **Menu / commands** — Schedule Resume, Show Status, Cancel Task, Open
  Log, Register Detection Hooks.
- **About links** — set `claudeAutoResume.author.github` / `.linkedin` /
  `.buyMeACoffee` in settings; each link shows only when its URL is set.

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
