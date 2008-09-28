package org.clarent.ivyidea.resolve;

import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.Library;

/**
 * @author Guy Mahieu
 */

public interface ResolvedDependency {

    void addTo(ModifiableRootModel intellijModule, Library.ModifiableModel libraryModel);

}
