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

package org.clarent.ivyidea.intellij.task;

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Key;
import org.clarent.ivyidea.intellij.ui.IvyIdeaIcons;
import org.clarent.ivyidea.resolve.ResolveActionHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * A "Before launch" task of run configurations that resolves the dependencies of all modules.
 */
public class ResolveBeforeRunTaskProvider extends BeforeRunTaskProvider<ResolveBeforeRunTaskProvider.ResolveBeforeRunTask> implements DumbAware {

    // the run configurations store the task under this key
    private static final Key<ResolveBeforeRunTask> ID = Key.create("IvyIdeaResolve");

    @Override
    public Key<ResolveBeforeRunTask> getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "IvyIDEA: Resolve for All Modules";
    }

    @Override
    public Icon getIcon() {
        return IvyIdeaIcons.FACET;
    }

    @Override
    public ResolveBeforeRunTask createTask(@NotNull RunConfiguration runConfiguration) {
        return new ResolveBeforeRunTask();
    }

    /**
     * Called on a pooled thread, and waits until the resolve is done. A failed or cancelled resolve stops the launch.
     */
    @Override
    public boolean executeTask(@NotNull DataContext context, @NotNull RunConfiguration configuration,
                               @NotNull ExecutionEnvironment environment, @NotNull ResolveBeforeRunTask task) {
        final CompletableFuture<Boolean> resolved = new CompletableFuture<>();
        // starting a resolve saves the open documents, which must happen on the event dispatch thread
        ApplicationManager.getApplication().invokeAndWait(
                () -> ResolveActionHelper.resolveForProject(configuration.getProject(), null, resolved::complete));
        try {
            return resolved.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException e) {
            return false;
        }
    }

    public static class ResolveBeforeRunTask extends BeforeRunTask<ResolveBeforeRunTask> {

        ResolveBeforeRunTask() {
            super(ID);
        }
    }
}
