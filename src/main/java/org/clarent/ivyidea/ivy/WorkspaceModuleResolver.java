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

package org.clarent.ivyidea.ivy;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ExcludeRule;
import org.apache.ivy.core.module.descriptor.License;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.report.MetadataArtifactDownloadReport;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.core.settings.IvySettings;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.ParseException;
import java.util.Map;
import java.util.logging.Logger;

public class WorkspaceModuleResolver extends WorkspaceResolver {

    private static final Logger LOG = Logger.getLogger(WorkspaceModuleResolver.class.getName());
    private static final String INTELLIJ_MODULE_TYPE = "intellij-module";
    private static final String INTELLIJ_MODULE_EXTENSION = "intellij-module";

    private final Project project;
    private final Map<File, ModuleDescriptor> workspaceIvyFileCache;
    private final WorkspaceModuleIndex workspaceModuleIndex;

    public WorkspaceModuleResolver(Project project, IvySettings settings, Map<File, ModuleDescriptor> workspaceIvyFileCache,
                                    WorkspaceModuleIndex workspaceModuleIndex) {
        super("ivyidea-workspace-resolver", IvyIdeaConfigHelper.getWorkspaceCacheDir());
        this.project = project;
        this.workspaceIvyFileCache = workspaceIvyFileCache;
        this.workspaceModuleIndex = workspaceModuleIndex;
        setSettings(settings);
        LOG.info("WorkspaceModuleResolver created for project: " + project.getName());
    }

    @Override
    public boolean isWorkspaceModule(ModuleRevisionId mrid) {
        return findWorkspaceModule(mrid) != null;
    }

    public ResolvedModuleRevision getDependency(DependencyDescriptor dd, ResolveData data) throws ParseException {
        final Module workspaceModule = findWorkspaceModule(dd.getDependencyRevisionId());
        if (workspaceModule == null) {
            return null;
        }

        DefaultModuleDescriptor clonedMd = cloneMd(getWorkspaceDescriptor(workspaceModule), workspaceModule);

        MetadataArtifactDownloadReport madr = new MetadataArtifactDownloadReport(
                new DefaultArtifact(clonedMd.getModuleRevisionId(),
                        clonedMd.getPublicationDate(),
                        workspaceModule.getName(),
                        INTELLIJ_MODULE_TYPE,
                        INTELLIJ_MODULE_EXTENSION));
        madr.setDownloadStatus(DownloadStatus.SUCCESSFUL);
        madr.setSearched(true);

        return new ResolvedModuleRevision(this, this, clonedMd, madr);
    }

    /**
     * Returns the workspace module with the organisation and name of the given module.
     */
    @Nullable
    private Module findWorkspaceModule(ModuleRevisionId mrid) {
        final IvySettings settings = (IvySettings) getSettings();
        return workspaceModuleIndex.findModule(mrid.getModuleId(), project, settings, workspaceIvyFileCache);
    }

    private ModuleDescriptor getWorkspaceDescriptor(Module workspaceModule) {
        // the index only contains modules whose ivy file could be parsed, so this is a cache hit
        final IvySettings settings = (IvySettings) getSettings();
        return workspaceIvyFileCache.computeIfAbsent(IvyUtil.getIvyFile(workspaceModule), f -> IvyUtil.parseIvyFile(f, settings));
    }

    static DefaultModuleDescriptor cloneMd(ModuleDescriptor original, Module workspaceModule) {
        DefaultModuleDescriptor cloned = new DefaultModuleDescriptor(
                original.getModuleRevisionId(), original.getStatus(), original.getPublicationDate(), true);
        cloned.setLastModified(System.currentTimeMillis());

        Configuration[] allConfigs = original.getConfigurations();
        if (allConfigs.length == 0) {
            cloned.addConfiguration(new Configuration("default"));
        } else {
            for (Configuration conf : allConfigs) {
                cloned.addConfiguration(conf);
            }
        }

        for (DependencyDescriptor dep : original.getDependencies()) {
            cloned.addDependency(dep);
        }

        for (ExcludeRule excludeRule : original.getAllExcludeRules()) {
            cloned.addExcludeRule(excludeRule);
        }

        for (License license : original.getLicenses()) {
            cloned.addLicense(license);
        }

        return cloned;
    }
}
