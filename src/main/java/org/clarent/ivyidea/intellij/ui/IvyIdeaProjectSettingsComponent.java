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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.clarent.ivyidea.config.model.IvyIdeaProjectSettings;
import org.clarent.ivyidea.intellij.IvyIdeaProjectComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Guy Mahieu
 */
public class IvyIdeaProjectSettingsComponent implements Configurable {

    private final Project project;

    private IvyIdeaProjectSettingsPanel settingsPanel;

    public IvyIdeaProjectSettingsComponent(Project project) {
        this.project = project;
    }

    @Nls
    public String getDisplayName() {
        return "IvyIDEA";
    }

    @Nullable
    public Icon getIcon() {
        return IvyIdeaIcons.MAIN_ICON;
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        return getSettingsPanel().createComponent();
    }

    private IvyIdeaProjectSettingsPanel getSettingsPanel() {
        if (settingsPanel == null) {
            IvyIdeaProjectSettings state = project.getComponent(IvyIdeaProjectComponent.class).getState();
            settingsPanel = new IvyIdeaProjectSettingsPanel(project, state);
        }
        return settingsPanel;
    }

    public boolean isModified() {
        return getSettingsPanel().isModified();
    }

    public void apply() throws ConfigurationException {
        getSettingsPanel().apply();
    }

    public void reset() {
        getSettingsPanel().reset();
    }

    public void disposeUIResources() {
        settingsPanel = null;
    }
}
