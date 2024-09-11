package org.clarent.ivyidea.intellij.facet.config;

import com.intellij.conversion.*;
import com.intellij.util.xmlb.XmlSerializer;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacetType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Convert legacy facet config to new format.
 */
public class IvyIdeaLegacyFacetConfigConverterProvider extends ConverterProvider {

    public IvyIdeaLegacyFacetConfigConverterProvider() {
        super();
    }

    @NotNull
    @Override
    public String getConversionDescription() {
        return "IvyIDEA facet configuration to new format";
    }

    @NotNull
    @Override
    public ProjectConverter createConverter(@NotNull ConversionContext context) {
        return new ProjectConverter() {
            @NotNull
            @Override
            public ConversionProcessor<ModuleSettings> createModuleFileConverter() {
                return new ConversionProcessor<ModuleSettings>() {
                    @Override
                    public boolean isConversionNeeded(ModuleSettings moduleSettings) {
                        Element facetElement = moduleSettings.getFacetElement(IvyIdeaFacetType.STRING_ID);
                        Element configurationElement = facetElement != null ? facetElement.getChild("configuration") : null;
                        return configurationElement != null && configurationElement.getAttribute("ivyFile") != null;
                    }

                    @Override
                    public void process(ModuleSettings moduleSettings) {
                        Element facetElement = moduleSettings.getFacetElement(IvyIdeaFacetType.STRING_ID);
                        Element configurationElement = facetElement != null ? facetElement.getChild("configuration") : null;
                        if (configurationElement != null) {
                            configurationElement.detach();
                            IvyIdeaFacetConfiguration.FacetConfig facetConfig = readLegacyFormatFrom(configurationElement);
                            Element convertedConfigurationElement = new Element("configuration");
                            XmlSerializer.serializeInto(facetConfig, convertedConfigurationElement);
                            facetElement.addContent(convertedConfigurationElement);
                        }
                    }

                    public IvyIdeaFacetConfiguration.FacetConfig readLegacyFormatFrom(Element element) {
                        IvyIdeaFacetConfiguration.FacetConfig result = new IvyIdeaFacetConfiguration.FacetConfig();
                        readBasicSettings(result, element);
                        final Element propertiesSettingsElement = element.getChild("propertiesSettings");
                        if (propertiesSettingsElement != null) {
                            FacetPropertiesSettings facetPropertiesSettings = new FacetPropertiesSettings();
                            final Element propertiesFilesElement = propertiesSettingsElement.getChild("propertiesFiles");
                            List<String> fileNames = new ArrayList<>();
                            if (propertiesFilesElement != null) {
                                facetPropertiesSettings.setIncludeProjectLevelPropertiesFiles(Boolean.parseBoolean(propertiesFilesElement.getAttributeValue("includeProjectLevelPropertiesFiles", Boolean.TRUE.toString())));
                                final List<Element> propertiesFileNames = propertiesFilesElement.getChildren("fileName");
                                for (Element propertiesFileNameElement : propertiesFileNames) {
                                    fileNames.add(propertiesFileNameElement.getValue());
                                }
                            }
                            facetPropertiesSettings.setPropertyFiles(fileNames);
                            result.setFacetPropertiesSettings(facetPropertiesSettings);
                        }
                        return result;
                    }

                    private void readBasicSettings(IvyIdeaFacetConfiguration.FacetConfig config, Element element) {
                        config.setIvyFile(element.getAttributeValue("ivyFile", ""));
                        config.setUseCustomIvySettings(Boolean.parseBoolean(element.getAttributeValue("useCustomIvySettings", Boolean.TRUE.toString())));
                        config.setIvySettingsFile(element.getAttributeValue("ivySettingsFile", ""));
                        config.setOnlyResolveSelectedConfigs(Boolean.parseBoolean(element.getAttributeValue("onlyResolveSelectedConfigs", Boolean.FALSE.toString())));
                        config.setUseProjectSettings(Boolean.parseBoolean(element.getAttributeValue("useProjectSettings", Boolean.TRUE.toString())));
                        final Element configsToResolveElement = element.getChild("configsToResolve");
                        if (configsToResolveElement != null) {
                            Set<String> configsToResolve = new TreeSet<>();
                            final List<Element> configElements = configsToResolveElement.getChildren("config");
                            for (Element configElement : configElements) {
                                configsToResolve.add(configElement.getTextTrim());
                            }
                            config.setConfigsToResolve(configsToResolve);
                        }
                    }
                };
            }


        };
    }

}
