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

package org.clarent.ivyidea.intellij.facet.config;

import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.clarent.ivyidea.config.model.PropertiesSettings;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Guy Mahieu
*/
public class FacetPropertiesSettings extends PropertiesSettings implements JDOMExternalizable {

    private boolean includeProjectLevelPropertiesFiles = true;
    private boolean includeProjectLevelAdditionalProperties = true;

    public boolean isIncludeProjectLevelPropertiesFiles() {
        return includeProjectLevelPropertiesFiles;
    }

    public void setIncludeProjectLevelPropertiesFiles(boolean includeProjectLevelPropertiesFiles) {
        this.includeProjectLevelPropertiesFiles = includeProjectLevelPropertiesFiles;
    }

    public boolean isIncludeProjectLevelAdditionalProperties() {
        return includeProjectLevelAdditionalProperties;
    }

    public void setIncludeProjectLevelAdditionalProperties(boolean includeProjectLevelAdditionalProperties) {
        this.includeProjectLevelAdditionalProperties = includeProjectLevelAdditionalProperties;
    }

    public void readExternal(Element propertiesSettingsElement) throws InvalidDataException {
        final Element propertiesFilesElement = propertiesSettingsElement.getChild("propertiesFiles");
        List<String> fileNames = new ArrayList<String>();
        if (propertiesFilesElement != null) {
            setIncludeProjectLevelPropertiesFiles(Boolean.valueOf(propertiesFilesElement.getAttributeValue("includeProjectLevelPropertiesFiles", Boolean.TRUE.toString())));
            @SuppressWarnings("unchecked")
            final List<Element> propertiesFileNames = (List<Element>) propertiesFilesElement.getChildren("fileName");
            for (Element element : propertiesFileNames) {
                fileNames.add(element.getValue());
            }
        }
        setPropertyFiles(fileNames);

        final Element additionalPropertiesElement = propertiesSettingsElement.getChild("additionalProperties");
        PropertiesSettings.SortedProperties additionalProperties = new PropertiesSettings.SortedProperties();
        if (additionalPropertiesElement != null) {
            setIncludeProjectLevelAdditionalProperties(Boolean.valueOf(propertiesFilesElement.getAttributeValue("includeProjectLevelAdditionalProperties", Boolean.TRUE.toString())));
            @SuppressWarnings("unchecked")
            final List<Element> additionalProperty = (List<Element>) additionalPropertiesElement.getChildren("property");
            for (Element element : additionalProperty) {
                final String key = element.getAttributeValue("name");
                final String value = element.getText();
                additionalProperties.add(key, value);
            }
            setAdditionalProperties(additionalProperties);
        }
    }

    public void writeExternal(Element propertiesSettingsElement) throws WriteExternalException {
        final Element propertiesFilesElement = new Element("propertiesFiles");
        propertiesFilesElement.setAttribute("includeProjectLevelPropertiesFiles", Boolean.toString(isIncludeProjectLevelPropertiesFiles()));
        propertiesSettingsElement.addContent(propertiesFilesElement);
        for (String fileName : getPropertyFiles()) {
            propertiesFilesElement.addContent(new Element("fileName").setText(fileName));
        }

        final Element additionalPropertiesElement = new Element("additionalProperties");
        additionalPropertiesElement.setAttribute("includeProjectLevelAdditionalProperties", Boolean.toString(isIncludeProjectLevelAdditionalProperties()));
        propertiesSettingsElement.addContent(additionalPropertiesElement);
        final SortedProperties additionalProperties = getAdditionalProperties();
        for (String propertyName : additionalProperties) {
            additionalPropertiesElement.addContent(new Element("property")
                    .setAttribute("name", propertyName)
                    .setText(additionalProperties.getValue(propertyName)));


        }
    }
}
