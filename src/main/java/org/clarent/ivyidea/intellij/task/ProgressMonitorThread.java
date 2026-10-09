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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Background thread that monitors the ProgressIndicator, and interrupts the running Ivy operations when it is
 * cancelled.
 *
 * @author Maarten Coene
 */
public class ProgressMonitorThread extends Thread {

    private final ProgressIndicator indicator;
    private final Map<Thread, Ivy> runningIvys = new ConcurrentHashMap<>();

    public ProgressMonitorThread(ProgressIndicator indicator) {
        super("ProgressIndicator Monitor");
        this.indicator = indicator;
    }

    /**
     * Registers the Ivy that is used by the current thread, until {@link #unregister()} is called.
     * <p>
     * When interrupting, Ivy waits for the thread to end, and stops it if it is still running after the interrupt
     * timeout of the Ivy settings.
     */
    public void register(Ivy ivy) {
        runningIvys.put(Thread.currentThread(), ivy);
    }

    public void unregister() {
        runningIvys.remove(Thread.currentThread());
    }

    @Override
    public void run() {
        while (indicator.isRunning()) {
            if (!runningIvys.isEmpty() && indicator.isCanceled()) {
                for (Map.Entry<Thread, Ivy> runningIvy : runningIvys.entrySet()) {
                    runningIvy.getValue().interrupt(runningIvy.getKey());
                }
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
            }
        }
    }
}
