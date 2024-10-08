/*
 * Copyright 2010 Guy Mahieu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.clarent.ivyidea;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.intellij.model.IntellijModuleWrapper;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;
import org.clarent.ivyidea.resolve.problem.ResolveProblem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * @author Guy Mahieu
 */
public abstract class AbstractResolveAction extends AnAction {

    protected void updateIntellijModel(final Module module, final List<ResolvedDependency> dependencies) {
        ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(() -> {
            try (IntellijModuleWrapper moduleWrapper = IntellijModuleWrapper.forModule(module)) {
                moduleWrapper.updateDependencies(dependencies);
            }
        }));
    }

    protected void clearConsole(final Project project) {
        ApplicationManager.getApplication().invokeLater(() -> IntellijUtils.getConsoleView(project).clear());
    }

    protected void reportProblems(final Module module, final List<ResolveProblem> problems) {
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

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

}

