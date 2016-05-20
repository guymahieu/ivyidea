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

package org.clarent.ivyidea.resolve;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.apache.ivy.core.module.descriptor.DefaultExcludeRule;
import org.apache.ivy.core.module.descriptor.ExcludeRule;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ArtifactId;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.plugins.matcher.GlobPatternMatcher;
import org.apache.ivy.plugins.matcher.PatternMatcher;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.ivy.IvyManager;

import java.util.ArrayList;
import java.util.List;

public class ResolveUtil {
    public static List<ExcludeRule> buildExcludeRulesForIvyModules(IvyManager ivyManager, Project project) throws IvySettingsFileReadException, IvySettingsNotFoundException {
        return buildExcludeRulesForIvyModules(ivyManager, IntellijUtils.getAllModulesWithIvyIdeaFacet(project));
    }

    public static List<ExcludeRule> buildExcludeRulesForIvyModules(IvyManager ivyManager, Module... ivyModules) throws IvySettingsFileReadException, IvySettingsNotFoundException {
        List<ExcludeRule> excludeRules = new ArrayList<ExcludeRule>();

        for (final Module ivyModule : ivyModules) {
            ModuleDescriptor moduleDescriptor = ivyManager.getModuleDescriptor(ivyModule);

            ModuleId moduleId = moduleDescriptor.getModuleRevisionId().getModuleId();

            ArtifactId aid = new ArtifactId(moduleId, PatternMatcher.ANY_EXPRESSION, PatternMatcher.ANY_EXPRESSION, PatternMatcher.ANY_EXPRESSION);
            DefaultExcludeRule rule = new DefaultExcludeRule(aid, GlobPatternMatcher.INSTANCE, null);

            for (String configurationName : moduleDescriptor.getConfigurationsNames()) {
                rule.addConfiguration(configurationName);
            }

            excludeRules.add(rule);
        }
        return excludeRules;
    }
}
