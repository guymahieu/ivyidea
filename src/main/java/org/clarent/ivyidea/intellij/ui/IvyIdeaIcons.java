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

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * Helper interface for easy access to icons used in IvyIDEA.
 *
 * @author Guy Mahieu
 */

public interface IvyIdeaIcons {

    Icon MAIN_ICON_SMALL = IconLoader.findIcon("/ivyidea13.png", IvyIdeaIcons.class.getClassLoader());
    Icon MAIN_ICON = IconLoader.findIcon("/ivyidea32.png", IvyIdeaIcons.class.getClassLoader());

//    public static final Icon ERROR_ICON = IconLoader.findIcon("/compiler/error.png");

}
