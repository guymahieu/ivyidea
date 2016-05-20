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

import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
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
        final String libraryName = IvyIdeaConfigHelper.getCreatedLibraryName(intellijModule, ivyConfiguration);
        if (!libraryModels.containsKey(libraryName)) {
            final Library.ModifiableModel libraryModel = getIvyIdeaLibrary(intellijModule, libraryName).getModifiableModel();
            libraryModels.putIfAbsent(libraryName, libraryModel);
        }
        return libraryModels.get(libraryName);
    }

    private Library getIvyIdeaLibrary(ModifiableRootModel modifiableRootModel, final String libraryName) {
        final LibraryTable libraryTable = modifiableRootModel.getModuleLibraryTable();
        Library library = libraryTable.getLibraryByName(libraryName);
        if (library == null) {
            LOGGER.info("Internal library not found for module " + modifiableRootModel.getModule().getModuleFilePath() + ", creating with name " + libraryName + "...");
            library = libraryTable.createLibrary(libraryName);
        }
        LibraryOrderEntry libraryOrderEntry = modifiableRootModel.findLibraryOrderEntry(library);
        libraryOrderEntry.setExported(true);
        return library;
    }

    public void removeDependency(OrderRootType type, String dependencyUrl) {
        LOGGER.info("Removing no longer needed dependency of type " + type + ": " + dependencyUrl);
        for (Library.ModifiableModel libraryModel : libraryModels.values()) {
            libraryModel.removeRoot(dependencyUrl, type);
        }
    }

    public List<String> getIntellijDependencyUrlsForType(OrderRootType type) {
        final List<String> intellijDependencies = new ArrayList<String>();
        for (final Library.ModifiableModel libraryModel : libraryModels.values()) {
            final String[] libraryModelUrls = libraryModel.getUrls(type);
            intellijDependencies.addAll(asList(libraryModelUrls));
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
