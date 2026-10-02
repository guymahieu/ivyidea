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
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;

import java.text.ParseException;

public class WorkspaceModuleResolver extends WorkspaceResolver {

    private static final String INTELLIJ_MODULE_TYPE = "intellij-module";
    private static final String INTELLIJ_MODULE_EXTENSION = "intellij-module";

    private final IvyManager ivyManager;

    /**
     * @param ivyManager provides the workspace modules, see {@link IvyManager#forProject}
     */
    public WorkspaceModuleResolver(IvyManager ivyManager) {
        super("ivyidea-workspace-resolver", IvyIdeaConfigHelper.getWorkspaceCacheDir());
        this.ivyManager = ivyManager;
    }

    @Override
    public boolean isWorkspaceModule(ModuleRevisionId mrid) {
        return ivyManager.getWorkspaceModule(mrid.getModuleId()) != null;
    }

    public ResolvedModuleRevision getDependency(DependencyDescriptor dd, ResolveData data) throws ParseException {
        final Module workspaceModule = ivyManager.getWorkspaceModule(dd.getDependencyId());
        if (workspaceModule == null) {
            return null;
        }

        DefaultModuleDescriptor clonedMd = cloneMd(ivyManager.getWorkspaceModuleDescriptor(dd.getDependencyId()), workspaceModule);

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
