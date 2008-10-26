package org.clarent.ivyidea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.intellij.IntellijUtils;

/**
 * @author Guy Mahieu
 */

public class RemoveAllIvyIdeaModuleLibrariesAction extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        final Project project = DataKeys.PROJECT.getData(e.getDataContext());
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                int count = 0;
                for (final Module module : IntellijUtils.getAllModulesWithIvyIdeaFacet(project)) {
                    final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
                    final LibraryTable moduleLibraryTable = model.getModuleLibraryTable();
                    final Library library = moduleLibraryTable.getLibraryByName(IvyIdeaConfigHelper.getCreatedLibraryName());
                    if (library != null) {
                        moduleLibraryTable.removeLibrary(library);
                        count++;
                        model.commit();
                    }
                }
                // TODO: Show dialog: "{0} libraries removed."                 
            }
        });
    }

}
