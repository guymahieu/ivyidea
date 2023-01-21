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

package org.clarent.ivyidea.intellij;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Guy Mahieu
 */

public class IntellijUtils {

    @NotNull
    public static Module[] getAllModulesWithIvyIdeaFacet(Project project) {
        final Module[] allModules = ModuleManager.getInstance(project).getModules();
        final List<Module> result = new ArrayList<>();
        for (Module module : allModules) {
            if (containsIvyIdeaFacet(module)) {
                result.add(module);
            }
        }
        return result.toArray(new Module[0]);
    }

    public static boolean containsIvyIdeaFacet(@Nullable Module module) {
        return module != null && FacetManager.getInstance(module).getFacetByType(IvyIdeaFacetType.ID) != null;
    }

    public static ConsoleView getConsoleView(Project project) {
        return project.getService(IvyIdeaConsoleService.class).getConsoleView();
    }

    public static ToolWindow getToolWindow(Project project) {
        return ToolWindowManager.getInstance(project).getToolWindow(IvyIdeaConsoleToolWindowFactory.TOOLWINDOW_ID);
    }

}
