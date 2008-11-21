package org.clarent.ivyidea.intellij.task;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.DialogBuilder;
import org.clarent.ivyidea.config.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.compatibility.IntellijCompatibilityService;
import org.clarent.ivyidea.intellij.ui.IvySettingsErrorDialog;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for background tasks that trigger an ivy resolve process.
 *
 * @author Guy Mahieu
 */
public abstract class IvyIdeaResolveBackgroundTask extends IvyIdeaBackgroundTask {

    IvySettingsNotFoundException exception;

    /**
     * Implementations should perform the resolve process inside this method.
     *
     * @param progressIndicator the progress indicator for this backgroundtask
     *
     * @throws IvySettingsNotFoundException if there was a problem opening the ivy settings file
     */
    public abstract void doResolve(@NotNull ProgressIndicator progressIndicator) throws IvySettingsNotFoundException;

    protected IvyIdeaResolveBackgroundTask(AnActionEvent event) {
        super(event);
    }


    public final void run(@NotNull ProgressIndicator progressIndicator) {
        try {
            doResolve(progressIndicator);
        } catch (IvySettingsNotFoundException e) {
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
            showIvySettingsDialog(exception);
        }
    }

    private void showIvySettingsDialog(IvySettingsNotFoundException exception) {
        // TODO: get rid of the cancel button
        IvySettingsErrorDialog dlg = new IvySettingsErrorDialog();
        dlg.setLinkData(exception.getConfigLocation(), exception.getConfigName());
        dlg.setMessage(exception.getMessage());
        final DialogBuilder builder = new DialogBuilder(myProject);
        builder.setCenterPanel(dlg.getRootPanel());
        builder.setOkActionEnabled(true);
        builder.setOkOperation(null);
        builder.setTitle("Ivy Settings Error");
        builder.showModal(false);        
    }

}