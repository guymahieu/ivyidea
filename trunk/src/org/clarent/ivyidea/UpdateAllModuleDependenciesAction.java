package org.clarent.ivyidea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;

public class UpdateAllModuleDependenciesAction extends AnAction {

    /*
        Features:
            * Ivy integration; no need for external ant build scripts to be called
            * Detection of intellij module dependencies; there are added as module references
            * Creation of module library with all resolved ivy dependencies

        TODO - critical features:
            * Externalize config + create UI
            * Code cleanup - (re)design of concepts
            * Exception handling

        Nice-to-have
            * Source configurations -> add as sources
            * Allow setting the ivy configurations to resolve and add as a library [eclipse/ide/default]
            * Auto re-resolve on changed ivy.xml file


     */

    public void actionPerformed(AnActionEvent e) {
        Project project = DataKeys.PROJECT.getData(e.getDataContext());
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            DependencyUpdater.setupLibraries(module);
        }
    }

}
