package org.clarent.ivyidea.intellij.model;

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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class IntellijModuleWrapper implements Closeable {

    private static final Logger LOGGER = Logger.getLogger(IntellijModuleWrapper.class.getName());

    private final ModifiableRootModel intellijModule;
    private final Library.ModifiableModel libraryModel;

    public static IntellijModuleWrapper forModule(Module module) {
        ModifiableRootModel modifiableModel = null;
        try {
            modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
            return new IntellijModuleWrapper(modifiableModel);
        } catch (RuntimeException e) {
            if (modifiableModel != null) {
                modifiableModel.dispose();
            }
            throw e;
        }
    }

    private IntellijModuleWrapper(ModifiableRootModel intellijModule) {
        this.intellijModule = intellijModule;
        this.libraryModel = getIvyIdeaLibrary(intellijModule).getModifiableModel();
    }

    private static Library getIvyIdeaLibrary(ModifiableRootModel modifiableRootModel) {
        final String libraryName = IvyIdeaConfigHelper.getCreatedLibraryName();
        final LibraryTable libraryTable = modifiableRootModel.getModuleLibraryTable();
        final Library library = libraryTable.getLibraryByName(libraryName);
        if (library == null) {
            LOGGER.info("Internal library not found for module " + modifiableRootModel.getModule().getModuleFilePath() + ", creating with name " + libraryName + "...");
            return libraryTable.createLibrary(libraryName);
        }        
        return library;
    }

    public void updateDependencies(Collection<ResolvedDependency> resolvedDependencies) {
        cleanupOnlyKeepGivenDependencies(resolvedDependencies);
        for (ResolvedDependency resolvedDependency : resolvedDependencies) {
            resolvedDependency.addTo(this);
        }
    }

    public void close() {
        if (libraryModel.isChanged()) {
            libraryModel.commit();
        }
        if (intellijModule.isChanged()) {
            intellijModule.commit();
        } else {
            intellijModule.dispose();
        }
    }

    public String getModuleName() {
        return intellijModule.getModule().getName();
    }

    public void addModuleDependency(Module module) {
        intellijModule.addModuleOrderEntry(module);
    }

    public void addExternalDependency(ExternalDependency externalDependency) {
        libraryModel.addRoot(externalDependency.getUrlForLibrary(), externalDependency.getType());
//        libraryModel.
    }

    public boolean alreadyHasDependencyOnModule(Module module) {
        final Module[] existingDependencies = intellijModule.getModuleDependencies();
        for (Module existingDependency : existingDependencies) {
            if (existingDependency.getName().equals(module.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean alreadyHasDependencyOnLibrary(ExternalDependency externalDependency) {
        for (VirtualFile file : libraryModel.getFiles(externalDependency.getType())) {
            if (externalDependency.isSameDependency(file)) {
                return true;
            }
        }
        return false;
    }

    public void cleanupOnlyKeepGivenDependencies(Collection<ResolvedDependency> resolvedDependencies) {
        for (OrderRootType type : IntellijCompatibilityService.getCompatibilityMethods().getAllOrderRootTypes()) {
            final VirtualFile[] intellijDependencies = libraryModel.getFiles(type);
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
                libraryModel.removeRoot(dependencyUrl, type);
            }
        }
    }

}
