package org.clarent.ivyidea.resolve.dependency;

import com.intellij.openapi.roots.OrderRootType;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.clarent.ivyidea.intellij.compatibility.IntellijCompatibilityService;

import java.io.File;

/**
 * @author Guy Mahieu
 */

public class ExternalJavaDocDependency extends ExternalDependency {

    public ExternalJavaDocDependency(Artifact artifact, File externalArtifact) {
        super(artifact, externalArtifact);
    }

    protected String getTypeName() {
        return "javadoc";
    }

    protected OrderRootType getType() {
        return IntellijCompatibilityService.getCompatibilityMethods().getJavadocOrderRootType();
    }

}