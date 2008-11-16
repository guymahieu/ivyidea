package org.clarent.ivyidea.intellij.compatibility;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.OrderRootType;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Guy Mahieu
 */

class Intellij8Methods implements IntellijCompatibilityMethods {

    private static final String COMPAT_ERROR_MSG = "Compatibility problem: this plugin is only meant to be used in IntelliJ version 7 and up.";

    public OrderRootType[] getAllOrderRootTypes() {
        try {
            // OrderRootType.getAllTypes()
            return (OrderRootType[]) OrderRootType.class.getMethod("getAllTypes").invoke(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        }
    }

    public OrderRootType getJavadocOrderRootType() {
        try {
            final Class<?> javadocOrderRootTypeClass = Class.forName("com.intellij.openapi.roots.JavadocOrderRootType");
            return (OrderRootType) javadocOrderRootTypeClass.getMethod("getInstance").invoke(null);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        }
    }

    public ModuleType getJavaModuleType() {
        try {
            final Class<?> stdModuleTypesClass = Class.forName("com.intellij.openapi.module.StdModuleTypes");
            return (ModuleType) stdModuleTypesClass.getField("JAVA").get(null);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        }
    }
}