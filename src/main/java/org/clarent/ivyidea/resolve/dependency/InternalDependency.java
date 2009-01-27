package org.clarent.ivyidea.resolve.dependency;

import com.intellij.openapi.module.Module;
import org.clarent.ivyidea.intellij.model.IntellijModuleWrapper;

import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public class InternalDependency implements ResolvedDependency {

    private static final Logger LOGGER = Logger.getLogger(InternalDependency.class.getName());

    private Module module;

    public InternalDependency(Module module) {
        this.module = module;
    }

    public void addTo(IntellijModuleWrapper intellijModuleWrapper) {
        if (!intellijModuleWrapper.alreadyHasDependencyOnModule(module)) {
            LOGGER.info("Registering module dependency from " + intellijModuleWrapper.getModuleName() + " on module " + module.getName());
            intellijModuleWrapper.addModuleDependency(module);
        } else {
            LOGGER.info("Dependency from " + intellijModuleWrapper.getModuleName() + " on module " + module.getName() + " was already present; not reregistring");
        }
    }

}
