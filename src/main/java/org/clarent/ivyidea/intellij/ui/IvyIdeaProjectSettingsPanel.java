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
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import org.clarent.ivyidea.config.model.IvyIdeaProjectSettings;
import org.clarent.ivyidea.config.model.PropertiesSettings;
import org.clarent.ivyidea.config.ui.orderedfilelist.OrderedFileList;
import org.clarent.ivyidea.logging.IvyLogLevel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.List;

import static org.clarent.ivyidea.config.model.ArtifactTypeSettings.DependencyCategory.*;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaProjectSettingsPanel {

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
    private JTextField txtClassesArtifactTypes;
    private JTextField txtSourcesArtifactTypes;
    private JTextField txtJavadocArtifactTypes;
    private JCheckBox chkResolveTransitively;
    private JCheckBox chkUseCacheOnly;
    private JCheckBox chkBackground;
    private JCheckBox autoAttachSources;
    private JCheckBox autoAttachJavadocs;
    private JCheckBox avoidInternalModuleDependeciesResolving;
    private JPanel pnlIvyFiles;
    private JPanel pnlArtefactTypes;
    private IvyIdeaProjectSettings internalState;
    private OrderedFileList orderedFileList;
    private final Project project;

    public IvyIdeaProjectSettingsPanel(Project project, IvyIdeaProjectSettings state) {
        this.project = project;
        this.internalState = state;

        txtIvySettingsFile.addBrowseFolderListener("Select ivy settings file", null, project, new FileChooserDescriptor(true, false, false, false, false, false));

        wireActivityWatchers();
        wireIvySettingsRadioButtons();
    }

    private void wireIvySettingsRadioButtons() {
        useYourOwnIvySettingsRadioButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                txtIvySettingsFile.setEnabled(useYourOwnIvySettingsRadioButton.isSelected());
            }
        });
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
        internalState.setResolveTransitively(chkResolveTransitively.isSelected());
        internalState.setResolveCacheOnly(chkUseCacheOnly.isSelected());
        internalState.setResolveInBackground(chkBackground.isSelected());
        internalState.setAlwaysAttachSources(autoAttachSources.isSelected());
        internalState.setAlwaysAttachJavadocs(autoAttachJavadocs.isSelected());
        internalState.setUseCustomIvySettings(useYourOwnIvySettingsRadioButton.isSelected());
        internalState.setAvoidInternalModuleDependenciesResolving(avoidInternalModuleDependeciesResolving.isSelected());
        final PropertiesSettings propertiesSettings = new PropertiesSettings();
        propertiesSettings.setPropertyFiles(getPropertiesFiles());
        internalState.setPropertiesSettings(propertiesSettings);
        internalState.setLibraryNameIncludesModule(includeModuleNameCheckBox.isSelected());
        internalState.setLibraryNameIncludesConfiguration(includeConfigurationNameCheckBox.isSelected());
        final Object selectedLogLevel = ivyLogLevelComboBox.getSelectedItem();
        internalState.setIvyLogLevelThreshold(selectedLogLevel == null ? IvyLogLevel.None.name() : selectedLogLevel.toString());
        internalState.getArtifactTypeSettings().setTypesForCategory(Classes, txtClassesArtifactTypes.getText());
        internalState.getArtifactTypeSettings().setTypesForCategory(Sources, txtSourcesArtifactTypes.getText());
        internalState.getArtifactTypeSettings().setTypesForCategory(Javadoc, txtJavadocArtifactTypes.getText());
    }

    public void reset() {
        IvyIdeaProjectSettings config = internalState;
        if (config == null) {
            config = new IvyIdeaProjectSettings();
        }
        txtIvySettingsFile.setText(config.getIvySettingsFile());
        chkValidateIvyFiles.setSelected(config.isValidateIvyFiles());
        chkResolveTransitively.setSelected(config.isResolveTransitively());
        chkUseCacheOnly.setSelected(config.isResolveCacheOnly());
        chkBackground.setSelected(config.isResolveInBackground());
        autoAttachSources.setSelected(config.isAlwaysAttachSources());
        autoAttachJavadocs.setSelected(config.isAlwaysAttachJavadocs());
        useYourOwnIvySettingsRadioButton.setSelected(config.isUseCustomIvySettings());
        avoidInternalModuleDependeciesResolving.setSelected(config.isAvoidInternalModuleDependenciesResolving());
        setPropertiesFiles(config.getPropertiesSettings().getPropertyFiles());
        includeModuleNameCheckBox.setSelected(config.isLibraryNameIncludesModule());
        includeConfigurationNameCheckBox.setSelected(config.isLibraryNameIncludesConfiguration());
        ivyLogLevelComboBox.setSelectedItem(IvyLogLevel.fromName(config.getIvyLogLevelThreshold()));
        txtSourcesArtifactTypes.setText(config.getArtifactTypeSettings().getTypesStringForCategory(Sources));
        txtClassesArtifactTypes.setText(config.getArtifactTypeSettings().getTypesStringForCategory(Classes));
        txtJavadocArtifactTypes.setText(config.getArtifactTypeSettings().getTypesStringForCategory(Javadoc));
    }

    public void disposeUIResources() {
    }

    private void createUIComponents() {
        pnlPropertiesFiles = new JPanel(new BorderLayout());
        orderedFileList = new OrderedFileList(project);
        pnlPropertiesFiles.add(orderedFileList.getRootPanel(), BorderLayout.CENTER);
        ivyLogLevelComboBox = new JComboBox(IvyLogLevel.values());
    }
}
