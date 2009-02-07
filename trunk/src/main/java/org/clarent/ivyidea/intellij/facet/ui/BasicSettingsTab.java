package org.clarent.ivyidea.intellij.facet.ui;

import com.intellij.facet.Facet;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import org.apache.commons.lang.StringUtils;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.settings.IvySettings;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.intellij.facet.ui.components.ConfigurationSelectionTable;
import org.clarent.ivyidea.intellij.facet.ui.components.ConfigurationSelectionTableModel;
import org.clarent.ivyidea.ivy.IvyUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
    private FacetEditorContext editorContext;
    private final PropertiesSettingsTab propertiesSettingsTab;
    private boolean modified;
    private boolean foundConfigsBefore = false;

    private Set<Configuration> selectedConfigurations = new HashSet<Configuration>();

    public BasicSettingsTab(@NotNull FacetEditorContext editorContext, @NotNull PropertiesSettingsTab propertiesSettingsTab) {
        this.editorContext = editorContext;
        this.propertiesSettingsTab = propertiesSettingsTab;

        UserActivityWatcher watcher = new UserActivityWatcher();
        watcher.addUserActivityListener(new UserActivityListener() {
            public void stateChanged() {
                modified = true;
            }
        });
        watcher.register(pnlRoot);

        txtIvyFile.addBrowseFolderListener("Select ivy file", "", editorContext.getProject(), new FileChooserDescriptor(true, false, false, false, false, false));
        txtIvySettingsFile.addBrowseFolderListener("Select ivy settings file", "", editorContext.getProject(), new FileChooserDescriptor(true, false, false, false, false, false));

        txtIvyFile.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            public void textChanged(DocumentEvent e) {
                reloadIvyFile();
            }
        });
        chkOverrideProjectIvySettings.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateIvySettingsUIState();
            }
        });

        chkOnlyResolveSpecificConfigs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateConfigurationsTable();
            }
        });
               
        rbnUseCustomIvySettings.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateIvySettingsFileTextfield();
            }
        });
    }

    private void updateUI() {
        updateIvySettingsFileTextfield();
        updateConfigurationsTable();
        updateIvySettingsUIState();
        reloadIvyFile();
    }

    private void updateIvySettingsFileTextfield() {
        txtIvySettingsFile.setEnabled(chkOverrideProjectIvySettings.isSelected() && rbnUseCustomIvySettings.isSelected());
    }

    private void updateConfigurationsTable() {
        tblConfigurationSelection.setEnabled(chkOnlyResolveSpecificConfigs.isSelected());
    }

    private void updateIvySettingsUIState() {
        rbnUseCustomIvySettings.setEnabled(chkOverrideProjectIvySettings.isSelected());
        rbnUseDefaultIvySettings.setEnabled(chkOverrideProjectIvySettings.isSelected());
        updateIvySettingsFileTextfield();
    }

    @Override
    public void onTabEntering() {
        // TODO: this does not seem to work!!!
//        if (propertiesSettingsTab.isModified()) {
            reloadIvyFile();
//        }
    }

    public void reloadIvyFile() {
        final Set<Configuration> allConfigurations;
        try {
            allConfigurations = IvyUtil.loadConfigurations(txtIvyFile.getText(), getIvySettings());
            chkOnlyResolveSpecificConfigs.setEnabled(allConfigurations != null);
            if (allConfigurations != null) {
                LOGGER.info("Detected configs in file " + txtIvyFile.getText() + ": " + allConfigurations.toString());
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
            lblIvyFileMessage.setText("Error parsing the file. If you use properties or specific ivy settings, configure those first.");
        }
    }

    @NotNull
    private IvySettings getIvySettings() {
        try {
            final Properties properties = getPropertiesForCurrentSettingsInUI();
            String ivySettingsFile = getIvySettingsFileNameForCurrentSettingsInUI();
            return IvyIdeaConfigHelper.createConfiguredIvySettings(editorContext.getModule(), ivySettingsFile, properties);
        } catch (Exception e) {
            return new IvySettings();
        }
    }

    @Nullable
    private String getIvySettingsFileNameForCurrentSettingsInUI() throws IvySettingsNotFoundException {
        if (chkOverrideProjectIvySettings.isSelected()) {
            final File projectIvySettingsFile = IvyIdeaConfigHelper.getProjectIvySettingsFile(editorContext.getProject());
            if (projectIvySettingsFile != null) {
                return projectIvySettingsFile.getAbsolutePath();
            } else {
                return null;
            }
        } else {
            if (rbnUseCustomIvySettings.isSelected())  {
                return txtIvySettingsFile.getTextField().getText();
            } else {
                return null;
            }
        }
    }

    private Properties getPropertiesForCurrentSettingsInUI() throws IvySettingsNotFoundException, IvySettingsFileReadException {
        final Properties properties;
        if (propertiesSettingsTab.isAlreadyOpenedBefore()) {
            final List<String> propertiesFiles = new ArrayList<String>(propertiesSettingsTab.getFileNames());
            // TODO: only include the project properties files if this option is chosen on the screen.
            //          for now this is not configurable yet - so it always is true
            boolean includeProjectProperties = true;
            //noinspection ConstantConditions
            if (includeProjectProperties) {
                propertiesFiles.addAll(IvyIdeaConfigHelper.getPropertiesFiles(editorContext.getProject()));
            }
            properties = IvyIdeaConfigHelper.loadProperties(editorContext.getModule(), propertiesFiles);
        } else {
            properties = IvyIdeaConfigHelper.getIvyProperties(editorContext.getModule());
        }
        return properties;
    }

    @Nls
    public String getDisplayName() {
        return "General";
    }

    public JComponent createComponent() {
        return pnlRoot;
    }

    public boolean isModified() {
        return modified;
    }

    public void apply() throws ConfigurationException {
        final Facet facet = editorContext.getFacet();
        if (facet != null) {
            IvyIdeaFacetConfiguration configuration = (IvyIdeaFacetConfiguration) facet.getConfiguration();
            configuration.setUseProjectSettings(!chkOverrideProjectIvySettings.isSelected());
            configuration.setUseCustomIvySettings(rbnUseCustomIvySettings.isSelected());
            configuration.setIvySettingsFile(txtIvySettingsFile.getText());
            configuration.setOnlyResolveSelectedConfigs(chkOnlyResolveSpecificConfigs.isSelected());
            configuration.setConfigsToResolve(getNames(tblConfigurationSelection.getSelectedConfigurations()));
            configuration.setIvyFile(txtIvyFile.getText());
        }
    }

    @NotNull
    private static Set<String> getNames(@NotNull Set<Configuration> selectedConfigurations) {
        Set<String> result = new HashSet<String>();
        for (Configuration selectedConfiguration : selectedConfigurations) {
            result.add(selectedConfiguration.getName());
        }
        return result;
    }

    public void reset() {
        final Facet facet = editorContext.getFacet();
        if (facet != null) {
            IvyIdeaFacetConfiguration configuration = (IvyIdeaFacetConfiguration) facet.getConfiguration();
            txtIvyFile.setText(configuration.getIvyFile());
            chkOverrideProjectIvySettings.setSelected(!configuration.isUseProjectSettings());
            txtIvySettingsFile.setText(configuration.getIvySettingsFile());
            chkOnlyResolveSpecificConfigs.setSelected(configuration.isOnlyResolveSelectedConfigs());
            rbnUseCustomIvySettings.setSelected(configuration.isUseCustomIvySettings());
            rbnUseDefaultIvySettings.setSelected(!configuration.isUseCustomIvySettings());
            Set<Configuration> allConfigurations;
            try {
                allConfigurations = IvyUtil.loadConfigurations(configuration.getIvyFile(), getIvySettings());
            } catch (ParseException e) {
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
                selectedConfigurations = new HashSet<Configuration>();
                tblConfigurationSelection.setEnabled(false);
            }
            updateUI();
        }
    }

    public void disposeUIResources() {
    }

    private void createUIComponents() {
    }


}
