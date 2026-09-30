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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.exception.IvyFileReadException;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.task.IvyIdeaResolveBackgroundTask;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.resolve.IntellijDependencyResolver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Action to resolve the dependencies for all modules that have an IvyIDEA facet configured.
 *
 * Resolves modules concurrently on a bounded worker pool: ivy.resolve() for one module is
 * almost entirely independent of any other module's resolve (separate Ivy engine, separate
 * dependency graph), and profiling showed that time evenly spread across all modules rather
 * than concentrated in a few -- exactly the shape of workload parallelism helps with. The
 * IvyManager instance (and its caches) is shared across all worker threads by design, so that
 * every workspace module's settings/descriptor are still only ever built once no matter how
 * many other modules depend on it; see IvyManager for the thread-safety measures that requires.
 * Parallelism can be turned off via the "Resolve modules in parallel" project setting, which
 * pins the worker pool to a single thread (module dependencies still resolve sequentially in
 * that case, just on the same worker instead of one-by-one on the EDT).
 *
 * Known limitation: cancelling mid-resolve no longer reliably interrupts every in-flight
 * worker (ProgressMonitorThread's interrupt mechanism was built for a single resolve thread).
 * Cancellation still stops new work from being scheduled, but already-running workers finish.
 *
 * @author Guy Mahieu
 */
public class ResolveForAllModulesAction extends AbstractResolveAction {

    private static final Logger LOG = Logger.getLogger(ResolveForAllModulesAction.class.getName());

    private static final int MAX_RESOLVE_THREADS = Runtime.getRuntime().availableProcessors();

    public void actionPerformed(AnActionEvent e) {
        FileDocumentManager.getInstance().saveAllDocuments();

        final Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
        ProgressManager.getInstance().run(new IvyIdeaResolveBackgroundTask(project, e) {
            public void doResolve(final @NotNull ProgressIndicator indicator) throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException {
                clearConsole(myProject);

                final IvyManager ivyManager = new IvyManager();

                final Module[] modules = ReadAction.compute(() -> IntellijUtils.getAllModulesWithIvyIdeaFacet(project));
                final int maxThreads = IvyIdeaConfigHelper.isParallelResolveEnabled(project) ? MAX_RESOLVE_THREADS : 1;
                final int threadCount = Math.max(1, Math.min(maxThreads, modules.length));

                final List<IntellijDependencyResolver> resolvers = new CopyOnWriteArrayList<>();
                final AtomicInteger moduleCount = new AtomicInteger();
                final Set<String> modulesInProgress = new ConcurrentSkipListSet<>();

                final ExecutorService executor = Executors.newFixedThreadPool(threadCount, runnable -> {
                    Thread thread = new Thread(runnable, "IvyIDEA-resolve-worker");
                    thread.setDaemon(true);
                    return thread;
                });
                try {
                    final List<Future<?>> futures = new ArrayList<>();
                    for (final Module module : modules) {
                        if (indicator.isCanceled()) {
                            break;
                        }
                        futures.add(executor.submit(() -> resolveOneModule(module, ivyManager, indicator, resolvers, moduleCount, modulesInProgress)));
                    }
                    for (Future<?> future : futures) {
                        try {
                            future.get();
                        } catch (InterruptedException | ExecutionException ex) {
                            LOG.warning("Resolve worker task failed unexpectedly: " + ex.getMessage());
                        }
                    }
                } finally {
                    executor.shutdownNow();
                }

                if (indicator.isCanceled()) {
                    return;
                }

                updateIntellijModel(resolvers);
                for (IntellijDependencyResolver resolver : resolvers) {
                    reportProblems(resolver.getModule(), resolver.getProblems());
                }
            }

            private void resolveOneModule(Module module, IvyManager ivyManager, ProgressIndicator indicator,
                                           List<IntellijDependencyResolver> resolvers, AtomicInteger moduleCount,
                                           Set<String> modulesInProgress) {
                if (indicator.isCanceled()) {
                    return;
                }
                modulesInProgress.add(module.getName());
                updateProgressText(indicator, modulesInProgress);
                try {
                    // Note: with concurrent workers this only ever tracks one engine for
                    // cancel-interrupt purposes -- see class javadoc.
                    getProgressMonitorThread().setIvy(ivyManager.getIvy(module));

                    final IntellijDependencyResolver resolver = new IntellijDependencyResolver(ivyManager);
                    resolver.resolve(module);

                    resolvers.add(resolver);
                    moduleCount.incrementAndGet();
                } catch (IvySettingsNotFoundException | IvyFileReadException | IvySettingsFileReadException ex) {
                    LOG.warning("Failed to resolve module '" + module.getName() + "': " + ex.getMessage());
                } finally {
                    modulesInProgress.remove(module.getName());
                    updateProgressText(indicator, modulesInProgress);
                }
            }

            private void updateProgressText(ProgressIndicator indicator, Set<String> modulesInProgress) {
                // Snapshot for display purposes only -- other worker threads may add/remove
                // concurrently, so this is a best-effort view of what's in flight right now.
                indicator.setText2("Resolving " + modulesInProgress.size() + " module(s): " + String.join(", ", modulesInProgress));
            }
        });
    }

}
