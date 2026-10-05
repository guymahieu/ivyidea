package org.clarent.ivyidea.resolve;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import org.clarent.ivyidea.exception.IvyFileReadException;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.intellij.model.IntellijModuleWrapper;
import org.clarent.ivyidea.intellij.task.IvyIdeaResolveBackgroundTask;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;
import org.clarent.ivyidea.resolve.problem.ResolveProblem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ResolveActionHelper {
    /**
     * Resolves the Ivy dependencies for the given Project.
     *
     * @param project     Mandatory: the Project to resolve for.
     * @param taskText    Optional: the text to show for this tasks' progress.
     */
    public static void resolveForProject(Project project, String taskText) {
        if (project == null)
            return;
        if (taskText == null)
            taskText = "resolve for project " + project.getName();

        FileDocumentManager.getInstance().saveAllDocuments();
        ProgressManager.getInstance().run(new IvyIdeaResolveBackgroundTask(project, taskText) {
            public void doResolve(@NotNull ProgressIndicator indicator) throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException {
                clearConsole(myProject);

                indicator.setText2("Loading IvyIDEA modules");
                final IvyManager ivyManager = IvyManager.forProject(myProject);

                Collection<IntellijDependencyResolver> resolvers = new ArrayList<>();
                for (final Module module : IntellijUtils.getAllModulesWithIvyIdeaFacet(project)) {
                    getProgressMonitorThread().setIvy(ivyManager.getIvy(module));
                    indicator.setText2("Resolving for module " + module.getName());
                    final IntellijDependencyResolver resolver = new IntellijDependencyResolver(ivyManager);
                    resolver.resolve(module);
                    resolvers.add(resolver);

                    if (indicator.isCanceled()) {
                        return;
                    }
                }

                for (IntellijDependencyResolver resolver : resolvers) {
                    Module module = resolver.getModule();
                    updateIntellijModel(module, resolver.getDependencies());
                    reportProblems(module, resolver.getProblems());
                }
            }
        });
    }

    /**
     * Resolves the Ivy dependencies for the given Module.
     *
     * @param module      Mandatory: the Module to resolve for.
     * @param taskText    Optional: the text to show for this tasks' progress.
     */
    public static void resolveForModule(Module module, String taskText) {
        if (module == null)
            return;
        if (taskText == null)
            taskText = "resolve for module " + module.getName();

        FileDocumentManager.getInstance().saveAllDocuments();
        Project project = module.getProject();
        ProgressManager.getInstance().run(new IvyIdeaResolveBackgroundTask(project, taskText) {
            public void doResolve(@NotNull ProgressIndicator progressIndicator) throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException {
                clearConsole(myProject);

                progressIndicator.setText2("Loading IvyIDEA modules");
                final IvyManager ivyManager = IvyManager.forProject(myProject);
                progressIndicator.setText2("Resolving for module " + module.getName());
                getProgressMonitorThread().setIvy(ivyManager.getIvy(module));

                final IntellijDependencyResolver resolver = new IntellijDependencyResolver(ivyManager);
                resolver.resolve(module);
                updateIntellijModel(module, resolver.getDependencies());
                reportProblems(module, resolver.getProblems());
            }
        });
    }

    public static void updateIntellijModel(final Module module, final List<ResolvedDependency> dependencies) {
        ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(() -> {
            try (IntellijModuleWrapper moduleWrapper = IntellijModuleWrapper.forModule(module)) {
                moduleWrapper.updateDependencies(dependencies);
            }
        }));
    }

    public static void clearConsole(final Project project) {
        ApplicationManager.getApplication().invokeLater(() -> IntellijUtils.getConsoleView(project).clear());
    }

    public static void reportProblems(final Module module, final List<ResolveProblem> problems) {
        ApplicationManager.getApplication().invokeLater(() -> {
            final IvyIdeaFacetConfiguration ivyIdeaFacetConfiguration = IvyIdeaFacetConfiguration.getInstance(module);
            if (ivyIdeaFacetConfiguration == null) {
                throw new RuntimeException("Internal error: module " + module.getName() + " does not seem to be have an IvyIDEA facet, but was included in the resolve process anyway.");
            }
            final ConsoleView consoleView = IntellijUtils.getConsoleView(module.getProject());
            String configsForModule;
            if (ivyIdeaFacetConfiguration.isOnlyResolveSelectedConfigs()) {
                final Set<String> configs = ivyIdeaFacetConfiguration.getConfigsToResolve();
                if (configs == null || configs.isEmpty()) {
                    configsForModule = "[No configurations selected!]";
                } else {
                    configsForModule = configs.toString();
                }
            } else {
                configsForModule = "[All configurations]";
            }
            if (problems.isEmpty()) {
                consoleView.print("No problems detected during resolve for module '" + module.getName() + "' " + configsForModule + ".\n", ConsoleViewContentType.NORMAL_OUTPUT);
            } else {
                consoleView.print("Problems for module '" + module.getName() + " " + configsForModule + "':" + '\n', ConsoleViewContentType.NORMAL_OUTPUT);
                for (ResolveProblem resolveProblem : problems) {
                    consoleView.print("\t" + resolveProblem.toString() + '\n', ConsoleViewContentType.ERROR_OUTPUT);
                }
                // Make sure the tool window becomes visible if there were problems
                IntellijUtils.getToolWindow(module.getProject()).show(null);
            }
        });
    }
}
