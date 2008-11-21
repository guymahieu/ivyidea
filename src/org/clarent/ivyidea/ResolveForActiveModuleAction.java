package org.clarent.ivyidea;

import com.intellij.facet.FacetManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.exception.IvyFileReadException;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacet;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacetType;
import org.clarent.ivyidea.intellij.task.IvyIdeaResolveBackgroundTask;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.resolve.IntellijDependencyResolver;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.List;

public class ResolveForActiveModuleAction extends AbstractResolveAction {

    private static final String MENU_TEXT = "Resolve for {0} module";

    public void actionPerformed(final AnActionEvent e) {
        final Module module = DataKeys.MODULE.getData(e.getDataContext());
        if (module != null) {
            ProgressManager.getInstance().run(new IvyIdeaResolveBackgroundTask(e) {

                public void doResolve(@NotNull ProgressIndicator progressIndicator) throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException {
                    clearConsole(myProject);
                    final IntellijDependencyResolver resolver = new IntellijDependencyResolver(new IvyManager());
                    final List<ResolvedDependency> list = resolver.resolve(module);
                    updateIntellijModel(module, list);
                    reportProblems(module, resolver.getProblems());
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