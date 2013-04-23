/*
 * Copyright 2013 Maarten Coene
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

import com.intellij.openapi.progress.ProgressIndicator;
import org.apache.ivy.Ivy;

/**
 * Background thread that monitors the ProgressIndicator.
 *
 * @author Maarten Coene
 */
public class ProgressMonitorThread extends Thread {

    private ProgressIndicator indicator;
    private Thread resolveThread;
    private Ivy ivy;

    public ProgressMonitorThread(ProgressIndicator indicator, Thread resolveThread) {
        super("ProgressIndicator Monitor");
        this.indicator = indicator;
        this.resolveThread = resolveThread;
    }

    public void setIvy(Ivy ivy) {
        this.ivy = ivy;
    }

    @Override
    public void run() {
        while (indicator.isRunning()) {
            if (ivy != null && indicator.isCanceled()) {
                ivy.interrupt(resolveThread);
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
            }
        }
    }
}
