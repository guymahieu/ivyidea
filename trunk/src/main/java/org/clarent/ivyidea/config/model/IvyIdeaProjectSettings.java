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

package org.clarent.ivyidea.config.model;

/**
 * @author Guy Mahieu
 */
public class IvyIdeaProjectSettings {

    private boolean useCustomIvySettings = true;
    private String ivySettingsFile = "";
    private boolean validateIvyFiles = false;
    private boolean libraryNameIncludesModule = false;
    private boolean libraryNameIncludesConfiguration = false;

//    private int ivyLogLevelFilter = -1;

    private PropertiesSettings propertiesSettings = new PropertiesSettings();

    public static IvyIdeaProjectSettings copyDataFrom(IvyIdeaProjectSettings ivyIdeaProjectSettings) {
        IvyIdeaProjectSettings result = new IvyIdeaProjectSettings();
        result.useCustomIvySettings = ivyIdeaProjectSettings.useCustomIvySettings;
        result.ivySettingsFile = ivyIdeaProjectSettings.ivySettingsFile;
        result.validateIvyFiles = ivyIdeaProjectSettings.validateIvyFiles;
        result.libraryNameIncludesModule = ivyIdeaProjectSettings.libraryNameIncludesModule;
        result.libraryNameIncludesConfiguration = ivyIdeaProjectSettings.libraryNameIncludesConfiguration;
        result.propertiesSettings = PropertiesSettings.copyDataFrom(ivyIdeaProjectSettings.propertiesSettings);

//        result.ivyLogLevelFilter = ivyIdeaProjectSettings.ivyLogLevelFilter;

        return result;
    }

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


//    public int getIvyLogLevelFilter() {
//        return ivyLogLevelFilter;
//    }
//
//    public void setIvyLogLevelFilter(final int ivyLogLevelFilter) {
//        this.ivyLogLevelFilter = ivyLogLevelFilter;
//    }
}
