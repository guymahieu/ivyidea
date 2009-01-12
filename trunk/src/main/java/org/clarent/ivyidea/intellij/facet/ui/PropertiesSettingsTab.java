package org.clarent.ivyidea.intellij.facet.ui;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

/**
 * @author Guy Mahieu
 */
public class PropertiesSettingsTab extends FacetEditorTab  {
    private JPanel pnlRoot;

    private final FacetEditorContext editorContext;

    public PropertiesSettingsTab(FacetEditorContext editorContext) {

        this.editorContext = editorContext;
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

}
