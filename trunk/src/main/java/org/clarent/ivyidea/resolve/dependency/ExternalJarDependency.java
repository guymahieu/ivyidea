package org.clarent.ivyidea.resolve.dependency;

import com.intellij.openapi.roots.OrderRootType;

import java.io.File;

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
