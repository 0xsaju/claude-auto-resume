#!/usr/bin/env bash
# log-hook.sh — capture everything about a hook firing.
# Usage (from hooks.json): log-hook.sh <EventName>
# Appends to ~/.claude/limit-hook-probe/hooks.log
# Never blocks Claude Code: always exits 0.

set -u
EVENT="${1:-unknown}"
LOG_DIR="${HOME}/.claude/limit-hook-probe"
LOG_FILE="${LOG_DIR}/hooks.log"
mkdir -p "${LOG_DIR}"

TS="$(date '+%Y-%m-%d %H:%M:%S %z')"

# Read the JSON payload Claude Code sends on stdin (may be empty).
PAYLOAD="$(cat 2>/dev/null || true)"

{
  echo "════════════════════════════════════════════════════════"
  echo "EVENT   : ${EVENT}"
  echo "TIME    : ${TS}"
  echo "PAYLOAD :"
  if command -v jq >/dev/null 2>&1 && [ -n "${PAYLOAD}" ]; then
    echo "${PAYLOAD}" | jq . 2>/dev/null || echo "${PAYLOAD}"
  else
    echo "${PAYLOAD:-<empty>}"
  fi
} >> "${LOG_FILE}"

# If the payload includes a transcript path, capture its tail —
# this is where the limit-hit message will (or won't) appear.
if [ -n "${PAYLOAD}" ]; then
  if command -v jq >/dev/null 2>&1; then
    TRANSCRIPT="$(echo "${PAYLOAD}" | jq -r '.transcript_path // empty' 2>/dev/null)"
  else
    # Fallback without jq: pull "transcript_path":"..." with sed
    TRANSCRIPT="$(echo "${PAYLOAD}" | sed -n 's/.*"transcript_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
  fi
  if [ -n "${TRANSCRIPT}" ] && [ -f "${TRANSCRIPT}" ]; then
    {
      echo "TRANSCRIPT TAIL (last 40 lines of ${TRANSCRIPT}):"
      tail -n 40 "${TRANSCRIPT}"
      echo "── end transcript tail ──"
    } >> "${LOG_FILE}"
  fi
fi

echo "" >> "${LOG_FILE}"
exit 0
