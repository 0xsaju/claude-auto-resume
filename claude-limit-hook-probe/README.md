# limit-hook-probe

A throwaway instrumentation plugin for Claude Code. It answers one question:

**When a rate limit hits, which lifecycle hooks fire — and what do their
payloads and the transcript contain?**

The whole auto-resume design hinges on this answer, so we measure it before
building anything else.

## What it does

Logs every `Stop`, `SessionEnd`, `SessionStart`, and `Notification` event to
`~/.claude/limit-hook-probe/hooks.log`, including:

- the full JSON payload (pretty-printed if `jq` is installed)
- a timestamp
- the last 40 lines of the session transcript, when a path is provided

It never blocks or modifies Claude Code behavior (`exit 0` always).

## Install (local, no marketplace needed)

1. Copy this folder somewhere stable, e.g. `~/claude-plugins/limit-hook-probe`
2. Make the script executable:
   ```
   chmod +x ~/claude-plugins/limit-hook-probe/scripts/log-hook.sh
   ```
3. Add it to Claude Code. Either:
   - **Settings file** (`~/.claude/settings.json`), if you already manage
     plugins there, point a local marketplace/plugin path at the folder, or
   - **Quick alternative without plugin machinery**: copy the four hook
     blocks from `hooks/hooks.json` into the `"hooks"` section of
     `~/.claude/settings.json`, replacing `${CLAUDE_PLUGIN_ROOT}` with the
     absolute folder path. Functionally identical for this test.
4. Restart Claude Code / start a new session. Run one trivial prompt, then:
   ```
   tail -f ~/.claude/limit-hook-probe/hooks.log
   ```
   You should immediately see SessionStart and Stop entries. If yes, the
   probe is live.

## The actual test (burn a limit on purpose)

Best done near the end of a usage window so you sacrifice little quota:

1. Start a real-ish agentic task in your normal interactive mode
   (something long enough to keep Claude producing turns).
2. Let it run until the limit message appears.
3. Note the wall-clock time of the limit hit.
4. Run `/probe-report` in a later session (or read the log directly) and
   check the entries around that timestamp.

Repeat once in headless mode if you can afford it:

```
claude -p "long task prompt here" --output-format stream-json
```

and watch which events land in the log when it dies.

## What we're looking for

| Question | Why it matters |
|---|---|
| Did `Stop` fire at the limit hit? | If yes, detection is trivial and instant |
| Did `SessionEnd` fire, and with what `reason`? | A distinct reason = clean structured detection |
| Did only `Notification` carry the limit message? | Then Notification becomes the detection point |
| Does the transcript tail contain the limit text + reset time? | That's our parse source for `resume_at` |
| Did *nothing* fire? | Then we switch to the supervisor-wrapper fallback — design already allows it |

## After the test

Send the relevant `hooks.log` excerpt back into the planning chat. The
detection logic in `on-stop.sh` gets written against the real payload
shapes, not guesses.
