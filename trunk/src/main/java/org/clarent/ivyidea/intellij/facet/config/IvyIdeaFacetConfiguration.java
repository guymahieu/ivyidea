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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacet;
import org.clarent.ivyidea.intellij.facet.ui.BasicSettingsTab;
import org.clarent.ivyidea.intellij.facet.ui.PropertiesSettingsTab;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaFacetConfiguration implements FacetConfiguration {

    private static final Logger LOGGER = Logger.getLogger(IvyIdeaFacetConfiguration.class.getName());

    /*
        Al the fields are initialized with a default value to avoid errors when adding a new IvyIDEA facet to an
        existing module.
    */
    private String ivyFile = "";
    private boolean useProjectSettings = true;
    private boolean useCustomIvySettings = true;
    private String ivySettingsFile = "";
    private boolean onlyResolveSelectedConfigs = false;
    private Set<String> configsToResolve = Collections.emptySet();
    private FacetPropertiesSettings facetPropertiesSettings = new FacetPropertiesSettings();

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

    @NotNull
    public String getIvyFile() {
        return ivyFile;
    }

    public void setIvyFile(@NotNull String ivyFile) {
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

    public FacetPropertiesSettings getFacetPropertiesSettings() {
        return facetPropertiesSettings;
    }

    public void setFacetPropertiesSettings(FacetPropertiesSettings facetPropertiesSettings) {
        this.facetPropertiesSettings = facetPropertiesSettings;
    }

    @NotNull
    public String getIvySettingsFile() {
        return ivySettingsFile;
    }

    public void setIvySettingsFile(@NotNull String ivySettingsFile) {
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

    public FacetPropertiesSettings getPropertiesSettings() {
        return facetPropertiesSettings;
    }

    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        final PropertiesSettingsTab propertiesSettingsTab = new PropertiesSettingsTab(editorContext);
        final BasicSettingsTab basicSettingsTab = new BasicSettingsTab(editorContext, propertiesSettingsTab);
        return new FacetEditorTab[]{basicSettingsTab, propertiesSettingsTab};
    }

    public void readExternal(Element element) throws InvalidDataException {
        readBasicSettings(element);
        final Element propertiesSettingsElement = element.getChild("propertiesSettings");
        if (propertiesSettingsElement != null) {
            facetPropertiesSettings.readExternal(propertiesSettingsElement);
        }
    }

    private void readBasicSettings(Element element) {
        setIvyFile(element.getAttributeValue("ivyFile", ""));
        setUseCustomIvySettings(Boolean.valueOf(element.getAttributeValue("useCustomIvySettings", Boolean.TRUE.toString())));
        setIvySettingsFile(element.getAttributeValue("ivySettingsFile", ""));
        setOnlyResolveSelectedConfigs(Boolean.valueOf(element.getAttributeValue("onlyResolveSelectedConfigs", Boolean.FALSE.toString())));
        setUseProjectSettings(Boolean.valueOf(element.getAttributeValue("useProjectSettings", Boolean.TRUE.toString())));
        final Element configsToResolveElement = element.getChild("configsToResolve");
        if (configsToResolveElement != null) {
            Set<String> configsToResolve = new TreeSet<String>();
            @SuppressWarnings("unchecked")
            final List<Element> configElements = (List<Element>) configsToResolveElement.getChildren("config");
            for (Element configElement : configElements) {
                configsToResolve.add(configElement.getTextTrim());
            }
            setConfigsToResolve(configsToResolve);
        }
    }

    public void writeExternal(Element element) throws WriteExternalException {
        writeBasicSettings(element);
        final Element propertiesSettingsElement = new Element("propertiesSettings");
        if (facetPropertiesSettings != null) {
            facetPropertiesSettings.writeExternal(propertiesSettingsElement);
        }
        element.addContent(propertiesSettingsElement);
    }

    private void writeBasicSettings(Element element) {
        element.setAttribute("ivyFile", ivyFile == null ? "" : ivyFile);
        element.setAttribute("useProjectSettings", Boolean.toString(useProjectSettings));
        element.setAttribute("useCustomIvySettings", Boolean.toString(useCustomIvySettings));
        element.setAttribute("ivySettingsFile", ivySettingsFile == null ? "" : ivySettingsFile);
        element.setAttribute("onlyResolveSelectedConfigs", Boolean.toString(onlyResolveSelectedConfigs));
        if (configsToResolve != null && !configsToResolve.isEmpty()) {
            final Element configsElement = new Element("configsToResolve");
            for (String configToResolve : configsToResolve) {
                configsElement.addContent(new Element("config").setText(configToResolve));
            }
            element.addContent(configsElement);
        }
    }

}
