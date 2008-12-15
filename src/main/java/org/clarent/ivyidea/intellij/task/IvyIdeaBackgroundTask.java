package org.clarent.ivyidea.intellij.task;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.Task;

/**
 * @author Guy Mahieu
 */

public abstract class IvyIdeaBackgroundTask extends Task.Backgroundable {

    private static final PerformInBackgroundOption BackgroundOptionStartWithDialog = new PerformInBackgroundOption() {
        public boolean shouldStartInBackground() {
            return false;
        }

        public void processSentToBackground() {
        }

        public void processRestoredToForeground() {
        }
    };

    public IvyIdeaBackgroundTask(AnActionEvent event) {
        super(DataKeys.PROJECT.getData(event.getDataContext()),
                "IvyIDEA " + event.getPresentation().getText(),
                true, BackgroundOptionStartWithDialog);
    }
}
