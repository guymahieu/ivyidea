package org.clarent.ivyidea.exception;

/**
 * @author Guy Mahieu
 */

public abstract class IvyIdeaException extends Exception {

    protected IvyIdeaException() {
    }

    protected IvyIdeaException(String message) {
        super(message);
    }

    protected IvyIdeaException(String message, Throwable cause) {
        super(message, cause);
    }

    protected IvyIdeaException(Throwable cause) {
        super(cause);
    }
    
}
