/*
 * Copyright 2009 Guy Mahieu
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

package org.clarent.ivyidea.resolve.dependency;

import com.intellij.openapi.module.Module;
import org.clarent.ivyidea.intellij.model.IntellijModuleWrapper;

import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public class InternalDependency implements ResolvedDependency {

    private static final Logger LOGGER = Logger.getLogger(InternalDependency.class.getName());

    private Module module;

    public InternalDependency(Module module) {
        this.module = module;
    }

    public void addTo(IntellijModuleWrapper intellijModuleWrapper) {
        if (!intellijModuleWrapper.alreadyHasDependencyOnModule(module)) {
            LOGGER.info("Registering module dependency from " + intellijModuleWrapper.getModuleName() + " on module " + module.getName());
            intellijModuleWrapper.addModuleDependency(module);
        } else {
            LOGGER.info("Dependency from " + intellijModuleWrapper.getModuleName() + " on module " + module.getName() + " was already present; not reregistring");
        }
    }

}
