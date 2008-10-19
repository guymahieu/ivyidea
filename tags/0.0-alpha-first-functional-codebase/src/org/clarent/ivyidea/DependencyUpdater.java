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

                    // TODO: Respect the order in the ivy file; this means module dependencies can be
                    //          interleaved with jar files!
                    //      --> wrap both types in the same kind of object from a resolver and just add them
                    //              in sequence here...
                    final IvyHelper ivyHelper = new IvyHelper(currentModule);
                    final List<Module> modules = ivyHelper.findModuleDependencies(currentModule);
                    for (Module module : modules) {
                        boolean alreadyInDependencies = false;
                        final Module[] existingDependencies = modifiableModel.getModuleDependencies();
                        for (Module existingDependency : existingDependencies) {
                            if (existingDependency.getName().equals(module.getName())) {
                                alreadyInDependencies = true;
                                break;
                            }
                        }
                        if (!alreadyInDependencies) {
                            LOGGER.info("Registering module dependency on module " + module.getName() + " for module " + currentModule.getName());
                            modifiableModel.addModuleOrderEntry(module);
                        } else {
                            LOGGER.info("Module dependency on " + module.getName() + " already existed in module " + currentModule.getName());
                        }
                    }

                    // TODO: DO not include module dependencies!!!!!
                    // TODO: clear library or remove old dependencies that are no longer needed
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
