package org.clarent.ivyidea.resolve;

import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public abstract class ExternalDependency implements ResolvedDependency {

    private static final Logger LOGGER = Logger.getLogger(ExternalJarDependency.class.getName());
    
    protected File externalArtifact;

    public ExternalDependency(File externalArtifact) {
        this.externalArtifact = externalArtifact;
    }

    public File getExternalArtifact() {
        return externalArtifact;
    }

    public void addTo(ModifiableRootModel moduleModel, Library.ModifiableModel libraryModel) {
        final String artifactPath = externalArtifact.getAbsolutePath();
        if (new File(artifactPath).exists()) {
            LOGGER.info("Registering external " + getTypeName() + " file dependency: " + artifactPath);
            libraryModel.addRoot("jar://" + externalArtifact.getAbsolutePath() + "!/", getType());
        } else {
            LOGGER.warning("Not registering external " + getTypeName() + " file dependency as the file does not seem to exist: " + artifactPath);
        }
    }

    protected abstract String getTypeName();

    protected abstract OrderRootType getType();
}
