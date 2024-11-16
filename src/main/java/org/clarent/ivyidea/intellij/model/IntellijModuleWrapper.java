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

package org.clarent.ivyidea.intellij.model;

import com.google.common.collect.Streams;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.Library.ModifiableModel;
import com.intellij.openapi.roots.libraries.LibraryTable;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.resolve.dependency.ExternalDependency;
import org.clarent.ivyidea.resolve.dependency.InternalDependency;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;

import java.util.*;

public class IntellijModuleWrapper implements AutoCloseable {

    private final ModifiableRootModel intellijModule;
    private final LibraryModels libraryModels;

    public static IntellijModuleWrapper forModule(Module module) {
        ModifiableRootModel modifiableModel = null;
        try {
            modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
            return new IntellijModuleWrapper(modifiableModel);
        } catch (RuntimeException e) {
            if (modifiableModel != null) {
                modifiableModel.dispose();
            }
            throw e;
        }
    }

    private IntellijModuleWrapper(ModifiableRootModel intellijModule) {
        this.intellijModule = intellijModule;
        this.libraryModels = new LibraryModels(intellijModule);
    }

    public void updateDependencies(Collection<ExternalDependency> resolvedExternalDependencies, Collection<InternalDependency> resolvedInternalDependencies) {
        Streams.concat(resolvedExternalDependencies.stream(), resolvedInternalDependencies.stream()).forEach(resolvedDependency -> resolvedDependency.addTo(this));
        removeDependenciesNotInList(resolvedExternalDependencies, resolvedInternalDependencies);
    }

    public void close() {
        libraryModels.close();
        if (intellijModule.isChanged()) {
            intellijModule.commit();
        } else {
            intellijModule.dispose();
        }
    }

    public String getModuleName() {
        return intellijModule.getModule().getName();
    }

    public void addModuleDependency(Module module) {
        intellijModule.addModuleOrderEntry(module);
    }

    public void addExternalDependency(ExternalDependency externalDependency) {
        ModifiableModel libraryModel = libraryModels.getForExternalDependency(externalDependency);
        libraryModel.addRoot(externalDependency.getUrlForLibraryRoot(), externalDependency.getType());
    }

    public boolean alreadyHasDependencyOnModule(Module module) {
        final Module[] existingDependencies = intellijModule.getModuleDependencies();
        for (Module existingDependency : existingDependencies) {
            if (existingDependency.getName().equals(module.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean alreadyHasDependencyOnLibrary(ExternalDependency externalDependency) {
        ModifiableModel libraryModel = libraryModels.getForExternalDependency(externalDependency);
        for (String url : libraryModel.getUrls(externalDependency.getType())) {
            if (externalDependency.isSameDependency(url)) {
                return true;
            }
        }
        return false;
    }

    public void removeDependenciesNotInList(Collection<ExternalDependency> externalDependenciesToKeep, Collection<InternalDependency> internalDependenciesToKeep) {
        for (OrderRootType type : OrderRootType.getAllTypes()) {
            List<String> dependenciesToRemove = getDependenciesToRemove(type, externalDependenciesToKeep);
            for (String dependencyUrl : dependenciesToRemove) {
                libraryModels.removeDependency(type, dependencyUrl);
            }
        }

        // remove resolved libraries that are no longer used
        Set<String> librariesInUse = new HashSet<String>();
        for (ResolvedDependency dependency : externalDependenciesToKeep) {
            if (dependency instanceof ExternalDependency) {
                ExternalDependency externalDependency = (ExternalDependency) dependency;
                String library = IvyIdeaConfigHelper.getCreatedLibraryName(intellijModule, externalDependency.getConfigurationName());
                librariesInUse.add(library);
            }
        }

        final LibraryTable libraryTable = intellijModule.getModuleLibraryTable();
        for (Library library : libraryTable.getLibraries()) {
            final String libraryName = library.getName();
            if (IvyIdeaConfigHelper.isCreatedLibraryName(libraryName) && !librariesInUse.contains(libraryName)) {
                libraryTable.removeLibrary(library);
            }
        }

        Set<String> internalModulesToKeep = new HashSet<>(internalDependenciesToKeep.size());
        for (ResolvedDependency resolvedDependency : internalDependenciesToKeep) {
            String moduleName = ((InternalDependency) resolvedDependency).getModuleName();
            internalModulesToKeep.add(moduleName);
        }
        for (Module moduleDependency : intellijModule.getModuleDependencies()) {
            if (!internalModulesToKeep.contains(moduleDependency.getName())) {
                ModuleOrderEntry moduleOrderEntry = intellijModule.findModuleOrderEntry(moduleDependency);
                if (moduleOrderEntry != null) {
                    intellijModule.removeOrderEntry(moduleOrderEntry);
                }
            }
        }
    }

    private List<String> getDependenciesToRemove(OrderRootType type, Collection<ExternalDependency> resolvedExternalDependencies) {
        final List<String> intellijDependencies = libraryModels.getIntellijDependencyUrlsForType(type);
        final List<String> dependenciesToRemove = new ArrayList<>(intellijDependencies); // add all dependencies initially
        for (String intellijDependency : intellijDependencies) {
            for (ResolvedDependency resolvedDependency : resolvedExternalDependencies) {
                ExternalDependency externalDependency = (ExternalDependency) resolvedDependency;
                if (externalDependency.isSameDependency(intellijDependency)) {
                    dependenciesToRemove.remove(intellijDependency); // remove existing ones
                }
            }
        }
        return dependenciesToRemove;
    }

}
