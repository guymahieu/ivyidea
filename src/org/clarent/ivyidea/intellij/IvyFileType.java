package org.clarent.ivyidea.intellij;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.UserFileType;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

/**
 * @author Guy Mahieu
 */

public class IvyFileType extends UserFileType {

    public SettingsEditor getEditor() {
        return null;
    }

    public boolean isBinary() {
        return false;
    }

    @Nullable
    public SyntaxHighlighter getHighlighter(@Nullable Project project, VirtualFile virtualFile) {
        return null;
    }
}
