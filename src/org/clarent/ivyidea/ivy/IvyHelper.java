package org.clarent.ivyidea.ivy;

import org.clarent.ivyidea.config.PostIvyPluginConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import org.apache.ivy.Ivy;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.parser.ModuleDescriptorParserRegistry;
import org.apache.ivy.core.cache.RepositoryCacheManager;
import org.apache.ivy.core.cache.DefaultRepositoryCacheManager;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public class IvyHelper {

    private static final Logger LOGGER = Logger.getLogger(IvyHelper.class.getName());


    private Ivy ivy;

    public IvyHelper() {
        ivy = IvyWrapper.getInstance().getIvy();
    }

    @SuppressWarnings("unchecked")
    private List<Artifact> resolveArtifacts(Module module) {
        //noinspection unchecked
        return resolveDependencies(module).getArtifacts();
    }

    private ResolveReport resolveDependencies(Module module) {
        try {
            return ivy.resolve(getIvyFile(module));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getJarFiles(Module module) {
        List<Artifact> artifacts = resolveArtifacts(module);
        List<String> list = new ArrayList<String>();
        for (Artifact artifact : artifacts) {
            final String resolverName = ivy.getSettings().getResolverName(artifact.getModuleRevisionId());
            final DependencyResolver resolver = ivy.getSettings().getResolver(resolverName);
            RepositoryCacheManager repositoryCacheManager = resolver.getRepositoryCacheManager();
            if (repositoryCacheManager instanceof DefaultRepositoryCacheManager) {
                DefaultRepositoryCacheManager defaultRepositoryCacheManager = (DefaultRepositoryCacheManager) repositoryCacheManager;
                list.add(defaultRepositoryCacheManager.getArchiveFileInCache(artifact).getAbsolutePath());
            } else {
                // TODO Check with Ivy dev team if the getArchiveFileInCache method shouldn't be in the interface
                // TODO         ===> Request logged: https://issues.apache.org/jira/browse/IVY-912
                throw new RuntimeException("Unsupported RepositoryCacheManager type: " + repositoryCacheManager.getClass().getName());
            }

        }
        return list;
    }

    public List<Module> findModuleDependencies(Module currentModule) {
        List<Module> result = new ArrayList<Module>();
        IntellijProjectIvyInfo ipi = new IntellijProjectIvyInfo(currentModule.getProject());
        final ModuleDescriptor descriptor = parseIvyFile(getIvyFile(currentModule));
        if (descriptor != null) {
            final DependencyDescriptor[] ivyDependencies = descriptor.getDependencies();
            final Module[] modules = ModuleManager.getInstance(currentModule.getProject()).getModules();
            for (Module dependencyModule : modules) {
                if (!dependencyModule.equals(currentModule)) {
                    for (DependencyDescriptor ivyDependency : ivyDependencies) {
                        final ModuleRevisionId ivyDependencyId = ivyDependency.getDependencyRevisionId();
                        final ModuleRevisionId dependencyModuleId = ipi.getRevisionId(dependencyModule);
                        if (ivyDependencyId.equals(dependencyModuleId)) {
                            LOGGER.info("Recognized dependency " + ivyDependency + " as intellij module '"+dependencyModule.getName()+"' in this project!");
                            result.add(dependencyModule);
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    public ModuleDescriptor getIvyModuleDescriptor(Module intellijModule) {
        final File ivyFile = getIvyFile(intellijModule);
        if (ivyFile.exists()) {
            return parseIvyFile(ivyFile);
        } else {
            return null;
        }
    }

    private ModuleDescriptor parseIvyFile(File ivyFile) {
        try {
            LOGGER.info("Parsing ivy file " + ivyFile.getAbsolutePath());
            return ModuleDescriptorParserRegistry.getInstance().parseDescriptor(ivy.getSettings(), ivyFile.toURI().toURL(), false);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File getIvyFile(Module module) {
        // TODO: convert to strategy pattern with multiple ways to look for ivy.xml files in modules!
        return new File(module.getModuleFile().getParent().getPath() + "/" + PostIvyPluginConfiguration.getCurrent().getIvyModuleDescriptorFileName());
    }

}
