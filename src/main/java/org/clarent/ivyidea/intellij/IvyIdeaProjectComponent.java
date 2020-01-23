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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.clarent.ivyidea.config.model.IvyIdeaProjectSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Guy Mahieu
 */

@State(
        name = IvyIdeaProjectComponent.COMPONENT_NAME,
        storages = {@Storage(file = "$PROJECT_FILE$")}
)
public class IvyIdeaProjectComponent implements ProjectComponent, PersistentStateComponent<IvyIdeaProjectSettings> {

    public static final String COMPONENT_NAME = "IvyIDEA.ProjectSettings";

    private final Project project;
    private final IvyIdeaProjectSettings internalState;

    public IvyIdeaProjectComponent(Project project) {
        this.project = project;
        this.internalState = new IvyIdeaProjectSettings();
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public IvyIdeaProjectSettings getState() {
        return internalState;
    }

    public void loadState(IvyIdeaProjectSettings state) {
        XmlSerializerUtil.copyBean(state, this.getState());
    }
}
