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

package org.clarent.ivyidea.intellij.facet.ui;

import com.intellij.facet.Facet;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.UserActivityWatcher;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.settings.IvySettings;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.intellij.facet.ui.components.ConfigurationSelectionTable;
import org.clarent.ivyidea.intellij.facet.ui.components.ConfigurationSelectionTableModel;
import org.clarent.ivyidea.ivy.IvyUtil;
import org.clarent.ivyidea.util.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.io.File;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public class BasicSettingsTab extends FacetEditorTab {

    private static final Logger LOGGER = Logger.getLogger(BasicSettingsTab.class.getName());

    private com.intellij.openapi.ui.TextFieldWithBrowseButton txtIvyFile;
    private JPanel pnlRoot;
    private JCheckBox chkOverrideProjectIvySettings;
    private TextFieldWithBrowseButton txtIvySettingsFile;
    private JCheckBox chkOnlyResolveSpecificConfigs;
    private ConfigurationSelectionTable tblConfigurationSelection;
    private JLabel lblIvyFileMessage;
    private JRadioButton rbnUseDefaultIvySettings;
    private JRadioButton rbnUseCustomIvySettings;
    private final FacetEditorContext editorContext;
    private final PropertiesSettingsTab propertiesSettingsTab;
    private boolean modified;
    private boolean foundConfigsBefore = false;

    private Set<Configuration> selectedConfigurations = new HashSet<>();

    public BasicSettingsTab(@NotNull FacetEditorContext editorContext, @NotNull PropertiesSettingsTab propertiesSettingsTab) {
        this.editorContext = editorContext;
        this.propertiesSettingsTab = propertiesSettingsTab;
        this.propertiesSettingsTab.reset();

        UserActivityWatcher watcher = new UserActivityWatcher();
        watcher.addUserActivityListener(() -> modified = true);
        watcher.register(pnlRoot);

        txtIvyFile.addBrowseFolderListener("Select Ivy File", "", editorContext.getProject(), new FileChooserDescriptor(true, false, false, false, false, false));
        txtIvySettingsFile.addBrowseFolderListener("Select Ivy Settings File", "", editorContext.getProject(), new FileChooserDescriptor(true, false, false, false, false, false));

        txtIvyFile.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            public void textChanged(@NotNull DocumentEvent e) {
                reloadIvyFile();
            }
        });
        chkOverrideProjectIvySettings.addChangeListener(e -> updateIvySettingsUIState());

        chkOnlyResolveSpecificConfigs.addChangeListener(e -> updateConfigurationsTable());
               
        rbnUseCustomIvySettings.addChangeListener(e -> updateIvySettingsFileTextField());
    }

    private void updateUI() {
        updateIvySettingsFileTextField();
        updateConfigurationsTable();
        updateIvySettingsUIState();
        reloadIvyFile();
    }

    private void updateIvySettingsFileTextField() {
        txtIvySettingsFile.setEnabled(chkOverrideProjectIvySettings.isSelected() && rbnUseCustomIvySettings.isSelected());
    }

    private void updateConfigurationsTable() {
        tblConfigurationSelection.setEditable(chkOnlyResolveSpecificConfigs.isSelected());
    }

    private void updateIvySettingsUIState() {
        rbnUseCustomIvySettings.setEnabled(chkOverrideProjectIvySettings.isSelected());
        rbnUseDefaultIvySettings.setEnabled(chkOverrideProjectIvySettings.isSelected());
        updateIvySettingsFileTextField();
    }

    @Override
    public void onTabEntering() {
        reloadIvyFile();
    }

    public void reloadIvyFile() {
        final Set<Configuration> allConfigurations;
        try {
            allConfigurations = loadConfigurations();
            chkOnlyResolveSpecificConfigs.setEnabled(allConfigurations != null);
            if (allConfigurations != null) {
                LOGGER.info("Detected configs in file " + txtIvyFile.getText() + ": " + allConfigurations);
                tblConfigurationSelection.setModel(new ConfigurationSelectionTableModel(allConfigurations, getNames(selectedConfigurations)));
                lblIvyFileMessage.setText("");
                foundConfigsBefore = true;
            } else {
                File ivyFile = new File(txtIvyFile.getText());
                if (ivyFile.isDirectory() || !ivyFile.exists()) {
                    lblIvyFileMessage.setText("Please enter the name of an existing ivy file.");
                } else {
                    lblIvyFileMessage.setText("Warning: No configurations could be found in the given ivy file");
                }
                if (foundConfigsBefore) {
                    selectedConfigurations = tblConfigurationSelection.getSelectedConfigurations();
                }
                tblConfigurationSelection.setModel(new ConfigurationSelectionTableModel());
                foundConfigsBefore = false;
            }
        } catch (ParseException e1) {
            // TODO: provide link to error display dialog with full exception
            lblIvyFileMessage.setText("Error parsing the file. If you use properties or specific ivy settings, configure those first.");
        } catch (IvySettingsNotFoundException e) {
            lblIvyFileMessage.setText("Could not find the settings file. Configure the settings file here or in the project settings first.");
        } catch (IvySettingsFileReadException e) {
            lblIvyFileMessage.setText("Error parsing the settings file. If you use properties, configure those first.");
        }
    }

    private Set<Configuration> loadConfigurations() throws IvySettingsNotFoundException, IvySettingsFileReadException, ParseException {
        return IvyUtil.loadConfigurations(txtIvyFile.getText(), createIvyEngineForCurrentSettingsInUI());
    }

    @NotNull
    private Ivy createIvyEngineForCurrentSettingsInUI() throws IvySettingsNotFoundException, IvySettingsFileReadException {
        final Module module = this.editorContext.getModule();
        final IvySettings ivySettings = IvyIdeaConfigHelper.createConfiguredIvySettings(module, this.getIvySettingsFileNameForCurrentSettingsInUI(), getPropertiesForCurrentSettingsInUI());
        return IvyUtil.createConfiguredIvyEngine(module, ivySettings);
    }

    @Nullable
    private String getIvySettingsFileNameForCurrentSettingsInUI() throws IvySettingsNotFoundException {
        if (chkOverrideProjectIvySettings.isSelected()) {
            if (rbnUseCustomIvySettings.isSelected())  {
                return txtIvySettingsFile.getTextField().getText();
            }
        } else {
            return IvyIdeaConfigHelper.getProjectIvySettingsFile(editorContext.getProject());
        }
        return null;
    }

    private Properties getPropertiesForCurrentSettingsInUI() throws IvySettingsNotFoundException, IvySettingsFileReadException {
        final List<String> propertiesFiles = new ArrayList<>(propertiesSettingsTab.getFileNames());
        // TODO: only include the project properties files if this option is chosen on the screen.
        //          for now this is not configurable yet - so it always is true
        boolean includeProjectProperties = true;
        //noinspection ConstantConditions
        if (includeProjectProperties) {
            propertiesFiles.addAll(IvyIdeaConfigHelper.getPropertiesFiles(editorContext.getProject()));
        }
        return IvyIdeaConfigHelper.loadProperties(editorContext.getModule(), propertiesFiles);
    }

    @Nls
    public String getDisplayName() {
        return "General";
    }

    @NotNull
    public JComponent createComponent() {
        return pnlRoot;
    }

    public boolean isModified() {
        return modified;
    }

    public void apply() {
        @SuppressWarnings("unchecked") final Facet<IvyIdeaFacetConfiguration> facet = (Facet<IvyIdeaFacetConfiguration>) editorContext.getFacet();
        IvyIdeaFacetConfiguration configuration = facet.getConfiguration();
        configuration.setUseProjectSettings(!chkOverrideProjectIvySettings.isSelected());
        configuration.setUseCustomIvySettings(rbnUseCustomIvySettings.isSelected());
        configuration.setIvySettingsFile(txtIvySettingsFile.getText());
        configuration.setOnlyResolveSelectedConfigs(chkOnlyResolveSpecificConfigs.isSelected());
        configuration.setConfigsToResolve(getNames(tblConfigurationSelection.getSelectedConfigurations()));
        configuration.setIvyFile(txtIvyFile.getText());
    }

    @NotNull
    private static Set<String> getNames(@NotNull Set<Configuration> selectedConfigurations) {
        Set<String> result = new TreeSet<>();
        for (Configuration selectedConfiguration : selectedConfigurations) {
            result.add(selectedConfiguration.getName());
        }
        return result;
    }

    public void reset() {
        @SuppressWarnings("unchecked") final Facet<IvyIdeaFacetConfiguration> facet = (Facet<IvyIdeaFacetConfiguration>) editorContext.getFacet();
        IvyIdeaFacetConfiguration configuration = facet.getConfiguration();
        txtIvyFile.setText(configuration.getIvyFile());
        chkOverrideProjectIvySettings.setSelected(!configuration.isUseProjectSettings());
        txtIvySettingsFile.setText(configuration.getIvySettingsFile());
        chkOnlyResolveSpecificConfigs.setSelected(configuration.isOnlyResolveSelectedConfigs());
        rbnUseCustomIvySettings.setSelected(configuration.isUseCustomIvySettings());
        rbnUseDefaultIvySettings.setSelected(!configuration.isUseCustomIvySettings());
        Set<Configuration> allConfigurations;
        try {
            allConfigurations = loadConfigurations();
        } catch (ParseException | IvySettingsNotFoundException | IvySettingsFileReadException e) {
            allConfigurations = null;
        }
        if (StringUtils.isNotBlank(configuration.getIvyFile())) {
            if (allConfigurations != null) {
                tblConfigurationSelection.setModel(new ConfigurationSelectionTableModel(allConfigurations, configuration.getConfigsToResolve()));
            } else {
                tblConfigurationSelection.setModel(new ConfigurationSelectionTableModel());
            }
            selectedConfigurations = tblConfigurationSelection.getSelectedConfigurations();
            updateConfigurationsTable();
        } else {
            tblConfigurationSelection.setModel(new ConfigurationSelectionTableModel());
            selectedConfigurations = new HashSet<>();
            tblConfigurationSelection.setEditable(false);
        }
        updateUI();
    }

    private void createUIComponents() {
    }


}
