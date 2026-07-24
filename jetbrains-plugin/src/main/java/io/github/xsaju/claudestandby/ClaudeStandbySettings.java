package io.github.xsaju.claudestandby;

import com.intellij.ide.util.PropertiesComponent;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

final class ClaudeStandbySettings {
    private static final String CLI_PATH_KEY =
        "io.github.0xsaju.claudestandby.cliPath";

    private ClaudeStandbySettings() {
    }

    static @NotNull String configuredCliPath() {
        return PropertiesComponent.getInstance().getValue(CLI_PATH_KEY, "").trim();
    }

    static void setConfiguredCliPath(@NotNull String path) {
        PropertiesComponent.getInstance().setValue(CLI_PATH_KEY, path.trim(), "");
    }

    static @NotNull String resolveCliPath() {
        String configured = configuredCliPath();
        if (!configured.isEmpty()) {
            return configured;
        }

        String home = System.getProperty("user.home", "");
        Path localBin = Path.of(home, ".local", "bin", "claude-standby");
        if (Files.isExecutable(localBin)) {
            return localBin.toString();
        }

        Path managedBin = Path.of(home, ".claude-standby", "bin", "claude-standby");
        if (Files.isExecutable(managedBin)) {
            return managedBin.toString();
        }

        return "claude-standby";
    }
}
