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
        final Project project = DataKeys.PROJECT.getData(e.getDataContext());
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            public void run() {
                final IvyManager ivyManager = new IvyManager();
                for (final Module module : IntellijUtils.getAllModulesWithIvyIdeaFacet(project)) {
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

    private static class Resolver {

        private List<ResolvedDependency> dependencies;
        private IvyManager ivyManager;

        private Resolver(IvyManager ivyManager) {
            this.ivyManager = ivyManager;
        }

        public List<ResolvedDependency> resolve(final Module module) {
            ApplicationManager.getApplication().runReadAction(new Runnable() {
                public void run() {
                    dependencies = new DependencyResolver().resolve(module, ivyManager);
                }
            });
            return dependencies;
        }
    }
}
