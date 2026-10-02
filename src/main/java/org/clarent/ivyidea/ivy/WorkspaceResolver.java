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

import org.apache.ivy.core.cache.DefaultRepositoryCacheManager;
import org.apache.ivy.core.cache.RepositoryCacheManager;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.DownloadReport;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.resolve.DownloadOptions;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.AbstractResolver;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;

import java.io.File;

/**
 * Resolves the modules of the workspace from their local module descriptors.
 *
 * @see WorkspaceAwareIvySettings
 */
public abstract class WorkspaceResolver extends AbstractResolver {

    private final File cacheDir;
    private RepositoryCacheManager cacheManager;

    /**
     * @param name the name of the resolver
     * @param cacheDir the directory of the repository cache of this resolver. Ivy saves in this cache which
     *                 resolver found each workspace module.
     */
    protected WorkspaceResolver(String name, File cacheDir) {
        this.cacheDir = cacheDir;
        setName(name);
    }

    /**
     * Returns whether the given module is a workspace module that this resolver resolves.
     */
    public abstract boolean isWorkspaceModule(ModuleRevisionId mrid);

    @Override
    public RepositoryCacheManager getRepositoryCacheManager() {
        // created on first use, as the settings are only known after setSettings()
        if (cacheManager == null) {
            cacheManager = new DefaultRepositoryCacheManager(getName() + "-cache", (IvySettings) getSettings(), cacheDir);
        }
        return cacheManager;
    }

    /**
     * Reports all artifacts as failed: workspace modules have no artifacts to download, they become module
     * dependencies in IntelliJ.
     */
    public DownloadReport download(Artifact[] artifacts, DownloadOptions options) {
        DownloadReport report = new DownloadReport();
        for (Artifact artifact : artifacts) {
            ArtifactDownloadReport adr = new ArtifactDownloadReport(artifact);
            adr.setDownloadStatus(DownloadStatus.FAILED);
            report.addArtifactReport(adr);
        }
        return report;
    }

    public void publish(Artifact artifact, File src, boolean overwrite) {
        throw new UnsupportedOperationException("publish not supported by " + getName());
    }

    /**
     * Returns {@code null}, as a workspace resolver creates the module descriptors itself in {@link #getDependency}.
     */
    public ResolvedResource findIvyFileRef(DependencyDescriptor dd, ResolveData data) {
        return null;
    }
}
