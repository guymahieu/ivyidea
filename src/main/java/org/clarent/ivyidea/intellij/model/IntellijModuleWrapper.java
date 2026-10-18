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
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.io.FileUtil;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.resolve.dependency.ExternalDependency;
import org.clarent.ivyidea.resolve.dependency.InternalDependency;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;

import java.io.File;
import java.util.*;

public class IntellijModuleWrapper implements AutoCloseable {

    private final ModifiableRootModel intellijModule;
    private final LibraryModels libraryModels;
    // decided once, as it may need to look at all modules
    private final boolean projectLibraries;
    // the project libraries this module no longer depends on, to delete when no other module uses them either
    private final List<Library> removedProjectLibraries = new ArrayList<>();
    // the IvyIDEA modules of the project that a resolve can add module dependencies on
    private Collection<Module> workspaceModules = Collections.emptySet();

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
        this.projectLibraries = IvyIdeaConfigHelper.isUseProjectLibraries(intellijModule.getProject());
        this.libraryModels = new LibraryModels(intellijModule, projectLibraries);
    }

    /**
     * Updates the dependencies of the module to the given ones. Module dependencies on the given IvyIDEA modules of
     * the project that aren't in the given dependencies are removed.
     */
    public void updateDependencies(Collection<ResolvedDependency> resolvedDependencies, Collection<Module> workspaceModules) {
        this.workspaceModules = workspaceModules;
        updateDependencies(resolvedDependencies);
    }

    public void updateDependencies(Collection<ResolvedDependency> resolvedDependencies) {
        if (projectLibraries) {
            // all at once, as each change of the project libraries is a separate change of the project model
            final Set<String> libraryNames = new HashSet<String>();
            for (ResolvedDependency resolvedDependency : resolvedDependencies) {
                if (resolvedDependency instanceof ExternalDependency && ((ExternalDependency) resolvedDependency).getLocalFile() != null) {
                    libraryNames.add(libraryModels.getLibraryName((ExternalDependency) resolvedDependency));
                }
            }
            ProjectLibraries.createMissing(intellijModule.getProject(), libraryNames);
        }
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
        ProjectLibraries.deleteUnused(intellijModule.getProject(), removedProjectLibraries);
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
        Set<String> configurations = new HashSet<String>();
        for (ResolvedDependency dependency : dependenciesToKeep) {
            if (dependency instanceof ExternalDependency) {
                ExternalDependency externalDependency = (ExternalDependency) dependency;
                String library = IvyIdeaConfigHelper.getCreatedLibraryName(intellijModule, externalDependency.getConfigurationName(), projectLibraries);
                librariesInUse.add(library);
                configurations.add(externalDependency.getConfigurationName());
            }
        }

        // the libraries are either module libraries or project libraries, so all libraries of the other kind are
        // left over from before the setting changed
        takeOverOrderEntries(configurations);
        final LibraryTable libraryTable = intellijModule.getModuleLibraryTable();
        for (Library library : libraryTable.getLibraries()) {
            final String libraryName = library.getName();
            if (IvyIdeaConfigHelper.isCreatedLibraryName(libraryName) && (projectLibraries || !librariesInUse.contains(libraryName))) {
                libraryTable.removeLibrary(library);
            }
        }
        removedProjectLibraries.addAll(ProjectLibraries.removeDependencies(intellijModule,
                projectLibraries ? librariesInUse : Collections.emptySet()));
        removeModuleDependenciesNotInList(dependenciesToKeep);
    }

    /**
     * Removes the module dependencies on IvyIDEA modules of the project that are no longer resolved: the ivy files
     * decide on the dependencies between them. Module dependencies on other modules, like the ones the user added,
     * are kept.
     */
    private void removeModuleDependenciesNotInList(Collection<ResolvedDependency> dependenciesToKeep) {
        final Set<Module> modulesToKeep = new HashSet<>();
        for (ResolvedDependency dependency : dependenciesToKeep) {
            if (dependency instanceof InternalDependency) {
                modulesToKeep.add(((InternalDependency) dependency).getModule());
            }
        }
        for (OrderEntry orderEntry : intellijModule.getOrderEntries()) {
            if (orderEntry instanceof ModuleOrderEntry) {
                final Module dependency = ((ModuleOrderEntry) orderEntry).getModule();
                if (dependency != null && workspaceModules.contains(dependency) && !modulesToKeep.contains(dependency)) {
                    intellijModule.removeOrderEntry(orderEntry);
                }
            }
        }
    }

    /**
     * Gives the new library entries the position, scope and export flag of the entries of the other kind of library
     * that they replace, after the setting changed.
     */
    private void takeOverOrderEntries(Set<String> configurations) {
        final Map<String, String> replacedNames = new HashMap<String, String>();
        final List<OrderEntry> orderEntries = new ArrayList<OrderEntry>(Arrays.asList(intellijModule.getOrderEntries()));
        for (String configuration : configurations) {
            final String newName = IvyIdeaConfigHelper.getCreatedLibraryName(intellijModule, configuration, projectLibraries);
            final String oldName = IvyIdeaConfigHelper.getCreatedLibraryName(intellijModule, configuration, !projectLibraries);
            final LibraryOrderEntry newEntry = findLibraryEntry(orderEntries, newName, projectLibraries);
            final LibraryOrderEntry oldEntry = findLibraryEntry(orderEntries, oldName, !projectLibraries);
            if (newEntry != null && oldEntry != null && !replacedNames.containsKey(newName)) {
                replacedNames.put(newName, oldName);
                orderEntries.remove(newEntry);
                orderEntries.add(orderEntries.indexOf(oldEntry), newEntry);
            }
        }
        if (replacedNames.isEmpty()) {
            return;
        }
        intellijModule.rearrangeOrderEntries(orderEntries.toArray(OrderEntry.EMPTY_ARRAY));
        final List<OrderEntry> rearranged = Arrays.asList(intellijModule.getOrderEntries());
        for (Map.Entry<String, String> names : replacedNames.entrySet()) {
            final LibraryOrderEntry newEntry = findLibraryEntry(rearranged, names.getKey(), projectLibraries);
            final LibraryOrderEntry oldEntry = findLibraryEntry(rearranged, names.getValue(), !projectLibraries);
            if (newEntry != null && oldEntry != null) {
                newEntry.setScope(oldEntry.getScope());
                newEntry.setExported(oldEntry.isExported());
            }
        }
    }

    private static LibraryOrderEntry findLibraryEntry(List<OrderEntry> orderEntries, String libraryName, boolean projectLevel) {
        for (OrderEntry orderEntry : orderEntries) {
            if (orderEntry instanceof LibraryOrderEntry) {
                final LibraryOrderEntry libraryEntry = (LibraryOrderEntry) orderEntry;
                if (libraryEntry.isModuleLevel() != projectLevel && libraryName.equals(libraryEntry.getLibraryName())) {
                    return libraryEntry;
                }
            }
        }
        return null;
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
