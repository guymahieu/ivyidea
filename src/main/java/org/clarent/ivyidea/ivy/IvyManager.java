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
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.settings.IvySettings;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Guy Mahieu
 */

public class IvyManager {

    private Map<Module, Ivy> configuredIvyInstances = new HashMap<Module, Ivy>();
    private Map<Module, ModuleDescriptor> moduleDescriptors = new HashMap<Module, ModuleDescriptor>();

    public Ivy getIvy(final Module module) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        if (!configuredIvyInstances.containsKey(module)) {
            final IvySettings configuredIvySettings = IvyIdeaConfigHelper.createConfiguredIvySettings(module);
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
}
