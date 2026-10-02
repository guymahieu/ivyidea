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

import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.jetbrains.annotations.NotNull;

/**
 * IvySettings that resolves the modules of the workspace with a {@link WorkspaceResolver}, and all other modules
 * with the resolver configured for them.
 */
public class WorkspaceAwareIvySettings extends IvySettings {

    private WorkspaceResolver workspaceResolver;

    /**
     * Sets the resolver for the modules in the workspace.
     */
    public synchronized void setWorkspaceResolver(@NotNull WorkspaceResolver workspaceResolver) {
        workspaceResolver.setSettings(this);
        this.workspaceResolver = workspaceResolver;
    }

    @Override
    public synchronized DependencyResolver getResolver(ModuleRevisionId mrid) {
        if (workspaceResolver != null && workspaceResolver.isWorkspaceModule(mrid)) {
            return workspaceResolver;
        }
        return super.getResolver(mrid);
    }
}
