package org.clarent.ivyidea.resolve.dependency;

import com.intellij.openapi.roots.OrderRootType;
import org.apache.ivy.core.module.descriptor.Artifact;

import java.io.File;

/**
 * @author Guy Mahieu
 */

public class ExternalJarDependency extends ExternalDependency {

    public ExternalJarDependency(Artifact artifact, File externalArtifact) {
        super(artifact, externalArtifact);
    }

    protected String getTypeName() {
        return "jar";
    }

    public OrderRootType getType() {
        return OrderRootType.CLASSES;
    }

}
