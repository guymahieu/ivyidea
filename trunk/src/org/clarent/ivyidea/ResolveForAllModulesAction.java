package org.clarent.ivyidea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.clarent.ivyidea.intellij.IntellijDependencyUpdater;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.resolve.DependencyResolver;
import org.clarent.ivyidea.resolve.ResolvedDependency;

import java.util.List;

public class ResolveForAllModulesAction extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        Project project = DataKeys.PROJECT.getData(e.getDataContext());
        IvyManager ivyManager = new IvyManager();
        for (final Module module : IntellijUtils.getAllModules(project)) {
            final List<ResolvedDependency> list = new DependencyResolver().resolve(module, ivyManager);
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                public void run() {
                    IntellijDependencyUpdater.updateDependencies(module, list);
                }
            });
        }
    }

}
