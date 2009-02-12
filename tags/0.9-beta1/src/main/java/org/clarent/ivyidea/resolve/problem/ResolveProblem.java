/*
 * Copyright 2009 Guy Mahieu
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

package org.clarent.ivyidea.resolve.problem;

import org.jetbrains.annotations.Nullable;

/**
 * @author Guy Mahieu
 */

public class ResolveProblem {

    private String targetId;
    private String message;
    private Throwable throwable;

    public ResolveProblem(String targetId, String message) {
        this(targetId, message, null);
    }

    public ResolveProblem(String targetId, String message, @Nullable Throwable throwable) {
        this.targetId = targetId;
        this.message = message;
        this.throwable = throwable;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return targetId + ":\t" + message;
    }
}
