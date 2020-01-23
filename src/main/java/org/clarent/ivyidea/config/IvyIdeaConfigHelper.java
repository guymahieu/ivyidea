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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.util.net.HttpConfigurable;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.clarent.ivyidea.config.model.ArtifactTypeSettings;
import org.clarent.ivyidea.config.model.IvyIdeaProjectSettings;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.facet.config.FacetPropertiesSettings;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.intellij.IvyIdeaProjectComponent;
import org.clarent.ivyidea.logging.IvyLogLevel;
import org.clarent.ivyidea.util.CollectionUtils;
import org.clarent.ivyidea.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;                                                                                    

/**
 * Handles retrieval of settings from the configuration.
 *
 * TODO: This class has grown to become a monster - let's get rid of all the
 *          statics and organize it a bit better 
 *
 * @author Guy Mahieu
 */
public class IvyIdeaConfigHelper {

    private static final String RESOLVED_LIB_NAME_ROOT = "IvyIDEA";

    public static String getCreatedLibraryName(final ModifiableRootModel model, final String configName) {
        final Project project = model.getProject();
        String libraryName = RESOLVED_LIB_NAME_ROOT;
        if (isLibraryNameIncludesModule(project)) {
            final String moduleName = model.getModule().getName();
            libraryName += "-" + moduleName;
        }
        if (isLibraryNameIncludesConfiguration(project)) {
            libraryName += "-" + configName;
        }
        return libraryName;
    }

    public static boolean isCreatedLibraryName(final String libraryName) {
        return libraryName != null && libraryName.startsWith(RESOLVED_LIB_NAME_ROOT);
    }

    @NotNull
    public static ResolveOptions createResolveOptions(Module module) {
        ResolveOptions options = new ResolveOptions();
        getProjectConfig(module.getProject()).updateResolveOptions(options);
        final Set<String> configsToResolve = getConfigurationsToResolve(module);
        if (!configsToResolve.isEmpty()) {
            options.setConfs(configsToResolve.toArray(new String[configsToResolve.size()]));
        }
        return options;
    }

    /**
     * Looks up the ivy configurations that should be resolved for the given module.
     *
     * @param module the module for which to check
     * @return an unmodifiable Set with the configurations to resolve for the given module
     */
    @NotNull
    public static Set<String> getConfigurationsToResolve(Module module) {
        final IvyIdeaFacetConfiguration moduleConfiguration = IvyIdeaFacetConfiguration.getInstance(module);
        if (moduleConfiguration != null && moduleConfiguration.isOnlyResolveSelectedConfigs() && moduleConfiguration.getConfigsToResolve() != null) {
            return Collections.unmodifiableSet(moduleConfiguration.getConfigsToResolve());
        } else {
            return Collections.emptySet();
        }
    }

    public static ArtifactTypeSettings getArtifactTypeSettings(Project project)   {
        return getProjectConfig(project).getArtifactTypeSettings();
    }

    public static List<String> getPropertiesFiles(Project project) {
         return getProjectConfig(project).getPropertiesSettings().getPropertyFiles();
    }

    public static boolean isLibraryNameIncludesModule(final Project project) {
        return getProjectConfig(project).isLibraryNameIncludesModule();
    }

    public static boolean isLibraryNameIncludesConfiguration(final Project project) {
        return getProjectConfig(project).isLibraryNameIncludesConfiguration();
    }

    public static IvyLogLevel getIvyLoggingThreshold(final Project project) {
        String ivyLogLevelThreshold = getProjectConfig(project).getIvyLogLevelThreshold();
        return IvyLogLevel.fromName(ivyLogLevelThreshold);
    }

    public static boolean getResolveInBackground(final Project project) {
        return getProjectConfig(project).isResolveInBackground();
    }

    public static boolean alwaysAttachSources(final Project project) {
        return getProjectConfig(project).isAlwaysAttachSources();
    }

    public static boolean alwaysAttachJavadocs(final Project project) {
        return getProjectConfig(project).isAlwaysAttachJavadocs();
    }

    public static boolean avoidInternalModuleDependeciesResolving(final Project project){
        return getProjectConfig(project).isAvoidInternalModuleDependenciesResolving();
    }

    @NotNull
    private static IvyIdeaProjectSettings getProjectConfig(Project project) {
        IvyIdeaProjectComponent component = project.getComponent(IvyIdeaProjectComponent.class);
        return component.getState();
    }

    @Nullable
    private static String getIvySettingsFile(Module module) throws IvySettingsNotFoundException {
        final IvyIdeaFacetConfiguration moduleConfiguration = getModuleConfiguration(module);
        if (moduleConfiguration.isUseProjectSettings()) {
            return getProjectIvySettingsFile(module.getProject());
        } else {
            return getModuleIvySettingsFile(module, moduleConfiguration);
        }
    }

    @Nullable
    private static String getModuleIvySettingsFile(Module module, IvyIdeaFacetConfiguration moduleConfiguration) throws IvySettingsNotFoundException {
        if (moduleConfiguration.isUseCustomIvySettings()) {
            final String ivySettingsFile = StringUtils.trim(moduleConfiguration.getIvySettingsFile());
            if (!StringUtils.isBlank(ivySettingsFile)) {
                if (!ivySettingsFile.startsWith("http://")
                        && !ivySettingsFile.startsWith("https://")
                        && !ivySettingsFile.startsWith("file://")) {
                    File result = new File(ivySettingsFile);
                    if (!result.exists()) {
                        throw new IvySettingsNotFoundException("The ivy settings file given in the module settings for module " + module.getName() + " does not exist: " + result.getAbsolutePath(), IvySettingsNotFoundException.ConfigLocation.Module, module.getName());
                    }
                    if (result.isDirectory()) {
                        throw new IvySettingsNotFoundException("The ivy settings file given in the module settings for module " + module.getName() + " is a directory: " + result.getAbsolutePath(), IvySettingsNotFoundException.ConfigLocation.Module, module.getName());
                    }
                }
                return ivySettingsFile;
            } else {
                throw new IvySettingsNotFoundException("No ivy settings file given in the settings of module " + module.getName(), IvySettingsNotFoundException.ConfigLocation.Module, module.getName());
            }
        } else {
            return null; // use ivy standard
        }
    }

    @Nullable
    public static String getProjectIvySettingsFile(Project project) throws IvySettingsNotFoundException {
        IvyIdeaProjectComponent component = project.getComponent(IvyIdeaProjectComponent.class);
        final IvyIdeaProjectSettings state = component.getState();
        if (state.isUseCustomIvySettings()) {
            String settingsFile = StringUtils.trim(state.getIvySettingsFile());
            if (StringUtils.isNotBlank(settingsFile)) {
                if (!settingsFile.startsWith("http://")
                        && !settingsFile.startsWith("https://")
                        && !settingsFile.startsWith("file://")) {
                    File result = new File(settingsFile);
                    if (!result.exists()) {
                        throw new IvySettingsNotFoundException("The ivy settings file given in the project settings does not exist: " + result.getAbsolutePath(), IvySettingsNotFoundException.ConfigLocation.Project, project.getName());
                    }
                    if (result.isDirectory()) {
                        throw new IvySettingsNotFoundException("The ivy settings file given in the project settings is a directory: " + result.getAbsolutePath(), IvySettingsNotFoundException.ConfigLocation.Project, project.getName());
                    }
                }
                return settingsFile;
            } else {
                throw new IvySettingsNotFoundException("No ivy settings file specified in the project settings.", IvySettingsNotFoundException.ConfigLocation.Project, project.getName());
            }
        } else {
            return null; // use ivy standard
        }
    }

    @NotNull
    public static Properties getIvyProperties(Module module) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        final IvyIdeaFacetConfiguration moduleConfiguration = getModuleConfiguration(module);
        final List<String> propertiesFiles = new ArrayList<String>();
        propertiesFiles.addAll(moduleConfiguration.getPropertiesSettings().getPropertyFiles());
        final FacetPropertiesSettings modulePropertiesSettings = moduleConfiguration.getPropertiesSettings();
        if (modulePropertiesSettings.isIncludeProjectLevelPropertiesFiles()) {
            propertiesFiles.addAll(getProjectConfig(module.getProject()).getPropertiesSettings().getPropertyFiles());
        }
        return loadProperties(module, propertiesFiles);
    }

    @NotNull
    public static Properties loadProperties(Module module, List<String> propertiesFiles) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        // Go over the files in reverse order --> files listed first should have priority and loading properties
        // overwrited previously loaded ones.
        final Properties properties = new Properties();
        for (String propertiesFile : CollectionUtils.createReversedList(propertiesFiles)) {
            if (propertiesFile != null) {
                File result = new File(propertiesFile);
                if (!result.exists()) {
                    throw new IvySettingsNotFoundException("The ivy properties file given in the module settings for module " + module.getName() + " does not exist: " + result.getAbsolutePath(), IvySettingsNotFoundException.ConfigLocation.Module, module.getName());
                }
                try {
                    properties.load(new FileInputStream(result));
                } catch (IOException e) {
                    throw new IvySettingsFileReadException(result.getAbsolutePath(), module.getName(), e);
                }
            }
        }
        return properties;
    }

    @NotNull
    public static IvySettings createConfiguredIvySettings(Module module) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        return createConfiguredIvySettings(module, getIvySettingsFile(module), getIvyProperties(module));
    }

    @NotNull
    public static IvySettings createConfiguredIvySettings(Module module, @Nullable String settingsFile, Properties properties) throws IvySettingsNotFoundException, IvySettingsFileReadException {
        IvySettings s = new IvySettings();
        injectProperties(s, module, properties); // inject our properties; they may be needed to parse the settings file

        try {
            if (!StringUtils.isBlank(settingsFile)) {
                if (settingsFile.startsWith("http://") || settingsFile.startsWith("https://")) {
                    HttpConfigurable.getInstance().prepareURL(settingsFile);
                    s.load(new URL(settingsFile));
                } else if (settingsFile.startsWith("file://")) {
                    s.load(new URL(settingsFile));
                } else {
                    s.load(new File(settingsFile));
                }
            } else {
                s.loadDefault();
            }
        } catch (ParseException e) {
            throw new IvySettingsFileReadException(settingsFile, module.getName(), e);
        } catch (IOException e) {
            throw new IvySettingsFileReadException(settingsFile, module.getName(), e);
        }

        // re-inject our properties; they may overwrite some properties loaded by the settings file
        for (Map.Entry<Object,Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            // we first clear the property to avoid possible cyclic-variable errors (cfr issue 95)
            s.setVariable(key, null);
            s.setVariable(key, value);
        }

        return s;
    }

    private static void injectProperties(IvySettings ivySettings, Module module, Properties properties) {
        // By default, we use the module root as basedir (can be overridden by properties injected below)
        fillDefaultBaseDir(ivySettings, module);
        fillSettingsVariablesWithProperties(ivySettings, properties);
    }

    private static void fillSettingsVariablesWithProperties(IvySettings ivySettings, Properties properties) {
        @SuppressWarnings("unchecked")
        final Enumeration<String> propertyNames = (Enumeration<String>) properties.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = propertyNames.nextElement();
            ivySettings.setVariable(propertyName, properties.getProperty(propertyName));
        }
    }

    private static void fillDefaultBaseDir(IvySettings ivySettings, Module module) {
        final File moduleFileFolder = new File(module.getModuleFilePath()).getParentFile();
        if (moduleFileFolder != null) {
            ivySettings.setBaseDir(moduleFileFolder.getAbsoluteFile());
        }
    }

    private static IvyIdeaFacetConfiguration getModuleConfiguration(Module module) {
        final IvyIdeaFacetConfiguration moduleConfiguration = IvyIdeaFacetConfiguration.getInstance(module);
        if (moduleConfiguration == null) {
            throw new RuntimeException("Internal error: No IvyIDEA facet configured for module " + module.getName() + ", but an attempt was made to use it as such.");
        }
        return moduleConfiguration;
    }
}
