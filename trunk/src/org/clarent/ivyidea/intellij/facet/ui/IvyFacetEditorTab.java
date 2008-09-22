package org.clarent.ivyidea.intellij.facet.ui;

import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;

import org.jetbrains.annotations.Nls;

/**
 * @author Guy Mahieu
 */

public class IvyFacetEditorTab extends FacetEditorTab {
    
    private JTextField txtIvyFile;
    private JPanel pnlRoot;
    private JLabel lblIvyFile;

    @Nls
    public String getDisplayName() {
        return "IvyIDEA";
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
    }
}
