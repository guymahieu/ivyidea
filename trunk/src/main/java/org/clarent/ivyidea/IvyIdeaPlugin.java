package org.clarent.ivyidea;

import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.components.ApplicationComponent;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacetType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaPlugin implements ApplicationComponent {

    @NonNls
    @NotNull
    public String getComponentName() {
        return "IvyIDEA.PluginApplicationComponent";
    }

    public void initComponent() {
        FacetTypeRegistry.getInstance().registerFacetType(new IvyIdeaFacetType());
    }

    public void disposeComponent() {
    }

}