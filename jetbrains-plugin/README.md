# Claude Standby for JetBrains IDEs

Native IntelliJ Platform tool window for the
[`claude-standby`](https://github.com/0xsaju/claude-standby) CLI.

The plugin provides workspace status, scheduling, cancellation, recent logs,
diagnostics, and CLI update checks. It does not duplicate the engine: every
state-changing operation is dispatched to `claude-standby` as an argument
vector in the current project directory.

## Requirements

- An IntelliJ Platform IDE based on build 233 or newer
- macOS or Linux
- The `claude-standby` CLI

The plugin checks PATH, `~/.local/bin/claude-standby`, and
`~/.claude-standby/bin/claude-standby`. Override the executable under
**Settings → Tools → Claude Standby**.

## Build

```sh
./gradlew clean buildPlugin verifyPlugin
```

The Marketplace upload archive is written to `build/distributions/`.

Install it locally with **Settings → Plugins → ⚙ → Install Plugin from
Disk…** before uploading it to JetBrains Marketplace.

JetBrains recommends signing Marketplace submissions. Keep signing credentials
outside the repository, provide `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, and
`PRIVATE_KEY_PASSWORD` in the local environment, then run:

```sh
./gradlew signPlugin
```

Do not commit the certificate private key.
