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

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetManagerListener;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectTracker;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Lets IntelliJ watch the ivy files of the project while it has IvyIDEA modules, so it offers to resolve again, or
 * does it by itself (depending on Settings | Build, Execution, Deployment | Build Tools), when they change.
 */
@Service(Service.Level.PROJECT)
public final class IvyIdeaAutoImport implements Disposable {

    // set in the workspace once IntelliJ knows the state of the watched files
    private static final String FILES_KNOWN_KEY = "IvyIDEA.autoImport.filesKnown";

    private final Project project;
    private final IvyIdeaProjectAware projectAware;

    // the registration with the tracker, while the project has IvyIDEA modules
    @Nullable
    private Disposable registration;

    public IvyIdeaAutoImport(Project project) {
        this.project = project;
        this.projectAware = new IvyIdeaProjectAware(project);
    }

    public static IvyIdeaAutoImport getInstance(Project project) {
        return project.getService(IvyIdeaAutoImport.class);
    }

    /**
     * Starts watching, and follows IvyIDEA facets and modules being added and removed. Other changes of the IvyIDEA
     * settings are ignored, like before: after changing them, the user resolves again.
     */
    void start() {
        // modules that are added outside the IDE only send this, e.g. when IntelliJ opened the project from its cache
        // and then loads the module files
        project.getMessageBus().connect(this).subscribe(ModuleListener.TOPIC, new ModuleListener() {
            @Override
            public void modulesAdded(@NotNull Project project, @NotNull List<? extends Module> modules) {
                updateRegistration();
            }

            @Override
            public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
                updateRegistration();
            }
        });
        project.getMessageBus().connect(this).subscribe(FacetManager.FACETS_TOPIC, new FacetManagerListener() {
            @Override
            public void facetAdded(@NotNull Facet facet) {
                facetAddedOrRemoved(facet);
            }

            @Override
            public void facetRemoved(@NotNull Facet facet) {
                facetAddedOrRemoved(facet);
            }
        });
        updateRegistration();
    }

    private void facetAddedOrRemoved(Facet<?> facet) {
        if (IvyIdeaFacetType.ID.equals(facet.getTypeId())) {
            updateRegistration();
        }
    }

    /**
     * Registers the project with the tracker while it has IvyIDEA modules, and tells the tracker when the files to
     * watch may have changed.
     */
    private synchronized void updateRegistration() {
        boolean hasIvyIdeaModules = ReadAction.compute(() -> IntellijUtils.getAllModulesWithIvyIdeaFacet(project).length > 0);
        if (hasIvyIdeaModules && registration == null) {
            registration = Disposer.newDisposable(this, "IvyIDEA auto-import");
            ExternalSystemProjectTracker tracker = ExternalSystemProjectTracker.getInstance(project);
            tracker.register(projectAware, registration);
            PropertiesComponent properties = PropertiesComponent.getInstance(project);
            if (!properties.getBoolean(FILES_KNOWN_KEY)) {
                // the first time, IntelliJ doesn't know the files yet: it would see all of them as changed outside the
                // IDE and resolve, so take the libraries of the last resolve as up to date instead
                projectAware.markResolved();
                properties.setValue(FILES_KNOWN_KEY, true);
            }
            // the tracker only reports changes of activated projects
            tracker.activate(projectAware.getProjectId());
        } else if (!hasIvyIdeaModules && registration != null) {
            Disposer.dispose(registration);
            registration = null;
        } else if (registration != null) {
            projectAware.settingsFilesChanged();
        }
    }

    @Override
    public void dispose() {
    }
}
