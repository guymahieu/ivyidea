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
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.ivy.IvyManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Holds the link between IntelliJ {@link com.intellij.openapi.module.Module}s and ivy
 * {@link org.apache.ivy.core.module.id.ModuleRevisionId}s
 */
class IntellijModuleDependencies {

    private static final Logger LOGGER = Logger.getLogger(IntellijModuleDependencies.class.getName());

    private IvyManager ivyManager;
    private Module module;
    private Map<ModuleId, Module> moduleDependencies = new LinkedHashMap<ModuleId, Module>();

    public IntellijModuleDependencies(Module module, IvyManager ivyManager) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        this.module = module;
        this.ivyManager = ivyManager;
        fillModuleDependencies();
    }

    public Module getModule() {
        return module;
    }

    public boolean isInternalIntellijModuleDependency(ModuleId moduleId) {
        return moduleDependencies.containsKey(moduleId);
    }

    public Module getModuleDependency(ModuleId moduleId) {
        return moduleDependencies.get(moduleId);
    }

    public Collection<Module> getModuleDependencies() {
        return Collections.unmodifiableCollection(moduleDependencies.values());
    }

    private void fillModuleDependencies() throws IvySettingsNotFoundException, IvySettingsFileReadException {
        final ModuleDescriptor descriptor = ivyManager.getModuleDescriptor(module);
        if (descriptor != null) {
            final DependencyDescriptor[] ivyDependencies = descriptor.getDependencies();
            final Module[] dependencyModules = IntellijUtils.getAllModulesWithIvyIdeaFacet(module.getProject());
            for (DependencyDescriptor ivyDependency : ivyDependencies) {
                for (Module dependencyModule : dependencyModules) {
                    if (!module.equals(dependencyModule)) {
                        final ModuleId ivyDependencyId = ivyDependency.getDependencyId();
                        final ModuleId dependencyModuleId = getModuleId(dependencyModule);
                        if (ivyDependencyId.equals(dependencyModuleId)) {
                            LOGGER.info("Recognized dependency " + ivyDependency + " as intellij module '" + dependencyModule.getName() + "' in this project!");
                            moduleDependencies.put(dependencyModuleId, dependencyModule);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Nullable
    private ModuleId getModuleId(Module module) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        if (!moduleDependencies.values().contains(module)) {
            final ModuleDescriptor ivyModuleDescriptor = ivyManager.getModuleDescriptor(module);
            if (ivyModuleDescriptor != null) {
                return ivyModuleDescriptor.getModuleRevisionId().getModuleId();
            }

        }
        for (ModuleId moduleId : moduleDependencies.keySet()) {
            if (module.equals(moduleDependencies.get(moduleId))) {
                return moduleId;
            }
        }
        return null;
    }


}
