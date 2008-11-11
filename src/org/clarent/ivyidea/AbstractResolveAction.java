package org.clarent.ivyidea;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.clarent.ivyidea.intellij.IntellijDependencyUpdater;
import org.clarent.ivyidea.intellij.ui.IvyIdeaToolWindow;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;
import org.clarent.ivyidea.resolve.problem.ResolveProblem;

import java.util.List;

/**
 * @author Guy Mahieu
 */
public abstract class AbstractResolveAction extends AnAction {

    protected void updateIntellijModel(final Module module, final List<ResolvedDependency> dependencies) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        IntellijDependencyUpdater.updateDependencies(module, dependencies);

                    }
                });
            }
        });
    }

    protected void clearConsole(final Project project) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                getConsoleView(project).clear();
            }
        });
    }

    protected void reportProblems(final Module module, final List<ResolveProblem> problems) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                final ConsoleView consoleView = getConsoleView(module.getProject());
                if (problems.isEmpty()) {
                    consoleView.print("No problems detected during resolve for module '" + module.getName() + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
                } else {
                    consoleView.print("Problems for module '" + module.getName() + "':" + '\n', ConsoleViewContentType.NORMAL_OUTPUT);
                    for (ResolveProblem resolveProblem : problems) {
                        consoleView.print("\t" + resolveProblem.toString() + '\n', ConsoleViewContentType.ERROR_OUTPUT);
                    }
                    // Make sure the toolwindow becomes visible if there were problems
                    getToolWindow(module.getProject()).show(null);
                }
            }
        });
    }

    private ConsoleView getConsoleView(Project project) {
        ToolWindow toolWindow = getToolWindow(project);
        return (ConsoleView) toolWindow.getContentManager().findContent("Console").getComponent();
    }

    private ToolWindow getToolWindow(Project project) {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        return toolWindowManager.getToolWindow(IvyIdeaToolWindow.ID);
    }
}

