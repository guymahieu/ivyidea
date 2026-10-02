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

import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;

public class WorkspaceModuleResolver extends WorkspaceResolver {

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

    public ResolvedModuleRevision getDependency(DependencyDescriptor dd, ResolveData data) {
        final ModuleDescriptor md = ivyManager.getWorkspaceModuleDescriptor(dd.getDependencyId());
        return md == null ? null : createResolvedModuleRevision(md);
    }
}
