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

package org.clarent.ivyidea.ivy;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.settings.IvySettings;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds per-run state shared across every module resolved through one "resolve" action.
 *
 * This is used concurrently: {@link org.clarent.ivyidea.ResolveForAllModulesAction} resolves
 * multiple modules in parallel on a worker thread pool, and every worker shares the SAME
 * IvyManager instance (that's the whole point -- so each workspace module's settings/ivy.xml
 * are only ever built/parsed once, no matter how many other modules also depend on it). All
 * caches below are therefore built for safe concurrent access: {@link ConcurrentHashMap} with
 * atomic compute-if-absent, so two threads racing to resolve the same underlying Module can
 * never see a half-built value or corrupt the map, and at worst redundantly compute the same
 * (pure) result once each.
 *
 * @author Guy Mahieu
 */

public class IvyManager {

    private final Map<Module, Ivy> configuredIvyInstances = new ConcurrentHashMap<Module, Ivy>();

    // ConcurrentHashMap can't hold null values, but "this module has no parseable ivy
    // descriptor" is a legitimate, cacheable result -- hence Optional as the map value.
    private final Map<Module, Optional<ModuleDescriptor>> moduleDescriptors = new ConcurrentHashMap<Module, Optional<ModuleDescriptor>>();

    // Shared across every module resolved through this IvyManager instance (i.e. for the
    // whole "resolve all modules" run), so each workspace module's ivy.xml is parsed at
    // most once, and the workspace is scanned to build the moduleId index at most once,
    // instead of once per dependency lookup per module.
    private final Map<File, ModuleDescriptor> workspaceIvyFileCache = new ConcurrentHashMap<File, ModuleDescriptor>();
    private final WorkspaceModuleIndex workspaceModuleIndex = new WorkspaceModuleIndex();

    // Maps each workspace module's own ivy ModuleId to the module that produces it. Built once,
    // lazily (double-checked-locking on the volatile reference below, so concurrent readers
    // after the first build never pay any locking cost), and shared across every module
    // resolved through this IvyManager instance, so that IntellijModuleDependencies can look up
    // "which workspace module satisfies this dependency" in O(1) instead of re-scanning every
    // workspace module for every declared dependency of every module being resolved.
    private volatile Map<ModuleId, Module> moduleIdIndex;

    @Nullable
    public Module getModuleForModuleId(ModuleId moduleId, Project project) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        Map<ModuleId, Module> index = moduleIdIndex;
        if (index == null) {
            synchronized (this) {
                index = moduleIdIndex;
                if (index == null) {
                    index = buildModuleIdIndex(project);
                    moduleIdIndex = index;
                }
            }
        }
        return index.get(moduleId);
    }

    private Map<ModuleId, Module> buildModuleIdIndex(Project project) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        Map<ModuleId, Module> index = new ConcurrentHashMap<ModuleId, Module>();
        for (Module workspaceModule : IntellijUtils.getAllModulesWithIvyIdeaFacet(project)) {
            final ModuleDescriptor descriptor = getModuleDescriptor(workspaceModule);
            if (descriptor != null) {
                index.put(descriptor.getModuleRevisionId().getModuleId(), workspaceModule);
            }
        }
        return index;
    }

    public Ivy getIvy(final Module module) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        try {
            return configuredIvyInstances.computeIfAbsent(module, this::buildIvyEngine);
        } catch (CheckedExceptionCarrier carrier) {
            throw carrier.rethrowChecked();
        }
    }

    private Ivy buildIvyEngine(Module module) {
        try {
            final IvySettings configuredIvySettings = IvyIdeaConfigHelper.createConfiguredIvySettings(module, workspaceIvyFileCache, workspaceModuleIndex);
            return IvyUtil.createConfiguredIvyEngine(module, configuredIvySettings);
        } catch (IvySettingsNotFoundException | IvySettingsFileReadException e) {
            throw new CheckedExceptionCarrier(e);
        }
    }

    @Nullable
    public ModuleDescriptor getModuleDescriptor(Module module) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        try {
            return moduleDescriptors.computeIfAbsent(module, this::computeModuleDescriptor).orElse(null);
        } catch (CheckedExceptionCarrier carrier) {
            throw carrier.rethrowChecked();
        }
    }

    private Optional<ModuleDescriptor> computeModuleDescriptor(Module module) {
        final File ivyFile = IvyUtil.getIvyFile(module);
        if (ivyFile == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(IvyUtil.parseIvyFile(ivyFile, getIvy(module)));
        } catch (RuntimeException e) {
            // ignore -- unparseable ivy file, treat as "no descriptor" like before
            return Optional.empty();
        } catch (IvySettingsNotFoundException | IvySettingsFileReadException e) {
            throw new CheckedExceptionCarrier(e);
        }
    }

    /**
     * Lets a checked exception cross a {@code computeIfAbsent} call (whose function can only
     * throw unchecked exceptions) so the public methods above can rethrow the original checked
     * exception type unchanged.
     */
    private static class CheckedExceptionCarrier extends RuntimeException {
        CheckedExceptionCarrier(Exception cause) {
            super(cause);
        }

        RuntimeException rethrowChecked() throws IvySettingsNotFoundException, IvySettingsFileReadException {
            final Throwable cause = getCause();
            if (cause instanceof IvySettingsNotFoundException) {
                throw (IvySettingsNotFoundException) cause;
            }
            if (cause instanceof IvySettingsFileReadException) {
                throw (IvySettingsFileReadException) cause;
            }
            return this;
        }
    }
}
