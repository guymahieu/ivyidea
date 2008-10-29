package org.clarent.ivyidea;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.task.IvyIdeaBackgroundTask;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.resolve.ResolvedDependency;
import org.clarent.ivyidea.resolve.Resolver;

import java.util.List;

public class ResolveForAllModulesAction extends AbstractResolveAction {

    public void actionPerformed(AnActionEvent e) {
        final Project project = DataKeys.PROJECT.getData(e.getDataContext());
        ProgressManager.getInstance().run(new IvyIdeaBackgroundTask(e) {
            public void run(ProgressIndicator indicator) {
                final IvyManager ivyManager = new IvyManager();
                for (final Module module : IntellijUtils.getAllModulesWithIvyIdeaFacet(project)) {
                    indicator.setText2("Resolving for module " + module.getName());
                    final List<ResolvedDependency> list = new Resolver(ivyManager).resolve(module, buildIvyListener(project));
                    updateIntellijModel(module, list);
                }
            }
        });
    }

}
