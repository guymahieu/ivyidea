package org.clarent.ivyidea.intellij;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import org.clarent.ivyidea.config.IvyIdeaTempConfiguration;
import org.clarent.ivyidea.resolve.ResolvedDependency;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public class IntellijDependencyUpdater {

    private static final Logger LOGGER = Logger.getLogger(IntellijDependencyUpdater.class.getName());

    public static void updateDependencies(Module module, List<ResolvedDependency> resolvedDependencies) {
        if (resolvedDependencies.size() > 0) {
            String libraryName = new IvyIdeaTempConfiguration().getCreatedLibraryName();
            final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
            try {
                LibraryTable libraryTable = modifiableModel.getModuleLibraryTable();
                Library library = getLibrary(libraryName, libraryTable, module);
                final Library.ModifiableModel libraryModifiableModel = library.getModifiableModel();
                for (ResolvedDependency resolvedDependency : resolvedDependencies) {
                    resolvedDependency.addTo(modifiableModel, libraryModifiableModel);
                }
                if (modifiableModel.isChanged()) {
                    modifiableModel.commit();
                } else {
                    modifiableModel.dispose();
                }
            } catch (RuntimeException e) {
                modifiableModel.dispose();
                throw e;
            }
        } else {
            LOGGER.info("No dependencies were found for module " + module.getName());
        }
    }

    public static Library getLibrary(String libraryName, LibraryTable libraryTable, Module currentModule) {
        Library library = libraryTable.getLibraryByName(libraryName);
        if (library == null) {
            LOGGER.info("Internal library not found for module " + currentModule.getModuleFilePath() + ", creating with name " + libraryName + "...");
            library = libraryTable.createLibrary(libraryName);
        }
        return library;
    }

}
