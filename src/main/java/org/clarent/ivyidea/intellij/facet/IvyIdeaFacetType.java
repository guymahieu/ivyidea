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

package org.clarent.ivyidea.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.intellij.ui.IvyIdeaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaFacetType extends FacetType<IvyIdeaFacet, IvyIdeaFacetConfiguration> {

    public static final FacetTypeId<IvyIdeaFacet> ID = new FacetTypeId<>("IvyIDEA");

    public static final String STRING_ID = "IvyIDEA";

    public static IvyIdeaFacetType getInstance() {
        return findInstance(IvyIdeaFacetType.class);
    }

    public IvyIdeaFacetType() {
        super(ID, STRING_ID, "IvyIDEA");
    }

    public IvyIdeaFacetConfiguration createDefaultConfiguration() {
        return new IvyIdeaFacetConfiguration();
    }

    public IvyIdeaFacet createFacet(@NotNull Module module, String name, @NotNull IvyIdeaFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
        return new IvyIdeaFacet(this, module, name, configuration, underlyingFacet);
    }

    public boolean isSuitableModuleType(ModuleType moduleType) {
        // Allow ivy facets for all module types...
        return true;
    }

    public javax.swing.Icon getIcon() {
        return IvyIdeaIcons.MAIN_ICON_SMALL;
    }

}
