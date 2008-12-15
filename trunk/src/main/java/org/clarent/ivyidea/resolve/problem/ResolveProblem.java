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
