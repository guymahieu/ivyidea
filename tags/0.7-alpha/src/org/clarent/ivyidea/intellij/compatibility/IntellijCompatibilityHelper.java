package org.clarent.ivyidea.intellij.compatibility;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.OrderRootType;

import java.util.logging.Logger;

/**
 * Abstracts changed api's between intellij 7 and 8 to allow the plugin
 * to be used in both versions.
 *
 * @author Guy Mahieu
 */
public class IntellijCompatibilityHelper implements IntellijCompatibilityFunctions {

    private static Logger LOGGER = Logger.getLogger("IvyIDEA-Compatibility");
    private static IntellijCompatibilityHelper instance = new IntellijCompatibilityHelper();

    private IntellijCompatibilityFunctions delegate;

    public static IntellijCompatibilityHelper getInstance() {
        return instance;
    }

    private IntellijCompatibilityHelper() {
        final String majorVersion = ApplicationInfo.getInstance().getMajorVersion();
        LOGGER.info("Detected intellij major version " + majorVersion + "; activating the appropriate compatibility mode.");
        if ("8".equals(majorVersion)) {
            LOGGER.info("Compatibility mode set to IntelliJ 8.0");
            this.delegate = new Intellij8Helper();
        } else {
            LOGGER.info("Compatibility mode set to IntelliJ 7.0");
            this.delegate = new Intellij7Helper();
        }
    }

    public OrderRootType[] getAllOrderRootTypes() {
        return delegate.getAllOrderRootTypes();
    }

    public OrderRootType getJavadocOrderRootType() {
        return delegate.getJavadocOrderRootType();
    }

    public ModuleType getJavaModuleType() {
        return delegate.getJavaModuleType();
    }
}
