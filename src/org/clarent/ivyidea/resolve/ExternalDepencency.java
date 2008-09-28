package org.clarent.ivyidea.resolve;

import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public class ExternalDepencency implements ResolvedDependency {

    private static final Logger LOGGER = Logger.getLogger(ExternalDepencency.class.getName());

    private File externalArtifact;

    public ExternalDepencency(File externalArtifact) {
        this.externalArtifact = externalArtifact;
    }

    public File getExternalArtifact() {
        return externalArtifact;
    }

    public void addTo(ModifiableRootModel moduleModel, Library.ModifiableModel libraryModel) {
        final String artefactPath = externalArtifact.getAbsolutePath();
        LOGGER.info("Registering external file dependency: " + artefactPath);
        libraryModel.addRoot("jar://" + externalArtifact.getAbsolutePath() + "!/", OrderRootType.CLASSES);
    }

}
