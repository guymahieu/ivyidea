package org.clarent.ivyidea.intellij.compatibility;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.awt.*;

/**
 * Compatibility implementation for Intellij IDEA 12.0 (and possibly later versions).
 *
 * @author Maarten Coene
 */
class Intellij12Methods extends Intellij8Methods {

    @Override
    public VirtualFile[] chooseFiles(FileChooserDescriptor descriptor, Component parent, Project project, VirtualFile toSelect) {
        return FileChooser.chooseFiles(descriptor, parent, project, toSelect);
    }
}
