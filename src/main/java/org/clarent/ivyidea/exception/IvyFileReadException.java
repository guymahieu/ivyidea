package org.clarent.ivyidea.exception;

/**
 * Thrown when there was a problem while reading (accessing/parsing/...)
 * the ivy xml file for a module. 
 *
 * @author Guy Mahieu
 */
public class IvyFileReadException extends IvyIdeaException {

    private String fileName;
    private String moduleName;

    public IvyFileReadException(String fileName, String moduleName, Throwable cause) {
        super(cause);
        this.fileName = fileName;
        this.moduleName = moduleName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getModuleName() {
        return moduleName;
    }

    @Override
    public String getMessage() {
        return "Exception while reading ivy file " + fileName + " for module " + moduleName;
    }
}

