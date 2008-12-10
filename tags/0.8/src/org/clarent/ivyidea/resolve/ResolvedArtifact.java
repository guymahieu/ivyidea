package org.clarent.ivyidea.resolve;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

/**
 * Wraps an ivy artifact and provides utility methods to check the type of artifact it is.
 *
 * @author Guy Mahieu
 */

public class ResolvedArtifact {

    // TODO: make these types configurable
    private static final String[] SOURCE_TYPES = {"source", "src", "sources", "srcs"};
    private static final String[] JAVADOC_TYPES = {"javadoc", "doc", "docs", "apidoc", "apidocs", "documentation", "documents"};
    private static final String[] CLASSES_TYPES = {"jar", "sar", "war", "ear"};

    private Artifact artifact;

    public ResolvedArtifact(@NotNull Artifact artifact) {
        this.artifact = artifact;
    }

    public boolean isSourceType() {
        return isOfType(Arrays.asList(SOURCE_TYPES));
    }

    public boolean isClassesType() {
        return isOfType(Arrays.asList(CLASSES_TYPES));
    }

    public boolean isJavaDocType() {
        return isOfType(Arrays.asList(JAVADOC_TYPES));
    }

    protected boolean isOfType(@NotNull Collection<String> types) {
        for (String type : types) {
            if (type.equals(artifact.getType())) {
                return true;
            }
        }
        return false;
    }

}

