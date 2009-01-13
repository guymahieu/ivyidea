package org.clarent.ivyidea.config;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.IvyIdeaProjectSettings;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.intellij.ui.IvyIdeaProjectSettingsComponent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.Set;

/**
 * Handles retrieval of settings from the configuration.
 *
 * @author Guy Mahieu
 */
public class IvyIdeaConfigHelper {

    public static String getCreatedLibraryName() {
        return "IvyIDEA-resolved";
    }

    public static ResolveOptions createResolveOptions(Module module) {
        ResolveOptions options = new ResolveOptions();
        options.setValidate(isValidationEnabled(module.getProject()));        
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
        if (moduleConfiguration != null && moduleConfiguration.getConfigsToResolve() != null) {
            return Collections.unmodifiableSet(moduleConfiguration.getConfigsToResolve());
        } else {
            return Collections.emptySet();
        }
    }

    public static boolean isValidationEnabled(Project project) {
        IvyIdeaProjectSettingsComponent component = project.getComponent(IvyIdeaProjectSettingsComponent.class);
        //noinspection SimplifiableIfStatement
        if (component != null && component.getState() != null) {
            return component.getState().isValidateIvyFiles();
        }
        return false;
    }

    @NotNull
    public static File getIvySettingsFile(Module module) throws IvySettingsNotFoundException {
        final IvyIdeaFacetConfiguration moduleConfiguration = IvyIdeaFacetConfiguration.getInstance(module);
        if (moduleConfiguration == null) {
            throw new RuntimeException("Internal error: No IvyIDEA facet configured for module " + module.getName() + ", but an attempt was made to use it as such.");
        }
        if (moduleConfiguration.isUseProjectSettings()) {
            IvyIdeaProjectSettingsComponent component = module.getProject().getComponent(IvyIdeaProjectSettingsComponent.class);
            final IvyIdeaProjectSettings state = component.getState();
            if (state != null && state.getIvySettingsFile() != null && state.getIvySettingsFile().trim().length() > 0) {
                File result = new File(state.getIvySettingsFile());
                if (!result.exists()) {
                    throw new IvySettingsNotFoundException("The ivy settings file given in the project settings does not exist: " + result.getAbsolutePath(), IvySettingsNotFoundException.ConfigLocation.Project, module.getProject().getName());
                }
                return result;
            } else {
                throw new IvySettingsNotFoundException("No ivy settings file specified in the project settings.", IvySettingsNotFoundException.ConfigLocation.Project, module.getProject().getName());
            }
        } else {
            final String ivySettingsFile = moduleConfiguration.getIvySettingsFile();
            if (ivySettingsFile != null) {
                File result = new File(ivySettingsFile);
                if (!result.exists()) {
                    throw new IvySettingsNotFoundException("The ivy settings file given in the module settings for module " + module.getName() + " does not exist: " + result.getAbsolutePath(), IvySettingsNotFoundException.ConfigLocation.Module, module.getName());
                }
                return result;
            } else {
                throw new IvySettingsNotFoundException("No ivy settings file given in the settings of module " + module.getName(), IvySettingsNotFoundException.ConfigLocation.Module, module.getName());
            }
        }
    }
}
