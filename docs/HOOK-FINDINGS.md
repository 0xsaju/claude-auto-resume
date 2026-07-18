# Hook findings — limit-hit behavior

**STATUS: UNVERIFIED — no probe data yet.**

All detection code must stay stubbed (TODO(C1) markers in
`plugin/scripts/on-stop.sh`) until the **Findings** section below contains
real probe output. Detection logic must cite this file and match only
formats documented here. No invented payload shapes.

## How to produce the data

Use the instrumentation plugin in `claude-limit-hook-probe/` (see its
README). Short version:

1. Install the probe hooks (copy hook blocks into `~/.claude/settings.json`
   or install the folder as a local plugin).
2. Verify liveness: run a trivial prompt, check
   `~/.claude/limit-hook-probe/hooks.log` shows SessionStart/Stop entries.
3. Near the end of a usage window, run a real agentic task until the limit
   message appears. Note the wall-clock time.
4. Repeat once in headless mode if affordable:
   `claude -p "…" --output-format stream-json`.
5. Paste the relevant `hooks.log` excerpts into **Findings** below.

## Open questions

| # | Question | Why it matters | Answer |
|---|---|---|---|
| Q1 | Does `Stop` fire at the limit hit? | If yes, detection is trivial and instant | *unknown* |
| Q2 | Does `SessionEnd` fire, and with what `reason` value? | A distinct reason = clean structured detection | *unknown* |
| Q3 | Does only `Notification` carry the limit message? | Then Notification becomes the detection point | *unknown* |
| Q4 | Does the transcript tail contain the limit text + reset time? | That's the parse source for `resume_at` | *unknown* |
| Q5 | Exact wording/format of the limit message and reset timestamp? | Drives the `resume_at` parser and fake-claude fixture | *unknown* |
| Q6 | Same behavior in headless (`-p`) mode as interactive? | The daemon resumes headlessly; detection must work there | *unknown* |
| Q7 | Does *nothing* fire? | Then we switch to the supervisor-wrapper fallback (see ARCHITECTURE.md) | *unknown* |

## Findings

*(empty — paste probe hooks.log excerpts here, with timestamps and the mode
— interactive or headless — they were captured in)*

## Consequences once filled

- `plugin/scripts/on-stop.sh` `detect_limit()` gets real matching.
- `resume_at` parser written against Q5's exact format.
- `test/fake-claude.sh` fixture text updated from GUESSED to the real
  format (see DECISIONS D5).
- If Q7 is "yes, nothing fires": build the supervisor wrapper; on-stop.sh
  keeps its shape, only the trigger changes.
