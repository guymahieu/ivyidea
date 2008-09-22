package org.clarent.ivyidea.intellij.facet.ui;

import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.UserActivityWatcher;
import com.intellij.ui.UserActivityListener;

import javax.swing.*;

import org.jetbrains.annotations.Nls;
import org.clarent.ivyidea.intellij.facet.IvyFacetConfiguration;
import org.clarent.ivyidea.intellij.IvyFileType;

/**
 * @author Guy Mahieu
 */

public class IvyFacetEditorTab extends FacetEditorTab {

    private com.intellij.openapi.ui.TextFieldWithBrowseButton txtIvyFile;
    private JPanel pnlRoot;
    private FacetEditorContext editorContext;
    private boolean modified;

    public IvyFacetEditorTab(FacetEditorContext editorContext) {
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
        IvyFacetConfiguration configuration = (IvyFacetConfiguration) editorContext.getFacet().getConfiguration();
        configuration.setIvyFile(txtIvyFile.getText());
    }

    public void reset() {
        IvyFacetConfiguration configuration = (IvyFacetConfiguration) editorContext.getFacet().getConfiguration();
        txtIvyFile.setText(configuration.getIvyFile());
    }

    public void disposeUIResources() {
    }

    private void createUIComponents() {
    }
}
