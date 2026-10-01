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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.io.FileUtil;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.resolve.dependency.ExternalDependency;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;

import java.io.File;
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

    public void updateDependencies(Collection<ResolvedDependency> resolvedDependencies) {
        for (ResolvedDependency resolvedDependency : resolvedDependencies) {
            resolvedDependency.addTo(this);
        }
        removeDependenciesNotInList(resolvedDependencies);
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
        libraryModels.addRoot(externalDependency);
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
        return libraryModels.containsRoot(externalDependency);
    }

    public void removeDependenciesNotInList(Collection<ResolvedDependency> dependenciesToKeep) {
        final Set<String> pathsToKeep = getCanonicalLocalPaths(dependenciesToKeep);
        for (OrderRootType type : OrderRootType.getAllTypes()) {
            List<String> dependenciesToRemove = getDependenciesToRemove(type, pathsToKeep);
            for (String dependencyUrl : dependenciesToRemove) {
                libraryModels.removeDependency(type, dependencyUrl);
            }
        }

        // remove resolved libraries that are no longer used
        Set<String> librariesInUse = new HashSet<String>();
        for (ResolvedDependency dependency : dependenciesToKeep) {
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
    }

    private List<String> getDependenciesToRemove(OrderRootType type, Set<String> pathsToKeep) {
        final List<String> intellijDependencies = libraryModels.getIntellijDependencyUrlsForType(type);
        final List<String> dependenciesToRemove = new ArrayList<>();
        for (String intellijDependency : intellijDependencies) {
            final String path = LibraryModels.toCanonicalPath(intellijDependency);
            if (!pathsToKeep.contains(path)) {
                dependenciesToRemove.add(intellijDependency);
            }
        }
        return dependenciesToRemove;
    }

    /**
     * Returns the canonical local file paths of the given external dependencies, in a set created by
     * {@link LibraryModels#createPathSet()}.
     */
    private static Set<String> getCanonicalLocalPaths(Collection<ResolvedDependency> dependencies) {
        Set<String> paths = LibraryModels.createPathSet();
        for (ResolvedDependency dependency : dependencies) {
            if (dependency instanceof ExternalDependency) {
                File localFile = ((ExternalDependency) dependency).getLocalFile();
                if (localFile != null) {
                    paths.add(FileUtil.toCanonicalPath(localFile.getPath()));
                }
            }
        }
        return paths;
    }

}
