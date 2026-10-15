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

package org.clarent.ivyidea.intellij.ui;

import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.ivy.IvyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

/**
 * Shows the IvyIDEA icon on the ivy file of an IvyIDEA module, whatever its name, instead of the XML icon.
 */
public class IvyFileIconProvider implements FileIconProvider {

    @Nullable
    @Override
    public Icon getIcon(@NotNull VirtualFile file, int flags, @Nullable Project project) {
        // called for every file the IDE shows, so rule out other files cheaply first
        if (project == null || project.isDisposed() || file.isDirectory() || !"xml".equalsIgnoreCase(file.getExtension())) {
            return null;
        }
        final Module module = ModuleUtilCore.findModuleForFile(file, project);
        if (!IntellijUtils.containsIvyIdeaFacet(module)) {
            return null;
        }
        final File ivyFile = IvyUtil.getIvyFile(module);
        return ivyFile != null && FileUtil.filesEqual(ivyFile, new File(file.getPath())) ? IvyIdeaIcons.IVYIDEA : null;
    }
}
