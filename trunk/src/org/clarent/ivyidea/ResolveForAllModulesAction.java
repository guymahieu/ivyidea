package org.clarent.ivyidea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.clarent.ivyidea.intellij.IntellijDependencyUpdater;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.resolve.ResolvedDependency;
import org.clarent.ivyidea.resolve.Resolver;

import java.util.List;

public class ResolveForAllModulesAction extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        final Project project = DataKeys.PROJECT.getData(e.getDataContext());
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "IvyIDEA " + this.getTemplatePresentation().getText()) {
            public void run(ProgressIndicator indicator) {
                final IvyManager ivyManager = new IvyManager();
                for (final Module module : IntellijUtils.getAllModulesWithIvyIdeaFacet(project)) {
                    indicator.setText2("Resolving for module " + module.getName());
                    final List<ResolvedDependency> list = new Resolver(ivyManager).resolve(module);
                    updateIntellijModel(module, list);
                }
            }
        });
    }

    private void updateIntellijModel(final Module module, final List<ResolvedDependency> list) {
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

}
