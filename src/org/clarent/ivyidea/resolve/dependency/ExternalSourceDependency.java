package org.clarent.ivyidea.resolve.dependency;

import com.intellij.openapi.roots.OrderRootType;

import java.io.File;

/**
 * @author Guy Mahieu
 */

public class ExternalSourceDependency extends ExternalDependency {

    public ExternalSourceDependency(File externalArtifact) {
        super(externalArtifact);
    }

    protected String getTypeName() {
        return "sources";
    }

    protected OrderRootType getType() {
        return OrderRootType.SOURCES;
    }
}