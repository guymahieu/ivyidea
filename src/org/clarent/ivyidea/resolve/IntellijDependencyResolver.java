package org.clarent.ivyidea.resolve;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import org.apache.ivy.util.MessageLogger;
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

    private List<ResolvedDependency> dependencies;
    private List<ResolveProblem> problems = Collections.emptyList();

    private IvyManager ivyManager;

    public IntellijDependencyResolver(IvyManager ivyManager) {
        this.ivyManager = ivyManager;
    }

    public List<ResolveProblem> getProblems() {
        return problems;
    }

    public List<ResolvedDependency> resolve(final Module module) {
        return resolve(module, null);
    }

    public List<ResolvedDependency> resolve(final Module module, final MessageLogger messageLogger) {
        ApplicationManager.getApplication().runReadAction(new Runnable() {
            public void run() {
                final DependencyResolver dependencyResolver = new DependencyResolver();
                dependencies = dependencyResolver.resolve(module, ivyManager, messageLogger);
                problems = dependencyResolver.getProblems();
            }
        });
        return dependencies;
    }

}
