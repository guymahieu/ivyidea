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

import com.intellij.icons.AllIcons;
import com.intellij.ide.macro.MacrosDialog;
import com.intellij.openapi.application.PathMacros;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.fields.ExtendableTextField;
import org.clarent.ivyidea.config.IvyIdeaPaths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.serialization.PathMacroUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

/**
 * Sets up a text field for a file in the IvyIDEA settings, whose path can contain path macros: the file chooser opens
 * at the file the path names, and a button in the field inserts a path macro.
 */
public final class PathMacroFileField {

    private PathMacroFileField() {
    }

    /**
     * Sets up the given field for a file in the settings of the given module.
     */
    public static void install(@NotNull TextFieldWithBrowseButton field, @NotNull String title, @NotNull Module module) {
        final Map<String, String> macros = new LinkedHashMap<>();
        macros.put(PathMacroUtil.MODULE_DIR_MACRO_NAME, ModuleUtilCore.getModuleDirPath(module));
        macros.put(PathMacroUtil.PROJECT_DIR_MACRO_NAME, module.getProject().getBasePath());
        install(field, title, module.getProject(), path -> IvyIdeaPaths.expand(module, path), macros,
                "The path can contain path macros and path variables. A relative path is relative to the module directory.");
    }

    /**
     * Sets up the given field for a file in the project settings.
     */
    public static void install(@NotNull TextFieldWithBrowseButton field, @NotNull String title, @NotNull Project project) {
        final Map<String, String> macros = new LinkedHashMap<>();
        macros.put(PathMacroUtil.PROJECT_DIR_MACRO_NAME, project.getBasePath());
        install(field, title, project, path -> IvyIdeaPaths.expand(project, path), macros,
                "The path can contain path macros and path variables. A relative path is relative to the project directory.");
    }

    private static void install(@NotNull TextFieldWithBrowseButton field, @NotNull String title, @NotNull Project project,
                                @NotNull UnaryOperator<String> pathExpander, @NotNull Map<String, String> macros, @NotNull String toolTip) {
        final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false).withTitle(title);
        field.addBrowseFolderListener(new PathMacroBrowseFolderListener(descriptor, project, pathExpander));
        field.getTextField().setToolTipText(toolTip);

        // the field of a form is an ExtendableTextField, unless the form creates it differently
        if (field.getTextField() instanceof ExtendableTextField) {
            final ExtendableTextField textField = (ExtendableTextField) field.getTextField();
            // the icon of the macro button in the run configurations
            textField.addExtension(ExtendableTextComponent.Extension.create(AllIcons.General.InlineVariables, AllIcons.General.InlineVariablesHover,
                    "Insert Macros", () -> showMacrosDialog(textField, macros)));
        }
    }

    private static void showMacrosDialog(@NotNull ExtendableTextField textField, @NotNull Map<String, String> macros) {
        final Map<String, String> allMacros = new LinkedHashMap<>(macros);
        allMacros.put(PathMacroUtil.USER_HOME_NAME, PathMacroUtil.getUserHomePath());
        allMacros.putAll(new TreeMap<>(PathMacros.getInstance().getUserMacros()));
        final Map<String, String> previews = new LinkedHashMap<>();
        allMacros.forEach((name, path) -> previews.put(name, path == null ? "" : FileUtil.toSystemDependentName(path)));
        // only the path macros: IvyIDEA doesn't expand the other macros, like $FileDir$, which depend on the editor
        MacrosDialog.show(textField, macro -> false, previews);
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
        protected String expandPath(@NotNull String path) {
            final String expanded = pathExpander.apply(path);
            return expanded == null ? path : expanded;
        }
    }
}
