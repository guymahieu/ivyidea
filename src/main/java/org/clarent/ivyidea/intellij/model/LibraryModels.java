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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.PathUtil;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.resolve.dependency.ExternalDependency;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import static java.util.Arrays.asList;
import static org.clarent.ivyidea.util.StringUtils.isBlank;

class LibraryModels implements Closeable {

    private static final Logger LOGGER = Logger.getLogger(LibraryModels.class.getName());    

    private final ConcurrentMap<String, Library.ModifiableModel> libraryModels = new ConcurrentHashMap<String, Library.ModifiableModel>();

    private final Map<Library.ModifiableModel, Map<OrderRootType, Set<String>>> rootPaths = new HashMap<>();

    private final IntellijModuleWrapper moduleWrapper;
    private final Project project;
    private final Module module;

    public LibraryModels(@NotNull IntellijModuleWrapper moduleWrapper, @NotNull Module module) {
        this.moduleWrapper = moduleWrapper;
        this.project = module.getProject();
        this.module = module;
    }

    public boolean containsRoot(final ExternalDependency externalDependency) {
        final File localFile = externalDependency.getLocalFile();
        if (localFile == null) {
            return false;
        }
        final Library.ModifiableModel libraryModel = getForExternalDependency(externalDependency);
        final Set<String> paths = getRootPaths(libraryModel, externalDependency.getType());
        final String path = FileUtil.toCanonicalPath(localFile.getPath());
        return paths.contains(path);
    }

    public void addRoot(final ExternalDependency externalDependency) {
        final Library.ModifiableModel libraryModel = getForExternalDependency(externalDependency);
        final String url = externalDependency.getUrlForLibraryRoot();
        libraryModel.addRoot(url, externalDependency.getType());
        getRootPaths(libraryModel, externalDependency.getType()).add(toCanonicalPath(url));
    }

    private Set<String> getRootPaths(final Library.ModifiableModel libraryModel, final OrderRootType type) {
        return rootPaths
                .computeIfAbsent(libraryModel, _libraryModel -> new HashMap<>())
                .computeIfAbsent(type, _type -> {
                    final Set<String> paths = createPathSet();
                    for (String url : libraryModel.getUrls(type)) {
                        paths.add(toCanonicalPath(url));
                    }
                    return paths;
                });
    }

    private Library.ModifiableModel getForExternalDependency(final ExternalDependency externalDependency) {
        String resolvedConfiguration = externalDependency.getConfigurationName();
        return getForConfiguration(isBlank(resolvedConfiguration) ? "default" : resolvedConfiguration);
    }

    private Library.ModifiableModel getForConfiguration(String ivyConfiguration) {
        final String libraryName = IvyIdeaConfigHelper.getCreatedLibraryName(project, module, ivyConfiguration);
        return libraryModels.computeIfAbsent(libraryName, _libraryName -> getIvyIdeaLibrary(libraryName).getModifiableModel());
    }

    private Library getIvyIdeaLibrary(final String libraryName) {
        final LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
        Library library = libraryTable.getLibraryByName(libraryName);
        if (library == null) {
            LOGGER.info("Internal library not found for module " + module.getName() + ", creating with name " + libraryName + "...");
            library = libraryTable.createLibrary(libraryName);
            moduleWrapper.addLibrary(library);
        }
        return library;
    }

    public void removeDependency(OrderRootType type, String dependencyUrl) {
        LOGGER.fine("Removing no longer needed dependency of type " + type + ": " + dependencyUrl);
        for (Library.ModifiableModel libraryModel : libraryModels.values()) {
            libraryModel.removeRoot(dependencyUrl, type);
        }
        final String path = toCanonicalPath(dependencyUrl);
        for (Map<OrderRootType, Set<String>> pathsByType : rootPaths.values()) {
            final Set<String> paths = pathsByType.get(type);
            if (paths != null) {
                paths.remove(path);
            }
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

    /**
     * Creates a set for canonical paths that ignores case on case-insensitive file systems, so lookups
     * match like {@link FileUtil#filesEqual}.
     */
    static Set<String> createPathSet() {
        return SystemInfo.isFileSystemCaseSensitive ? new HashSet<>() : new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * Returns the canonical local path of a library root url, e.g. {@code jar://C:/repo/x.jar!/}.
     */
    static String toCanonicalPath(String libraryRootUrl) {
        return FileUtil.toCanonicalPath(PathUtil.toPresentableUrl(libraryRootUrl));
    }

    public void close() {
        for (Library.ModifiableModel libraryModel : libraryModels.values()) {
            if (libraryModel.isChanged()) {
                LOGGER.fine("commit modified libraryModel " + libraryModel.getName());
                libraryModel.commit();
            } else {
                LOGGER.fine("dispose unmodified libraryModel " + libraryModel.getName());
                Disposer.dispose(libraryModel);
            }
        }
    }
}
