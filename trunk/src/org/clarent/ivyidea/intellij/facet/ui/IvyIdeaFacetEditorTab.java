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
import org.apache.ivy.core.module.descriptor.Configuration;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.intellij.facet.ui.components.ConfigurationSelectionTable;
import org.clarent.ivyidea.intellij.facet.ui.components.ConfigurationSelectionTableModel;
import org.clarent.ivyidea.ivy.IvyUtil;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaFacetEditorTab extends FacetEditorTab {

    private static final Logger LOGGER = Logger.getLogger(IvyIdeaFacetEditorTab.class.getName());

    private com.intellij.openapi.ui.TextFieldWithBrowseButton txtIvyFile;
    private JPanel pnlRoot;
    private JCheckBox chkUseProjectSettings;
    private TextFieldWithBrowseButton txtIvySettingsFile;
    private JLabel lblIvySettingsFile;
    private JCheckBox chkOnlyResolveSpecificConfigs;
    private ConfigurationSelectionTable tblConfigurationSelection;
    private FacetEditorContext editorContext;
    private boolean modified;

    public IvyIdeaFacetEditorTab(FacetEditorContext editorContext) {
        this.editorContext = editorContext;

        UserActivityWatcher watcher = new UserActivityWatcher();
        watcher.addUserActivityListener(new UserActivityListener() {

            public void stateChanged() {
                modified = true;
            }
        });
        watcher.register(pnlRoot);

        final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        descriptor.setNewFileType(IntellijUtils.getXmlFileType());
        txtIvyFile.addBrowseFolderListener("Select ivy file", "", editorContext.getProject(), descriptor);
        txtIvySettingsFile.addBrowseFolderListener("Select ivy settings file", "", editorContext.getProject(), new FileChooserDescriptor(true, false, false, false, false, false));

        txtIvyFile.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                final Set<Configuration> configs = IvyUtil.loadConfigurations(txtIvyFile.getText());
                chkOnlyResolveSpecificConfigs.setEnabled(configs != null);
                if (configs != null) {
                    // fill table
                    LOGGER.info("Detected configs in file " + txtIvyFile.getText() + ": " + configs.toString());
                    tblConfigurationSelection.setModel(new ConfigurationSelectionTableModel(configs));
                } else {
                    tblConfigurationSelection.setModel(new ConfigurationSelectionTableModel());
                }
            }
        });

        chkUseProjectSettings.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                lblIvySettingsFile.setEnabled(!chkUseProjectSettings.isSelected());
                txtIvySettingsFile.setEnabled(!chkUseProjectSettings.isSelected());
            }
        });

        chkOnlyResolveSpecificConfigs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                tblConfigurationSelection.setEnabled(chkOnlyResolveSpecificConfigs.isSelected());
            }
        });

    }

    @Nls
    public String getDisplayName() {
        return "IvyIDEA";
    }

    public JComponent createComponent() {
        return pnlRoot;
    }

    public boolean isModified() {
        return modified;
    }

    public void apply() throws ConfigurationException {
        final Facet facet = editorContext.getFacet();
        IvyIdeaFacetConfiguration configuration;
        if (facet != null) {
            configuration = (IvyIdeaFacetConfiguration) facet.getConfiguration();
            configuration.setIvyFile(txtIvyFile.getText());
            configuration.setUseProjectSettings(chkUseProjectSettings.isSelected());
            configuration.setIvySettingsFile(txtIvySettingsFile.getText());
            configuration.setOnlyResolveSelectedConfigs(chkOnlyResolveSpecificConfigs.isSelected());
        }
    }

    public void reset() {
        final Facet facet = editorContext.getFacet();
        IvyIdeaFacetConfiguration configuration;
        if (facet != null) {
            configuration = (IvyIdeaFacetConfiguration) facet.getConfiguration();
            txtIvyFile.setText(configuration.getIvyFile());
            chkUseProjectSettings.setSelected(configuration.isUseProjectSettings());
            txtIvySettingsFile.setText(configuration.getIvySettingsFile());
            chkOnlyResolveSpecificConfigs.setSelected(configuration.isOnlyResolveSelectedConfigs());
        }
    }

    public void disposeUIResources() {
    }

    private void createUIComponents() {
    }


}
