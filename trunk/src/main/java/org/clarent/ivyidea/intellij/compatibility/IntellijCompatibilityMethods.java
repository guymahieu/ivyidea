package org.clarent.ivyidea.intellij.compatibility;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.OrderRootType;

/**
 * Interface defining methods that are impacted by API differences in supported
 * versions of Intellij IDEA
 *
 * @author Guy Mahieu
 */
public interface IntellijCompatibilityMethods {

    OrderRootType[] getAllOrderRootTypes();

    OrderRootType getJavadocOrderRootType();

    ModuleType getJavaModuleType();

    boolean isTaskCancelledOnProgressIndicatorCancel();

}
