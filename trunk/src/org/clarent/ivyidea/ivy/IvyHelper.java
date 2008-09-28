package org.clarent.ivyidea.ivy;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.cache.DefaultRepositoryCacheManager;
import org.apache.ivy.core.cache.RepositoryCacheManager;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.plugins.resolver.DependencyResolver;

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

    public IvyHelper(Module module) {
        ivy = new IvyManager().getIvy(module);
    }

    @SuppressWarnings("unchecked")
    private List<Artifact> resolveArtifacts(Module module) {
        //noinspection unchecked
        return resolveDependencies(module).getArtifacts();
    }

    private ResolveReport resolveDependencies(Module module) {
        try {
            return ivy.resolve(IvyUtil.getIvyFile(module));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: Remove dependencies first; or don't add existing dependencies

    // TODO: Order of jars and module depenencies could be interlaced!

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
        final File ivyFile = IvyUtil.getIvyFile(currentModule);
        final ModuleDescriptor descriptor = IvyUtil.parseIvyFile(ivyFile, ivy.getSettings());
        if (descriptor != null) {
            final DependencyDescriptor[] ivyDependencies = descriptor.getDependencies();
            final Module[] modules = ModuleManager.getInstance(currentModule.getProject()).getModules();
            for (Module dependencyModule : modules) {
                if (!dependencyModule.equals(currentModule)) {
                    for (DependencyDescriptor ivyDependency : ivyDependencies) {
                        final ModuleRevisionId ivyDependencyId = ivyDependency.getDependencyRevisionId();
                        final ModuleRevisionId dependencyModuleId = ipi.getRevisionId(dependencyModule);
                        if (ivyDependencyId.equals(dependencyModuleId)) {
                            LOGGER.info("Recognized dependency " + ivyDependency + " as intellij module '" + dependencyModule.getName() + "' in this project!");
                            result.add(dependencyModule);
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }


}
