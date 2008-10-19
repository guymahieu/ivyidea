package org.clarent.ivyidea.tempstuff;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import org.clarent.ivyidea.DependencyUpdater;

public class UpdateDependenciesAction extends AnAction {


    public static final String DEP_DIR = "dep-cache/eclipse";

    public void actionPerformed(AnActionEvent e) {
        Module currentModule = DataKeys.MODULE.getData(e.getDataContext());
        DependencyUpdater.setupLibraries(currentModule);
    }


}
