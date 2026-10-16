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

package org.clarent.ivyidea.intellij.facet.config;

import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IvyIdeaFacetConfigurationTest {

    @Test
    public void loadsEmptyPropertiesSettingsAsDefaults() throws Exception {
        // a module file like this made the resolve fail with a NullPointerException (#170)
        final IvyIdeaFacetConfiguration.FacetConfig config = XmlSerializer.deserialize(
                JDOMUtil.load("<configuration><option name=\"facetPropertiesSettings\" /></configuration>"),
                IvyIdeaFacetConfiguration.FacetConfig.class);

        assertThat(config.getFacetPropertiesSettings()).isNotNull();
        assertThat(config.getFacetPropertiesSettings().getPropertyFiles()).isEmpty();
        assertThat(config.getFacetPropertiesSettings().isIncludeProjectLevelPropertiesFiles()).isTrue();
    }
}
