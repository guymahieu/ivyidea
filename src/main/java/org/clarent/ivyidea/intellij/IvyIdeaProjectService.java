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
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.clarent.ivyidea.config.model.IvyIdeaProjectSettings;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * @author Guy Mahieu
 */
@State(
        name = IvyIdeaProjectService.COMPONENT_NAME,
        storages = {@Storage("$PROJECT_FILE$")}
)
public class IvyIdeaProjectService implements PersistentStateComponent<IvyIdeaProjectSettings> {

    public static final String COMPONENT_NAME = "IvyIDEA.ProjectSettings";

    private final IvyIdeaProjectSettings internalState;
    // whether the project file contained IvyIDEA settings; only remembered, never saved
    private volatile boolean savedSettingsLoaded;

    public IvyIdeaProjectService() {
        this.internalState = new IvyIdeaProjectSettings();
    }

    @NotNull
    public IvyIdeaProjectSettings getState() {
        return internalState;
    }

    public void loadState(@NotNull IvyIdeaProjectSettings state) {
        XmlSerializerUtil.copyBean(state, this.getState());
        // IntelliJ also loads the default settings, e.g. when the project file is reloaded without IvyIDEA settings
        final Element saved = XmlSerializer.serialize(state);
        final Element defaults = XmlSerializer.serialize(new IvyIdeaProjectSettings());
        if (!JDOMUtil.areElementsEqual(saved, defaults)) {
            savedSettingsLoaded = true;
        }
    }

    /**
     * Returns whether the IvyIDEA settings were loaded from the project file, which only contains them when they
     * differ from the defaults, so the project used IvyIDEA before.
     */
    public boolean isSavedSettingsLoaded() {
        return savedSettingsLoaded;
    }
}
