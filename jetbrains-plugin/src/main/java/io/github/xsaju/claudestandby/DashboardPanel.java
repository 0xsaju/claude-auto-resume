package io.github.xsaju.claudestandby;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

final class DashboardPanel implements Disposable {
    private final Project project;
    private final JPanel root = new JPanel(new BorderLayout(0, JBUI.scale(10)));
    private final JTextArea output = new JTextArea();
    private final JLabel activity = new JLabel("Ready");
    private final List<JButton> buttons = new ArrayList<>();
    private final AtomicBoolean busy = new AtomicBoolean(false);

    DashboardPanel(@NotNull Project project) {
        this.project = project;
        buildUi();
        refreshStatus();
    }

    @NotNull JPanel component() {
        return root;
    }

    private void buildUi() {
        root.setBorder(JBUI.Borders.empty(12));

        JPanel heading = new JPanel(new GridLayout(0, 1, 0, JBUI.scale(3)));
        JLabel title = new JLabel("Claude Standby");
        Font titleFont = title.getFont();
        title.setFont(titleFont.deriveFont(Font.BOLD, titleFont.getSize2D() + 3f));
        heading.add(title);
        heading.add(new JLabel("Auto-resume cockpit for this project"));
        heading.add(activity);
        root.add(heading, BorderLayout.NORTH);

        output.setEditable(false);
        output.setLineWrap(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, output.getFont().getSize()));
        output.setBorder(JBUI.Borders.empty(8));
        JScrollPane scrollPane = new JScrollPane(output);
        scrollPane.setBorder(BorderFactory.createEtchedBorder());
        root.add(scrollPane, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(0, 1, 0, JBUI.scale(5)));
        JPanel primary = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(6), 0));
        primary.add(button("Refresh", this::refreshStatus));
        primary.add(button("Schedule…", this::schedule));
        primary.add(button("Cancel", this::cancel));
        actions.add(primary);

        JPanel secondary = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(6), 0));
        secondary.add(button("Log", () -> runCommand(
            "Loading log", List.of("log", "200"), 15_000
        )));
        secondary.add(button("Doctor", () -> runCommand(
            "Running diagnostics", List.of("doctor"), 20_000
        )));
        secondary.add(button("Check update", () -> runCommand(
            "Checking for updates", List.of("update", "--check"), 20_000
        )));
        secondary.add(button("Settings", () ->
            ShowSettingsUtil.getInstance().showSettingsDialog(
                project,
                ClaudeStandbyConfigurable.class
            )
        ));
        actions.add(secondary);
        root.add(actions, BorderLayout.SOUTH);
    }

    private JButton button(@NotNull String label, @NotNull Runnable action) {
        JButton button = new JButton(label);
        button.addActionListener(ignored -> action.run());
        buttons.add(button);
        return button;
    }

    private void refreshStatus() {
        runCommand("Refreshing status", List.of("status"), 15_000);
    }

    private void schedule() {
        String when = Messages.showInputDialog(
            project,
            "When should this project resume? Examples: auto, reset, 30m, 2h30m, 20:00, now",
            "Schedule Claude Standby",
            Messages.getQuestionIcon(),
            "auto",
            null
        );
        if (when == null) {
            return;
        }
        when = when.trim();
        if (when.isEmpty() || when.contains("\n") || when.contains("\r")) {
            Messages.showErrorDialog(
                project,
                "Enter one valid time expression.",
                "Claude Standby"
            );
            return;
        }
        runCommand("Scheduling resume", List.of("resume-at", when), 20_000);
    }

    private void cancel() {
        int answer = Messages.showYesNoDialog(
            project,
            "Cancel this project's waiting or in-flight resume?",
            "Cancel Claude Standby Task",
            "Cancel task",
            "Keep task",
            Messages.getWarningIcon()
        );
        if (answer == Messages.YES) {
            runCommand("Cancelling task", List.of("cancel"), 20_000);
        }
    }

    private void runCommand(
        @NotNull String label,
        @NotNull List<String> arguments,
        int timeoutMillis
    ) {
        if (!busy.compareAndSet(false, true)) {
            return;
        }
        setBusy(true, label + "…");

        CompletableFuture
            .supplyAsync(
                () -> CliRunner.run(project, arguments, timeoutMillis),
                AppExecutorUtil.getAppExecutorService()
            )
            .whenComplete((result, failure) ->
                ApplicationManager.getApplication().invokeLater(() -> {
                    busy.set(false);
                    if (project.isDisposed()) {
                        return;
                    }
                    setBusy(false, "Ready");
                    if (failure != null) {
                        output.setText("Command failed: " + failure.getMessage());
                    } else {
                        renderResult(result);
                    }
                    output.setCaretPosition(0);
                })
            );
    }

    private void renderResult(@NotNull CliRunner.Result result) {
        if (result.notFound()) {
            output.setText(
                "claude-standby was not found.\n\n"
                    + "Install it with:\n"
                    + "curl -fsSL https://raw.githubusercontent.com/0xsaju/"
                    + "claude-standby/main/install.sh | bash\n\n"
                    + "Or set its executable path under Settings → Tools → "
                    + "Claude Standby."
            );
            activity.setText("CLI not found");
            return;
        }

        String text = result.text().isBlank()
            ? "(command produced no output)"
            : result.text();
        output.setText(text);
        activity.setText(result.exitCode() == 0 ? "Ready" : "Command exited with an error");
    }

    private void setBusy(boolean value, @NotNull String message) {
        activity.setText(message);
        for (JButton button : buttons) {
            button.setEnabled(!value);
        }
    }

    @Override
    public void dispose() {
        buttons.clear();
    }
}
