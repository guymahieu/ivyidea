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
import org.apache.ivy.util.MessageLogger;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
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

class DependencyResolver {

    private static final Logger LOGGER = Logger.getLogger(DependencyResolver.class.getName());

    private final Map<IvyNode, String> problems;

    public DependencyResolver() {
        problems = new HashMap<IvyNode, String>();
    }

    public Map<IvyNode, String> getProblems() {
        return Collections.unmodifiableMap(problems);
    }

    public List<ResolvedDependency> resolve(Module module, IvyManager ivyManager, MessageLogger messageLogger) {
        final Ivy ivy = ivyManager.getIvy(module);
        final File ivyFile = IvyUtil.getIvyFile(module);
        try {
            if (messageLogger != null) {
                ivy.getLoggerEngine().setDefaultLogger(messageLogger);
            }
            final ResolveReport resolveReport = ivy.resolve(ivyFile.toURI().toURL(), IvyIdeaConfigHelper.createResolveOptions(module));
            return extractDependencies(resolveReport, ivy.getSettings(), new ModuleDependencies(module, ivyManager));
        } catch (ParseException e) {
            throw new RuntimeException("The ivy file " + ivyFile.getAbsolutePath() + " could not be parsed correctly!", e);
        } catch (IOException e) {
            throw new RuntimeException("The ivy file " + ivyFile.getAbsolutePath() + " could not be accessed!", e);
/*
            if (ivyListener != null) {
                ivy.getResolveEngine().getEventManager().removeIvyListener(ivyListener);
            }
        } finally {
*/
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
                    if (dependency.hasProblem()) {
                        problems.put(dependency, dependency.getProblemMessage());
                        LOGGER.info("DEPENDENCY PROBLEM: " + dependency.getId() + ": " + dependency.getProblemMessage());
                    } else {
                        if (dependency.isCompletelyEvicted()) {
                            LOGGER.info("Not adding evicted dependency " + dependency);
                        } else if (dependency.isCompletelyBlacklisted()) {
                            // From quickly looking at the ivy sources, i think this means that there was a conflict,
                            // and this dependency lost in the conflict resolution - don't know how this is different
                            // from evicted modules, but it is probably not something we want to add
                            LOGGER.info("Not adding blacklisted dependency " + dependency);
                        } else {
                            final Artifact[] artifacts = dependency.getAllArtifacts();
                            for (Artifact artifact : artifacts) {
                                final ExternalDependency externalDependency = createExternalDependency(defaultRepositoryCacheManager, artifact);
                                if (externalDependency != null) {
                                    result.add(externalDependency);
                                }
                            }
                        }
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

    @Nullable
    private ExternalDependency createExternalDependency(DefaultRepositoryCacheManager defaultRepositoryCacheManager, Artifact artifact) {
        final File file = defaultRepositoryCacheManager.getArchiveFileInCache(artifact);
        ResolvedArtifact resolvedArtifact = new ResolvedArtifact(artifact);
        if (resolvedArtifact.isSourceType()) {
            return new ExternalSourceDependency(file);
        }
        if (resolvedArtifact.isJavaDocType()) {
            return new ExternalJavaDocDependency(file);
        }
        if (resolvedArtifact.isClassesType()) {
            return new ExternalJarDependency(file);
        }
        LOGGER.warning("Artifact of unrecognized type " + artifact.getType() + " found, *not* adding as a dependency.");
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<IvyNode> getDependencies(ResolveReport resolveReport) {
        // TODO: Check if this is even correct....
        List<IvyNode> result = new ArrayList<IvyNode>();
        // Add unresolved dependencies as well so module dependencies for which no artifacts are in the repository
        // will also be taken into account!
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
                for (Module dependencyModule : IntellijUtils.getAllModulesWithIvyIdeaFacet()) {
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
