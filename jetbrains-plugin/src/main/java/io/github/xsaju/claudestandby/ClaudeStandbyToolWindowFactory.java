package io.github.xsaju.claudestandby;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public final class ClaudeStandbyToolWindowFactory
    implements ToolWindowFactory, DumbAware {
    static final String ID = "Claude Standby";

    @Override
    public void createToolWindowContent(
        @NotNull Project project,
        @NotNull ToolWindow toolWindow
    ) {
        DashboardPanel dashboard = new DashboardPanel(project);
        Content content = ContentFactory.getInstance()
            .createContent(dashboard.component(), "", false);
        Disposer.register(content, dashboard);
        toolWindow.getContentManager().addContent(content);
    }
}
