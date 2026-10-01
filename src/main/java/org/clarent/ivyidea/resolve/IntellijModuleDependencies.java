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
import org.apache.ivy.core.module.id.ModuleId;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.ivy.IvyManager;

import java.util.logging.Logger;

/**
 * Holds the link between IntelliJ {@link com.intellij.openapi.module.Module}s and ivy
 * {@link org.apache.ivy.core.module.id.ModuleRevisionId}s.
 *
 * Looks up any workspace module by {@link ModuleId} via {@link IvyManager#getModuleForModuleId},
 * not just ones declared directly in this module's own ivy.xml -- a dependency resolved
 * transitively (e.g. this module depends on B, and B depends on workspace module C) is just as
 * much an "internal" dependency of this module as a direct one once Ivy has flattened the
 * dependency graph, and needs to be recognized as such so it becomes a module dependency in
 * IntelliJ instead of an unrecognized/failed external artifact.
 */
class IntellijModuleDependencies {

    private static final Logger LOGGER = Logger.getLogger(IntellijModuleDependencies.class.getName());

    private final IvyManager ivyManager;
    private final Module module;

    public IntellijModuleDependencies(Module module, IvyManager ivyManager) {
        this.module = module;
        this.ivyManager = ivyManager;
    }

    public Module getModule() {
        return module;
    }

    public boolean isInternalIntellijModuleDependency(ModuleId moduleId) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        return getModuleDependency(moduleId) != null;
    }

    public Module getModuleDependency(ModuleId moduleId) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        final Module dependencyModule = ivyManager.getModuleForModuleId(moduleId, module.getProject());
        if (dependencyModule == null || module.equals(dependencyModule)) {
            return null;
        }
        LOGGER.fine("Recognized dependency " + moduleId + " as intellij module '" + dependencyModule.getName() + "' in this project!");
        return dependencyModule;
    }

}
