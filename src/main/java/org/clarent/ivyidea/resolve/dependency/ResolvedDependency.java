package org.clarent.ivyidea.resolve.dependency;

import org.clarent.ivyidea.intellij.model.IntellijModuleWrapper;

/**
 * @author Guy Mahieu
 */

public interface ResolvedDependency {

    void addTo(IntellijModuleWrapper intellijModuleWrapper);

}
