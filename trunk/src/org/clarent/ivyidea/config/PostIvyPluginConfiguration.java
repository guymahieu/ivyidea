package org.clarent.ivyidea.config;

/**
 * TODO: externalize this configuration
 *
 * @author Guy Mahieu
 */

public class PostIvyPluginConfiguration {

    private static final String POSTIVYPLUGIN_PROPERTIES = "ivyidea.properties";

    public static PostIvyPluginConfiguration getCurrent() {
        return new PostIvyPluginConfiguration();
    }

    public String getIvyConfFile() {
        return "C:\\Users\\guy\\IdeaProjects\\PostIvyPlugin\\sample\\untitled\\ivy-settings.xml";
    }

    public String getIvyConfiguration() {
        return "eclipse";
    }

    public String getCreatedLibraryName() {
        return "IvyIDEA-resolved";
    }

    public String getIvyModuleDescriptorFileName() {
        return "ivy.xml";
    }

}
