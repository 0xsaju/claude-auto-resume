# claude-auto-resume

Make long Claude Code tasks survive rate-limit hits. When a tracked session
stops on a usage limit, this tool detects it, waits until the limit resets,
and automatically resumes the same session with proper context — no
babysitting the terminal.

> **Status: early development (Phase 0 complete).** The scaffold, state
> library, slash commands, and test harness exist and are fully tested.
> Limit **detection is intentionally stubbed** until real hook-payload data
> is collected (see [How detection gets built](#how-detection-gets-built)).
> Not yet installable for real use.

## How it works

1. You start a tracked task: `/task-start <critical|normal|low> <prompt>`.
2. The session runs. If a usage limit stops it, a `Stop`/`SessionEnd` hook
   inspects the transcript.
3. On a limit hit, the hook records the reset time, notifies you, and
   spawns a small detached daemon.
4. The daemon wakes every 60 seconds until the reset time (suspend-safe —
   never one long `sleep`), then resumes headlessly:
   `claude --resume <session_id> -p "<resume prompt>"`.
5. The loop closes when the task finishes — bounded by `max_resumes` and
   stuck detection.

Behavior is graded by importance:

| Importance | Behavior at reset |
|---|---|
| `critical` | Resume automatically, no confirmation |
| `normal`   | Notify, then auto-proceed after a 60 s window |
| `low`      | Notify only; you resume manually |

Tracked tasks keep a `PROGRESS.md` in the workspace; resume prompts point
the session at it first, with a fallback that re-sends the task summary if
the resumed session seems lost.

## Components

- **Engine** — a Claude Code plugin (`plugin/`). Editor-agnostic: works
  from a plain terminal, SSH, JetBrains, or VS Code.
- **Cockpit** — a future VS Code extension (`vscode-extension/`, empty
  for now). A pure UI over the state file; it never spawns Claude itself.
- **Contract** — `~/.claude/auto-resume/state.json`. Everything the daemon
  knows lives there; anything a UI needs, it reads there.

## Repo layout

```
plugin/                  the Claude Code plugin (engine)
  .claude-plugin/        manifest
  hooks/                 Stop/SessionEnd wiring
  commands/              /task-start, /task-status, /task-cancel
  scripts/               lib.sh (state helpers), on-stop.sh (hook entry)
test/
  fake-claude.sh         claude CLI stub — all iterative testing runs here
  run-tests.sh           shell test suite
docs/
  ARCHITECTURE.md        full design
  DECISIONS.md           append-only decision log
  HOOK-FINDINGS.md       probe results (source of truth for detection)
claude-limit-hook-probe/ throwaway plugin that measures hook behavior
vscode-extension/        future cockpit (Phase 4)
```

## How detection gets built

The exact behavior of Claude Code hooks at a limit hit (which events fire,
what the payloads and transcript contain) is treated as **unknown until
measured**. The `claude-limit-hook-probe/` plugin logs every lifecycle
event; you run it through a real limit hit once, paste the log excerpts
into `docs/HOOK-FINDINGS.md`, and the detection code is then written
against those exact shapes — never against guesses.

To run the probe test: see `claude-limit-hook-probe/README.md`. Short
version: install its hooks, run a session into a limit on purpose (a
subscription that is already limited works too — any session stop while
limited produces data), then check `~/.claude/limit-hook-probe/hooks.log`.

## Development

```sh
bash test/run-tests.sh
```

Runs ~90 tests: the state library against every JSON engine (`jq`,
`python3`, and a pure awk/sed fallback), cross-engine interop, timestamp
helpers, the fake-claude stub, and hook smoke tests. Everything iterative
tests against `test/fake-claude.sh` — real quota is only spent on
milestone verification.

Ground rules (details in `CLAUDE.md`):

- Portable bash: macOS (BSD) + Linux (GNU), no hard `jq` dependency.
- Hooks never break the host: always exit 0, fast, log to file.
- Safety rails: `max_resumes`, stuck detection, permission allowlist by
  default.

## Honest limitations

- Window warm-up scheduling (planned `/warmup`) helps the **5-hour rolling
  window only** — it cannot do anything about weekly caps.
- Windows support is best-effort via Git Bash/WSL; desktop notifications
  there are log-only for now.
- Auto-resume consumes your quota the moment it resets, by design. Use
  `critical` sparingly.

## Roadmap

- **Phase 1 — Detection** (blocked on probe data): real limit matching,
  `resume_at` parsing.
- **Phase 2 — Daemon**: wait loop, importance tiers, resume execution,
  safety rails.
- **Phase 3 — Polish**: resume verification, `/warmup` scheduler, packaging.
- **Phase 4 — VS Code cockpit.**
- Later: burn-rate awareness, pre-limit checkpointing, model downshift,
  task queue.

## License

MIT — see [LICENSE](LICENSE).
