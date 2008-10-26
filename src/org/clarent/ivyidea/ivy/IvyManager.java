package org.clarent.ivyidea.ivy;

import com.intellij.openapi.module.Module;
import org.apache.ivy.Ivy;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;

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
        final File ivySettingsFile = IvyIdeaConfigHelper.getIvySettingsFile(module);
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

}
