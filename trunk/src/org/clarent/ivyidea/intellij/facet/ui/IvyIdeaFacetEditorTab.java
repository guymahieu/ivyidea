package org.clarent.ivyidea.intellij.facet.ui;

import com.intellij.facet.Facet;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import org.clarent.ivyidea.intellij.IvyFileType;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacetConfiguration;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaFacetEditorTab extends FacetEditorTab {

    private com.intellij.openapi.ui.TextFieldWithBrowseButton txtIvyFile;
    private JPanel pnlRoot;
    private JCheckBox chkUseProjectSettings;
    private TextFieldWithBrowseButton txtIvySettingsFile;
    private JLabel lblIvySettingsFile;
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
        descriptor.setNewFileType(IvyFileType.IVY_FILE_TYPE);
        txtIvyFile.addBrowseFolderListener("Select ivy file", "", editorContext.getProject(), descriptor);

        descriptor.setNewFileType(IvyFileType.IVY_FILE_TYPE);
        txtIvySettingsFile.addBrowseFolderListener("Select ivy settings file", "", editorContext.getProject(), new FileChooserDescriptor(true, false, false, false, false, false));

        chkUseProjectSettings.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                lblIvySettingsFile.setEnabled(!chkUseProjectSettings.isSelected());
                txtIvySettingsFile.setEnabled(!chkUseProjectSettings.isSelected());
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
        }
    }

    public void disposeUIResources() {
    }

    private void createUIComponents() {
    }
}
