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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Guy Mahieu
 */

public class IvyManager {

    private final Map<Module, Ivy> configuredIvyInstances = new HashMap<>();
    private final Map<Module, ModuleDescriptor> moduleDescriptors = new HashMap<>();
    private Map<ModuleId, Module> workspaceModules;

    private IvyManager() {
    }

    /**
     * Creates the IvyManager for resolving modules of the given project. When dependencies on other modules are
     * detected, this loads the IvyIDEA modules of the project whose ivy file can be parsed, each with the Ivy
     * settings of the module itself.
     */
    public static IvyManager forProject(Project project) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        final IvyManager ivyManager = new IvyManager();
        if (IvyIdeaConfigHelper.detectDependenciesOnOtherModulesWhileResolving(project)) {
            ivyManager.loadWorkspaceModules(project);
        }
        return ivyManager;
    }

    public Ivy getIvy(final Module module) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        if (!configuredIvyInstances.containsKey(module)) {
            final IvySettings configuredIvySettings = IvyIdeaConfigHelper.createConfiguredIvySettings(module, this);
            final Ivy ivy = IvyUtil.createConfiguredIvyEngine(module, configuredIvySettings);

            configuredIvyInstances.put(module, ivy);
        }
        return configuredIvyInstances.get(module);
    }

    @Nullable
    public ModuleDescriptor getModuleDescriptor(Module module) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        if (!moduleDescriptors.containsKey(module)) {
            final File ivyFile = IvyUtil.getIvyFile(module);
            if (ivyFile != null) {
                try {
                    final ModuleDescriptor descriptor = IvyUtil.parseIvyFile(ivyFile, getIvy(module));
                    moduleDescriptors.put(module, descriptor);
                } catch (RuntimeException e) {
                    // ignore
                    moduleDescriptors.put(module, null);
                }
            } else {
                moduleDescriptors.put(module, null);
            }
        }

        return moduleDescriptors.get(module);
    }

    private void loadWorkspaceModules(Project project) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        final Map<ModuleId, Module> modules = new HashMap<>();
        for (Module module : IntellijUtils.getAllModulesWithIvyIdeaFacet(project)) {
            final ModuleDescriptor descriptor = getModuleDescriptor(module);
            if (descriptor != null) {
                modules.put(descriptor.getModuleRevisionId().getModuleId(), module);
            }
        }
        workspaceModules = modules;
    }

    /**
     * Returns the workspace module with the given organisation and name, or {@code null} if there is none. While
     * the workspace modules are being loaded, this always returns {@code null}.
     *
     * @see #forProject(Project)
     */
    @Nullable
    public Module getWorkspaceModule(ModuleId moduleId) {
        return workspaceModules == null ? null : workspaceModules.get(moduleId);
    }

    /**
     * Returns the module descriptor of the workspace module with the given organisation and name.
     *
     * @see #getWorkspaceModule(ModuleId)
     */
    @Nullable
    public ModuleDescriptor getWorkspaceModuleDescriptor(ModuleId moduleId) {
        final Module module = getWorkspaceModule(moduleId);
        return module == null ? null : moduleDescriptors.get(module);
    }
}
