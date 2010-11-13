/*
 * Copyright 2009 Guy Mahieu
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
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.clarent.ivyidea.intellij.model.IntellijModuleWrapper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

/**
 * Represents a dependency to an external artifact somewhere on the filesystem.
 *
 * @author Guy Mahieu
 */
public abstract class ExternalDependency implements ResolvedDependency {

    private static final Logger LOGGER = Logger.getLogger(ExternalJarDependency.class.getName());

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

    public Artifact getArtifact() {
        return artifact;
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

    @NotNull
    public String getUrlForLibrary() {
        return VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, localFile.getAbsolutePath()) + JarFileSystem.JAR_SEPARATOR;
    }

    public boolean isMissing() {
        return localFile != null && !new File(localFile.getAbsolutePath()).exists();
    }

    protected boolean isAlreadyRegistered(Library.ModifiableModel libraryModel) {
        for (VirtualFile file : libraryModel.getFiles(getType())) {
            if (isSameDependency(file)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSameDependency(VirtualFile file) {
        if (localFile == null) {
            return false;
        }
        // TODO: see if this naive check is good enough - is there a better way to do this?
        final String artifactPath = localFile.getAbsolutePath();
        final String existingDependencyPath = file.getFileSystem().extractPresentableUrl(file.getPath());
        // Compare the files not just the paths
        // TODO: Are these paths always absolute??
        return new File(existingDependencyPath).equals(new File(artifactPath));
    }

    public abstract OrderRootType getType();

    protected abstract String getTypeName();
}
