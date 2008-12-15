package org.clarent.ivyidea.intellij.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.clarent.ivyidea.intellij.facet.ui.IvyIdeaFacetEditorTab;
import org.jdom.Element;
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

    private String ivyFile;
    private boolean useProjectSettings = true;
    private String ivySettingsFile;
    private boolean onlyResolveSelectedConfigs = true;
    private Set<String> configsToResolve = Collections.emptySet();

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

    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        return new FacetEditorTab[]{new IvyIdeaFacetEditorTab(editorContext)};
    }

    public void readExternal(Element element) throws InvalidDataException {
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

}
