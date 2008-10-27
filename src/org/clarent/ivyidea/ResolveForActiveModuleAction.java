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
import org.clarent.ivyidea.resolve.ResolvedDependency;
import org.clarent.ivyidea.resolve.Resolver;

import java.text.MessageFormat;
import java.util.List;

public class ResolveForActiveModuleAction extends AnAction {

    private static final String MENU_TEXT = "Resolve for {0} module";

    public void actionPerformed(final AnActionEvent e) {
        final Module module = DataKeys.MODULE.getData(e.getDataContext());
        if (module != null) {
            ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
                public void run() {
                    final List<ResolvedDependency> list = new Resolver(new IvyManager()).resolve(module);
                    updateIntellijModel(module, list);
                }
            });
        }
    }

    private void updateIntellijModel(final Module module, final List<ResolvedDependency> list) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        IntellijDependencyUpdater.updateDependencies(module, list);
                    }
                });
            }
        });
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