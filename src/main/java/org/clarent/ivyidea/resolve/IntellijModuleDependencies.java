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
import org.clarent.ivyidea.ivy.IvyManager;

import java.util.logging.Logger;

/**
 * Holds the link between IntelliJ {@link com.intellij.openapi.module.Module}s and ivy
 * {@link org.apache.ivy.core.module.id.ModuleRevisionId}s.
 * <p>
 * Every workspace module is recognized, not only the ones in the module's own ivy.xml, as dependencies on
 * workspace modules can also be transitive.
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

    public boolean isInternalIntellijModuleDependency(ModuleId moduleId) {
        return getModuleDependency(moduleId) != null;
    }

    public Module getModuleDependency(ModuleId moduleId) {
        final Module dependencyModule = ivyManager.getWorkspaceModule(moduleId);
        if (dependencyModule == null || module.equals(dependencyModule)) {
            return null;
        }
        LOGGER.fine("Recognized dependency " + moduleId + " as intellij module '" + dependencyModule.getName() + "' in this project!");
        return dependencyModule;
    }

}
