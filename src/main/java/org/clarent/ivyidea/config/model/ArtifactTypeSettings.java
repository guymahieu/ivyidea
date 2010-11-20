/*
 * Copyright 2010 Guy Mahieu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.clarent.ivyidea.config.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.util.Arrays.asList;
import static org.clarent.ivyidea.config.model.ArtifactTypeSettings.DependencyCategory.Classes;
import static org.clarent.ivyidea.config.model.ArtifactTypeSettings.DependencyCategory.Javadoc;
import static org.clarent.ivyidea.config.model.ArtifactTypeSettings.DependencyCategory.Sources;

/**
 * @author Guy Mahieu
 */
public class ArtifactTypeSettings implements PersistentStateComponent<ArtifactTypeSettings> {

    public enum DependencyCategory {
        Sources("source", "src", "sources", "srcs"),
        Javadoc("javadoc", "doc", "docs", "apidoc", "apidocs", "documentation", "documents"),
        Classes("jar", "sar", "war", "ear", "ejb", "bundle", "test-jar");

        private final String[] defaultTypes;

        DependencyCategory(String... defaultTypes) {
            this.defaultTypes = defaultTypes;
        }

        public List<String> getDefaultTypes() {
            return asList(defaultTypes);
        }
    }

    private Map<DependencyCategory, Set<String>> typesPerCategory = new HashMap<DependencyCategory, Set<String>>();

    @Nullable
    public DependencyCategory getCategoryForType(String type) {
        if (type == null) {
            return null;
        }
        if (isConfigurationEmpty()) {
            fillDefaults();
        }
        for (DependencyCategory dependencyCategory : typesPerCategory.keySet()) {
            final Set<String> types = typesPerCategory.get(dependencyCategory);
            if (types != null && types.contains(type.trim().toLowerCase())) {
                return dependencyCategory;
            }
        }
        return null;
    }

    private void fillDefaults() {
        for (DependencyCategory category : DependencyCategory.values()) {
            setTypesForCategory(category, joinArtifactTypes(category.getDefaultTypes()));
        }
    }

    public void setTypesForCategory(@NotNull DependencyCategory category, String types) {
        if (types != null) {
            typesPerCategory.put(category, splitArtifactTypes(types));
        }
    }

    public String getTypesStringForCategory(@NotNull DependencyCategory category) {
        if (isConfigurationEmpty()) {
            // nothing is configured for any category --> use defaults 
            return joinArtifactTypes(category.getDefaultTypes());
        }
        return joinArtifactTypes(typesPerCategory.get(category));
    }

    protected boolean isConfigurationEmpty() {
        boolean configFound = false;
        for (DependencyCategory dependencyCategory : DependencyCategory.values()) {
            final Set<String> types = typesPerCategory.get(dependencyCategory);
            configFound = types != null && types.size() > 0;
            if (configFound) {
                break;
            }
        }
        return !configFound;
    }

    private Set<String> splitArtifactTypes(String artifactTypesString) {
        Set<String> result = new LinkedHashSet<String>();
        if (artifactTypesString != null) {
            final String[] types = artifactTypesString.split(",");
            for (String type : types) {
                final String typeToAdd = type.trim().toLowerCase();
                if (typeToAdd.length() > 0) {
                    result.add(typeToAdd);
                }
            }
        }
        return result;
    }

    private String joinArtifactTypes(Iterable<String> artifactTypes) {
        if (artifactTypes == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String separator = "";
        for (String artifactType : artifactTypes) {
            sb.append(separator).append(artifactType);
            separator = ", ";
        }
        return sb.toString();
    }

    public ArtifactTypeSettings getState() {
        return this;
    }

    public void loadState(ArtifactTypeSettings state) {
        if (state == null) {
            state = new ArtifactTypeSettings();
        }
        XmlSerializerUtil.copyBean(state, this);
    }

    // Getters and setters needed for intellij settings serialization

    public String getSourcesTypes() {
        return joinArtifactTypes(typesPerCategory.get(Sources));
    }

    public String getClassesTypes() {
        return joinArtifactTypes(typesPerCategory.get(Classes));
    }

    public String getJavadocTypes() {
        return joinArtifactTypes(typesPerCategory.get(Javadoc));
    }

    public void setSourcesTypes(String types) {
        setTypesForCategory(Sources, types);
    }

    public void setClassesTypes(String types) {
        setTypesForCategory(Classes, types);
    }

    public void setJavadocTypes(String types) {
        setTypesForCategory(Javadoc, types);
    }
}
