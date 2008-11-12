package org.clarent.ivyidea.intellij.compatibility;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.OrderRootType;

/**
 * @author Guy Mahieu
 */

class Intellij7Helper implements IntellijCompatibilityFunctions {

    private static final String COMPAT_ERROR_MSG = "Compatibility problem: this plugin is only meant to be used in IntelliJ version 7 and up.";

    public OrderRootType[] getAllOrderRootTypes() {
        try {
            // OrderRootType.ALL_TYPES
            return (OrderRootType[]) OrderRootType.class.getField("ALL_TYPES").get(null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        }
    }

    public OrderRootType getJavadocOrderRootType() {
        try {
            // OrderRootType.JAVADOC
            return (OrderRootType) OrderRootType.class.getField("JAVADOC").get(null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        }
    }

    public ModuleType getJavaModuleType() {
        try {
            // ModuleType.JAVA
            return (ModuleType) ModuleType.class.getField("JAVA").get(null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        }
    }
}
