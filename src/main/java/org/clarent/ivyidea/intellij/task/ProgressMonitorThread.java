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
     * When interrupting, Ivy marks itself as interrupted and interrupts the thread, which then stops at the next
     * check of Ivy.
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
                // each interrupt waits until its thread has stopped, so interrupt them all at the same time
                for (Map.Entry<Thread, Ivy> runningIvy : runningIvys.entrySet()) {
                    Thread interrupter = new Thread(() -> interrupt(runningIvy.getValue(), runningIvy.getKey()), "Ivy Interrupter");
                    interrupter.setDaemon(true);
                    interrupter.start();
                }
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
            }
        }
    }

    private static void interrupt(Ivy ivy, Thread thread) {
        try {
            ivy.interrupt(thread);
        } catch (UnsupportedOperationException e) {
            // when the thread is still running after the interrupt timeout of the Ivy settings, Ivy tries to stop it
            // with Thread.stop, which Java no longer supports; Ivy stays marked as interrupted, so the thread still
            // stops at the next check of Ivy
        }
    }
}
