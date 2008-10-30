package org.clarent.ivyidea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.apache.ivy.util.MessageLogger;
import org.clarent.ivyidea.intellij.IntellijDependencyUpdater;
import org.clarent.ivyidea.intellij.ui.IvyIdeaToolWindow;
import org.clarent.ivyidea.resolve.ResolvedDependency;

import java.util.List;

/**
 * @author Guy Mahieu
 */
public abstract class AbstractResolveAction extends AnAction {

    protected void updateIntellijModel(final Module module, final List<ResolvedDependency> list) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        IntellijDependencyUpdater.updateDependencies(module, list);
                    }
                });
            }
        });
    }

    protected MessageLogger buildIvyListener(final Project project) {
        return null;
//        return new AbstractMessageLogger() {
//
//
//            protected void doProgress() {
//            }
//
//            protected void doEndProgress(String msg) {
//            }
//
//            public void log(final String msg, final int level) {
//                logInEDT(msg, project);
//            }
//
//            public void rawlog(final String msg, final int level) {
//                logInEDT(msg, project);
//            }
//
//        };
    }

    private void logInEDT(final String msg, final Project project) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
                ToolWindow toolWindow = toolWindowManager.getToolWindow(IvyIdeaToolWindow.ID);
                final IvyIdeaToolWindow toolWindowComponent = (IvyIdeaToolWindow) toolWindow.getContentManager().findContent("Console").getComponent();
                toolWindowComponent.append(msg);
            }
        });
    }

}

