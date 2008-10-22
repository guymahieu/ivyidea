package org.clarent.ivyidea.intellij;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaProjectSettings {

    private String ivySettingsFile = "";
    private boolean validateIvyFiles = false;

    public String getIvySettingsFile() {
        return ivySettingsFile;
    }

    public void setIvySettingsFile(String ivySettingsFile) {
        this.ivySettingsFile = ivySettingsFile;
    }

    public boolean isValidateIvyFiles() {
        return validateIvyFiles;
    }

    public void setValidateIvyFiles(boolean validateIvyFiles) {
        this.validateIvyFiles = validateIvyFiles;
    }
}
