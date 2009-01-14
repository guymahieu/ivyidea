package org.clarent.ivyidea.ivy;

import com.intellij.openapi.module.Module;
import org.apache.ivy.Ivy;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Guy Mahieu
 */

public class IvyManager {

    private Map<String, Ivy> configuredIvyInstances = new HashMap<String, Ivy>();

    public Ivy getIvy(Module module) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        final File ivySettingsFile = IvyIdeaConfigHelper.getIvySettingsFile(module);
        final String ivySettingsPath = ivySettingsFile.getAbsolutePath();
        if (!configuredIvyInstances.containsKey(ivySettingsPath)) {
            try {
                Ivy ivy = Ivy.newInstance();
                injectProperties(ivy, module);
                ivy.configure(ivySettingsFile);
                configuredIvyInstances.put(ivySettingsPath, ivy);
            } catch (ParseException e) {
                throw new IvySettingsFileReadException(ivySettingsPath, module.getName(), e);
            } catch (IOException e) {
                throw new IvySettingsFileReadException(ivySettingsPath, module.getName(), e);
            }
        }
        return configuredIvyInstances.get(ivySettingsPath);
    }

    protected void injectProperties(Ivy ivy, Module module) throws IvySettingsFileReadException, IvySettingsNotFoundException {
        // By default, we use the module root as basedir (can be overridden by properties injected below)
        final File moduleFileFolder = new File(module.getModuleFilePath()).getParentFile();
        if (moduleFileFolder != null) {
            ivy.getSettings().setBaseDir(moduleFileFolder.getAbsoluteFile());
        }
        // Inject properties from settings
        final Properties properties = IvyIdeaConfigHelper.getIvyProperties(module);
        @SuppressWarnings("unchecked")
        final Enumeration<String> propertyNames = (Enumeration<String>) properties.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = propertyNames.nextElement();
            ivy.getSettings().setVariable(propertyName, properties.getProperty(propertyName));
        }
    }

}
