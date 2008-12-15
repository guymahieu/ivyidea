package org.clarent.ivyidea.resolve.dependency;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.Library;

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

    public void addTo(ModifiableRootModel intellijModule, Library.ModifiableModel libraryModel) {
        if (!isExistingDependency(intellijModule)) {
            LOGGER.info("Registering module dependency from " + intellijModule.getModule().getName() + " on module " + module.getName());
            intellijModule.addModuleOrderEntry(module);
        } else {
            LOGGER.info("Dependency from " + intellijModule.getModule().getName() + " on module " + module.getName() + " was already present; not reregistring");
        }
    }

    private boolean isExistingDependency(ModifiableRootModel intellijModule) {
        final Module[] existingDependencies = intellijModule.getModuleDependencies();
        for (Module existingDependency : existingDependencies) {
            if (existingDependency.getName().equals(module.getName())) {
                return true;
            }
        }
        return false;
    }

}
