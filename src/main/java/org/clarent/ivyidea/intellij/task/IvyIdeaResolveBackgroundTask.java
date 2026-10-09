/*
 * Copyright 2010 Guy Mahieu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.clarent.ivyidea.intellij.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.clarent.ivyidea.exception.IvyFileReadException;
import org.clarent.ivyidea.exception.IvyIdeaException;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.exception.ui.IvyIdeaExceptionDialog;
import org.clarent.ivyidea.exception.ui.LinkBehavior;
import org.clarent.ivyidea.intellij.ui.IvyIdeaProjectSettingsComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Base class for background tasks that trigger an ivy resolve process.
 *
 * @author Guy Mahieu
 */
public abstract class IvyIdeaResolveBackgroundTask extends IvyIdeaBackgroundTask {

    private IvyIdeaException exception;
    private final Project project;
    private ProgressMonitorThread monitorThread;
    private volatile boolean started;
    @Nullable
    private Consumer<Boolean> doneListener;

    /**
     * Implementations should perform the resolve process inside this method.
     *
     * @param progressIndicator the progress indicator for this backgroundtask
     * @throws IvySettingsNotFoundException if no settings file was configured or the configured file was not found
     * @throws IvySettingsFileReadException if there was a problem opening or parsing the ivy settings file
     * @throws IvyFileReadException         if there was a problem opening or parsing the ivy file
     */
    public abstract void doResolve(@NotNull ProgressIndicator progressIndicator) throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException;

    protected IvyIdeaResolveBackgroundTask(Project project, String taskText) {
        this(project, taskText, null);
    }

    /**
     * @param doneListener called on the event dispatch thread when the task is done: with true when the resolve
     *                     succeeded and the module libraries are updated, with false when it failed or was cancelled
     */
    protected IvyIdeaResolveBackgroundTask(Project project, String taskText, @Nullable Consumer<Boolean> doneListener) {
        super(project, taskText);
        this.project = project;
        this.doneListener = doneListener;
    }

    protected ProgressMonitorThread getProgressMonitorThread() {
        return monitorThread;
    }

    public final void run(@NotNull final ProgressIndicator indicator) {
        started = true;
        ResolveIndicator.resolveStarted(project);
        monitorThread = new ProgressMonitorThread(indicator);
        monitorThread.start();

        try {
            // Intercept URL requests and force the intellij proxy to be used
            //
            // TODO: This does not seem to work...
            /*
                IntellijProxyURLHandler.setupHttpProxy();
            */
            // Start the actual resolve process
            doResolve(indicator);
            if (!indicator.isCanceled()) {
                // doResolve updates the module libraries with invokeLater, so this runs after those updates
                ApplicationManager.getApplication().invokeLater(() -> done(true));
            }
        } catch (IvyIdeaException e) {
            exception = e;
            indicator.cancel();
        } catch (RuntimeException e) {
            if (!indicator.isCanceled()) {
                throw e;
            }
        }
    }

    @Override
    public void onFinished() {
        super.onFinished();
        // also called when the task was cancelled before it started
        if (started) {
            ResolveIndicator.resolveFinished(project);
        }
    }

    @Override
    public void onCancel() {
        super.onCancel();
        if (exception != null) {
            handle(exception);
        }
        done(false);
    }

    @Override
    public void onThrowable(@NotNull Throwable error) {
        super.onThrowable(error);
        done(false);
    }

    private void done(boolean success) {
        if (doneListener != null) {
            Consumer<Boolean> listener = doneListener;
            doneListener = null; // only the first result counts, e.g. when cancelled while finishing
            listener.accept(success);
        }
    }

    private void handle(IvyIdeaException exception) {
        if (exception instanceof IvyFileReadException) {
            showSimpleErrorDialog("Ivy File Error", exception);
        } else if (exception instanceof IvySettingsFileReadException) {
            showSimpleErrorDialog("Ivy Settings File Error", exception);
        } else if (exception instanceof IvySettingsNotFoundException) {
            showIvySettingsNotFoundErrorDialog((IvySettingsNotFoundException) exception);
        }
    }

    private void showSimpleErrorDialog(String title, IvyIdeaException exception) {
        IvyIdeaExceptionDialog.showModalDialog(title, exception, myProject);
    }

    private void showIvySettingsNotFoundErrorDialog(IvySettingsNotFoundException exception) {
        LinkBehavior linkBehavior = null;
        // TODO: For now I can only activate the link on project settings errors...
        //       Currently I don't know how to open the facet setting from the code...
        //       if only the facet config was a Configurable instance!
        if (exception.getConfigLocation() == IvySettingsNotFoundException.ConfigLocation.Project) {
            linkBehavior = new LinkBehavior(
                    "Open " + exception.getConfigLocation() + " settings for " + exception.getConfigName() + "...",
                    (linkLabel, o) -> ShowSettingsUtil.getInstance().showSettingsDialog(project, IvyIdeaProjectSettingsComponent.class), null);
        }
        IvyIdeaExceptionDialog.showModalDialog("Ivy Settings Error", exception, myProject, linkBehavior);
    }

}
