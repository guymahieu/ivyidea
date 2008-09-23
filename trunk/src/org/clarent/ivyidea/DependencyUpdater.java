package org.clarent.ivyidea;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import org.clarent.ivyidea.config.PostIvyPluginConfiguration;
import org.clarent.ivyidea.ivy.IvyHelper;

import java.util.List;
import java.util.logging.Logger;

public class DependencyUpdater {

    private static final Logger LOGGER = Logger.getLogger(DependencyUpdater.class.getName());

    public static void setupLibraries(final Module currentModule) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                try {
                    String libraryName = new PostIvyPluginConfiguration().getCreatedLibraryName();
                    final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(currentModule).getModifiableModel();

                    // TODO: clear library or remove old modules that are no longer needed

                    // TODO: should there be interleaving of modules & jars??
                    final IvyHelper ivyHelper = new IvyHelper(currentModule);
                    final List<Module> modules = ivyHelper.findModuleDependencies(currentModule);
                    for (Module module : modules) {
                        LOGGER.info("Registering module dependency: " + module);
                        modifiableModel.addModuleOrderEntry(module);
                    }

                    // TODO: DO not include module dependencies!!!!!
                    LibraryTable libraryTable = modifiableModel.getModuleLibraryTable();
                    Library library = getLibrary(libraryName, libraryTable, currentModule);
                    final Library.ModifiableModel libraryModifiableModel = library.getModifiableModel();
                    final List<String> jarFiles = ivyHelper.getJarFiles(currentModule);
                    for (String jarFile : jarFiles) {
                        LOGGER.info("Registering external file dependency: " + jarFile);
                        libraryModifiableModel.addRoot("jar://" + jarFile + "!/", OrderRootType.CLASSES);
                    }

                    libraryModifiableModel.commit();
                    modifiableModel.commit();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static Library getLibrary(String libraryName, LibraryTable libraryTable, Module currentModule) {
        Library library = libraryTable.getLibraryByName(libraryName);
        if (library == null) {
            LOGGER.info("Internal library not found for module " + currentModule.getModuleFilePath() + ", creating with name " + libraryName + "...");
            library = libraryTable.createLibrary(libraryName);
        }
        return library;
    }

}
