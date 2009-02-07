package org.clarent.ivyidea.intellij.facet.ui;

import com.intellij.facet.Facet;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import org.clarent.ivyidea.config.ui.orderedfilelist.OrderedFileList;
import org.clarent.ivyidea.config.ui.propertieseditor.PropertiesEditor;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import java.awt.*;

/**
 * @author Guy Mahieu
 */
public class PropertiesSettingsTab extends FacetEditorTab  {
    private JPanel pnlRoot;
    private JPanel pnlPropertiesFiles;
    private JPanel pnlAdditionalProperties;
    private JLabel lblAdditionalPropertiesDescription;
    private JLabel lblAdditionalProperties;

    private final FacetEditorContext editorContext;
    private OrderedFileList orderedFileList;
    private PropertiesEditor additionalPropertiesEditor;
    private boolean alreadyOpenedBefore;
    private boolean modified;

    public PropertiesSettingsTab(FacetEditorContext editorContext) {
        this.editorContext = editorContext;

        /* No additional properties support yet in this release */
        pnlAdditionalProperties.setVisible(false);
        lblAdditionalProperties.setVisible(false);
        lblAdditionalPropertiesDescription.setVisible(false);        
        /* -- */

        wireActivityWatcher();        
    }

    private void wireActivityWatcher() {
        UserActivityWatcher watcher = new UserActivityWatcher();
        watcher.addUserActivityListener(new UserActivityListener() {
            public void stateChanged() {
                modified = true;
            }
        });
        watcher.register(pnlRoot);
    }

    @Nls
    public String getDisplayName() {
        return "Properties (optional)";
    }

    public JComponent createComponent() {
        return pnlRoot;
    }

    public boolean isModified() {
        return modified || orderedFileList.isModified();
    }

    public boolean isAlreadyOpenedBefore() {
        return alreadyOpenedBefore;
    }

    public java.util.List<String> getFileNames() {
        return orderedFileList.getFileNames();
    }

    public void apply() throws ConfigurationException {
        final Facet facet = editorContext.getFacet();
        if (facet != null) {
            IvyIdeaFacetConfiguration configuration = (IvyIdeaFacetConfiguration) facet.getConfiguration();
            configuration.getPropertiesSettings().setPropertyFiles(orderedFileList.getFileNames());
        }        
    }

    @Override
    public void onTabEntering() {
        super.onTabEntering();
        alreadyOpenedBefore = true;
    }

    public void reset() {
        final Facet facet = editorContext.getFacet();
        if (facet != null) {
            IvyIdeaFacetConfiguration configuration = (IvyIdeaFacetConfiguration) facet.getConfiguration();
            orderedFileList.setFileNames(configuration.getPropertiesSettings().getPropertyFiles());
        }        
    }

    public void disposeUIResources() {
    }

    private void createUIComponents() {
        pnlPropertiesFiles = new JPanel(new BorderLayout());
        orderedFileList = new OrderedFileList(editorContext.getProject());
        pnlPropertiesFiles.add(orderedFileList.getRootPanel(), BorderLayout.CENTER);

        pnlAdditionalProperties = new JPanel(new BorderLayout());
        additionalPropertiesEditor = new PropertiesEditor();
        pnlAdditionalProperties.add(additionalPropertiesEditor.getRootPanel(), BorderLayout.CENTER);
    }
}
