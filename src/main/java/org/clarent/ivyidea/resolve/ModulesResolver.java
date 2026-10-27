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
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import com.intellij.openapi.project.Project;
import org.apache.ivy.Ivy;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.exception.IvyFileReadException;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.ivy.IvyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Resolves the IvyIDEA modules of a project, one by one or in parallel.
 */
class ModulesResolver {

    // how long to wait at a time before checking whether the resolve is cancelled
    private static final long WAIT_MILLIS = 10;

    private final Project project;
    private final IvyManager ivyManager;
    private final ProgressIndicator indicator;

    ModulesResolver(Project project, IvyManager ivyManager, ProgressIndicator indicator) {
        this.project = project;
        this.ivyManager = ivyManager;
        this.indicator = indicator;
    }

    List<IntellijDependencyResolver> resolveOneByOne(List<Module> modules)
            throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException {
        return resolve(modules, 1);
    }

    List<IntellijDependencyResolver> resolveInParallel(List<Module> modules)
            throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException {
        return resolve(modules, IvyIdeaConfigHelper.getResolveThreads(project));
    }

    /**
     * Resolves each module on a thread of its own, at most the given number of threads at a time, while the current
     * thread waits and stops them when the resolve is cancelled. These are threads of their own rather than pooled
     * threads, as Ivy interrupts the thread of a cancelled resolve and waits for it to end.
     *
     * @throws ProcessCanceledException when the resolve is cancelled, after the threads have ended
     */
    private List<IntellijDependencyResolver> resolve(List<Module> modules, int maxThreads)
            throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException {
        final IntellijDependencyResolver[] resolvers = new IntellijDependencyResolver[modules.size()];
        final Throwable[] failures = new Throwable[modules.size()];
        final Semaphore freeThreads = new Semaphore(maxThreads);
        final Map<Thread, Ivy> runningIvys = new ConcurrentHashMap<>();
        final Set<String> resolvingModules = Collections.synchronizedSet(new LinkedHashSet<>());
        final AtomicInteger resolvedModules = new AtomicInteger();
        final AtomicBoolean failed = new AtomicBoolean();
        final AtomicBoolean warnedAboutLocking = new AtomicBoolean();
        final List<Thread> threads = new ArrayList<>();

        indicator.setIndeterminate(false);
        try {
            for (int i = 0; i < modules.size(); i++) {
                ProgressIndicatorUtils.awaitWithCheckCanceled(() -> freeThreads.tryAcquire(WAIT_MILLIS, TimeUnit.MILLISECONDS));
                if (failed.get()) {
                    break;
                }

                final int index = i;
                final Module module = modules.get(i);
                final Thread thread = new Thread(() -> {
                    resolvingModules.add(module.getName());
                    showProgress(resolvingModules, resolvedModules.get(), modules.size());
                    try {
                        if (module.isDisposed()) {
                            // removed from the project since the resolve started, so there is nothing to resolve
                            return;
                        }
                        final Ivy ivy = ivyManager.getIvy(module);
                        if (maxThreads > 1 && IvyUtil.usesNoLockStrategy(ivy) && warnedAboutLocking.compareAndSet(false, true)) {
                            ResolveActionHelper.printWarning(project, "Resolving in parallel, but the Ivy cache doesn't lock the files it writes "
                                    + "(no lockStrategy in the Ivy settings). This can corrupt the Ivy cache.");
                        }
                        runningIvys.put(Thread.currentThread(), ivy);
                        if (indicator.isCanceled()) {
                            // cancelled before the registration, so this Ivy might not be interrupted
                            return;
                        }
                        final IntellijDependencyResolver resolver = new IntellijDependencyResolver(ivyManager);
                        resolver.resolve(module);
                        resolvers[index] = resolver;
                    } catch (Throwable t) {
                        // a module removed from the project while resolving has nothing to update, so it doesn't fail the others
                        if (!module.isDisposed()) {
                            failures[index] = t;
                            failed.set(true);
                        }
                    } finally {
                        runningIvys.remove(Thread.currentThread());
                        resolvingModules.remove(module.getName());
                        showProgress(resolvingModules, resolvedModules.incrementAndGet(), modules.size());
                        freeThreads.release();
                    }
                }, "IvyIDEA resolve " + module.getName());
                thread.setDaemon(true);
                threads.add(thread);
                thread.start();
            }
            // the threads give back their permit when they end
            ProgressIndicatorUtils.awaitWithCheckCanceled(() -> freeThreads.tryAcquire(maxThreads, WAIT_MILLIS, TimeUnit.MILLISECONDS));
            // a thread that was cancelled before it started resolving ends without a result
            indicator.checkCanceled();
        } catch (ProcessCanceledException e) {
            interrupt(runningIvys);
            join(threads);
            throw e;
        }

        // the first failure in module order, as when resolving the modules one by one
        for (Throwable failure : failures) {
            if (failure != null) {
                rethrow(failure);
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

    /**
     * Interrupts the given Ivys all at the same time, as each interrupt waits until its thread has ended.
     */
    private static void interrupt(Map<Thread, Ivy> runningIvys) {
        for (Map.Entry<Thread, Ivy> runningIvy : runningIvys.entrySet()) {
            final Thread interrupter = new Thread(() -> interrupt(runningIvy.getValue(), runningIvy.getKey()), "IvyIDEA interrupt");
            interrupter.setDaemon(true);
            interrupter.start();
        }
    }

    private static void interrupt(Ivy ivy, Thread thread) {
        try {
            ivy.interrupt(thread);
        } catch (UnsupportedOperationException e) {
            // when the thread is still running after the interrupt timeout of the Ivy settings, Ivy tries to stop it
            // with Thread.stop, which Java no longer supports; Ivy stays marked as interrupted, so the thread still
            // stops at the next check of Ivy
        }
    }

    private static void join(List<Thread> threads) {
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void showProgress(Set<String> resolvingModules, int resolvedModules, int modules) {
        final String resolving = String.join(", ", resolvingModules.toArray(new String[0]));
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
