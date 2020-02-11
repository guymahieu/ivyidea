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

package org.clarent.ivyidea.intellij.facet.config;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.module.Module;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacet;
import org.clarent.ivyidea.intellij.facet.ui.BasicSettingsTab;
import org.clarent.ivyidea.intellij.facet.ui.PropertiesSettingsTab;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaFacetConfiguration implements PersistentStateComponent<IvyIdeaFacetConfiguration.FacetConfig>, FacetConfiguration {

    private static final Logger LOGGER = Logger.getLogger(IvyIdeaFacetConfiguration.class.getName());

    private FacetConfig configuration = new FacetConfig();

    @Nullable
    public static IvyIdeaFacetConfiguration getInstance(Module module) {
        final IvyIdeaFacet ivyIdeaFacet = IvyIdeaFacet.getInstance(module);
        if (ivyIdeaFacet != null) {
            return ivyIdeaFacet.getConfiguration();
        } else {
            LOGGER.info("Module " + module.getName() + " does not have the IvyIDEA facet configured; ignoring.");
            return null;
        }
    }

    @Override
    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        final PropertiesSettingsTab propertiesSettingsTab = new PropertiesSettingsTab(editorContext);
        final BasicSettingsTab basicSettingsTab = new BasicSettingsTab(editorContext, propertiesSettingsTab);
        return new FacetEditorTab[]{basicSettingsTab, propertiesSettingsTab};
    }

    public String getIvyFile() {
        return configuration.getIvyFile();
    }

    public void setIvyFile(String ivyFile) {
        configuration.setIvyFile(ivyFile);
    }

    public boolean isUseProjectSettings() {
        return configuration.isUseProjectSettings();
    }

    public void setUseProjectSettings(boolean useProjectSettings) {
        configuration.setUseProjectSettings(useProjectSettings);
    }

    public boolean isUseCustomIvySettings() {
        return configuration.isUseCustomIvySettings();
    }

    public void setUseCustomIvySettings(boolean useCustomIvySettings) {
        configuration.setUseCustomIvySettings(useCustomIvySettings);
    }

    public String getIvySettingsFile() {
        return configuration.getIvySettingsFile();
    }

    public void setIvySettingsFile(String ivySettingsFile) {
        configuration.setIvySettingsFile(ivySettingsFile);
    }

    public boolean isOnlyResolveSelectedConfigs() {
        return configuration.isOnlyResolveSelectedConfigs();
    }

    public void setOnlyResolveSelectedConfigs(boolean onlyResolveSelectedConfigs) {
        configuration.setOnlyResolveSelectedConfigs(onlyResolveSelectedConfigs);
    }

    public Set<String> getConfigsToResolve() {
        return configuration.getConfigsToResolve();
    }

    public void setConfigsToResolve(Set<String> configsToResolve) {
        configuration.setConfigsToResolve(configsToResolve);
    }

    public FacetPropertiesSettings getPropertiesSettings() {
        return configuration.getFacetPropertiesSettings();
    }

    public void setPropertiesSettings(FacetPropertiesSettings facetPropertiesSettings) {
        configuration.setFacetPropertiesSettings(facetPropertiesSettings);
    }

    @Nullable
    @Override
    public FacetConfig getState() {
        return configuration;
    }


    @Override
    public void loadState(@NotNull FacetConfig facetConfig) {
        this.configuration = facetConfig;
    }

    public static class FacetConfig {

        private String ivyFile = "";
        private boolean useProjectSettings = true;
        private boolean useCustomIvySettings = true;
        private String ivySettingsFile = "";
        private boolean onlyResolveSelectedConfigs = false;
        private Set<String> configsToResolve = Collections.emptySet();
        private FacetPropertiesSettings facetPropertiesSettings = new FacetPropertiesSettings();

        public String getIvyFile() {
            return ivyFile;
        }

        public void setIvyFile(String ivyFile) {
            this.ivyFile = ivyFile;
        }

        public boolean isUseProjectSettings() {
            return useProjectSettings;
        }

        public void setUseProjectSettings(boolean useProjectSettings) {
            this.useProjectSettings = useProjectSettings;
        }

        public boolean isUseCustomIvySettings() {
            return useCustomIvySettings;
        }

        public void setUseCustomIvySettings(boolean useCustomIvySettings) {
            this.useCustomIvySettings = useCustomIvySettings;
        }

        public String getIvySettingsFile() {
            return ivySettingsFile;
        }

        public void setIvySettingsFile(String ivySettingsFile) {
            this.ivySettingsFile = ivySettingsFile;
        }

        public boolean isOnlyResolveSelectedConfigs() {
            return onlyResolveSelectedConfigs;
        }

        public void setOnlyResolveSelectedConfigs(boolean onlyResolveSelectedConfigs) {
            this.onlyResolveSelectedConfigs = onlyResolveSelectedConfigs;
        }

        public Set<String> getConfigsToResolve() {
            return configsToResolve;
        }

        public void setConfigsToResolve(Set<String> configsToResolve) {
            this.configsToResolve = configsToResolve;
        }

        public FacetPropertiesSettings getFacetPropertiesSettings() {
            return facetPropertiesSettings;
        }

        public void setFacetPropertiesSettings(FacetPropertiesSettings facetPropertiesSettings) {
            this.facetPropertiesSettings = facetPropertiesSettings;
        }

    }

}
