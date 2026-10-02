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

package org.clarent.ivyidea.ivy;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.report.MetadataArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorParser;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkspaceAwareIvySettingsTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private File repo;
    private File cache;
    private File workspaceCache;

    @Before
    public void setUp() throws Exception {
        repo = temp.newFolder("repo");
        cache = temp.newFolder("cache");
        workspaceCache = temp.newFolder("workspace-cache");
        publish("lib", "1.0", "");
        publish("lib", "2.0", "");
        // the published (outdated) version of the workspace module still depends on lib 1.0
        publish("ws", "1.0", "<dependency org='org' name='lib' rev='1.0'/>");
    }

    @Test
    public void configuredResolversKeepTheirName() throws Exception {
        WorkspaceAwareIvySettings settings = createSettings(new WorkspaceAwareIvySettings());
        settings.setWorkspaceResolver(new StubWorkspaceResolver(workspaceCache));

        assertThat(settings.getResolver("repo")).isInstanceOf(FileSystemResolver.class);
        assertThat(settings.getResolver(ModuleRevisionId.newInstance("org", "lib", "1.0"))).isSameAs(settings.getResolver("repo"));
    }

    @Test
    public void returnsWorkspaceResolverForWorkspaceModules() throws Exception {
        StubWorkspaceResolver workspaceResolver = new StubWorkspaceResolver(workspaceCache);
        workspaceResolver.add(parse(writeIvyFile("ws", "")));
        WorkspaceAwareIvySettings settings = createSettings(new WorkspaceAwareIvySettings());
        settings.setWorkspaceResolver(workspaceResolver);

        assertThat(settings.getResolver(ModuleRevisionId.newInstance("org", "ws", "1.0"))).isSameAs(workspaceResolver);
        assertThat(settings.getResolver(ModuleRevisionId.newInstance("org", "lib", "1.0"))).isSameAs(settings.getResolver("repo"));
    }

    @Test
    public void usesModulesCachedUnderTheConfiguredResolverName() throws Exception {
        File app = writeIvyFile("app", "<dependency org='org' name='lib' rev='1.0'/>");
        assertThat(resolve(createSettings(new IvySettings()), app, false).hasError()).isFalse();

        WorkspaceAwareIvySettings settings = createSettings(new WorkspaceAwareIvySettings());
        settings.setWorkspaceResolver(new StubWorkspaceResolver(workspaceCache));
        ResolveReport report = resolve(settings, app, true);

        assertThat(report.getAllProblemMessages()).isEmpty();
        assertThat(report.getAllArtifactsReports()).hasSize(1);
    }

    @Test
    public void resolvesWorkspaceModuleFromWorkspace() throws Exception {
        StubWorkspaceResolver workspaceResolver = new StubWorkspaceResolver(workspaceCache);
        workspaceResolver.add(parse(writeIvyFile("ws", "<dependency org='org' name='lib' rev='2.0'/>")));
        WorkspaceAwareIvySettings settings = createSettings(new WorkspaceAwareIvySettings());
        settings.setWorkspaceResolver(workspaceResolver);

        ResolveReport report = resolve(settings, writeIvyFile("app", "<dependency org='org' name='ws' rev='latest.integration'/>"), false);

        assertThat(report.getAllProblemMessages()).isEmpty();
        assertThat(report.getAllArtifactsReports())
                .extracting(adr -> adr.getArtifact().getModuleRevisionId().toString())
                .containsExactly("org#lib;2.0");
    }

    @Test
    public void savesWorkspaceModulesInWorkspaceCache() throws Exception {
        StubWorkspaceResolver workspaceResolver = new StubWorkspaceResolver(workspaceCache);
        workspaceResolver.add(parse(writeIvyFile("ws", "<dependency org='org' name='lib' rev='2.0'/>")));
        WorkspaceAwareIvySettings settings = createSettings(new WorkspaceAwareIvySettings());
        settings.setWorkspaceResolver(workspaceResolver);

        resolve(settings, writeIvyFile("app", "<dependency org='org' name='ws' rev='latest.integration'/>"), false);

        assertThat(new File(workspaceCache, "org/ws")).isDirectory();
        assertThat(new File(cache, "org/ws")).doesNotExist();
    }

    @Test
    public void doesNotUseWorkspaceWithoutWorkspaceResolver() throws Exception {
        ResolveReport report = resolve(createSettings(new WorkspaceAwareIvySettings()),
                writeIvyFile("app", "<dependency org='org' name='ws' rev='1.0'/>"), false);

        assertThat(report.getAllArtifactsReports())
                .extracting(adr -> adr.getArtifact().getModuleRevisionId().toString())
                .containsExactlyInAnyOrder("org#ws;1.0", "org#lib;1.0");
    }

    private <T extends IvySettings> T createSettings(T settings) {
        settings.setDefaultCache(cache);
        FileSystemResolver resolver = new FileSystemResolver();
        resolver.setName("repo");
        resolver.addIvyPattern(repo.getAbsolutePath() + "/[organisation]/[module]/[revision]/ivy.xml");
        resolver.addArtifactPattern(repo.getAbsolutePath() + "/[organisation]/[module]/[revision]/[artifact].[ext]");
        settings.addResolver(resolver);
        settings.setDefaultResolver("repo");
        return settings;
    }

    private static ResolveReport resolve(IvySettings settings, File ivyFile, boolean useCacheOnly) throws Exception {
        ResolveOptions options = new ResolveOptions();
        options.setUseCacheOnly(useCacheOnly);
        return Ivy.newInstance(settings).resolve(ivyFile.toURI().toURL(), options);
    }

    private void publish(String module, String revision, String dependencies) throws IOException {
        File dir = new File(repo, "org/" + module + "/" + revision);
        dir.mkdirs();
        write(new File(dir, "ivy.xml"), "<ivy-module version='2.0'>"
                + "<info organisation='org' module='" + module + "' revision='" + revision + "'/>"
                + "<publications><artifact name='" + module + "' type='jar' ext='jar'/></publications>"
                + "<dependencies>" + dependencies + "</dependencies>"
                + "</ivy-module>");
        write(new File(dir, module + ".jar"), module);
    }

    private File writeIvyFile(String module, String dependencies) throws IOException {
        File file = new File(temp.newFolder(), "ivy.xml");
        write(file, "<ivy-module version='2.0'>"
                + "<info organisation='org' module='" + module + "'/>"
                + "<publications/>"
                + "<dependencies>" + dependencies + "</dependencies>"
                + "</ivy-module>");
        return file;
    }

    private static ModuleDescriptor parse(File ivyFile) throws Exception {
        return XmlModuleDescriptorParser.getInstance().parseDescriptor(new IvySettings(), ivyFile.toURI().toURL(), false);
    }

    private static void write(File file, String content) throws IOException {
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }

    private static class StubWorkspaceResolver extends WorkspaceResolver {

        private final Map<ModuleId, ModuleDescriptor> modules = new HashMap<>();

        StubWorkspaceResolver(File cacheDir) {
            super("workspace", cacheDir);
        }

        void add(ModuleDescriptor md) {
            modules.put(md.getModuleRevisionId().getModuleId(), md);
        }

        @Override
        public boolean isWorkspaceModule(ModuleRevisionId mrid) {
            return modules.containsKey(mrid.getModuleId());
        }

        public ResolvedModuleRevision getDependency(DependencyDescriptor dd, ResolveData data) {
            ModuleDescriptor md = modules.get(dd.getDependencyId());
            MetadataArtifactDownloadReport madr = new MetadataArtifactDownloadReport(md.getMetadataArtifact());
            madr.setDownloadStatus(DownloadStatus.SUCCESSFUL);
            return new ResolvedModuleRevision(this, this, md, madr);
        }
    }
}
