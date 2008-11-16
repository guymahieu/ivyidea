package org.clarent.ivyidea.intellij.compatibility;

import com.intellij.openapi.application.ApplicationInfo;

import java.util.logging.Logger;

/**
 * Abstracts changed api's between intellij 7 and 8 to allow the plugin
 * to be used in both versions.
 *
 * @author Guy Mahieu
 */
public class IntellijCompatibilityService {

    private static Logger LOGGER = Logger.getLogger("IvyIDEA-Compatibility");

    private static IntellijCompatibilityMethods compatibilityMethods;

    static {
        final String majorVersion = ApplicationInfo.getInstance().getMajorVersion();
        LOGGER.info("Detected intellij major version " + majorVersion + "; activating the appropriate compatibility mode.");
        if ("8".equals(majorVersion)) {
            LOGGER.info("Compatibility mode set to IntelliJ 8.0");
            compatibilityMethods = new Intellij8Methods();
        } else {
            LOGGER.info("Compatibility mode set to IntelliJ 7.0");
            compatibilityMethods = new Intellij7Methods();
        }
    }

    public static IntellijCompatibilityMethods getCompatibilityMethods() {
        return compatibilityMethods;
    }

}
