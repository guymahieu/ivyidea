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
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.settings.IvySettings;
import org.clarent.ivyidea.intellij.IntellijUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Maps the ModuleId declared in each workspace module's ivy.xml to the IntelliJ Module that
 * owns it. Built once, lazily, on the first lookup, and meant to be shared (via IvyManager)
 * across every WorkspaceModuleResolver created during a single resolve run: without this,
 * WorkspaceModuleResolver.getDependency() would otherwise scan and log every faceted module
 * in the workspace for every single dependency it tries to resolve.
 *
 * Safe for concurrent use: double-checked locking on a volatile reference means only the build
 * itself is ever synchronized (contested once, by whichever threads race to trigger it first);
 * every lookup afterward -- including from other threads -- is a lock-free map read.
 */
public class WorkspaceModuleIndex {

    private static final Logger LOG = Logger.getLogger(WorkspaceModuleIndex.class.getName());

    private volatile Map<ModuleId, Module> moduleIdToModule;

    public Module findModule(ModuleId moduleId, Project project, IvySettings settings,
                              Map<File, ModuleDescriptor> workspaceIvyFileCache) {
        Map<ModuleId, Module> index = moduleIdToModule;
        if (index == null) {
            synchronized (this) {
                index = moduleIdToModule;
                if (index == null) {
                    index = buildIndex(project, settings, workspaceIvyFileCache);
                    moduleIdToModule = index;
                }
            }
        }
        return index.get(moduleId);
    }

    private Map<ModuleId, Module> buildIndex(Project project, IvySettings settings,
                                              Map<File, ModuleDescriptor> workspaceIvyFileCache) {
        Map<ModuleId, Module> result = new HashMap<>();
        Module[] facetedModules = IntellijUtils.getAllModulesWithIvyIdeaFacet(project);
        LOG.info("Building workspace module index over " + facetedModules.length + " faceted modules");
        for (Module workspaceModule : facetedModules) {
            File ivyFile = IvyUtil.getIvyFile(workspaceModule);
            if (ivyFile == null || !ivyFile.exists()) {
                continue;
            }
            try {
                ModuleDescriptor workspaceMd = workspaceIvyFileCache.computeIfAbsent(ivyFile, f -> IvyUtil.parseIvyFile(f, settings));
                result.put(workspaceMd.getModuleRevisionId().getModuleId(), workspaceModule);
            } catch (RuntimeException e) {
                LOG.info("error parsing ivy file " + ivyFile + " while building workspace module index: " + e.getMessage());
            }
        }
        LOG.info("Workspace module index built with " + result.size() + " entries");
        return result;
    }
}
