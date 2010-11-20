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

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

import static org.clarent.ivyidea.config.model.ArtifactTypeSettings.DependencyCategory.Classes;

/**
 * @author Guy Mahieu
 */
public class ArtifactTypeSettingsTest extends TestCase {

    @Test
    public void testNewObjectAlwaysEmpty() {
        Assert.assertTrue(new ArtifactTypeSettings().isConfigurationEmpty());
    }

    @Test
    public void testObjectWithOnlyEmptyStringsAlwaysEmpty() {
        final ArtifactTypeSettings artifactTypeSettings = new ArtifactTypeSettings();
        artifactTypeSettings.setClassesTypes("");
        Assert.assertTrue(artifactTypeSettings.isConfigurationEmpty());
    }

    @Test
    public void testObjectWithDataNeverEmpty() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        typeSettings.setTypesForCategory(Classes, "jar");
        Assert.assertFalse(typeSettings.isConfigurationEmpty());
    }

    @Test
    public void testTypeNamesAreCaseInsensitive() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        typeSettings.setTypesForCategory(Classes, "JAR");
        Assert.assertEquals(Classes, typeSettings.getCategoryForType("jar"));
    }

    @Test
    public void testCorrectCategoryReturnedForType() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        typeSettings.setTypesForCategory(Classes, "jar");
        Assert.assertEquals(Classes, typeSettings.getCategoryForType("jar"));
        Assert.assertEquals(Classes, typeSettings.getCategoryForType("jar "));
        Assert.assertEquals(Classes, typeSettings.getCategoryForType(" jar"));
        Assert.assertEquals(Classes, typeSettings.getCategoryForType(" jar "));
    }
    @Test
    public void testOrderOfTypesPreservedWhenSettingAndGetting() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        final String expected = "a, b, c, d, e, f, g";
        typeSettings.setTypesForCategory(Classes, expected);
        final String actual = typeSettings.getTypesStringForCategory(Classes);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testNullForUnknownType() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        typeSettings.setTypesForCategory(Classes, "jar");
        Assert.assertNull(typeSettings.getCategoryForType("foo"));
    }

    @Test
    public void testSerializationGettersDoNotReturnDefaultValuesIfObjectEmpty() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        Assert.assertEquals("", typeSettings.getClassesTypes());
        Assert.assertEquals("", typeSettings.getSourcesTypes());
        Assert.assertEquals("", typeSettings.getJavadocTypes());
    }

    @Test
    public void testGetTypeStringReturnsDefaultValuesIfObjectEmpty() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        for (ArtifactTypeSettings.DependencyCategory category : ArtifactTypeSettings.DependencyCategory.values()) {
            final String typesStringForCategory = typeSettings.getTypesStringForCategory(category);
            Assert.assertNotNull(typesStringForCategory);
            Assert.assertTrue(typesStringForCategory.length() > 0);
        }
    }

}
