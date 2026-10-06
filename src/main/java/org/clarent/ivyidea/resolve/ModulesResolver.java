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

package org.clarent.ivyidea.resolve;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.apache.ivy.Ivy;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.exception.IvyFileReadException;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.task.ProgressMonitorThread;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.ivy.IvyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Resolves the IvyIDEA modules of a project, one by one or in parallel.
 */
class ModulesResolver {

    private final Project project;
    private final IvyManager ivyManager;
    private final ProgressIndicator indicator;
    private final ProgressMonitorThread progressMonitor;

    ModulesResolver(Project project, IvyManager ivyManager, ProgressIndicator indicator, ProgressMonitorThread progressMonitor) {
        this.project = project;
        this.ivyManager = ivyManager;
        this.indicator = indicator;
        this.progressMonitor = progressMonitor;
    }

    List<IntellijDependencyResolver> resolveOneByOne(List<Module> modules)
            throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException {
        final List<IntellijDependencyResolver> resolvers = new ArrayList<>();
        indicator.setIndeterminate(false);
        for (final Module module : modules) {
            showProgress(Collections.singleton(module.getName()), resolvers.size(), modules.size());
            final IntellijDependencyResolver resolver = new IntellijDependencyResolver(ivyManager);
            progressMonitor.register(ivyManager.getIvy(module));
            try {
                resolver.resolve(module);
            } finally {
                progressMonitor.unregister();
            }
            resolvers.add(resolver);

            if (indicator.isCanceled()) {
                break;
            }
        }
        return resolvers;
    }

    /**
     * Resolves each module on a thread of its own, at most the configured number of threads at a time. These are
     * threads of their own rather than pooled threads, as Ivy waits for an interrupted thread to end when the
     * resolve is cancelled.
     */
    List<IntellijDependencyResolver> resolveInParallel(List<Module> modules)
            throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException {
        final IntellijDependencyResolver[] resolvers = new IntellijDependencyResolver[modules.size()];
        final Throwable[] failures = new Throwable[modules.size()];
        final Semaphore freeThreads = new Semaphore(IvyIdeaConfigHelper.getResolveThreads(project));
        final Set<String> resolvingModules = Collections.synchronizedSet(new LinkedHashSet<>());
        final AtomicInteger resolvedModules = new AtomicInteger();
        final AtomicBoolean failed = new AtomicBoolean();
        final AtomicBoolean warnedAboutLocking = new AtomicBoolean();
        final List<Thread> threads = new ArrayList<>();

        indicator.setIndeterminate(false);
        try {
            for (int i = 0; i < modules.size(); i++) {
                freeThreads.acquire();
                if (indicator.isCanceled() || failed.get()) {
                    break;
                }

                final int index = i;
                final Module module = modules.get(i);
                final Thread thread = new Thread(() -> {
                    resolvingModules.add(module.getName());
                    showProgress(resolvingModules, resolvedModules.get(), modules.size());
                    try {
                        final Ivy ivy = ivyManager.getIvy(module);
                        if (IvyUtil.usesNoLockStrategy(ivy) && warnedAboutLocking.compareAndSet(false, true)) {
                            ResolveActionHelper.printWarning(project, "Resolving in parallel, but the Ivy cache doesn't lock the files it writes "
                                    + "(no lockStrategy in the Ivy settings). This can corrupt the Ivy cache.");
                        }
                        progressMonitor.register(ivy);
                        if (indicator.isCanceled()) {
                            // cancelled before the registration, so the monitor might not interrupt this Ivy
                            return;
                        }
                        final IntellijDependencyResolver resolver = new IntellijDependencyResolver(ivyManager);
                        resolver.resolve(module);
                        resolvers[index] = resolver;
                    } catch (Throwable t) {
                        failures[index] = t;
                        failed.set(true);
                    } finally {
                        progressMonitor.unregister();
                        resolvingModules.remove(module.getName());
                        showProgress(resolvingModules, resolvedModules.incrementAndGet(), modules.size());
                        freeThreads.release();
                    }
                }, "IvyIDEA resolve " + module.getName());
                thread.setDaemon(true);
                threads.add(thread);
                thread.start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            indicator.cancel();
            return Collections.emptyList();
        }

        if (!indicator.isCanceled()) {
            // the first failure in module order, as when resolving the modules one by one
            for (Throwable failure : failures) {
                if (failure != null) {
                    rethrow(failure);
                }
            }
        }

        final List<IntellijDependencyResolver> result = new ArrayList<>();
        for (IntellijDependencyResolver resolver : resolvers) {
            if (resolver != null) {
                result.add(resolver);
            }
        }
        return result;
    }

    private void showProgress(Set<String> resolvingModules, int resolvedModules, int modules) {
        final String resolving;
        synchronized (resolvingModules) {
            resolving = String.join(", ", resolvingModules);
        }
        indicator.setFraction((double) resolvedModules / modules);
        indicator.setText("Resolved " + resolvedModules + " of " + modules + " modules");
        indicator.setText2("Resolving " + resolving);
    }

    private static void rethrow(Throwable failure) throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException {
        if (failure instanceof IvySettingsNotFoundException) {
            throw (IvySettingsNotFoundException) failure;
        }
        if (failure instanceof IvyFileReadException) {
            throw (IvyFileReadException) failure;
        }
        if (failure instanceof IvySettingsFileReadException) {
            throw (IvySettingsFileReadException) failure;
        }
        if (failure instanceof RuntimeException) {
            throw (RuntimeException) failure;
        }
        if (failure instanceof Error) {
            throw (Error) failure;
        }
        throw new RuntimeException(failure);
    }
}
