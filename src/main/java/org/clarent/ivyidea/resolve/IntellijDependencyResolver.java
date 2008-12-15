package org.clarent.ivyidea.resolve;

import com.intellij.openapi.application.ApplicationManager;
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

    private List<ResolvedDependency> dependencies;
    private List<ResolveProblem> problems = Collections.emptyList();

    private IvyManager ivyManager;

    public IntellijDependencyResolver(IvyManager ivyManager) {
        this.ivyManager = ivyManager;
    }

    public List<ResolveProblem> getProblems() {
        return problems;
    }

    public List<ResolvedDependency> resolve(final Module module) throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException {
        final IvySettingsNotFoundException[] settingsNotFoundException = new IvySettingsNotFoundException[1];
        final IvySettingsFileReadException[] settingsFileReadException = new IvySettingsFileReadException[1];
        final IvyFileReadException[] ivyFileReadException = new IvyFileReadException[1];
        ApplicationManager.getApplication().runReadAction(new Runnable() {
            public void run() {
                final DependencyResolver dependencyResolver = new DependencyResolver();
                try {
                    dependencyResolver.resolve(module, ivyManager);
                    dependencies = dependencyResolver.getResolvedDependencies();
                    problems = dependencyResolver.getResolveProblems();
                } catch (IvySettingsNotFoundException e) {
                    settingsNotFoundException[0] = e;
                } catch (IvyFileReadException e) {
                    ivyFileReadException[0] = e;
                } catch (IvySettingsFileReadException e) {
                    settingsFileReadException[0] = e;
                }
            }
        });
        if (settingsNotFoundException.length == 1 && settingsNotFoundException[0] != null) {
            throw settingsNotFoundException[0];
        }
        if (ivyFileReadException.length == 1 && ivyFileReadException[0] != null) {
            throw ivyFileReadException[0];
        }
        if (settingsFileReadException.length == 1 && settingsFileReadException[0] != null) {
            throw settingsFileReadException[0];
        }
        return dependencies;
    }

}
