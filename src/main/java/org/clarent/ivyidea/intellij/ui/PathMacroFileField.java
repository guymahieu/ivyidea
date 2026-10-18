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

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.clarent.ivyidea.config.IvyIdeaPaths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

/**
 * Sets up a text field for a file in the IvyIDEA settings, whose path can contain path macros: the file chooser opens
 * at the file the path names.
 */
public final class PathMacroFileField {

    private PathMacroFileField() {
    }

    /**
     * Sets up the given field for a file in the settings of the given module.
     */
    public static void install(@NotNull TextFieldWithBrowseButton field, @NotNull String title, @NotNull Module module) {
        install(field, title, module.getProject(), path -> IvyIdeaPaths.expand(module, path),
                "<html>The path can contain path macros, like $MODULE_DIR$ and $PROJECT_DIR$, and path variables.<br>A relative path is relative to the module directory.</html>");
    }

    /**
     * Sets up the given field for a file in the project settings.
     */
    public static void install(@NotNull TextFieldWithBrowseButton field, @NotNull String title, @NotNull Project project) {
        install(field, title, project, path -> IvyIdeaPaths.expand(project, path),
                "<html>The path can contain path macros, like $PROJECT_DIR$, and path variables.<br>A relative path is relative to the project directory.</html>");
    }

    private static void install(@NotNull TextFieldWithBrowseButton field, @NotNull String title, @NotNull Project project,
                                @NotNull UnaryOperator<String> pathExpander, @NotNull String toolTip) {
        final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false).withTitle(title);
        field.addBrowseFolderListener(new PathMacroBrowseFolderListener(descriptor, project, pathExpander));
        field.getTextField().setToolTipText(toolTip);
    }

    /**
     * Opens the file chooser at the file the text field names, also when its path contains path macros.
     */
    private static class PathMacroBrowseFolderListener extends TextBrowseFolderListener {

        private final UnaryOperator<String> pathExpander;

        PathMacroBrowseFolderListener(@NotNull FileChooserDescriptor descriptor, @Nullable Project project, @NotNull UnaryOperator<String> pathExpander) {
            super(descriptor, project);
            this.pathExpander = pathExpander;
        }

        @NotNull
        @Override
        @SuppressWarnings("UnstableApiUsage") // the only way to let the file chooser expand path macros
        protected String expandPath(@NotNull String path) {
            final String expanded = pathExpander.apply(path);
            return expanded == null ? path : expanded;
        }
    }
}
