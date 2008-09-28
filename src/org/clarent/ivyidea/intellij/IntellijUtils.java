package org.clarent.ivyidea.intellij;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;

/**
 * @author Guy Mahieu
 */

public class IntellijUtils {

    public static Project getCurrentProject() {
        DataContext dataContext = DataManager.getInstance().getDataContext();
        return DataKeys.PROJECT.getData(dataContext);
    }

    public static Module[] getAllModules() {
        return getAllModules(getCurrentProject());
    }

    public static Module[] getAllModules(Project project) {
        return ModuleManager.getInstance(project).getModules();
    }
}
