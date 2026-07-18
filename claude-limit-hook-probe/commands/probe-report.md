---
description: Show the limit-hook-probe log (which hooks fired, with payloads)
---

Read the file `~/.claude/limit-hook-probe/hooks.log` and summarize it for me:

1. List every hook event that fired, in order, with timestamps.
2. For each event, show the key payload fields (session_id, hook_event_name, reason if present, transcript_path).
3. If any transcript tail contains a rate-limit or usage-limit message, quote that exact line and tell me which event captured it.
4. Tell me clearly: did Stop and/or SessionEnd fire around the limit hit, and what distinguishing signal exists in the payload or transcript that a detection script could match on?
