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

package org.clarent.ivyidea.intellij.autoimport;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectAware;
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectId;
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectListener;
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectReloadContext;
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemRefreshStatus;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.util.containers.ContainerUtil;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.config.IvyIdeaPaths;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.task.ResolveResult;
import org.clarent.ivyidea.ivy.IvyUtil;
import org.clarent.ivyidea.resolve.ResolveActionHelper;
import org.clarent.ivyidea.resolve.ResolveAllModulesListener;
import org.clarent.ivyidea.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Describes the files that a resolve of all modules depends on, so IntelliJ can offer to resolve again, or do it by
 * itself, when they change, like it does for Maven and Gradle projects.
 */
final class IvyIdeaProjectAware implements ExternalSystemProjectAware {

    private static final Logger LOGGER = Logger.getLogger(IvyIdeaProjectAware.class.getName());

    // the tracker stores its state for the project under this id
    private static final ProjectSystemId SYSTEM_ID = new ProjectSystemId("IvyIDEA", "IvyIDEA");

    private final Project project;
    private final ExternalSystemProjectId projectId;
    private final List<ExternalSystemProjectListener> listeners = ContainerUtil.createLockFreeCopyOnWriteList();

    IvyIdeaProjectAware(Project project) {
        this.project = project;
        String basePath = project.getBasePath();
        this.projectId = new ExternalSystemProjectId(SYSTEM_ID, basePath == null ? "" : basePath);
    }

    @NotNull
    @Override
    public ExternalSystemProjectId getProjectId() {
        return projectId;
    }

    /**
     * The ivy files, ivy settings files and properties files of the IvyIDEA modules.
     */
    @NotNull
    @Override
    public Set<String> getSettingsFiles() {
        return ReadAction.compute(() -> {
            Set<String> settingsFiles = new LinkedHashSet<>();
            for (Module module : IntellijUtils.getAllModulesWithIvyIdeaFacet(project)) {
                File ivyFile = IvyUtil.getIvyFile(module);
                addFile(settingsFiles, ivyFile == null ? null : ivyFile.getPath());
                try {
                    addFile(settingsFiles, IvyIdeaConfigHelper.getIvySettingsFile(module));
                } catch (IvySettingsNotFoundException e) {
                    // a missing settings file is reported when resolving
                }
                for (String propertiesFile : IvyIdeaConfigHelper.getPropertiesFiles(module)) {
                    addFile(settingsFiles, propertiesFile);
                }
            }
            LOGGER.fine("Watching " + settingsFiles.size() + " files for changes in project " + project.getName());
            return settingsFiles;
        });
    }

    private static void addFile(Set<String> settingsFiles, @Nullable String path) {
        // URLs can't be watched, nor paths with a path variable that isn't defined
        if (!StringUtils.isBlank(path) && !IvyIdeaPaths.isUrl(path) && IvyIdeaPaths.findMacro(path) == null) {
            settingsFiles.add(FileUtilRt.toSystemIndependentName(new File(path.trim()).getAbsolutePath()));
        }
    }

    @Override
    public void subscribe(@NotNull ExternalSystemProjectListener listener, @NotNull Disposable parentDisposable) {
        listeners.add(listener);
        Disposer.register(parentDisposable, () -> listeners.remove(listener));
        project.getMessageBus().connect(parentDisposable).subscribe(ResolveAllModulesListener.TOPIC, new ResolveAllModulesListener() {
            @Override
            public void resolveStarted() {
                listener.onProjectReloadStart();
            }

            @Override
            public void resolveFinished(ResolveResult result) {
                listener.onProjectReloadFinish(toRefreshStatus(result));
            }
        });
    }

    private static ExternalSystemRefreshStatus toRefreshStatus(ResolveResult result) {
        switch (result) {
            case SUCCEEDED:
                return ExternalSystemRefreshStatus.SUCCESS;
            case CANCELLED:
                return ExternalSystemRefreshStatus.CANCEL;
            default:
                return ExternalSystemRefreshStatus.FAILURE;
        }
    }

    /**
     * Tells IntelliJ that the module libraries are up to date with the current files, without resolving.
     */
    void markResolved() {
        for (ExternalSystemProjectListener listener : listeners) {
            listener.onProjectReloadStart();
            listener.onProjectReloadFinish(ExternalSystemRefreshStatus.SUCCESS);
        }
    }

    /**
     * Tells IntelliJ to ask for the files to watch again, because an IvyIDEA facet was added or removed.
     */
    void settingsFilesChanged() {
        for (ExternalSystemProjectListener listener : listeners) {
            listener.onSettingsFilesListChange();
        }
    }

    @Override
    public void reloadProject(@NotNull ExternalSystemProjectReloadContext context) {
        // starting a resolve saves the open documents, which must happen on the event dispatch thread
        ApplicationManager.getApplication().invokeLater(() -> ResolveActionHelper.resolveForProject(project, null), project.getDisposed());
    }
}
