package org.clarent.ivyidea.intellij.facet.ui;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.options.ConfigurationException;
import org.clarent.ivyidea.config.ui.orderedfilelist.OrderedFileList;
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

    private final FacetEditorContext editorContext;
    private OrderedFileList orderedFileList;

    public PropertiesSettingsTab(FacetEditorContext editorContext) {

        this.editorContext = editorContext;
    }

    public OrderedFileList getOrderedFileList() {
        return orderedFileList;
    }

    @Nls
    public String getDisplayName() {
        return "Properties (optional)";
    }

    public JComponent createComponent() {
        return pnlRoot;
    }

    public boolean isModified() {
        return false;
    }

    public void apply() throws ConfigurationException {
    }

    public void reset() {
    }

    public void disposeUIResources() {
    }

    private void createUIComponents() {
        pnlPropertiesFiles = new JPanel(new BorderLayout());
        orderedFileList = new OrderedFileList(editorContext.getProject());
        pnlPropertiesFiles.add(orderedFileList.getRootPanel(), BorderLayout.CENTER);
    }
}
