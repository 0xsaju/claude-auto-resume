# PROGRESS

Living checklist for claude-auto-resume. Update before ending any session.

## Done

- [x] **Phase 0 — Scaffold + harness** (2026-07-18)
  - [x] Repo structure: `plugin/` (manifest, hooks, commands, scripts),
        `test/`, `docs/`, `vscode-extension/.gitkeep`, CLAUDE.md,
        .gitignore, MIT LICENSE
  - [x] Governance docs: ARCHITECTURE.md, DECISIONS.md (D1–D9),
        HOOK-FINDINGS.md template (STATUS: UNVERIFIED)
  - [x] `plugin/scripts/lib.sh` — state.json helpers with atomic writes and
        a jq → python3 → awk/sed text-tier fallback chain (D2), logging,
        notify (osascript → notify-send → log-only), timestamp helpers
        (BSD/GNU dual path)
  - [x] `/task-start`, `/task-status`, `/task-cancel` commands + backends
  - [x] `plugin/scripts/on-stop.sh` — hook entry, detection **stubbed**
        per C1 with TODO(C1) markers citing docs/HOOK-FINDINGS.md
  - [x] `test/fake-claude.sh` — claude CLI stub (clean/limit modes, resume,
        stream-json, JSONL transcripts; format explicitly GUESSED, D5)
  - [x] `test/run-tests.sh` — 90 tests, all green on macOS (BSD userland):
        per-engine state suites (jq / python3 / text), cross-engine
        interop, timestamps, fake-claude, on-stop smoke
  - [x] Cleanup pass: user-facing README.md added, junk files removed,
        JSON/syntax/test verification re-run, repo initialized (D8)

## In progress

- (nothing)

## Next

- [ ] **Human action required:** run the probe (`claude-limit-hook-probe/`)
      through a real limit hit and paste hooks.log excerpts into
      `docs/HOOK-FINDINGS.md` — Phase 1 is blocked on this
- [ ] **Phase 1 — Detection:** real `detect_limit()` in on-stop.sh, limit
      message → `resume_at` parser, task-done vs limit-hit branching;
      update fake-claude fixture text to the real format (D5)
- [ ] **Phase 2 — Daemon:** 60 s wake sleep-loop, importance tiers, resume
      execution against fake-claude, journal writes, max-resume + stuck
      detection, early-resume retry with backoff
- [ ] **Phase 3 — Loop closure + polish:** resume-verification fallback
      prompt, `/warmup` scheduler installer, user README, marketplace
      packaging
- [ ] **Phase 4:** VS Code cockpit reading state.json

## Handoff note (Phase 0 → Phase 1)

Phase 0 is complete: the scaffold, docs, state library, slash commands, and
test harness all exist and `bash test/run-tests.sh` passes 90/90 on macOS.
The one thing the code will not tell you is how a limit hit actually looks —
`on-stop.sh` deliberately detects nothing (C1) and `fake-claude.sh`'s limit
message is a marked guess (D5). The next session should start from
`docs/HOOK-FINDINGS.md`: once real probe data is pasted in, implement
`detect_limit()` and the `resume_at` parser against those exact shapes,
re-point the fake-claude fixture, and extend run-tests.sh with
detection-path tests. All state manipulation should go through lib.sh's
public API (`ar_task_get/upsert/set`, `ar_journal_append`) — it already
handles atomicity, engine fallback, and escaping; don't reach into
state.json directly.
