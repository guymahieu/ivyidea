package org.clarent.ivyidea.intellij.compatibility;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.OrderRootType;

/**
 * @author Guy Mahieu
 */

interface IntellijCompatibilityFunctions {

    OrderRootType[] getAllOrderRootTypes();

    OrderRootType getJavadocOrderRootType();

    ModuleType getJavaModuleType();

}
