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

package org.clarent.ivyidea.resolve;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ConfigurationResolveReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.DownloadOptions;
import org.apache.ivy.core.resolve.IvyNode;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.config.model.ArtifactTypeSettings;
import org.clarent.ivyidea.exception.IvyFileReadException;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.ivy.IvyUtil;
import org.clarent.ivyidea.resolve.dependency.ExternalDependency;
import org.clarent.ivyidea.resolve.dependency.ExternalDependencyFactory;
import org.clarent.ivyidea.resolve.dependency.InternalDependency;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;
import org.clarent.ivyidea.resolve.problem.ResolveProblem;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

class DependencyResolver {

    private static final Logger LOGGER = Logger.getLogger(DependencyResolver.class.getName());

    private final List<ResolveProblem> resolveProblems;
    private final List<ResolvedDependency> resolvedDependencies;

    public DependencyResolver() {
        resolveProblems = new ArrayList<ResolveProblem>();
        resolvedDependencies = new ArrayList<ResolvedDependency>();
    }

    public List<ResolveProblem> getResolveProblems() {
        return Collections.unmodifiableList(resolveProblems);
    }

    public List<ResolvedDependency> getResolvedDependencies() {
        return Collections.unmodifiableList(resolvedDependencies);
    }

    public void resolve(Module module, IvyManager ivyManager) throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException {
        final File ivyFile = IvyUtil.getIvyFile(module);
        if (ivyFile == null) {
            throw new IvyFileReadException(null, module.getName(), null);
        }

        final Ivy ivy = ivyManager.getIvy(module);
        try {
            final ResolveReport resolveReport = ivy.resolve(ivyFile.toURI().toURL(), IvyIdeaConfigHelper.createResolveOptions(module));
            extractDependencies(ivy, resolveReport, new IntellijModuleDependencies(module, ivyManager));
        } catch (ParseException e) {
            throw new IvyFileReadException(ivyFile.getAbsolutePath(), module.getName(), e);
        } catch (IOException e) {
            throw new IvyFileReadException(ivyFile.getAbsolutePath(), module.getName(), e);
        }
    }

    // TODO: This method performs way too much tasks -- refactor it!
    protected void extractDependencies(Ivy ivy, ResolveReport resolveReport, IntellijModuleDependencies moduleDependencies) {
        final String[] resolvedConfigurations = resolveReport.getConfigurations();
        for (String resolvedConfiguration : resolvedConfigurations) {
            ConfigurationResolveReport configurationReport = resolveReport.getConfigurationReport(resolvedConfiguration);

            // TODO: Refactor this a bit
            registerProblems(configurationReport, moduleDependencies);

            @SuppressWarnings({"unchecked"})
            Set<ModuleRevisionId> dependencies = (Set<ModuleRevisionId>) configurationReport.getModuleRevisionIds();
            for (ModuleRevisionId dependency : dependencies) {
                if (moduleDependencies.isInternalIntellijModuleDependency(dependency.getModuleId())) {
                    resolvedDependencies.add(new InternalDependency(moduleDependencies.getModuleDependency(dependency.getModuleId())));
                } else {
                    final Project project = moduleDependencies.getModule().getProject();
                    final ArtifactDownloadReport[] artifactDownloadReports = configurationReport.getDownloadReports(dependency);
                    for (ArtifactDownloadReport artifactDownloadReport : artifactDownloadReports) {
                        final Artifact artifact = artifactDownloadReport.getArtifact();
                        final File artifactFile = artifactDownloadReport.getLocalFile();
                        addExternalDependency(artifact, artifactFile, resolvedConfiguration, project);
                    }

                    // If activated manually download any missing javadoc or source dependencies,
                    // in case they weren't selected by the Ivy configuration.
                    // This means that dependencies in ivy.xml don't need to explicitly include configurations
                    // for javadoc or sources, just to ensure that the plugin can see them. The plugin will
                    // get all javadocs and sources it can find for each dependency.
                    final boolean attachSources = IvyIdeaConfigHelper.alwaysAttachSources(project);
                    final boolean attachJavadocs = IvyIdeaConfigHelper.alwaysAttachJavadocs(project);
                    if (attachSources || attachJavadocs) {
                        final IvyNode node = configurationReport.getDependency(dependency);
                        final ModuleDescriptor md = node.getDescriptor();
                        final Artifact[] artifacts = md.getAllArtifacts();
                        for (Artifact artifact : artifacts) {
                            // TODO: if sources are found, don't bother attaching javadoc?
                            // That way, IDEA will generate the javadoc and resolve links to other javadocs
                            if ((attachSources && isSource(project, artifact))
                                    || (attachJavadocs && isJavadoc(project, artifact))) {
                                if (resolveReport.getArtifacts().contains(artifact)) {
                                    continue; // already resolved, ignore.
                                }

                                // try to download
                                ArtifactDownloadReport adr = ivy.getResolveEngine().download(artifact, new DownloadOptions());
                                addExternalDependency(artifact, adr.getLocalFile(), resolvedConfiguration, project);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addExternalDependency(Artifact artifact, File artifactFile, String resolvedConfiguration, Project project) {
        ExternalDependency externalDependency = ExternalDependencyFactory.getInstance().createExternalDependency(artifact, artifactFile, project, resolvedConfiguration);
        if (externalDependency == null) {
            resolveProblems.add(new ResolveProblem(
                    artifact.getModuleRevisionId().toString(),
                    "Unrecognized artifact type: " + artifact.getType() + ", will not add this as a dependency in IntelliJ.",
                    null));
            LOGGER.warning("Artifact of unrecognized type " + artifact.getType() + " found, *not* adding as a dependency.");
        }
        else if (externalDependency.isMissing()) {
            resolveProblems.add(new ResolveProblem(
                    artifact.getModuleRevisionId().toString(),
                    "File not found: " + externalDependency.getLocalFile().getAbsolutePath())
            );
        } else {
            resolvedDependencies.add(externalDependency);
        }
    }

    private boolean isSource(Project project, Artifact artifact) {
        return ArtifactTypeSettings.DependencyCategory.Sources == ExternalDependencyFactory.determineCategory(project, artifact);
    }

    private boolean isJavadoc(Project project, Artifact artifact) {
        return ArtifactTypeSettings.DependencyCategory.Javadoc == ExternalDependencyFactory.determineCategory(project, artifact);
    }

    private void registerProblems(ConfigurationResolveReport configurationReport, IntellijModuleDependencies moduleDependencies) {
        for (IvyNode unresolvedDependency : configurationReport.getUnresolvedDependencies()) {
            if (moduleDependencies.isInternalIntellijModuleDependency(unresolvedDependency.getModuleId())) {
                // centralize  this!
                resolvedDependencies.add(new InternalDependency(moduleDependencies.getModuleDependency(unresolvedDependency.getModuleId())));
            } else {
                //noinspection ThrowableResultOfMethodCallIgnored
                resolveProblems.add(new ResolveProblem(
                        unresolvedDependency.getId().toString(),
                        unresolvedDependency.getProblemMessage(),
                        unresolvedDependency.getProblem()));
                LOGGER.info("DEPENDENCY PROBLEM: " + unresolvedDependency.getId() + ": " + unresolvedDependency.getProblemMessage());
            }
        }
    }
}
