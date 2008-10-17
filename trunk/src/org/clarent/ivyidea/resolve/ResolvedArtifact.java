package org.clarent.ivyidea.resolve;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Collection;
import java.util.Arrays;

/**
 * @author Guy Mahieu
 */

public class ResolvedArtifact {

    // TODO: make these types configurable
    private static final String[] SOURCE_TYPES = {"source", "src", "sources", "srcs"};
    private static final String[] CLASSES_TYPES = {"jar", "sar", "war", "ear"};
    private static final String[] JAVADOC_TYPES = {"javadoc", "doc", "docs", "apidoc", "apidocs", "documentation", "documents"};

    private Artifact artifact;

    public ResolvedArtifact(@NotNull Artifact artifact) {
        this.artifact = artifact;
    }

    @NotNull
    public Artifact getArtifact() {
        return artifact;
    }

    public boolean isSourceType() {
        return isOfType(artifact, Arrays.asList(SOURCE_TYPES));
    }

    public boolean isClassesType() {
        return isOfType(artifact, Arrays.asList(CLASSES_TYPES));
    }

    public boolean isJavaDocType() {
        return isOfType(artifact, Arrays.asList(JAVADOC_TYPES));
    }

    protected boolean isOfType(@NotNull Artifact artifact, @NotNull Collection<String> types) {
        for (String type : types) {
            if (type.equals(artifact.getType())) {
                return true;
            }
        }
        return false;
    }
    
}

