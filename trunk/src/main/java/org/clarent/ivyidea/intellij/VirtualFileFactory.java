package org.clarent.ivyidea.intellij;

import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Guy Mahieu
 */
public class VirtualFileFactory {

    public static VirtualFile forFile(@NotNull File file) {
        if (looksLikeJarFile(file)) {
            return createFromJarFile(file);
        }
        return createFromRegularFile(file);
    }

    private static VirtualFile createFromRegularFile(@NotNull File file) {
        return VirtualFileManager.getInstance().findFileByUrl("file://" + file.getAbsolutePath());
    }

    protected static VirtualFile createFromJarFile(@NotNull File file) {
        return JarFileSystem.getInstance().findFileByPath(file.getAbsolutePath() + "!/");
    }

    protected static boolean looksLikeJarFile(@NotNull File file) {        
        final String fileName = file.getName();
        return file.isFile() && fileName.endsWith(".jar");
    }

}
