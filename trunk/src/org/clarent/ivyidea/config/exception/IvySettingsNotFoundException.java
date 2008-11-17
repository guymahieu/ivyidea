package org.clarent.ivyidea.config.exception;

/**
 * Typically thrown when no ivy settings file can be found during the resolve process.
 *
 * @author Guy Mahieu
 */

public class IvySettingsNotFoundException extends Exception {

    public enum ConfigLocation {
        Project,
        Module
    }

    private ConfigLocation configLocation;
    private String configName;

    public IvySettingsNotFoundException(String message, ConfigLocation configLocation, String configName) {
        super(message);
        this.configLocation = configLocation;
        this.configName = configName;
    }

    public ConfigLocation getConfigLocation() {
        return configLocation;
    }

    public String getConfigName() {
        return configName;
    }
}

