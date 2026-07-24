# Publishing the IDE extensions

All IDE extension publication is manual. No workflow in this repository
publishes to a marketplace.

## VS Code family

The cockpit (`vscode-extension/`) ships to **two** registries, because
VS Code and Cursor/Windsurf/VSCodium use different ones:

- **Microsoft Marketplace** — VS Code (`marketplace.visualstudio.com`)
- **Open VSX** — Cursor, Windsurf, VSCodium (`open-vsx.org`)

Publisher id is **`0xsaju`** (set in `package.json`); it must exist on each
registry under an account you control. Tokens are personal — never commit
them, and prefer running the publish commands yourself (they take a secret).

## One-time build

```sh
cd vscode-extension
rm -f *.vsix
npx @vscode/vsce package --no-dependencies -o claude-standby-cockpit-<version>.vsix
```

The `.vsix` is gitignored (build artifact). `.vscodeignore` keeps dev files
(other vsix files; the design brief now lives in docs/COCKPIT-DESIGN-BRIEF.md) out of the package.

## A. Microsoft Marketplace (VS Code)

1. Create a free Azure DevOps org: https://dev.azure.com
2. Create the `0xsaju` publisher: https://marketplace.visualstudio.com/manage
3. Azure DevOps → User Settings → **Personal Access Tokens** → New:
   scope **Marketplace → Manage**, organization **All accessible**.
4. Publish:
   ```sh
   npx @vscode/vsce publish -p <AZURE_PAT>
   # or: vsce login 0xsaju   (paste PAT once), then: vsce publish
   ```

## B. Open VSX (Cursor / Windsurf / VSCodium)

1. Sign in at https://open-vsx.org with GitHub; create an access token
   (Settings → Access Tokens).
2. First time only, claim the namespace:
   ```sh
   npx ovsx create-namespace 0xsaju -p <OVSX_TOKEN>
   ```
3. Publish the built vsix:
   ```sh
   npx ovsx publish claude-standby-cockpit-<version>.vsix -p <OVSX_TOKEN>
   ```

## JetBrains Marketplace

The JetBrains adapter is a separate native IntelliJ Platform plugin; a VSIX
cannot be uploaded to JetBrains Marketplace.

Build and verify it:

```sh
cd jetbrains-plugin
./gradlew clean buildPlugin verifyPluginProjectConfiguration verifyPlugin
```

The uploadable archive is
`build/distributions/claude-standby-jetbrains-<version>.zip`. It is ignored by
git. Before submission, install it in a clean target IDE with **Settings →
Plugins → ⚙ → Install Plugin from Disk…** and exercise every action.

JetBrains recommends signing the archive before Marketplace publication.
Keep the certificate chain/private key outside git, expose them locally as
`CERTIFICATE_CHAIN`, `PRIVATE_KEY`, and `PRIVATE_KEY_PASSWORD`, and run
`./gradlew signPlugin`. Never commit signing material.

For the first publication, sign in at https://plugins.jetbrains.com/, create
or select the vendor profile, accept the Marketplace Developer Agreement, and
choose **Upload plugin**. Upload the ZIP manually and provide the license/EULA,
source URL, description, tags, and compatible products. JetBrains reviews new
plugins and updates before they become public.

## Release checklist

- [ ] `bash test/run-tests.sh` green
- [ ] bump `VERSION`, `vscode-extension/package.json`, and
      `jetbrains-plugin/gradle.properties` to the same version
- [ ] rebuild the `.vsix` (above)
- [ ] `vsce publish` (Marketplace) and `ovsx publish` (Open VSX)
- [ ] rebuild, verify, locally install, and manually upload the JetBrains ZIP
- [ ] tag the release in git and note the version in `PROGRESS.md`

## Requirements already satisfied

`displayName`, `description`, `publisher`, `icon` (256×256 ≥ the 128 min),
`license`, `repository`, `homepage`, `bugs`, `engines.vscode`, `categories`
(`AI`, `Other`), `keywords`, a non-trivial `README.md`, and a `.vscodeignore`.
`vsce package` builds with no warnings.
