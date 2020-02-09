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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.clarent.ivyidea.config.model.ArtifactTypeSettings.DependencyCategory.Classes;

/**
 * @author Guy Mahieu
 */
public class ArtifactTypeSettingsTest {

    @Test
    public void testNewObjectAlwaysEmpty() {
        assertThat(new ArtifactTypeSettings().isConfigurationEmpty()).isTrue();
    }

    @Test
    public void testObjectWithOnlyEmptyStringsAlwaysEmpty() {
        final ArtifactTypeSettings artifactTypeSettings = new ArtifactTypeSettings();
        artifactTypeSettings.setClassesTypes("");
        assertThat(artifactTypeSettings.isConfigurationEmpty()).isTrue();
    }

    @Test
    public void testObjectWithDataNeverEmpty() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        typeSettings.setTypesForCategory(Classes, "jar");
        assertThat(typeSettings.isConfigurationEmpty()).isFalse();
    }

    @Test
    public void testTypeNamesAreCaseInsensitive() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        typeSettings.setTypesForCategory(Classes, "JAR");
        assertThat(typeSettings.getCategoryForType("jar")).isSameAs(Classes);
    }

    @Test
    public void testCorrectCategoryReturnedForType() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        typeSettings.setTypesForCategory(Classes, "jar");
        assertThat(typeSettings.getCategoryForType("jar")).isSameAs(Classes);
        assertThat(typeSettings.getCategoryForType(" jar")).isSameAs(Classes);
        assertThat(typeSettings.getCategoryForType("jar ")).isSameAs(Classes);
        assertThat(typeSettings.getCategoryForType(" jar ")).isSameAs(Classes);
    }

    @Test
    public void testOrderOfTypesPreservedWhenSettingAndGetting() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        final String expected = "a, b, c, d, e, f, g";
        typeSettings.setTypesForCategory(Classes, expected);
        final String actual = typeSettings.getTypesStringForCategory(Classes);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testNullForUnknownType() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        typeSettings.setTypesForCategory(Classes, "jar");
        assertThat(typeSettings.getCategoryForType("foo")).isNull();
    }

    @Test
    public void testSerializationGettersDoNotReturnDefaultValuesIfObjectEmpty() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        assertThat(typeSettings.getClassesTypes()).isEmpty();
        assertThat(typeSettings.getSourcesTypes()).isEmpty();
        assertThat(typeSettings.getJavadocTypes()).isEmpty();
    }

    @Test
    public void testGetTypeStringReturnsDefaultValuesIfObjectEmpty() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        for (ArtifactTypeSettings.DependencyCategory category : ArtifactTypeSettings.DependencyCategory.values()) {
            final String typesStringForCategory = typeSettings.getTypesStringForCategory(category);
            assertThat(typesStringForCategory).isNotNull();
            assertThat(typesStringForCategory.length() > 0).isTrue();
        }
    }

}
