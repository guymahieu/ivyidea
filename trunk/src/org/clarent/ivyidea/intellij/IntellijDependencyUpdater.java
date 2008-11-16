package org.clarent.ivyidea.intellij;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.VirtualFile;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.intellij.compatibility.IntellijCompatibilityService;
import org.clarent.ivyidea.resolve.dependency.ExternalDependency;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public class IntellijDependencyUpdater {

    private static final Logger LOGGER = Logger.getLogger(IntellijDependencyUpdater.class.getName());

    public static void updateDependencies(Module module, List<ResolvedDependency> resolvedDependencies) {
        String libraryName = IvyIdeaConfigHelper.getCreatedLibraryName();
        final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
        try {
            LibraryTable libraryTable = modifiableModel.getModuleLibraryTable();
            Library library = getLibrary(libraryName, libraryTable, module);
            final Library.ModifiableModel libraryModifiableModel = library.getModifiableModel();
            removeOldDependencies(libraryModifiableModel, resolvedDependencies);
            for (ResolvedDependency resolvedDependency : resolvedDependencies) {
                resolvedDependency.addTo(modifiableModel, libraryModifiableModel);
            }
            if (libraryModifiableModel.isChanged()) {
                libraryModifiableModel.commit();
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
    }

    private static void removeOldDependencies(Library.ModifiableModel libraryModifiableModel, List<ResolvedDependency> resolvedDependencies) {
        for (OrderRootType type : IntellijCompatibilityService.getCompatibilityMethods().getAllOrderRootTypes()) {
            final VirtualFile[] intellijDependencies = libraryModifiableModel.getFiles(type);
            List<VirtualFile> dependenciesToRemove = new ArrayList<VirtualFile>(Arrays.asList(intellijDependencies)); // add all dependencies initially
            for (VirtualFile intellijDependency : intellijDependencies) {
                for (ResolvedDependency resolvedDependency : resolvedDependencies) {
                    if (resolvedDependency instanceof ExternalDependency) {
                        ExternalDependency externalDependency = (ExternalDependency) resolvedDependency;
                        if (externalDependency.isSameDependency(intellijDependency)) {
                            dependenciesToRemove.remove(intellijDependency); // remove existing ones
                        }
                    }
                }
            }
            for (VirtualFile virtualFile : dependenciesToRemove) {
                final String dependencyUrl = virtualFile.getUrl();
                LOGGER.info("Removing no longer needed dependency of type " + type + ": " + dependencyUrl);
                libraryModifiableModel.removeRoot(dependencyUrl, type);
            }
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
