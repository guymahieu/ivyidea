package org.clarent.ivyidea.intellij;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.UserFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.clarent.ivyidea.intellij.ui.IvyIdeaIcons;

import javax.swing.*;

/**
 * @author Guy Mahieu
 */

public class IvyFileType extends UserFileType {

    public static final FileType IVY_FILE_TYPE = new IvyFileType();

    private IvyFileType() {
        setName("Ivy");
        setDescription("Ivy File");
    }

    public SettingsEditor getEditor() {
        return null;
    }

    public boolean isBinary() {
        return false;
    }

    public Icon getIcon() {
        return IvyIdeaIcons.MAIN_ICON;
    }

    @Nullable
    public SyntaxHighlighter getHighlighter(@Nullable Project project, VirtualFile virtualFile) {
        return null;
    }
}
