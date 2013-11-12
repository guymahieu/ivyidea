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

package org.clarent.ivyidea.intellij.compatibility;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VirtualFile;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Compatibility implementation for Intellij IDEA 7.0 and earlier.
 *
 * @author Guy Mahieu
 */

class Intellij7Methods implements IntellijCompatibilityMethods {

    private static final String COMPAT_ERROR_MSG = "Compatibility problem: this plugin is only meant to be used in IntelliJ version 7 and up.";

    public OrderRootType[] getAllOrderRootTypes() {
        try {
            // OrderRootType.ALL_TYPES
            return (OrderRootType[]) OrderRootType.class.getField("ALL_TYPES").get(null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        }
    }

    public OrderRootType getJavadocOrderRootType() {
        try {
            // OrderRootType.JAVADOC
            return (OrderRootType) OrderRootType.class.getField("JAVADOC").get(null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        }
    }

    public ModuleType getJavaModuleType() {
        try {
            // ModuleType.JAVA
            return (ModuleType) ModuleType.class.getField("JAVA").get(null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        }
    }

    public boolean isTaskCancelledOnProgressIndicatorCancel() {
        return false;
    }

    public VirtualFile[] chooseFiles(FileChooserDescriptor descriptor, Component parent, Project project, VirtualFile toSelect) {
        if (parent == null) {
            try {
                Method chooseFilesMethod = FileChooser.class.getMethod("chooseFiles", Project.class, FileChooserDescriptor.class, VirtualFile.class);
                return (VirtualFile[]) chooseFilesMethod.invoke(null, project, descriptor, toSelect);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(COMPAT_ERROR_MSG, e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(COMPAT_ERROR_MSG, e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(COMPAT_ERROR_MSG, e);
            }
        } else {
            try {
                Method chooseFilesMethod = FileChooser.class.getMethod("chooseFiles", Component.class, FileChooserDescriptor.class, VirtualFile.class);
                return (VirtualFile[]) chooseFilesMethod.invoke(null, parent, descriptor, toSelect);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(COMPAT_ERROR_MSG, e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(COMPAT_ERROR_MSG, e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(COMPAT_ERROR_MSG, e);
            }
        }
    }
}
