package org.clarent.ivyidea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import org.clarent.ivyidea.intellij.IntellijDependencyUpdater;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.resolve.DependencyResolver;
import org.clarent.ivyidea.resolve.ResolvedDependency;

import java.util.List;

public class UpdateSingleModuleDependenciesAction extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        final Module module = DataKeys.MODULE.getData(e.getDataContext());
        if (module != null) {
            final List<ResolvedDependency> list = new DependencyResolver().resolve(module, new IvyManager());
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                public void run() {
                    IntellijDependencyUpdater.updateDependencies(module, list);
                }
            });
        }
    }


}