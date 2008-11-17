package org.clarent.ivyidea.intellij.task;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.DialogBuilder;
import org.clarent.ivyidea.config.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.ui.IvySettingsNotFoundDialog;
import org.clarent.ivyidea.intellij.ui.IvySettingsErrorDialog;
import org.jetbrains.annotations.NotNull;

/**
 * @author Guy Mahieu
 */

public abstract class IvyIdeaResolveBackgroundTask extends IvyIdeaBackgroundTask {

    IvySettingsNotFoundException exception;

    /**
     * Implementations should perform the resolve process inside this method.
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