#!/usr/bin/env bash
# setup-hooks.sh — register/remove the Stop/SessionEnd hooks directly in
# ~/.claude/settings.json (D20). This makes the Claude Code plugin
# unnecessary: settings-file hooks and plugin hooks are functionally
# identical, and the installer can write the former.
#
# Usage: setup-hooks.sh install [--force] | remove | status
#
# Safety rules:
#   - merge, never overwrite: only our entries (command contains
#     "on-stop.sh") are added/removed; everything else is preserved
#   - timestamped backup before every modification
#   - idempotent: installing twice adds nothing
#   - requires python3 to edit JSON safely; otherwise prints the manual
#     snippet instead of guessing with sed
#   - refuses to double-register when the Claude Code plugin already
#     provides the hooks (they would fire twice) unless --force
set -u
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck disable=SC1091
. "$SCRIPT_DIR/lib.sh" || { echo "setup-hooks: cannot load lib.sh"; exit 1; }

SETTINGS="${CLAUDE_SETTINGS_FILE:-$HOME/.claude/settings.json}"
PLUGIN_SCAN_DIR="${CLAUDE_AUTO_RESUME_PLUGIN_SCAN:-$HOME/.claude/plugins}"
ONSTOP="$SCRIPT_DIR/on-stop.sh"
MODE="${1:-install}"
FORCE="${2:-}"

hooks_registered() {
  [ -f "$SETTINGS" ] && grep -q "on-stop.sh" "$SETTINGS" 2>/dev/null
}

plugin_installed() {
  [ -d "$PLUGIN_SCAN_DIR" ] &&
    find "$PLUGIN_SCAN_DIR" -maxdepth 4 -name "*claude-auto-resume*" 2>/dev/null | grep -q .
}

manual_snippet() {
  cat <<EOF
Add this to the "hooks" section of $SETTINGS by hand:

  "Stop":       [ { "hooks": [ { "type": "command", "command": "bash \"$ONSTOP\" Stop",       "timeout": 10 } ] } ],
  "SessionEnd": [ { "hooks": [ { "type": "command", "command": "bash \"$ONSTOP\" SessionEnd", "timeout": 10 } ] } ]
EOF
}

edit_settings() {
  # $1: install | remove
  python3 - "$SETTINGS" "$ONSTOP" "$1" <<'PY'
import json, os, sys, tempfile

settings_path, onstop, op = sys.argv[1], sys.argv[2], sys.argv[3]
data = {}
if os.path.exists(settings_path):
    with open(settings_path) as fh:
        data = json.load(fh)

hooks = data.get("hooks") or {}

def strip_ours(event):
    kept = []
    for matcher in hooks.get(event, []):
        inner = [h for h in (matcher.get("hooks") or [])
                 if "on-stop.sh" not in str(h.get("command", ""))]
        if inner:
            m2 = dict(matcher)
            m2["hooks"] = inner
            kept.append(m2)
        elif not matcher.get("hooks"):
            kept.append(matcher)  # not ours; unusual shape — leave untouched
    if kept:
        hooks[event] = kept
    elif event in hooks:
        del hooks[event]

for ev in ("Stop", "SessionEnd"):
    strip_ours(ev)

if op == "install":
    for ev in ("Stop", "SessionEnd"):
        hooks.setdefault(ev, []).append({
            "hooks": [{
                "type": "command",
                "command": 'bash "%s" %s' % (onstop, ev),
                "timeout": 10,
            }]
        })

if hooks:
    data["hooks"] = hooks
elif "hooks" in data:
    del data["hooks"]

d = os.path.dirname(settings_path) or "."
os.makedirs(d, exist_ok=True)
fd, tmp = tempfile.mkstemp(dir=d, prefix=".settings-tmp-")
with os.fdopen(fd, "w") as fh:
    json.dump(data, fh, indent=2)
    fh.write("\n")
os.replace(tmp, settings_path)
PY
}

backup_settings() {
  [ -f "$SETTINGS" ] && cp "$SETTINGS" "$SETTINGS.car-backup-$(date +%Y%m%d-%H%M%S)"
  return 0
}

case "$MODE" in
  status)
    if hooks_registered; then
      echo "hooks: registered in $SETTINGS"
    else
      echo "hooks: not registered"
    fi
    exit 0
    ;;

  install)
    if hooks_registered; then
      echo "Hooks already registered in $SETTINGS."
      exit 0
    fi
    if [ "$FORCE" != "--force" ] && plugin_installed; then
      echo "The claude-auto-resume Claude Code plugin appears to be installed —"
      echo "its hooks already fire, and registering them in settings.json too"
      echo "would run everything twice. Either keep the plugin (nothing to do),"
      echo "or inside Claude Code run:"
      echo "  /plugin uninstall claude-auto-resume"
      echo "and then re-run: claude-auto-resume setup-hooks"
      exit 0
    fi
    if ! command -v python3 >/dev/null 2>&1; then
      echo "python3 is required to edit $SETTINGS safely — not found."
      manual_snippet
      exit 1
    fi
    backup_settings
    if ! edit_settings install; then
      echo "Could not edit $SETTINGS (invalid JSON?). Fix it, or add manually:"
      manual_snippet
      exit 1
    fi
    ar_log "setup-hooks: registered in $SETTINGS"
    echo "Hooks registered in $SETTINGS (Stop + SessionEnd → on-stop.sh)."
    echo "They take effect for new Claude Code sessions."
    exit 0
    ;;

  remove)
    if ! hooks_registered; then
      echo "Hooks not registered in $SETTINGS — nothing to remove."
      exit 0
    fi
    if ! command -v python3 >/dev/null 2>&1; then
      echo "python3 is required to edit $SETTINGS safely — not found."
      echo "Remove the entries whose command mentions on-stop.sh by hand."
      exit 1
    fi
    backup_settings
    if ! edit_settings remove; then
      echo "Could not edit $SETTINGS (invalid JSON?). Remove the on-stop.sh entries by hand."
      exit 1
    fi
    ar_log "setup-hooks: removed from $SETTINGS"
    echo "Hooks removed from $SETTINGS."
    exit 0
    ;;

  *)
    echo "Usage: setup-hooks.sh install [--force] | remove | status"
    exit 1
    ;;
esac
