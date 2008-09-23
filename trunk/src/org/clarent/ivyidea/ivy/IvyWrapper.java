package org.clarent.ivyidea.ivy;

import com.intellij.openapi.module.Module;
import org.apache.ivy.Ivy;
import org.clarent.ivyidea.intellij.facet.IvyFacetConfiguration;
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
        // TODO: check project settings if not overridden in module when project settings are implemented!
        return new File(IvyFacetConfiguration.getInstance(module).getIvySettingsFile());
    }

}
