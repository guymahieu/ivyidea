package org.clarent.ivyidea.resolve;

import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public class ExternalJarDependency extends ExternalDependency {

    public ExternalJarDependency(File externalArtifact) {
        super(externalArtifact);
    }

    protected String getTypeName() {
        return "jar";
    }

    protected OrderRootType getType() {
        return OrderRootType.CLASSES;
    }

}
