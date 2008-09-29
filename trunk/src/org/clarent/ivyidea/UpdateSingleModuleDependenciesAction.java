package org.clarent.ivyidea;

import com.intellij.facet.FacetManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import org.clarent.ivyidea.intellij.IntellijDependencyUpdater;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacet;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacetType;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.resolve.DependencyResolver;
import org.clarent.ivyidea.resolve.ResolvedDependency;

import java.text.MessageFormat;
import java.util.List;

public class UpdateSingleModuleDependenciesAction extends AnAction {

    private static final String MENU_TEXT = "For {0} module";

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

    public void update(AnActionEvent e) {
        final Presentation presentation = e.getPresentation();
        final Module activeModule = DataKeys.MODULE.getData(e.getDataContext());
        if (activeModule != null) {
            final IvyIdeaFacet ivyIdeaFacet = FacetManager.getInstance(activeModule).getFacetByType(IvyIdeaFacetType.ID);
            if (ivyIdeaFacet != null) {
                presentation.setText(MessageFormat.format(MENU_TEXT, activeModule.getName()));
                presentation.setEnabled(true);
                presentation.setVisible(true);
                return;
            }
        }
        presentation.setText(MessageFormat.format(MENU_TEXT, "active"));
        presentation.setEnabled(false);
        presentation.setVisible(false);
    }
}