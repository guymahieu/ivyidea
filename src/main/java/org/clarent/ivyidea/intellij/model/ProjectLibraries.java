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
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ExternalProjectSystemRegistry;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The IvyIDEA libraries that are stored as project libraries, so the module files only refer to them. They are
 * marked as imported from IvyIDEA, which lets IntelliJ store them outside the project directory when the external
 * storage of the project is enabled, like the libraries of Maven and Gradle projects. They are recognised by the
 * IvyIDEA library type, as project libraries share their names with the libraries that users create themselves.
 * <p>
 * All methods must be called in a write action.
 */
public final class ProjectLibraries {

    // the same id as the external system of the auto-import
    private static final String EXTERNAL_SOURCE_ID = "IvyIDEA";

    private ProjectLibraries() {
    }

    /**
     * Returns the IvyIDEA project library with the given name, created if needed, and makes sure the module depends
     * on it.
     */
    static Library getOrCreate(ModifiableRootModel model, String libraryName) {
        final LibraryTable libraryTable = getLibraryTable(model.getProject());
        // a library with this name is taken as an IvyIDEA library, also when it hasn't got the type yet
        Library library = libraryTable.getLibraryByName(libraryName);
        if (library == null) {
            final LibraryTable.ModifiableModel tableModel = libraryTable.getModifiableModel();
            library = createLibrary(tableModel, libraryName);
            tableModel.commit();
        }
        if (!hasProjectLibraryEntry(model, libraryName)) {
            model.addLibraryEntry(library);
        }
        return library;
    }

    /**
     * Creates the IvyIDEA project libraries with the given names that don't exist yet, in one change.
     */
    static void createMissing(Project project, Collection<String> libraryNames) {
        final LibraryTable libraryTable = getLibraryTable(project);
        LibraryTable.ModifiableModel tableModel = null;
        for (String libraryName : libraryNames) {
            if (libraryTable.getLibraryByName(libraryName) == null) {
                if (tableModel == null) {
                    tableModel = libraryTable.getModifiableModel();
                }
                createLibrary(tableModel, libraryName);
            }
        }
        if (tableModel != null) {
            tableModel.commit();
        }
    }

    private static Library createLibrary(LibraryTable.ModifiableModel tableModel, String libraryName) {
        return tableModel.createLibrary(libraryName, IvyIdeaLibraryType.KIND,
                ExternalProjectSystemRegistry.getInstance().getSourceById(EXTERNAL_SOURCE_ID));
    }

    // by name: an entry to a library created after the model was taken doesn't resolve in the model
    private static boolean hasProjectLibraryEntry(ModifiableRootModel model, String libraryName) {
        for (OrderEntry orderEntry : model.getOrderEntries()) {
            if (orderEntry instanceof LibraryOrderEntry) {
                final LibraryOrderEntry libraryEntry = (LibraryOrderEntry) orderEntry;
                if (LibraryTablesRegistrar.PROJECT_LEVEL.equals(libraryEntry.getLibraryLevel())
                        && libraryName.equals(libraryEntry.getLibraryName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Removes the dependencies of the module on IvyIDEA project libraries that are not in the given names, and
     * returns those libraries. Dependencies on missing project libraries with an IvyIDEA name are removed too.
     */
    public static List<Library> removeDependencies(ModifiableRootModel model, Set<String> librariesInUse) {
        final List<Library> removed = new ArrayList<>();
        for (OrderEntry orderEntry : model.getOrderEntries()) {
            if (orderEntry instanceof LibraryOrderEntry) {
                final LibraryOrderEntry libraryEntry = (LibraryOrderEntry) orderEntry;
                final Library library = libraryEntry.getLibrary();
                if (!libraryEntry.isModuleLevel() && library != null && isIvyIdeaLibrary(library)
                        && !librariesInUse.contains(library.getName())) {
                    model.removeOrderEntry(libraryEntry);
                    removed.add(library);
                } else if (library == null && LibraryTablesRegistrar.PROJECT_LEVEL.equals(libraryEntry.getLibraryLevel())
                        && IvyIdeaConfigHelper.isCreatedLibraryName(libraryEntry.getLibraryName())
                        && !librariesInUse.contains(libraryEntry.getLibraryName())) {
                    model.removeOrderEntry(libraryEntry);
                }
            }
        }
        return removed;
    }

    /**
     * Deletes the given project libraries that no module depends on anymore. Call this after committing the
     * modules.
     */
    public static void deleteUnused(Project project, Collection<Library> libraries) {
        final List<Library> unused = new ArrayList<>();
        for (Library library : libraries) {
            if (!isUsed(project, library) && !unused.contains(library)) {
                unused.add(library);
            }
        }
        if (!unused.isEmpty()) {
            final LibraryTable.ModifiableModel tableModel = getLibraryTable(project).getModifiableModel();
            for (Library library : unused) {
                tableModel.removeLibrary(library);
            }
            tableModel.commit();
        }
    }

    /**
     * Deletes all IvyIDEA project libraries that no module depends on, e.g. those of removed modules. Nothing is
     * deleted while modules are unloaded, as their dependencies aren't known.
     */
    public static void deleteAllUnused(Project project) {
        final ModuleManager moduleManager = ModuleManager.getInstance(project);
        if (!moduleManager.getUnloadedModuleDescriptions().isEmpty()) {
            return;
        }
        final Set<Library> used = new HashSet<>();
        for (Module module : moduleManager.getModules()) {
            for (OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
                if (orderEntry instanceof LibraryOrderEntry && ((LibraryOrderEntry) orderEntry).getLibrary() != null) {
                    used.add(((LibraryOrderEntry) orderEntry).getLibrary());
                }
            }
        }
        final List<Library> unused = new ArrayList<>();
        for (Library library : getLibraryTable(project).getLibraries()) {
            if (isIvyIdeaLibrary(library) && !used.contains(library)) {
                unused.add(library);
            }
        }
        deleteUnused(project, unused);
    }

    /**
     * Returns whether the project has IvyIDEA project libraries.
     */
    public static boolean hasIvyIdeaLibraries(Project project) {
        for (Library library : getLibraryTable(project).getLibraries()) {
            if (isIvyIdeaLibrary(library)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUsed(Project project, Library library) {
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            for (OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
                if (orderEntry instanceof LibraryOrderEntry && library.equals(((LibraryOrderEntry) orderEntry).getLibrary())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isIvyIdeaLibrary(Library library) {
        return library instanceof LibraryEx && IvyIdeaLibraryType.KIND.equals(((LibraryEx) library).getKind());
    }

    private static LibraryTable getLibraryTable(Project project) {
        return LibraryTablesRegistrar.getInstance().getLibraryTable(project);
    }
}
