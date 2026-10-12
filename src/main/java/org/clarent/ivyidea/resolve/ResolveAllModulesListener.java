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

package org.clarent.ivyidea.resolve;

import com.intellij.util.messages.Topic;
import org.clarent.ivyidea.intellij.task.ResolveResult;

/**
 * Notified on the event dispatch thread when the dependencies of all modules of a project are resolved. Resolving a
 * single module isn't reported, because it leaves the other modules as they are.
 */
public interface ResolveAllModulesListener {

    @Topic.ProjectLevel
    Topic<ResolveAllModulesListener> TOPIC = new Topic<>(ResolveAllModulesListener.class, Topic.BroadcastDirection.NONE);

    void resolveStarted();

    void resolveFinished(ResolveResult result);
}
