package org.clarent.ivyidea.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

/**
 * @author Guy Mahieu
 */

public class IvyFacet extends Facet<IvyFacetConfiguration> {

    public IvyFacet(@NotNull FacetType facetType, @NotNull Module module, String name, @NotNull IvyFacetConfiguration configuration, Facet underlyingFacet) {
        super(facetType, module, name, configuration, underlyingFacet);
    }

    public IvyFacet(@NotNull Module module) {
        this(FacetTypeRegistry.getInstance().findFacetType(IvyFacetType.ID), module, "IvyFacet",new IvyFacetConfiguration(), null);
    }


}
