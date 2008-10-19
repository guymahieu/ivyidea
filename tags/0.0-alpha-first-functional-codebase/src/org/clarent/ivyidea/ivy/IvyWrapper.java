package org.clarent.ivyidea.ivy;

import com.intellij.openapi.module.Module;
import org.apache.ivy.Ivy;
import org.clarent.ivyidea.intellij.IvyIdeaProjectSettings;
import org.clarent.ivyidea.intellij.facet.IvyFacetConfiguration;
import org.clarent.ivyidea.intellij.ui.IvyIdeaProjectSettingsComponent;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 * @author Guy Mahieu
 */

public class IvyWrapper {

    private Ivy ivy;

    public IvyWrapper(Module module) {
        try {
            this.ivy = Ivy.newInstance();
            this.ivy.configure(getIvySettingsFile(module));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Ivy getIvy() {
        return ivy;
    }

    @Nullable
    protected File getIvySettingsFile(Module module) {
        final IvyFacetConfiguration moduleConfiguration = IvyFacetConfiguration.getInstance(module);
        if (moduleConfiguration.isUseProjectSettings()) {
            IvyIdeaProjectSettingsComponent component = module.getProject().getComponent(IvyIdeaProjectSettingsComponent.class);
            final IvyIdeaProjectSettings state = component.getState();
            if (state != null && state.getIvySettingsFile() != null && state.getIvySettingsFile().trim().length() > 0) {
                return new File(state.getIvySettingsFile());
            } else {
                throw new RuntimeException("No ivy settings file found in the project configuration.");
            }
        } else {
            return new File(moduleConfiguration.getIvySettingsFile());
        }
    }

}
