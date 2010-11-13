/*
 * Copyright 2009 Guy Mahieu
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
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import org.clarent.ivyidea.config.model.IvyIdeaProjectSettings;
import org.clarent.ivyidea.config.model.PropertiesSettings;
import org.clarent.ivyidea.config.ui.orderedfilelist.OrderedFileList;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.logging.IvyLogLevel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.List;

/**
 * @author Guy Mahieu
 */

@State(
        name = IvyIdeaProjectSettingsComponent.COMPONENT_NAME,
        storages = {@Storage(id = "IvyIDEA", file = "$PROJECT_FILE$")}
)
public class IvyIdeaProjectSettingsComponent implements ProjectComponent, Configurable, PersistentStateComponent<IvyIdeaProjectSettings> {

    public static final String COMPONENT_NAME = "IvyIDEA.ProjectSettings";

    private boolean modified;
    private TextFieldWithBrowseButton txtIvySettingsFile;
    private JPanel projectSettingsPanel;
    private JCheckBox chkValidateIvyFiles;
    private JTabbedPane tabbedPane1;
    private JLabel lblIvySettingsErrorMessage;
    private JRadioButton useIvyDefaultRadioButton;
    private JRadioButton useYourOwnIvySettingsRadioButton;
    private JPanel pnlPropertiesFiles;
    private JComboBox ivyLogLevelComboBox;
    private JCheckBox includeModuleNameCheckBox;
    private JCheckBox includeConfigurationNameCheckBox;
    private JPanel pnlIvyLogging;
    private JPanel pnlLibraryNaming;
    private IvyIdeaProjectSettings internalState;
    private OrderedFileList orderedFileList;

    public IvyIdeaProjectSettingsComponent() {
        internalState = new IvyIdeaProjectSettings();
        wireActivityWatchers();
        wireIvySettingsTextbox();
        wireIvySettingsRadioButtons();
    }

    private void wireIvySettingsRadioButtons() {
        useYourOwnIvySettingsRadioButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                txtIvySettingsFile.setEnabled(useYourOwnIvySettingsRadioButton.isSelected());
            }
        });
    }

    private void wireIvySettingsTextbox() {
        txtIvySettingsFile.addBrowseFolderListener("Select ivy settings file", "", IntellijUtils.getCurrentProject(), new FileChooserDescriptor(true, false, false, false, false, false));
        txtIvySettingsFile.setEnabled(useYourOwnIvySettingsRadioButton.isSelected());
    }

    private void wireActivityWatchers() {
        UserActivityWatcher watcher = new UserActivityWatcher();
        watcher.addUserActivityListener(new UserActivityListener() {
            public void stateChanged() {
                modified = true;
            }
        });
        watcher.register(projectSettingsPanel);
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
        return projectSettingsPanel;
    }

    public boolean isModified() {
        return modified;
    }

    private List<String> getPropertiesFiles() {
        return orderedFileList.getFileNames();
    }

    private void setPropertiesFiles(List<String> fileNames) {
        orderedFileList.setFileNames(fileNames);
    }

    public void apply() throws ConfigurationException {
        if (internalState == null) {
            internalState = new IvyIdeaProjectSettings();
        }
        internalState.setIvySettingsFile(txtIvySettingsFile.getText());
        internalState.setValidateIvyFiles(chkValidateIvyFiles.isSelected());
        internalState.setUseCustomIvySettings(useYourOwnIvySettingsRadioButton.isSelected());
        final PropertiesSettings propertiesSettings = new PropertiesSettings();
        propertiesSettings.setPropertyFiles(getPropertiesFiles());
        internalState.setPropertiesSettings(propertiesSettings);
        internalState.setLibraryNameIncludesModule(includeModuleNameCheckBox.isSelected());
        internalState.setLibraryNameIncludesConfiguration(includeConfigurationNameCheckBox.isSelected());
        final Object selectedLogLevel = ivyLogLevelComboBox.getSelectedItem();
        internalState.setIvyLogLevelThreshold(selectedLogLevel == null ? IvyLogLevel.None.name() : selectedLogLevel.toString());
    }

    public void reset() {
        IvyIdeaProjectSettings config = internalState;
        if (config == null) {
            config = new IvyIdeaProjectSettings();
        }
        txtIvySettingsFile.setText(config.getIvySettingsFile());
        chkValidateIvyFiles.setSelected(config.isValidateIvyFiles());
        useYourOwnIvySettingsRadioButton.setSelected(config.isUseCustomIvySettings());
        setPropertiesFiles(config.getPropertiesSettings().getPropertyFiles());
        includeModuleNameCheckBox.setSelected(config.isLibraryNameIncludesModule());
        includeConfigurationNameCheckBox.setSelected(config.isLibraryNameIncludesConfiguration());
        ivyLogLevelComboBox.setSelectedItem(IvyLogLevel.fromName(config.getIvyLogLevelThreshold()));
    }

    public void disposeUIResources() {
    }

    @NotNull
    public IvyIdeaProjectSettings getState() {
        return internalState;
    }

    public void loadState(IvyIdeaProjectSettings state) {
        if (state == null) {
            state = new IvyIdeaProjectSettings();
        }
        this.internalState = IvyIdeaProjectSettings.copyDataFrom(state);
    }

    private void createUIComponents() {
        pnlPropertiesFiles = new JPanel(new BorderLayout());
        orderedFileList = new OrderedFileList(IntellijUtils.getCurrentProject());
        pnlPropertiesFiles.add(orderedFileList.getRootPanel(), BorderLayout.CENTER);
        ivyLogLevelComboBox = new JComboBox(IvyLogLevel.values());
    }
}
