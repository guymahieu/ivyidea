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

package org.clarent.ivyidea.config;

import com.intellij.openapi.util.io.FileUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

public class IvyIdeaPathsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private String moduleDir;
    private String projectDir;
    private UnaryOperator<String> macros;

    @Before
    public void setUp() throws Exception {
        projectDir = FileUtil.toSystemIndependentName(temporaryFolder.getRoot().getCanonicalPath());
        moduleDir = projectDir + "/module";
        macros = path -> path.replace("$MODULE_DIR$", moduleDir).replace("$PROJECT_DIR$", projectDir);
    }

    @Test
    public void expandsMacros() {
        assertThat(IvyIdeaPaths.expand("$MODULE_DIR$/ivy.xml", macros, moduleDir)).isEqualTo(moduleDir + "/ivy.xml");
        assertThat(IvyIdeaPaths.expand("$PROJECT_DIR$/ivysettings.xml", macros, moduleDir)).isEqualTo(projectDir + "/ivysettings.xml");
    }

    @Test
    public void normalizesPaths() {
        assertThat(IvyIdeaPaths.expand(" $MODULE_DIR$/../ivysettings.xml ", macros, moduleDir)).isEqualTo(projectDir + "/ivysettings.xml");
        assertThat(IvyIdeaPaths.expand(FileUtil.toSystemDependentName(moduleDir + "/ivy.xml"), macros, moduleDir)).isEqualTo(moduleDir + "/ivy.xml");
    }

    @Test
    public void resolvesRelativePathsAgainstTheBaseDir() {
        assertThat(IvyIdeaPaths.expand("ivy.xml", macros, moduleDir)).isEqualTo(moduleDir + "/ivy.xml");
        assertThat(IvyIdeaPaths.expand("../ivysettings.xml", macros, moduleDir)).isEqualTo(projectDir + "/ivysettings.xml");
        assertThat(IvyIdeaPaths.expand("ivy.xml", macros, null)).isEqualTo("ivy.xml");
    }

    @Test
    public void keepsUndefinedMacros() {
        final String expanded = IvyIdeaPaths.expand("$UNDEFINED$/ivy.xml", macros, moduleDir);
        assertThat(expanded).isEqualTo("$UNDEFINED$/ivy.xml");
        assertThat(IvyIdeaPaths.findMacro(expanded)).isEqualTo("UNDEFINED");
        assertThat(IvyIdeaPaths.findMacro(moduleDir + "/ivy.xml")).isNull();
    }

    @Test
    public void onlyExpandsMacrosInUrls() {
        assertThat(IvyIdeaPaths.expand("http://example.com/ivysettings.xml", macros, moduleDir)).isEqualTo("http://example.com/ivysettings.xml");
        assertThat(IvyIdeaPaths.expand("file://$PROJECT_DIR$/ivysettings.xml", macros, moduleDir)).isEqualTo("file://" + projectDir + "/ivysettings.xml");
    }

    @Test
    public void keepsBlankPaths() {
        assertThat(IvyIdeaPaths.expand(null, macros, moduleDir)).isNull();
        assertThat(IvyIdeaPaths.expand("", macros, moduleDir)).isEmpty();
    }

    @Test
    public void collapsesWithTheFirstMacroThatContainsThePath() {
        final UnaryOperator<String> moduleMacros = path -> path.replace(moduleDir, "$MODULE_DIR$").replace(projectDir, "$MODULE_DIR$/..");
        final UnaryOperator<String> projectMacros = path -> path.replace(projectDir, "$PROJECT_DIR$");
        final List<UnaryOperator<String>> collapsers = Arrays.asList(moduleMacros, projectMacros);

        assertThat(IvyIdeaPaths.collapse(moduleDir + "/ivy.xml", collapsers)).isEqualTo("$MODULE_DIR$/ivy.xml");
        assertThat(IvyIdeaPaths.collapse(FileUtil.toSystemDependentName(moduleDir + "/ivy.xml"), collapsers)).isEqualTo("$MODULE_DIR$/ivy.xml");
        assertThat(IvyIdeaPaths.collapse(projectDir + "/ivysettings.xml", collapsers)).isEqualTo("$PROJECT_DIR$/ivysettings.xml");
    }

    @Test
    public void keepsPathsThatCantBeCollapsed() {
        final List<UnaryOperator<String>> collapsers = Collections.singletonList(path -> path.replace(moduleDir, "$MODULE_DIR$"));

        assertThat(IvyIdeaPaths.collapse(projectDir + "/ivysettings.xml", collapsers)).isEqualTo(projectDir + "/ivysettings.xml");
        assertThat(IvyIdeaPaths.collapse("$UNDEFINED$/ivy.xml", collapsers)).isEqualTo("$UNDEFINED$/ivy.xml");
        assertThat(IvyIdeaPaths.collapse("http://example.com/ivysettings.xml", collapsers)).isEqualTo("http://example.com/ivysettings.xml");
        assertThat(IvyIdeaPaths.collapse(null, collapsers)).isNull();
    }

    @Test
    public void storesPathsWithForwardSlashes() {
        assertThat(IvyIdeaPaths.toStored(" C:\\dev\\ivy.xml ")).isEqualTo("C:/dev/ivy.xml");
        assertThat(IvyIdeaPaths.toStored("$MODULE_DIR$\\ivy.xml")).isEqualTo("$MODULE_DIR$/ivy.xml");
        assertThat(IvyIdeaPaths.toStored("http://example.com/ivysettings.xml")).isEqualTo("http://example.com/ivysettings.xml");
        assertThat(IvyIdeaPaths.toDisplayed("http://example.com/ivysettings.xml")).isEqualTo("http://example.com/ivysettings.xml");
        assertThat(IvyIdeaPaths.toDisplayed("$MODULE_DIR$/ivy.xml")).isEqualTo("$MODULE_DIR$" + File.separator + "ivy.xml");
    }
}
