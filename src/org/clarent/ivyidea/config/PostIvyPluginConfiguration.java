package org.clarent.ivyidea.config;

/**
 * TODO: externalize this configuration
 *
 * @author Guy Mahieu
 */

public class PostIvyPluginConfiguration {

    public static PostIvyPluginConfiguration getCurrent() {
        return new PostIvyPluginConfiguration();
    }

    public String getIvyConfiguration() {
        return "eclipse";
    }

    public String getCreatedLibraryName() {
        return "IvyIDEA-resolved";
    }

}
