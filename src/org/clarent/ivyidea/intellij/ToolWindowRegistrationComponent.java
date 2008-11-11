package org.clarent.ivyidea.intellij;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.peer.PeerFactory;
import com.intellij.ui.content.Content;
import org.clarent.ivyidea.intellij.ui.IvyIdeaIcons;
import org.clarent.ivyidea.intellij.ui.IvyIdeaToolWindow;
import org.jetbrains.annotations.NotNull;

/**
 * @author Guy Mahieu
 */

public class ToolWindowRegistrationComponent implements ProjectComponent {

    private Project project;

    public ToolWindowRegistrationComponent(Project project) {
        this.project = project;
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "IvyIDEA.ToolWindowRegistrationComponent";
    }

    public void projectOpened() {
        registerToolWindow();
    }

    public void projectClosed() {
        unregisterToolWindow();
    }

    private void unregisterToolWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        toolWindowManager.unregisterToolWindow(IvyIdeaToolWindow.ID);
    }

    private void registerToolWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.registerToolWindow(IvyIdeaToolWindow.ID, false, ToolWindowAnchor.BOTTOM);
        final ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        Content content = PeerFactory.getInstance().getContentFactory().createContent(consoleView.getComponent(), "Console", true);
        toolWindow.setIcon(IvyIdeaIcons.MAIN_ICON_SMALL);
        toolWindow.getContentManager().addContent(content);
/*
        Content content = PeerFactory.getInstance().getContentFactory().createContent(consoleView.getComponent(), "Console", true);
        toolWindow.getContentManager().addContent(content);
*/
    }
    /*Hi Igor,

    I've solved it by using module.getModuleType().equals(com.intellij.openapi.module.StdModuleTypes.JAVA).

    Don't know if this is the 'preferred' way, but it seems to do the trick.

    Cheers,

    Guy*/
}
