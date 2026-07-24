package io.github.xsaju.claudestandby;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;

final class CliRunner {
    private CliRunner() {
    }

    record Result(int exitCode, @NotNull String text, boolean notFound, boolean timedOut) {
    }

    static @NotNull Result run(
        @NotNull Project project,
        @NotNull List<String> arguments,
        int timeoutMillis
    ) {
        String executable = ClaudeStandbySettings.resolveCliPath();
        String workingDirectory = project.getBasePath();
        if (workingDirectory == null || workingDirectory.isBlank()) {
            workingDirectory = System.getProperty("user.home", ".");
        }

        GeneralCommandLine commandLine = new GeneralCommandLine()
            .withExePath(executable)
            .withParameters(arguments)
            .withWorkDirectory(workingDirectory)
            .withCharset(StandardCharsets.UTF_8);

        try {
            ProcessOutput output =
                new CapturingProcessHandler(commandLine).runProcess(timeoutMillis);
            String text = joinOutput(output.getStdout(), output.getStderr());
            if (output.isTimeout()) {
                return new Result(1, "Command timed out.", false, true);
            }
            return new Result(output.getExitCode(), text, false, false);
        } catch (ExecutionException exception) {
            String message = exception.getMessage() == null
                ? "Could not start claude-standby."
                : exception.getMessage();
            boolean notFound = message.contains("No such file")
                || message.contains("CreateProcess error=2")
                || message.contains("cannot find");
            return new Result(1, message, notFound, false);
        }
    }

    private static @NotNull String joinOutput(String stdout, String stderr) {
        String out = stdout == null ? "" : stdout.trim();
        String err = stderr == null ? "" : stderr.trim();
        if (out.isEmpty()) {
            return err;
        }
        if (err.isEmpty()) {
            return out;
        }
        return out + System.lineSeparator() + err;
    }
}
