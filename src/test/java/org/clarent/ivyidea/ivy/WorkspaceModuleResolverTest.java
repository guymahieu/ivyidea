package org.clarent.ivyidea.ivy;

import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ExcludeRule;
import org.apache.ivy.core.module.descriptor.License;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.settings.IvySettings;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkspaceModuleResolverTest {

    private ModuleDescriptor originalMd;
    private ModuleDescriptor emptyConfigMd;

    @Before
    public void setUp() throws Exception {
        IvySettings settings = new IvySettings();
        originalMd = parseTestIvy("test-ivy.xml", settings);
        emptyConfigMd = parseTestIvy("test-ivy-empty-configs.xml", settings);
    }

    @Test
    public void cloneMdPreservesModuleRevisionId() {
        DefaultModuleDescriptor cloned = WorkspaceModuleResolver.cloneMd(originalMd, null);
        assertThat(cloned.getModuleRevisionId()).isEqualTo(originalMd.getModuleRevisionId());
    }

    @Test
    public void cloneMdPreservesStatus() {
        DefaultModuleDescriptor cloned = WorkspaceModuleResolver.cloneMd(originalMd, null);
        assertThat(cloned.getStatus()).isEqualTo(originalMd.getStatus());
    }

    @Test
    public void cloneMdPreservesPublicationDate() {
        DefaultModuleDescriptor cloned = WorkspaceModuleResolver.cloneMd(originalMd, null);
        assertThat(cloned.getPublicationDate()).isEqualTo(originalMd.getPublicationDate());
    }

    @Test
    public void cloneMdCopiesAllConfigurations() {
        DefaultModuleDescriptor cloned = WorkspaceModuleResolver.cloneMd(originalMd, null);
        assertThat(cloned.getConfigurations()).hasSize(3);
        assertThat(cloned.getConfigurations())
                .extracting(Configuration::getName)
                .containsExactlyInAnyOrder("default", "compile", "runtime");
    }

    @Test
    public void cloneMdCopiesAllDependencies() {
        DefaultModuleDescriptor cloned = WorkspaceModuleResolver.cloneMd(originalMd, null);
        assertThat(cloned.getDependencies()).hasSize(2);
    }

    @Test
    public void cloneMdCopiesExcludeRules() {
        DefaultModuleDescriptor cloned = WorkspaceModuleResolver.cloneMd(originalMd, null);
        assertThat(cloned.getAllExcludeRules()).hasSize(1);
        ExcludeRule rule = cloned.getAllExcludeRules()[0];
        assertThat(rule.getId().getModuleId().getOrganisation()).isEqualTo("com.example");
        assertThat(rule.getId().getModuleId().getName()).isEqualTo("dep-c");
    }

    @Test
    public void cloneMdCopiesLicenses() {
        DefaultModuleDescriptor cloned = WorkspaceModuleResolver.cloneMd(originalMd, null);
        assertThat(cloned.getLicenses()).hasSize(1);
        License license = cloned.getLicenses()[0];
        assertThat(license.getName()).isEqualTo("Apache 2.0");
        assertThat(license.getUrl()).isEqualTo("http://www.apache.org/licenses/LICENSE-2.0");
    }

    @Test
    public void cloneMdAddsDefaultConfigurationWhenOriginalHasNone() {
        DefaultModuleDescriptor cloned = WorkspaceModuleResolver.cloneMd(emptyConfigMd, null);
        assertThat(cloned.getConfigurations()).hasSize(1);
        assertThat(cloned.getConfigurations()[0].getName()).isEqualTo("default");
    }

    @Test
    public void cloneMdUpdatesLastModified() {
        DefaultModuleDescriptor cloned = WorkspaceModuleResolver.cloneMd(originalMd, null);
        assertThat(cloned.getLastModified()).isGreaterThan(0);
    }

    private static ModuleDescriptor parseTestIvy(String resourceName, IvySettings settings) throws URISyntaxException {
        URL url = WorkspaceModuleResolverTest.class.getResource(resourceName);
        assertThat(url).isNotNull();
        return IvyUtil.parseIvyFile(new File(url.toURI()), settings);
    }
}
