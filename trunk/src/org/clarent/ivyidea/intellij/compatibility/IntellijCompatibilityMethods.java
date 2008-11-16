package org.clarent.ivyidea.intellij.compatibility;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.OrderRootType;

/**
 * @author Guy Mahieu
 */

public interface IntellijCompatibilityMethods {

    OrderRootType[] getAllOrderRootTypes();

    OrderRootType getJavadocOrderRootType();

    ModuleType getJavaModuleType();

}
