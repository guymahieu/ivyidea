package org.clarent.ivyidea;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import org.clarent.ivyidea.exception.IvyFileReadException;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.task.IvyIdeaResolveBackgroundTask;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.resolve.IntellijDependencyResolver;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Action to resolve the dependencies for all modules that have an IvyIDEA facet configured.
 *
 * @author Guy Mahieu
 */
public class ResolveForAllModulesAction extends AbstractResolveAction {

    public void actionPerformed(AnActionEvent e) {
        final Project project = DataKeys.PROJECT.getData(e.getDataContext());
        ProgressManager.getInstance().run(new IvyIdeaResolveBackgroundTask(e) {
            public void doResolve(@NotNull ProgressIndicator indicator) throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException {
                clearConsole(myProject);
                final IvyManager ivyManager = new IvyManager();
                for (final Module module : IntellijUtils.getAllModulesWithIvyIdeaFacet(project)) {
                    indicator.setText2("Resolving for module " + module.getName());
                    final IntellijDependencyResolver resolver = new IntellijDependencyResolver(ivyManager);
                    final List<ResolvedDependency> list = resolver.resolve(module);
                    updateIntellijModel(module, list);
                    reportProblems(module, resolver.getProblems());
                }
            }
        });
    }

}
