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
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ExcludeRule;
import org.apache.ivy.core.module.descriptor.License;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.DownloadReport;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.report.MetadataArtifactDownloadReport;
import org.apache.ivy.core.resolve.DownloadOptions;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.AbstractResolver;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.apache.ivy.plugins.version.VersionMatcher;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

public class WorkspaceModuleResolver extends AbstractResolver {

    private static final Logger LOG = Logger.getLogger(WorkspaceModuleResolver.class.getName());
    private static final String INTELLIJ_MODULE_TYPE = "intellij-module";
    private static final String INTELLIJ_MODULE_EXTENSION = "intellij-module";

    private final Project project;
    private final Map<File, ModuleDescriptor> workspaceIvyFileCache;
    private final WorkspaceModuleIndex workspaceModuleIndex;

    public WorkspaceModuleResolver(Project project, IvySettings settings, Map<File, ModuleDescriptor> workspaceIvyFileCache,
                                    WorkspaceModuleIndex workspaceModuleIndex) {
        this.project = project;
        this.workspaceIvyFileCache = workspaceIvyFileCache;
        this.workspaceModuleIndex = workspaceModuleIndex;
        setName("ivyidea-workspace-resolver");
        setSettings(settings);
        LOG.info("WorkspaceModuleResolver created for project: " + project.getName());
    }

    public ResolvedModuleRevision getDependency(DependencyDescriptor dd, ResolveData data) throws ParseException {
        if (!IvyIdeaConfigHelper.detectDependenciesOnOtherModulesWhileResolving(project)) {
            LOG.info("detectDependenciesOnOtherModulesWhileResolving is false, skipping");
            return null;
        }

        ModuleRevisionId requestedMrid = dd.getDependencyRevisionId();

        IvySettings settings = (IvySettings) getSettings();
        if (settings == null) {
            LOG.info("settings is null, skipping lookup for " + requestedMrid);
            return null;
        }

        ModuleId requestedModuleId = requestedMrid.getModuleId();
        Module workspaceModule = workspaceModuleIndex.findModule(requestedModuleId, project, settings, workspaceIvyFileCache);
        if (workspaceModule == null) {
            return null;
        }

        File ivyFile = IvyUtil.getIvyFile(workspaceModule);
        try {
            // computeIfAbsent (not get-then-put) so concurrent resolves of different modules
            // racing on the same shared ivy.xml can't corrupt the cache -- at worst they'd
            // redundantly parse the same file once each, never see a half-written value.
            ModuleDescriptor workspaceMd = workspaceIvyFileCache.computeIfAbsent(ivyFile, f -> IvyUtil.parseIvyFile(f, settings));

            VersionMatcher versionMatcher = settings.getVersionMatcher();
            if (!versionMatcher.accept(requestedMrid, workspaceMd)) {
                LOG.info("versionMatcher rejected workspace module '" + workspaceModule.getName() + "' for " + requestedMrid);
                return null;
            }

            LOG.info("MATCH! Returning workspace descriptor for '" + workspaceModule.getName() + "', " + requestedMrid);
            DefaultModuleDescriptor clonedMd = cloneMd(workspaceMd, workspaceModule);

            MetadataArtifactDownloadReport madr = new MetadataArtifactDownloadReport(
                    new DefaultArtifact(clonedMd.getModuleRevisionId(),
                            clonedMd.getPublicationDate(),
                            workspaceModule.getName(),
                            INTELLIJ_MODULE_TYPE,
                            INTELLIJ_MODULE_EXTENSION));
            madr.setDownloadStatus(DownloadStatus.SUCCESSFUL);
            madr.setSearched(true);

            return new ResolvedModuleRevision(this, this, clonedMd, madr);
        } catch (RuntimeException e) {
            LOG.info("error parsing ivy file '" + ivyFile + "': " + e.getMessage());
            return null;
        }
    }

    public DownloadReport download(Artifact[] artifacts, DownloadOptions options) {
        DownloadReport dr = new DownloadReport();
        for (Artifact artifact : artifacts) {
            ArtifactDownloadReport adr = new ArtifactDownloadReport(artifact);
            adr.setDownloadStatus(DownloadStatus.FAILED);
            adr.setSize(0);
            dr.addArtifactReport(adr);
        }
        return dr;
    }

    public void publish(Artifact artifact, File src, boolean overwrite) throws IOException {
        throw new UnsupportedOperationException("publish not supported by " + getName());
    }

    public ResolvedResource findIvyFileRef(DependencyDescriptor dd, ResolveData data) {
        return null;
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
