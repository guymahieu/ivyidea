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

package org.clarent.ivyidea.resolve.dependency;

import com.intellij.openapi.project.Project;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.config.model.ArtifactTypeSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Guy Mahieu
 */
public class ExternalDependencyFactory {

    private static ExternalDependencyFactory instance = new ExternalDependencyFactory();

    public static ExternalDependencyFactory getInstance() {
        return instance;
    }

    @Nullable
    public ExternalDependency createExternalDependency(@NotNull Artifact artifact, @Nullable File file,
                                                       @NotNull Project project, @NotNull final String configurationName) {
        final ArtifactTypeSettings.DependencyCategory category = determineCategory(project, artifact);
        if (category != null) {
            switch (category) {
                case Classes:
                    return new ExternalJarDependency(artifact, file, configurationName);
                case Sources:
                    return new ExternalSourceDependency(artifact, file, configurationName);
                case Javadoc:
                    return new ExternalJavaDocDependency(artifact, file, configurationName);
            }
        }
        return null;
    }

    private static ArtifactTypeSettings.DependencyCategory determineCategory(@NotNull Project project, @NotNull Artifact artifact) {
        final ArtifactTypeSettings typeSettings = IvyIdeaConfigHelper.getArtifactTypeSettings(project);
        if (typeSettings == null) {
            return null;
        }
        return typeSettings.getCategoryForType(artifact.getType());
    }

}
