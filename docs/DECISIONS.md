# Decisions

Append-only log. Newest at the bottom. Format: ID, date, decision, reasoning.

---

## D1 — 2026-07-18 — Engine is a Claude Code plugin; UI is a separate VS Code extension; state.json is the only contract

Editor-agnostic engine (hooks + detached daemon) serves terminal/SSH/
JetBrains/VS Code users. The extension is a pure UI shell over
`~/.claude/auto-resume/state.json` and never spawns or parses Claude Code.
Decided before Phase 0 in planning; recorded here for the record.

## D2 — 2026-07-18 — JSON access fallback chain: jq → python3 → awk/sed on canonical layout

C2 forbids a hard jq dependency and requires a sed/grep-class fallback. Pure
sed/grep JSON editing is fragile, so the chain is: `jq` when present (most
robust), else `python3` (present on ~all target systems, fully robust), else
a text tier using awk/sed. The text tier is viable because *this library is
the only writer* of state.json and always writes a canonical 2-space-indent,
one-key-per-line layout (jq's and `json.dumps(indent=2)`'s shared style), so
line-oriented matching is reliable. The text tier's string unescaping is
best-effort on adversarial content (e.g. literal `\\n` sequences); accepted
for a last-resort tier. Engine is overridable via `AR_JSON_ENGINE` for tests.

## D3 — 2026-07-18 — Workspace key = absolute $PWD at /task-start time; one task per workspace

Simplest identity that both hooks (cwd in payload) and the daemon can agree
on without coordination. Multi-task-per-workspace is deferred to the task
queue feature (phase 4+).

## D4 — 2026-07-18 — on-stop.sh wired to both Stop and SessionEnd until probe data says otherwise

We don't yet know which event fires on a limit hit (C1). Wiring both is
harmless (the stub only logs) and means the probe findings can prune rather
than add. The script takes the event name as $1 so one file serves both.

## D5 — 2026-07-18 — fake-claude's transcript/limit-message format is an explicit guess

`test/fake-claude.sh` emits a JSONL transcript whose first line is a
`fake_meta` marker stating the format is GUESSED. When HOOK-FINDINGS.md
lands, fake-claude gets re-pointed at the real format; the test suite's
structure doesn't change, only the fixture text.

## D6 — 2026-07-18 — session_id is filled by hooks, not by /task-start

Slash commands don't receive the session id; hook payloads do. `/task-start`
leaves `session_id` empty and the Stop/SessionEnd hook fills it (Phase 1).

## D7 — 2026-07-18 — Notifications: osascript → notify-send → log-only; no Windows toast in v1

A blocking mechanism (e.g. PowerShell MessageBox) is unacceptable in a hook
path (C4), and non-blocking Windows toasts need modules we can't assume.
Windows users get log-only notifications in v1; documented limitation,
revisit in Phase 3.

## D8 — 2026-07-18 — Not a git repo yet

Phase 0 work order didn't ask for `git init`; left to the user (flagged in
PROGRESS.md handoff). **Superseded same day:** user requested repo init +
commit after the Phase 0 cleanup pass; repo initialized with `main` as the
default branch.

## D9 — 2026-07-18 — claude-limit-hook-probe/ stays at repo root

It predates the scaffold, is referenced by HOOK-FINDINGS.md as the measuring
instrument, and is throwaway after the probe. Its zip is gitignored.
