package org.clarent.ivyidea.config;

/**
 * TODO: externalize this configuration
 *
 * @author Guy Mahieu
 */

public class IvyIdeaTempConfiguration {

    public static IvyIdeaTempConfiguration getCurrent() {
        return new IvyIdeaTempConfiguration();
    }

    public String getCreatedLibraryName() {
        return "IvyIDEA-resolved";
    }

}
