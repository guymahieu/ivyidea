package org.clarent.ivyidea.intellij;

import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.io.File;

/**
 * @author Guy Mahieu
 */
public class VirtualFileFactory {

    public static VirtualFile forFile(File file) {
        return VirtualFileManager.getInstance().findFileByUrl("file://" + file.getAbsolutePath());
    }

    public static VirtualFile forJarFile(File file) {
        return JarFileSystem.getInstance().findFileByPath(file.getAbsolutePath() + "!/");
    }

}
