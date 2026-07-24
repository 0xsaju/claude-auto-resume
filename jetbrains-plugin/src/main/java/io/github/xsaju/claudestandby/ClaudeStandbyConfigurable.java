package io.github.xsaju.claudestandby;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.Objects;

public final class ClaudeStandbyConfigurable implements SearchableConfigurable {
    private JBTextField cliPathField;
    private JPanel panel;

    @Override
    public @NotNull String getId() {
        return "io.github.0xsaju.claudestandby.settings";
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Claude Standby";
    }

    @Override
    public @Nullable JComponent createComponent() {
        cliPathField = new JBTextField();
        cliPathField.getEmptyText().setText(
            "Auto-detect PATH, ~/.local/bin, or ~/.claude-standby/bin"
        );
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("CLI executable:", cliPathField, 1, false)
            .addComponent(new JBLabel(
                "Leave empty to auto-detect claude-standby. This is an IDE-level setting."
            ))
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
        reset();
        return panel;
    }

    @Override
    public boolean isModified() {
        return cliPathField != null
            && !Objects.equals(
                cliPathField.getText().trim(),
                ClaudeStandbySettings.configuredCliPath()
            );
    }

    @Override
    public void apply() {
        if (cliPathField != null) {
            ClaudeStandbySettings.setConfiguredCliPath(cliPathField.getText());
        }
    }

    @Override
    public void reset() {
        if (cliPathField != null) {
            cliPathField.setText(ClaudeStandbySettings.configuredCliPath());
        }
    }

    @Override
    public void disposeUIResources() {
        cliPathField = null;
        panel = null;
    }
}
