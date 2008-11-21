package org.clarent.ivyidea.intellij.task;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import org.clarent.ivyidea.exception.IvyFileReadException;
import org.clarent.ivyidea.exception.IvyIdeaException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.exception.ui.IvyIdeaExceptionDialog;
import org.clarent.ivyidea.exception.ui.LinkBehavior;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.compatibility.IntellijCompatibilityService;
import org.clarent.ivyidea.intellij.ui.IvyIdeaProjectSettingsComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for background tasks that trigger an ivy resolve process.
 *
 * @author Guy Mahieu
 */
public abstract class IvyIdeaResolveBackgroundTask extends IvyIdeaBackgroundTask {

    private IvyIdeaException exception;

    /**
     * Implementations should perform the resolve process inside this method.
     *
     * @param progressIndicator the progress indicator for this backgroundtask
     * @throws IvySettingsNotFoundException if there was a problem opening the ivy settings file
     * @throws IvyFileReadException         if there was a problem opening the ivy file
     */
    public abstract void doResolve(@NotNull ProgressIndicator progressIndicator) throws IvySettingsNotFoundException, IvyFileReadException;

    protected IvyIdeaResolveBackgroundTask(AnActionEvent event) {
        super(event);
    }

    public final void run(@NotNull ProgressIndicator progressIndicator) {
        try {
            doResolve(progressIndicator);
        } catch (IvyIdeaException e) {
            exception = e;
            progressIndicator.cancel();
            // In InteliJ 7 cancelling the progressIndicator does not trigger the
            // onCancel() method, but in IntelliJ 8 it does
            if (!IntellijCompatibilityService.isTaskCancelledOnProgressIndicatorCancel()) {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    public void run() {
                        onCancel();
                    }
                });
            }
        }
    }

    @Override
    public void onCancel() {
        super.onCancel();
        if (exception != null) {
            handle(exception);
        }
    }

    private void handle(IvyIdeaException exception) {
        if (exception instanceof IvyFileReadException) {
            showIvyFileErrorDialog(exception);
        } else if (exception instanceof IvySettingsNotFoundException) {
            showIvySettingsErrorDialog((IvySettingsNotFoundException) exception);
        }
    }

    private void showIvyFileErrorDialog(IvyIdeaException exception) {
        IvyIdeaExceptionDialog.showModalDialog("Ivy File Error", exception, myProject);
    }

    private void showIvySettingsErrorDialog(IvySettingsNotFoundException exception) {
        LinkBehavior linkBehavior = null;
        // TODO: For no I can only activate the link on project settings errors...
        //       Currently I don't know how to open the facet setting from the code...
        //       if only the facet config was a Configurable instance!
        if (exception.getConfigLocation() == IvySettingsNotFoundException.ConfigLocation.Project) {
            linkBehavior = new LinkBehavior(
                    "Open " + exception.getConfigLocation() + " settings for " + exception.getConfigName() + "...",
                    new LinkListener() {
                        public void linkSelected(LinkLabel linkLabel, Object o) {
                            final Project project = IntellijUtils.getCurrentProject();
                            Configurable component = project.getComponent(IvyIdeaProjectSettingsComponent.class);
                            ShowSettingsUtil.getInstance().editConfigurable(project, component);
                        }
                    }, null);
        }
        IvyIdeaExceptionDialog.showModalDialog("Ivy Settings Error", exception, myProject, linkBehavior);
    }

}