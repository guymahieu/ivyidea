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
        int numericMajorVersion = getNumericMajorVersion();
        if (numericMajorVersion > 0) {
            // Numeric comparison...
            if (numericMajorVersion >= 8) {
                compatibilityMethods = new Intellij8Methods();
            } else if (numericMajorVersion > 0) {
                compatibilityMethods = new Intellij7Methods();
            }
        } else {
            // literal string comparison...
            if ("8".equals(majorVersion)) {
                LOGGER.info("Compatibility mode set to IntelliJ 8.0");
                compatibilityMethods = new Intellij8Methods();
            } else {
                LOGGER.info("Compatibility mode set to IntelliJ 7.0");
                compatibilityMethods = new Intellij7Methods();
            }
        }
    }

    public static IntellijCompatibilityMethods getCompatibilityMethods() {
        return compatibilityMethods;
    }

    private static int getNumericMajorVersion() {
        final String version = ApplicationInfo.getInstance().getMajorVersion();
        try {
            return Integer.parseInt(version);
        } catch (NumberFormatException e) {
            LOGGER.info("Non-numeric major version encountered:" + version);
            return -1;
        }
    }

}
