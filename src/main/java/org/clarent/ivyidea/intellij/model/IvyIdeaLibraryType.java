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

package org.clarent.ivyidea.intellij.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.DummyLibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryType;
import com.intellij.openapi.roots.libraries.NewLibraryConfiguration;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import com.intellij.openapi.roots.libraries.ui.LibraryEditorComponent;
import com.intellij.openapi.roots.libraries.ui.LibraryPropertiesEditor;
import com.intellij.openapi.vfs.VirtualFile;
import org.clarent.ivyidea.intellij.ui.IvyIdeaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * The type of the libraries that IvyIDEA creates, which gives them the IvyIDEA icon. The build of IntelliJ doesn't
 * know this type and treats them as plain Java libraries.
 */
public class IvyIdeaLibraryType extends LibraryType<DummyLibraryProperties> {

    public static final PersistentLibraryKind<DummyLibraryProperties> KIND = new PersistentLibraryKind<>("ivyidea") {
        @NotNull
        @Override
        public DummyLibraryProperties createDefaultProperties() {
            return DummyLibraryProperties.INSTANCE;
        }
    };

    public IvyIdeaLibraryType() {
        super(KIND);
    }

    /**
     * Not offered when creating a library by hand: only a resolve creates these libraries.
     */
    @Nullable
    @Override
    public String getCreateActionName() {
        return null;
    }

    @Nullable
    @Override
    public NewLibraryConfiguration createNewLibrary(@NotNull JComponent parentComponent, @Nullable VirtualFile contextDirectory,
                                                    @NotNull Project project) {
        return null;
    }

    @Nullable
    @Override
    public LibraryPropertiesEditor createPropertiesEditor(@NotNull LibraryEditorComponent<DummyLibraryProperties> editorComponent) {
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon(@Nullable DummyLibraryProperties properties) {
        return IvyIdeaIcons.IVYIDEA;
    }
}
