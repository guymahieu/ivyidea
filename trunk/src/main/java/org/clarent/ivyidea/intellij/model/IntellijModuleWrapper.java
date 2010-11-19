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

package org.clarent.ivyidea.intellij.model;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library.ModifiableModel;
import com.intellij.openapi.vfs.VirtualFile;
import org.clarent.ivyidea.intellij.compatibility.IntellijCompatibilityService;
import org.clarent.ivyidea.resolve.dependency.ExternalDependency;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class IntellijModuleWrapper implements Closeable {

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
        removeDependenciesNotInList(resolvedDependencies);
        for (ResolvedDependency resolvedDependency : resolvedDependencies) {
            resolvedDependency.addTo(this);
        }
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
        libraryModel.addRoot(externalDependency.getVirtualFile(), externalDependency.getType());
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
        for (VirtualFile file : libraryModel.getFiles(externalDependency.getType())) {
            if (externalDependency.isSameDependency(file)) {
                return true;
            }
        }
        return false;
    }

    public void removeDependenciesNotInList(Collection<ResolvedDependency> dependenciesToKeep) {
        for (OrderRootType type : IntellijCompatibilityService.getCompatibilityMethods().getAllOrderRootTypes()) {
            List<VirtualFile> dependenciesToRemove = getDependenciesToRemove(type, dependenciesToKeep);
            for (VirtualFile virtualFile : dependenciesToRemove) {
                libraryModels.removeDependency(type, virtualFile);
            }
        }
    }

    private List<VirtualFile> getDependenciesToRemove(OrderRootType type, Collection<ResolvedDependency> resolvedDependencies) {
        final List<VirtualFile> intellijDependencies = libraryModels.getIntellijDependenciesForType(type);
        final List<VirtualFile> dependenciesToRemove = new ArrayList<VirtualFile>(intellijDependencies); // add all dependencies initially
        for (VirtualFile intellijDependency : intellijDependencies) {
            for (ResolvedDependency resolvedDependency : resolvedDependencies) {
                // TODO: We don't touch module to module dependencies here because we currently can't determine if
                //          they were added by IvyIDEA or by the user
                if (resolvedDependency instanceof ExternalDependency) {
                    ExternalDependency externalDependency = (ExternalDependency) resolvedDependency;
                    if (externalDependency.isSameDependency(intellijDependency)) {
                        dependenciesToRemove.remove(intellijDependency); // remove existing ones
                    }
                }
            }
        }
        return dependenciesToRemove;
    }

}
