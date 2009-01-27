package org.clarent.ivyidea.resolve.dependency;

import com.intellij.openapi.roots.OrderRootType;
import org.apache.ivy.core.module.descriptor.Artifact;

import java.io.File;

/**
 * @author Guy Mahieu
 */

public class ExternalSourceDependency extends ExternalDependency {

    public ExternalSourceDependency(Artifact artifact, File externalArtifact) {
        super(artifact, externalArtifact);
    }

    protected String getTypeName() {
        return "sources";
    }

    public OrderRootType getType() {
        return OrderRootType.SOURCES;
    }
}