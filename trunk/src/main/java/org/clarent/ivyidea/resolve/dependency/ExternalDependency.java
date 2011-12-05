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

import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.PathUtil;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.clarent.ivyidea.intellij.model.IntellijModuleWrapper;

import java.io.File;
import java.util.logging.Logger;

/**
 * Represents a dependency to an external artifact somewhere on the filesystem.
 *
 * @author Guy Mahieu
 */
public abstract class ExternalDependency implements ResolvedDependency {

    private static final Logger LOGGER = Logger.getLogger(ExternalDependency.class.getName());

    private final Artifact artifact;
    private final String configurationName;
    private final File localFile;

    public ExternalDependency(Artifact artifact, File localFile, final String configurationName) {
        this.artifact = artifact;
        this.localFile = localFile;
        this.configurationName = configurationName;
    }

    public File getLocalFile() {
        return localFile;
    }

    public String getUrlForLibraryRoot() {
        return VfsUtil.getUrlForLibraryRoot(getLocalFile());
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void addTo(IntellijModuleWrapper intellijModuleWrapper) {
        if (localFile == null) {
            LOGGER.warning("Not registering external " + getTypeName() + " dependency for module " + artifact.getModuleRevisionId() +  " as the file does not seem to exist.");
            return;
        }
        final String artifactPath = localFile.getAbsolutePath();
        if (isMissing()) {
            LOGGER.warning("Not registering external " + getTypeName() + " file dependency as the file does not seem to exist: " + artifactPath);
            return;
        }
        if (intellijModuleWrapper.alreadyHasDependencyOnLibrary(this)) {
            LOGGER.info("Not re-registering external " + getTypeName() + " file dependency " + artifactPath + " as it is already present.");
            return;
        }
        LOGGER.info("Registering external " + getTypeName() + " file dependency: " + artifactPath);
        intellijModuleWrapper.addExternalDependency(this);
    }

    public boolean isMissing() {
        return localFile != null && !new File(localFile.getAbsolutePath()).exists();
    }

    public boolean isSameDependency(String url) {
        if (localFile == null) {
            return false;
        }

        final String path = PathUtil.toPresentableUrl(url);
        return localFile.equals(new File(path));
    }

    public abstract OrderRootType getType();

    protected abstract String getTypeName();
}
