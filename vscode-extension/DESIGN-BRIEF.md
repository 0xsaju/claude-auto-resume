# Design Brief — Claude Auto-Resume for VS Code (single design, all screens)

Paste this whole document into Claude design. Produce **ONE design** — one
coherent visual system across three screens — not multiple options.

## 1. Product context

**claude-auto-resume** rescues developers whose long Claude Code tasks die
when they hit a usage limit. It detects the limit, waits for the reset, and
resumes the SAME conversation automatically (`claude --resume <session>`).
This VS Code extension is its cockpit.

**Personality: professional developer tool.** Reference quality bar:
Linear settings pages, Vercel dashboard, GitHub's settings, the VS Code
Welcome page. Calm, dense-but-breathing, restrained. The user described the
previous iteration as "childish" — that is the failure mode to avoid.

### Anti-goals (learned from rejected iterations — do NOT do these)

- No oversized centered hero with a big logo and giant tagline floating in
  empty space. The brand mark stays small (16–20 px in headers).
- No cramped control strips: never two selects squeezed side-by-side.
- No cartoon-large border radii, no oversized buttons, no decorative
  illustration. Typography and spacing do the work.
- No dead vertical voids. Content is a comfortable top-aligned column
  (~640–720 px) with real sections; the page should feel *composed*, like
  documentation, not like a splash screen.

## 2. Platform constraints (hard)

- Renders in **VS Code webviews** (editor-tab pages, 700–1400 px wide) and
  one small popup (~320 px card).
- **Must inherit the user's VS Code theme.** ALL neutral colors via the CSS
  variables in §7. Must work in light AND dark themes.
- **CSP: no external anything.** No web fonts, images, CDNs, frameworks.
  `var(--vscode-font-family)` + monospace stack, inline SVG only, plain CSS
  and small vanilla JS.
- Single scrollable column per screen. No horizontal scroll ever.

## 3. The three screens (one HTML file, tab/state switcher at top)

### Screen A — Onboarding / Setup (first run)

Shown the first time the user clicks the activity-bar icon, and reachable
later via a "Setup" link. Two parts:

1. **What this does** — compact intro. Brand mark (small) + product name +
   one sentence, then a 3-step strip (numbered, inline, not giant cards):
   ① You hit a usage limit → ② the tool waits for the reset →
   ③ your exact conversation continues automatically. One line of caption
   under each.
2. **Setup checklist** — a card of check rows, each with live status:
   - Terminal CLI installed (`claude-auto-resume`) — states: checking
     (subtle spinner) / ✓ installed v0.3.0 (green) / ✗ missing + **Install**
     button (runs installer in a terminal)
   - Hooks registered in `~/.claude/settings.json` — ✓ / ✗ + **Register**
   - Claude Code CLI found — ✓ v2.1.x / ✗ + doc link
   - State file healthy — ✓ `~/.claude/auto-resume/state.json`
   When every row is green: a success row "**Ready.** Everything is wired."
   plus a primary button **Open dashboard →**. Design both the mixed state
   (one ✗) and the all-green state.

### Screen B — Dashboard (the main page)

Top→bottom:

1. **Header** — small brand mark + "Claude Auto-Resume" + version, right
   side: health dot + "Setup" link + quiet refresh. One line, ~40 px.
2. **Usage strip** (data may be present or absent — design both): a slim
   row of three inline meters, exactly like a native status readout:
   `Session ▰▰▱▱ 8% · resets 4:22 PM` · `Week 52% · resets Tue` ·
   `Model 92%`. Muted; green→amber→red fill by utilization. If usage data
   is unavailable, the strip shows only:
   `Session resets ~1:01 PM (inferred from activity)`.
3. **Current workspace section** — header row: folder name +
   muted full path. Contains:
   - **Schedule composer** (a bordered card, labeled fields, full width):
     - **Conversation** — dropdown, entries formatted
       `<session title> — <id8> · <relative age>`; first entry is the
       newest; a "New chat" entry exists but is last and marked.
     - **Resume prompt** — text input whose VALUE is prefilled with the
       default ("Limit reset. Continue from where you stopped. Check
       PROGRESS.md first.") so the user edits it in place; small
       "reset to default" affordance when edited.
     - **When** — one row: chips `Auto-detect reset` (default, selected) ·
       `30m` · `1h` · `2h`, then a simple time input with **AM/PM format**
       (`8 : 30 PM` — hour, minute, AM/PM segmented control or a masked
       input; NOT "20:00 · 45m · ISO" cryptic placeholder). Typing a time
       deselects the chips.
     - Footer row: importance select (critical / normal / low, quiet) +
       primary amber button **Schedule resume**.
   - **Scheduled resumes list** (a workspace can have SEVERAL): each row =
     status dot + conversation title (id8) + when ("4:22 PM · auto-detect"
     or "8:30 PM") + prompt indicator (default / "custom") + attempts
     `1/3` + a live countdown for the nearest one + cancel (✕, needs
     confirm affordance). Empty state: one muted line "Nothing scheduled
     — the composer above is all you need."
4. **Other workspaces section** — header + a **project dropdown** listing
   every known project (tracked tasks first, then any project with Claude
   sessions on disk; entries: folder name + muted path). Picking one
   reveals the SAME composer (identical component, don't redesign it) for
   that project, plus that project's scheduled-resumes list. Below the
   dropdown: compact rows for every workspace that already has schedules
   (dot · name · next event · cancel).
5. **Activity** — timeline of journal events, newest first: small glyph +
   time + event + muted detail ("resumed — attempt 1 of 3, continuing
   session 612fb08b"). 6–8 realistic rows telling an overnight story:
   scheduled → limit-hit → reset-detected → resumed → done.
6. **CLI reference** — collapsible section ("Do all of this from the
   terminal"), a two-column mini-table in mono font:
   `claude-auto-resume resume-at` — schedule/reschedule ·
   `sessions` — list conversations · `status` · `cancel` · `doctor` ·
   `log`. One example line under it:
   `claude-auto-resume resume-at 8:30pm --session 2 --prompt "…"`.
   Link "Full user guide →".
7. **About** — one quiet row, author credit: GitHub · LinkedIn ·
   Buy me a coffee (text links with small inline SVG glyphs, no big
   badges) + MIT + version.
8. **Footer** — `~/.claude/auto-resume/state.json · live` + Log · Config.

### Screen C — Status bar + usage popup

1. **Status bar item** (mock a 22 px VS Code status bar strip, bottom):
   `⟳ 8% · resets 4:22 PM · next resume 8:30 PM` (degrades to
   `⟳ waiting 20:00` when no usage data).
2. **Usage popup** — a ~320 px card anchored above the status item
   (VS Code will render this as a rich hover/quick-pick, so design it as a
   self-contained card): title row "Claude usage · updated 1m ago", then
   three labeled progress bars —
   **Session** `8% used · resets in 4h 29m`,
   **Weekly** `52% used · resets in 1d 10h`,
   **Model (Fable)** `92% used · resets in 1d 10h` —
   bar color green <60% / amber <85% / red ≥85%. Divider, then OUR line:
   **Next resume** `8:30 PM · claude-auto-resume · session 612fb08b` and a
   "Open dashboard" link. Design also the fallback variant where only the
   inferred session reset is known (single bar-less row).

## 4. Live data available to the page

Per task: status (waiting · resuming · running · limit-hit · done · failed
· cancelled), session_id + session title, importance tier, resume_at (ISO),
resume_mode (`at` | `auto`), resume_count/max_resumes, custom prompt,
journal events. Global: current workspace path, all projects (name+path),
per-project recent sessions (id, title/first-prompt summary, age, size),
health booleans (CLI, hooks, daemons), version. Usage (when available):
session/weekly/model utilization % + reset timestamps; else inferred
session reset time only. Countdown ticks every second — tabular numerals,
no layout shift.

## 5. Component consistency rules

- ONE composer component reused in both sections (current + other).
- ONE meter/progress component reused in usage strip, popup, attempts.
- ONE row component for schedules, workspaces, and timeline (varying
  slots), so the page reads as a system.
- Buttons: exactly one amber primary per screen; everything else
  secondary/ghost per VS Code button vars.

## 6. States to include in the mock

Onboarding mixed + all-green; dashboard with 2 schedules on the current
workspace (one auto-detect counting down, one fixed-time) + 1 other
workspace tracked; dashboard empty state (no schedules anywhere — composer
still primary, page must NOT look broken or hollow); usage strip present
and absent; popup full and fallback. A tiny fixed state-switcher bar at
the very top of the HTML is fine (dev-only chrome).

## 7. Visual tokens

Brand accent (fixed, not themed): **amber `#F59E0B`** — circular
resume-arrow mark on dark navy `#0B1220`. Use sparingly: mark, primary
button, selected chip, meter fill.

Status colors via theme vars: waiting `--vscode-charts-yellow` ·
resuming/running `--vscode-charts-blue` · done `--vscode-charts-green` ·
failed `--vscode-charts-red` · limit-hit `--vscode-charts-orange` ·
cancelled `--vscode-descriptionForeground`.

Neutrals — only these vars: `--vscode-editor-background`,
`--vscode-foreground`, `--vscode-descriptionForeground`,
`--vscode-editorWidget-background`, `--vscode-widget-border`,
`--vscode-input-background`, `--vscode-input-foreground`,
`--vscode-input-border`, `--vscode-button-background`,
`--vscode-button-foreground`, `--vscode-button-secondaryBackground`,
`--vscode-button-secondaryForeground`, `--vscode-textLink-foreground`,
`--vscode-badge-background`, `--vscode-badge-foreground`,
`--vscode-font-family`.

## 8. Deliverable

A single self-contained **HTML file** (inline CSS + minimal vanilla JS for
the countdown, chip behavior, composer reveal, and the dev state
switcher), using §7 vars for all neutrals, containing Screens A, B, C with
every state from §6, realistic sample data (workspace
`~/Documents/claude-auto-resume`, sessions like "Master Prompt — Claude
Auto-Resume Project — 612fb08b · 2h ago", the overnight journal). It will
be dropped into a VS Code webview nearly verbatim — no React, no Tailwind,
no external assets.
