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

package org.clarent.ivyidea.intellij.model;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.testFramework.HeavyPlatformTestCase;
import org.clarent.ivyidea.resolve.dependency.InternalDependency;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ModuleDependenciesTest extends HeavyPlatformTestCase {

    private Module module;
    private Module other;
    private Module third;
    // the IvyIDEA modules of the project
    private Collection<Module> workspaceModules;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        module = createModule("module");
        other = createModule("other");
        third = createModule("third");
        workspaceModules = Arrays.asList(module, other);
    }

    public void testRemovesModuleDependencyThatIsNoLongerResolved() {
        resolve(other);
        assertThat(moduleDependencies()).containsExactly("other");

        resolve();
        assertThat(moduleDependencies()).isEmpty();
    }

    public void testKeepsModuleDependencyThatIsStillResolved() {
        ModuleRootModificationUtil.addDependency(module, other);
        resolve(other);
        assertThat(moduleDependencies()).containsExactly("other");
    }

    public void testKeepsModuleDependencyOnModuleOutsideTheWorkspace() {
        ModuleRootModificationUtil.addDependency(module, third);
        resolve(other);
        resolve();
        assertThat(moduleDependencies()).containsExactly("third");
    }

    public void testRemovesModuleDependencyOnWorkspaceModuleThatTheIvyFileDoesNotDeclare() {
        // between IvyIDEA modules, the ivy files decide on the dependencies
        ModuleRootModificationUtil.addDependency(module, other);
        resolve();
        assertThat(moduleDependencies()).isEmpty();
    }

    public void testKeepsModuleDependenciesWithoutWorkspaceModules() {
        // e.g. when dependencies on other modules aren't detected, or for Remove All Resolved Libraries
        ModuleRootModificationUtil.addDependency(module, other);
        WriteAction.run(() -> {
            try (IntellijModuleWrapper wrapper = IntellijModuleWrapper.forModule(module)) {
                wrapper.updateDependencies(Collections.emptyList());
            }
        });
        assertThat(moduleDependencies()).containsExactly("other");
    }

    private void resolve(Module... resolvedModules) {
        final List<ResolvedDependency> dependencies = new ArrayList<>();
        for (Module resolvedModule : resolvedModules) {
            dependencies.add(new InternalDependency(resolvedModule));
        }
        WriteAction.run(() -> {
            try (IntellijModuleWrapper wrapper = IntellijModuleWrapper.forModule(module)) {
                wrapper.updateDependencies(dependencies, workspaceModules);
            }
        });
    }

    private List<String> moduleDependencies() {
        final List<String> names = new ArrayList<>();
        for (Module dependency : ModuleRootManager.getInstance(module).getDependencies()) {
            names.add(dependency.getName());
        }
        return names;
    }
}
