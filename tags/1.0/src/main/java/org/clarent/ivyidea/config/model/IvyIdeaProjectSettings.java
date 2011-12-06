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

package org.clarent.ivyidea.config.model;

import org.clarent.ivyidea.logging.IvyLogLevel;

import java.util.Collections;
import java.util.Set;

/**
 * @author Guy Mahieu
 */
public class IvyIdeaProjectSettings {

    private boolean useCustomIvySettings = true;
    private String ivySettingsFile = "";
    private boolean validateIvyFiles = false;
    private boolean libraryNameIncludesModule = false;
    private boolean libraryNameIncludesConfiguration = false;
    private String ivyLogLevelThreshold = IvyLogLevel.None.name();
    private ArtifactTypeSettings artifactTypeSettings = new ArtifactTypeSettings();

    private PropertiesSettings propertiesSettings = new PropertiesSettings();

    public String getIvySettingsFile() {
        return ivySettingsFile;
    }

    public void setIvySettingsFile(String ivySettingsFile) {
        this.ivySettingsFile = ivySettingsFile;
    }

    public boolean isValidateIvyFiles() {
        return validateIvyFiles;
    }

    public void setValidateIvyFiles(boolean validateIvyFiles) {
        this.validateIvyFiles = validateIvyFiles;
    }

    public boolean isUseCustomIvySettings() {
        return useCustomIvySettings;
    }

    public void setUseCustomIvySettings(boolean useCustomIvySettings) {
        this.useCustomIvySettings = useCustomIvySettings;
    }

    public PropertiesSettings getPropertiesSettings() {
        return propertiesSettings;
    }

    public void setPropertiesSettings(PropertiesSettings propertiesSettings) {
        this.propertiesSettings = propertiesSettings;
    }

    public boolean isLibraryNameIncludesModule() {
        return libraryNameIncludesModule;
    }

    public void setLibraryNameIncludesModule(final boolean libraryNameIncludesModule) {
        this.libraryNameIncludesModule = libraryNameIncludesModule;
    }

    public boolean isLibraryNameIncludesConfiguration() {
        return libraryNameIncludesConfiguration;
    }

    public void setLibraryNameIncludesConfiguration(final boolean libraryNameIncludesConfiguration) {
        this.libraryNameIncludesConfiguration = libraryNameIncludesConfiguration;
    }

    public String getIvyLogLevelThreshold() {
        return ivyLogLevelThreshold;
    }

    public void setIvyLogLevelThreshold(String ivyLogLevelThreshold) {
        this.ivyLogLevelThreshold = ivyLogLevelThreshold;
    }

    public ArtifactTypeSettings getArtifactTypeSettings() {
        return artifactTypeSettings;
    }

    public void setArtifactTypeSettings(ArtifactTypeSettings artifactTypeSettings) {
        this.artifactTypeSettings = artifactTypeSettings;
    }
}
