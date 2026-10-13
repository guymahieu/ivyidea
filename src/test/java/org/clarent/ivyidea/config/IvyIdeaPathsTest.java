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
import org.apache.ivy.core.settings.IvySettings;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
        // an IntelliJ file URL, which IvyIdeaConfigHelper loads with IvyIdeaPaths.toUrl
        assertThat(IvyIdeaPaths.expand("file://$PROJECT_DIR$/ivysettings.xml", macros, moduleDir)).isEqualTo("file://" + projectDir + "/ivysettings.xml");
    }

    @Test
    public void keepsBlankPaths() {
        assertThat(IvyIdeaPaths.expand(null, macros, moduleDir)).isNull();
        assertThat(IvyIdeaPaths.expand("", macros, moduleDir)).isEmpty();
    }

    @Test
    public void storesPathsWithForwardSlashes() {
        assertThat(IvyIdeaPaths.toStored(" C:\\dev\\ivy.xml ")).isEqualTo("C:/dev/ivy.xml");
        assertThat(IvyIdeaPaths.toStored("$MODULE_DIR$\\ivy.xml")).isEqualTo("$MODULE_DIR$/ivy.xml");
        assertThat(IvyIdeaPaths.toStored("http://example.com/ivysettings.xml")).isEqualTo("http://example.com/ivysettings.xml");
        assertThat(IvyIdeaPaths.toDisplayed("http://example.com/ivysettings.xml")).isEqualTo("http://example.com/ivysettings.xml");
        assertThat(IvyIdeaPaths.toDisplayed("$MODULE_DIR$/ivy.xml")).isEqualTo("$MODULE_DIR$/ivy.xml");
        assertThat(IvyIdeaPaths.toDisplayed("$PROJECT_DIR$/../ivysettings.xml")).isEqualTo("$PROJECT_DIR$/../ivysettings.xml");
        assertThat(IvyIdeaPaths.toDisplayed("D:/dev/ivy.xml")).isEqualTo("D:" + File.separator + "dev" + File.separator + "ivy.xml");
    }

    @Test
    public void keepsFileUrlsThatAreUrlsForJava() throws Exception {
        final URL windowsUrl = IvyIdeaPaths.toUrl("file:///C:/dev/ivysettings.xml");
        assertThat(windowsUrl.getHost()).isEmpty();
        assertThat(windowsUrl.getPath()).isEqualTo("/C:/dev/ivysettings.xml");
        // a UNC path keeps its host
        final URL uncUrl = IvyIdeaPaths.toUrl("file://server/share/ivysettings.xml");
        assertThat(uncUrl.getHost()).isEqualTo("server");
        assertThat(uncUrl.getPath()).isEqualTo("/share/ivysettings.xml");
        final URL localhostUrl = IvyIdeaPaths.toUrl("file://localhost/home/dev/ivysettings.xml");
        assertThat(localhostUrl.getHost()).isEqualTo("localhost");
        assertThat(localhostUrl.getPath()).isEqualTo("/home/dev/ivysettings.xml");
    }

    @Test
    public void convertsIntelliJFileUrlsWithAWindowsDrive() throws Exception {
        assertThat(IvyIdeaPaths.startsWithWindowsDrive("C:/dev/ivysettings.xml")).isTrue();
        assertThat(IvyIdeaPaths.startsWithWindowsDrive("/C:/dev/ivysettings.xml")).isFalse();
        assertThat(IvyIdeaPaths.startsWithWindowsDrive("server/share/ivysettings.xml")).isFalse();
        // C:/dev is only an absolute path on Windows
        Assume.assumeTrue(File.separatorChar == '\\');
        final URL url = IvyIdeaPaths.toUrl("file://C:/dev/ivysettings.xml");
        // Java would read the drive letter as a host name, and Ivy needs a hierarchical URL to resolve included files
        assertThat(url.getHost()).isEmpty();
        assertThat(url.toURI().isOpaque()).isFalse();
        assertThat(new File(url.toURI())).isEqualTo(new File("C:/dev/ivysettings.xml"));
    }

    @Test
    public void loadsIvySettingsThatIncludeOtherFilesFromAnIntelliJFileUrl() throws Exception {
        final File settingsDir = temporaryFolder.newFolder("settings");
        Files.write(new File(settingsDir, "ivysettings.xml").toPath(), "<ivysettings><include file=\"included.xml\"/></ivysettings>".getBytes(StandardCharsets.UTF_8));
        Files.write(new File(settingsDir, "included.xml").toPath(), "<ivysettings><property name=\"included\" value=\"yes\"/></ivysettings>".getBytes(StandardCharsets.UTF_8));
        // what file://$PROJECT_DIR$/settings/ivysettings.xml expands to: file://C:/... on Windows
        final String intelliJUrl = "file://" + FileUtil.toSystemIndependentName(settingsDir.getCanonicalPath()) + "/ivysettings.xml";

        final IvySettings ivySettings = new IvySettings();
        ivySettings.load(IvyIdeaPaths.toUrl(intelliJUrl));

        assertThat(ivySettings.getVariable("included")).isEqualTo("yes");
    }

    @Test
    public void storesDoubledSeparatorsAsOne() {
        // as in a path copied from a Java properties file, which shouldn't become C://dev//ivy.xml, a URL for isUrl
        assertThat(IvyIdeaPaths.toStored("C:\\\\dev\\\\ivy.xml")).isEqualTo("C:/dev/ivy.xml");
        assertThat(IvyIdeaPaths.toStored("$MODULE_DIR$//ivy.xml")).isEqualTo("$MODULE_DIR$/ivy.xml");
        // the double slash of a UNC path is kept
        assertThat(IvyIdeaPaths.toStored("\\\\server\\share\\ivy.xml")).isEqualTo("//server/share/ivy.xml");
        assertThat(IvyIdeaPaths.toStored("//server/share//ivy.xml")).isEqualTo("//server/share/ivy.xml");
        assertThat(IvyIdeaPaths.toStored("file://C:/dev/ivysettings.xml")).isEqualTo("file://C:/dev/ivysettings.xml");
    }
}
