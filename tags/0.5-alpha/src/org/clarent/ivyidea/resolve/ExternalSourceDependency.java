package org.clarent.ivyidea.resolve;

import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;

import java.io.File;
import java.util.logging.Logger;

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