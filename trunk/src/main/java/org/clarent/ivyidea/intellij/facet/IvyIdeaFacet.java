package org.clarent.ivyidea.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.module.Module;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaFacet extends Facet<IvyIdeaFacetConfiguration> {

    @Nullable
    public static IvyIdeaFacet getInstance(@NotNull final Module module) {
        return FacetManager.getInstance(module).getFacetByType(IvyIdeaFacetType.ID);
    }

    public IvyIdeaFacet(@NotNull FacetType facetType, @NotNull Module module, String name, @NotNull IvyIdeaFacetConfiguration configuration, Facet underlyingFacet) {
        super(facetType, module, name, configuration, underlyingFacet);
    }

    public IvyIdeaFacet(@NotNull Module module) {
        this(FacetTypeRegistry.getInstance().findFacetType(IvyIdeaFacetType.ID), module, "IvyIdeaFacet", new IvyIdeaFacetConfiguration(), null);
    }


}
