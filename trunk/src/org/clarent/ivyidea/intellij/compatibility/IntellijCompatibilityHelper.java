package org.clarent.ivyidea.intellij.compatibility;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.OrderRootType;

/**
 * Abstracts changed api's between intellij 7 and 8 to allow the plugin
 * to be used in both versions.
 *
 * @author Guy Mahieu
 */
public class IntellijCompatibilityHelper implements IntellijCompatibilityFunctions {

    private static IntellijCompatibilityHelper instance = new IntellijCompatibilityHelper();

    private IntellijCompatibilityFunctions delegate;

    public static IntellijCompatibilityHelper getInstance() {
        return instance;
    }

    private IntellijCompatibilityHelper() {
        if ("8".equals(ApplicationInfo.getInstance().getMajorVersion())) {
            this.delegate = new Intellij8Helper();
        } else {
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
