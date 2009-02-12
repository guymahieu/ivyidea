/*
 * Copyright 2009 Guy Mahieu
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
