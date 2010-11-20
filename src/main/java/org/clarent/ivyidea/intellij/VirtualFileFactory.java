/*
 * Copyright 2010 Guy Mahieu
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
