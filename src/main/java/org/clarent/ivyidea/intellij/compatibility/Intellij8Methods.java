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

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.OrderRootType;

import java.lang.reflect.InvocationTargetException;

/**
 * Compatibility implementation for Intellij IDEA 8.0 (and possibly later versions).
 *
 * @author Guy Mahieu
 */
class Intellij8Methods implements IntellijCompatibilityMethods {

    private static final String COMPAT_ERROR_MSG = "Compatibility problem: this plugin is only meant to be used in IntelliJ version 7 and up.";

    public OrderRootType[] getAllOrderRootTypes() {
        try {
            // OrderRootType.getAllTypes()
            return (OrderRootType[]) OrderRootType.class.getMethod("getAllTypes").invoke(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        }
    }

    public OrderRootType getJavadocOrderRootType() {
        try {
            final Class<?> javadocOrderRootTypeClass = Class.forName("com.intellij.openapi.roots.JavadocOrderRootType");
            return (OrderRootType) javadocOrderRootTypeClass.getMethod("getInstance").invoke(null);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        }
    }

    public ModuleType getJavaModuleType() {
        try {
            final Class<?> stdModuleTypesClass = Class.forName("com.intellij.openapi.module.StdModuleTypes");
            return (ModuleType) stdModuleTypesClass.getField("JAVA").get(null);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(COMPAT_ERROR_MSG, e);
        }
    }

    public boolean isTaskCancelledOnProgressIndicatorCancel() {
        return true;
    }
}