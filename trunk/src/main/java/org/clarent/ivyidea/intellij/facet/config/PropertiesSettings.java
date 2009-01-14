package org.clarent.ivyidea.intellij.facet.config;

import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Guy Mahieu
*/
public class PropertiesSettings implements JDOMExternalizable {

    private boolean includeProjectLevelPropertiesFiles = true;
    private List<String> propertyFiles = Collections.emptyList();
    private IvyIdeaFacetConfiguration.SortedProperties additionalProperties = new IvyIdeaFacetConfiguration.SortedProperties();

    public boolean isIncludeProjectLevelPropertiesFiles() {
        return includeProjectLevelPropertiesFiles;
    }

    public void setIncludeProjectLevelPropertiesFiles(boolean includeProjectLevelPropertiesFiles) {
        this.includeProjectLevelPropertiesFiles = includeProjectLevelPropertiesFiles;
    }

    public List<String> getPropertyFiles() {
        return propertyFiles;
    }

    public void setPropertyFiles(List<String> propertyFiles) {
        this.propertyFiles = propertyFiles;
    }

    public IvyIdeaFacetConfiguration.SortedProperties getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(IvyIdeaFacetConfiguration.SortedProperties additionalProperties) {
        this.additionalProperties = additionalProperties;
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
        IvyIdeaFacetConfiguration.SortedProperties additionalProperties = new IvyIdeaFacetConfiguration.SortedProperties();
        if (additionalPropertiesElement != null) {
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
        propertiesSettingsElement.addContent(additionalPropertiesElement);
        final IvyIdeaFacetConfiguration.SortedProperties additionalProperties = getAdditionalProperties();
        for (String propertyName : additionalProperties) {
            additionalPropertiesElement.addContent(new Element("property")
                    .setAttribute("name", propertyName)
                    .setText(additionalProperties.getValue(propertyName)));


        }
    }
}
