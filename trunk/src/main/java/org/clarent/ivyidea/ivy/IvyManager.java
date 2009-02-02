package org.clarent.ivyidea.ivy;

import com.intellij.openapi.module.Module;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.settings.IvySettings;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Guy Mahieu
 */

public class IvyManager {

    private Map<Module, Ivy> configuredIvyInstances = new HashMap<Module, Ivy>();

    public Ivy getIvy(Module module) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        if (!configuredIvyInstances.containsKey(module)) {
            final IvySettings configuredIvySettings = IvyIdeaConfigHelper.createConfiguredIvySettings(module);
            final Ivy ivy = Ivy.newInstance(configuredIvySettings);
            configuredIvyInstances.put(module, ivy);
        }
        return configuredIvyInstances.get(module);
    }

}
