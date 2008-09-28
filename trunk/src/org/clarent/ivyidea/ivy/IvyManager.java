package org.clarent.ivyidea.ivy;

import com.intellij.openapi.module.Module;
import org.apache.ivy.Ivy;
import org.clarent.ivyidea.intellij.IvyIdeaProjectSettings;
import org.clarent.ivyidea.intellij.facet.IvyFacetConfiguration;
import org.clarent.ivyidea.intellij.ui.IvyIdeaProjectSettingsComponent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Guy Mahieu
 */

public class IvyManager {

    private Map<String, Ivy> configuredIvyInstances = new HashMap<String, Ivy>();

    public Ivy getIvy(Module module) {
        final File ivySettingsFile = getIvySettingsFile(module);
        final String ivySettingsPath = ivySettingsFile.getAbsolutePath();
        if (!configuredIvyInstances.containsKey(ivySettingsPath)) {
            try {
                Ivy ivy = Ivy.newInstance();
                ivy.configure(ivySettingsFile);
                configuredIvyInstances.put(ivySettingsPath, ivy);
            } catch (ParseException e) {
                throw new RuntimeException("An error occured while parsing ivy settings file " + ivySettingsPath, e);
            } catch (IOException e) {
                throw new RuntimeException("An error occured while accessing ivy settings file " + ivySettingsPath, e);
            }
        }
        return configuredIvyInstances.get(ivySettingsPath);
    }

    @NotNull
    protected File getIvySettingsFile(Module module) {
        final IvyFacetConfiguration moduleConfiguration = IvyFacetConfiguration.getInstance(module);
        if (moduleConfiguration.isUseProjectSettings()) {
            IvyIdeaProjectSettingsComponent component = module.getProject().getComponent(IvyIdeaProjectSettingsComponent.class);
            final IvyIdeaProjectSettings state = component.getState();
            if (state != null && state.getIvySettingsFile() != null && state.getIvySettingsFile().trim().length() > 0) {
                File result = new File(state.getIvySettingsFile());
                if (!result.exists()) {
                    throw new RuntimeException("The ivy settings file configured for this project does not exist (" + result.getAbsolutePath() + ")");
                }
                return result;
            } else {
                throw new RuntimeException("No ivy settings file found in the project configuration.");
            }
        } else {
            final String ivySettingsFile = moduleConfiguration.getIvySettingsFile();
            if (ivySettingsFile != null) {
                File result = new File(ivySettingsFile);
                if (!result.exists()) {
                    throw new RuntimeException("The ivy settings file configured for module " + module.getName() + " does not exist (" + result.getAbsolutePath() + ")");
                }
                return result;
            } else {
                throw new RuntimeException("No ivy settings file found in the configuration of module " + module.getName());
            }
        }
    }

}
