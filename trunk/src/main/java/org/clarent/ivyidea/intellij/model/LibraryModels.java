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

import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.VirtualFile;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.resolve.dependency.ExternalDependency;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import static java.util.Arrays.asList;
import static org.clarent.ivyidea.util.StringUtils.isBlank;

class LibraryModels implements Closeable {

    private static final Logger LOGGER = Logger.getLogger(LibraryModels.class.getName());    

    private final ConcurrentMap<String, Library.ModifiableModel> libraryModels = new ConcurrentHashMap<String, Library.ModifiableModel>();

    private ModifiableRootModel intellijModule;

    LibraryModels(ModifiableRootModel intellijModule) {
        this.intellijModule = intellijModule;
    }

    public Library.ModifiableModel getForExternalDependency(final ExternalDependency externalDependency) {
        String resolvedConfiguration = externalDependency.getConfigurationName();
        return getForConfiguration(isBlank(resolvedConfiguration) ? "default" : resolvedConfiguration);
    }

    private Library.ModifiableModel getForConfiguration(String ivyConfiguration) {
        if (!libraryModels.containsKey(ivyConfiguration)) {
            final Library.ModifiableModel libraryModel = getIvyIdeaLibrary(intellijModule, ivyConfiguration).getModifiableModel();
            libraryModels.putIfAbsent(ivyConfiguration, libraryModel);
        }
        return libraryModels.get(ivyConfiguration);
    }

    private Library getIvyIdeaLibrary(ModifiableRootModel modifiableRootModel, final String configurationName) {
        final String libraryName = IvyIdeaConfigHelper.getCreatedLibraryName(modifiableRootModel, configurationName);
        final LibraryTable libraryTable = modifiableRootModel.getModuleLibraryTable();
        final Library library = libraryTable.getLibraryByName(libraryName);
        if (library == null) {
            LOGGER.info("Internal library not found for module " + modifiableRootModel.getModule().getModuleFilePath() + ", creating with name " + libraryName + "...");
            return libraryTable.createLibrary(libraryName);
        }
        return library;
    }

    public void removeDependency(OrderRootType type, VirtualFile virtualFile) {
        final String dependencyUrl = virtualFile.getUrl();
        LOGGER.info("Removing no longer needed dependency of type " + type + ": " + dependencyUrl);
        for (Library.ModifiableModel libraryModel : libraryModels.values()) {
            libraryModel.removeRoot(dependencyUrl, type);
        }
    }

    public List<VirtualFile> getIntellijDependenciesForType(OrderRootType type) {
        final List<VirtualFile> intellijDependencies = new ArrayList<VirtualFile>();
        for (final Library.ModifiableModel libraryModel : libraryModels.values()) {
            final VirtualFile[] libraryModelFiles = libraryModel.getFiles(type);
            intellijDependencies.addAll(asList(libraryModelFiles));
        }
        return intellijDependencies;
    }

    public void close() {
        for (Library.ModifiableModel libraryModel : libraryModels.values()) {
            if (libraryModel.isChanged()) {
                libraryModel.commit();
            } else {
                libraryModel.dispose();
            }
        }
    }
}
