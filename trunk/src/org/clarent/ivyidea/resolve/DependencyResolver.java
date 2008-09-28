package org.clarent.ivyidea.resolve;

import com.intellij.openapi.module.Module;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.cache.DefaultRepositoryCacheManager;
import org.apache.ivy.core.cache.RepositoryCacheManager;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.IvyNode;
import org.apache.ivy.core.settings.IvySettings;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.ivy.IvyUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public class DependencyResolver {

    private static final Logger LOGGER = Logger.getLogger(DependencyResolver.class.getName());

    /*
        public List<ResolvedDependency> resolveAll() {
            final List<ResolvedDependency> result = new ArrayList<ResolvedDependency>();
            final Module[] modules = IntellijUtils.getAllModules();
            final IvyManager ivyManager = new IvyManager();
            for (Module module : modules) {
                result.addAll(resolve(module, ivyManager));
            }
            return result;
        }
    */
    public List<ResolvedDependency> resolve(Module module) {
        return resolve(module, new IvyManager());
    }

    public List<ResolvedDependency> resolve(Module module, IvyManager ivyManager) {
        final Ivy ivy = ivyManager.getIvy(module);
        final File ivyFile = IvyUtil.getIvyFile(module);
        try {
            final ResolveReport resolveReport = ivy.resolve(ivyFile);
            return extractDependencies(resolveReport, ivy.getSettings(), new ModuleDependencies(module, ivyManager));
        } catch (ParseException e) {
            throw new RuntimeException("The ivy file " + ivyFile.getAbsolutePath() + " could not be parsed!", e);
        } catch (IOException e) {
            throw new RuntimeException("The ivy file " + ivyFile.getAbsolutePath() + " could not be accessed!", e);
        }
    }

    protected List<ResolvedDependency> extractDependencies(ResolveReport resolveReport, IvySettings ivySettings, ModuleDependencies moduleDependencies) {
        List<ResolvedDependency> result = new ArrayList<ResolvedDependency>();
        List<IvyNode> dependencies = getDependencies(resolveReport);
        for (IvyNode dependency : dependencies) {
            final String resolverName = ivySettings.getResolverName(dependency.getResolvedId());
            final org.apache.ivy.plugins.resolver.DependencyResolver resolver = ivySettings.getResolver(resolverName);
            RepositoryCacheManager repositoryCacheManager = resolver.getRepositoryCacheManager();
            if (repositoryCacheManager instanceof DefaultRepositoryCacheManager) {
                DefaultRepositoryCacheManager defaultRepositoryCacheManager = (DefaultRepositoryCacheManager) repositoryCacheManager;
                final ModuleRevisionId dependencyRevisionId = dependency.getResolvedId();
                if (moduleDependencies.isModuleDependency(dependencyRevisionId)) {
                    result.add(new InternalDependency(moduleDependencies.getModuleDependency(dependencyRevisionId)));
                } else {
                    final Artifact[] artifacts = dependency.getAllArtifacts();
                    for (Artifact artifact : artifacts) {
                        result.add(new ExternalDepencency(defaultRepositoryCacheManager.getArchiveFileInCache(artifact)));
                    }
                }
            } else {
                // TODO Check with Ivy dev team if the getArchiveFileInCache method shouldn't be in the interface
                // TODO         ===> Request logged: https://issues.apache.org/jira/browse/IVY-912
                throw new RuntimeException("Unsupported RepositoryCacheManager type: " + repositoryCacheManager.getClass().getName());
            }

        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<IvyNode> getDependencies(ResolveReport resolveReport) {
        // TODO: Check if this is even correct....
        List<IvyNode> result = new ArrayList<IvyNode>();
        result.addAll(Arrays.asList(resolveReport.getUnresolvedDependencies()));
        result.addAll(resolveReport.getDependencies());
        return result;
    }

    /**
     * Holds the link between IntelliJ {@link com.intellij.openapi.module.Module}s and ivy
     * {@link org.apache.ivy.core.module.id.ModuleRevisionId}s
     */
    private static class ModuleDependencies {

        private IvyManager ivyManager;
        private Module module;

        private Map<ModuleRevisionId, Module> moduleDependencies = new HashMap<ModuleRevisionId, Module>();

        public ModuleDependencies(Module module, IvyManager ivyManager) {
            this.module = module;
            this.ivyManager = ivyManager;
            fillModuleDependencies();
        }

        public boolean isModuleDependency(ModuleRevisionId moduleRevisionId) {
            return moduleDependencies.containsKey(moduleRevisionId);
        }

        public Module getModuleDependency(ModuleRevisionId moduleRevisionId) {
            return moduleDependencies.get(moduleRevisionId);
        }

        private void fillModuleDependencies() {
            final File ivyFile = IvyUtil.getIvyFile(module);
            final ModuleDescriptor descriptor = IvyUtil.parseIvyFile(ivyFile, ivyManager.getIvy(module).getSettings());
            if (descriptor != null) {
                final DependencyDescriptor[] ivyDependencies = descriptor.getDependencies();
                for (Module dependencyModule : IntellijUtils.getAllModules()) {
                    if (!module.equals(dependencyModule)) {
                        for (DependencyDescriptor ivyDependency : ivyDependencies) {
                            final ModuleRevisionId ivyDependencyId = ivyDependency.getDependencyRevisionId();
                            final ModuleRevisionId dependencyModuleId = getModuleRevision(dependencyModule);
                            if (ivyDependencyId.equals(dependencyModuleId)) {
                                LOGGER.info("Recognized dependency " + ivyDependency + " as intellij module '" + dependencyModule.getName() + "' in this project!");
                                moduleDependencies.put(dependencyModuleId, dependencyModule);
                                break;
                            }
                        }
                    }
                }
            }
        }

        @Nullable
        private ModuleRevisionId getModuleRevision(Module module) {
            final IvySettings ivySettings = ivyManager.getIvy(module).getSettings();
            if (!moduleDependencies.values().contains(module)) {
                final ModuleDescriptor ivyModuleDescriptor = IvyUtil.getIvyModuleDescriptor(module, ivySettings);
                if (ivyModuleDescriptor != null) {
                    moduleDependencies.put(ivyModuleDescriptor.getModuleRevisionId(), module);
                }
            }
            for (ModuleRevisionId moduleRevisionId : moduleDependencies.keySet()) {
                if (module.equals(moduleDependencies.get(moduleRevisionId))) {
                    return moduleRevisionId;
                }
            }
            return null;
        }


    }
}
