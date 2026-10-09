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
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.clarent.ivyidea.intellij.IvyIdeaConsoleToolWindowFactory;
import org.clarent.ivyidea.intellij.ui.IvyIdeaIcons;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shows a dot on the icon of the IvyIDEA tool window while a resolve is running, like the Build tool window does
 * while building.
 */
final class ResolveIndicator {

    private static final Key<AtomicInteger> RUNNING_RESOLVES = Key.create("IvyIDEA.runningResolves");

    private ResolveIndicator() {
    }

    static void resolveStarted(Project project) {
        getRunningResolves(project).incrementAndGet();
        updateIcon(project);
    }

    static void resolveFinished(Project project) {
        getRunningResolves(project).decrementAndGet();
        updateIcon(project);
    }

    private static AtomicInteger getRunningResolves(Project project) {
        synchronized (RUNNING_RESOLVES) {
            AtomicInteger runningResolves = project.getUserData(RUNNING_RESOLVES);
            if (runningResolves == null) {
                runningResolves = new AtomicInteger();
                project.putUserData(RUNNING_RESOLVES, runningResolves);
            }
            return runningResolves;
        }
    }

    private static void updateIcon(Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(IvyIdeaConsoleToolWindowFactory.TOOLWINDOW_ID);
            if (toolWindow != null) {
                boolean resolving = getRunningResolves(project).get() > 0;
                toolWindow.setIcon(resolving ? IvyIdeaIcons.TOOL_WINDOW_RESOLVING : IvyIdeaIcons.TOOL_WINDOW);
            }
        }, ModalityState.any(), project.getDisposed());
    }
}
