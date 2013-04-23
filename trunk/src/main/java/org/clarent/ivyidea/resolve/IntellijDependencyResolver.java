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
import org.clarent.ivyidea.exception.IvyFileReadException;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;
import org.clarent.ivyidea.resolve.problem.ResolveProblem;

import java.util.Collections;
import java.util.List;

/**
 * Wraps the actual resolve process and manages that it is done with the
 * right locking level for IntelliJ.
 *
 * @author Guy Mahieu
 */
public class IntellijDependencyResolver {

    private Module module;
    private List<ResolvedDependency> dependencies = Collections.emptyList();
    private List<ResolveProblem> problems = Collections.emptyList();

    private IvyManager ivyManager;

    public IntellijDependencyResolver(IvyManager ivyManager) {
        this.ivyManager = ivyManager;
    }

    public Module getModule() {
        return module;
    }

    public List<ResolveProblem> getProblems() {
        return problems;
    }

    public List<ResolvedDependency> getDependencies() {
        return dependencies;
    }

    public void resolve(final Module module) throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException {
        this.module = module;
        final DependencyResolver dependencyResolver = new DependencyResolver();
        dependencyResolver.resolve(module, ivyManager);
        dependencies = dependencyResolver.getResolvedDependencies();
        problems = dependencyResolver.getResolveProblems();
    }

}
