package org.clarent.ivyidea.intellij.facet.config;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacet;
import org.clarent.ivyidea.intellij.facet.ui.BasicSettingsTab;
import org.clarent.ivyidea.intellij.facet.ui.PropertiesSettingsTab;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaFacetConfiguration implements FacetConfiguration {

    private static final Logger LOGGER = Logger.getLogger(IvyIdeaFacetConfiguration.class.getName());

    private String ivyFile;
    private boolean useProjectSettings = true;
    private String ivySettingsFile;
    private boolean onlyResolveSelectedConfigs = true;
    private Set<String> configsToResolve = Collections.emptySet();
    private PropertiesSettings propertiesSettings = new PropertiesSettings();

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

    public PropertiesSettings getPropertiesSettings() {
        return propertiesSettings;
    }

    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        return new FacetEditorTab[]{new BasicSettingsTab(editorContext), new PropertiesSettingsTab(editorContext)};
    }

    public void readExternal(Element element) throws InvalidDataException {
        readBasicSettings(element);
        final Element propertiesSettingsElement = element.getChild("propertiesSettings");
        if (propertiesSettingsElement != null) {
            propertiesSettings.readExternal(propertiesSettingsElement);
        }
    }

    private void readBasicSettings(Element element) {
        setIvyFile(element.getAttributeValue("ivyFile", ""));
        setIvySettingsFile(element.getAttributeValue("ivySettingsFile", ""));
        Boolean useProjectSettingsDefault = Boolean.TRUE;
        if (getIvySettingsFile() == null || getIvyFile().trim().length() == 0) {
            useProjectSettingsDefault = Boolean.FALSE;
        }
        setUseProjectSettings(Boolean.valueOf(element.getAttributeValue("useProjectSettings", useProjectSettingsDefault.toString())));
        setOnlyResolveSelectedConfigs(Boolean.valueOf(element.getAttributeValue("onlyResolveSelectedConfigs", Boolean.FALSE.toString())));
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
        if (propertiesSettings != null) {
            propertiesSettings.writeExternal(propertiesSettingsElement);
        }
        element.addContent(propertiesSettingsElement);
    }

    private void writeBasicSettings(Element element) {
        element.setAttribute("ivyFile", ivyFile == null ? "" : ivyFile);
        element.setAttribute("useProjectSettings", Boolean.toString(useProjectSettings));
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

    protected static class PropertiesSettings implements JDOMExternalizable {

        private boolean includeProjectLevelPropertiesFiles = true;
        private List<String> propertyFiles = Collections.emptyList();
        private SortedProperties additionalProperties = new SortedProperties();

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

        public SortedProperties getAdditionalProperties() {
            return additionalProperties;
        }

        public void setAdditionalProperties(SortedProperties additionalProperties) {
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
            SortedProperties additionalProperties = new SortedProperties();
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
            final SortedProperties additionalProperties = getAdditionalProperties();
            for (String propertyName : additionalProperties) {
                additionalPropertiesElement.addContent(new Element("property")
                        .setAttribute("name", propertyName)
                        .setText(additionalProperties.getValue(propertyName)));

                
            }
        }
    }

    protected static class SortedProperties implements Iterable<String> {

        private List<String> sortedKeys = new ArrayList<String>();
        private Map<String, String> data = new HashMap<String, String>();

        public void add(String key, String value) {
            if (sortedKeys.contains(key)) {
                sortedKeys.remove(key);
            }
            sortedKeys.add(key);
            data.put(key, value);
        }

        public void remove(String key) {
            sortedKeys.remove(key);
            data.remove(key);
        }

        public Iterator<String> keySet() {
            return sortedKeys.iterator();
        }

        public String getValue(String key) {
            return data.get(key);
        }

        public Iterator<String> iterator() {
            return keySet();
        }
    }

}
