package org.clarent.ivyidea.intellij.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import org.clarent.ivyidea.config.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.IntellijUtils;

import javax.swing.*;

/**
 * @author Guy Mahieu
 */

public class IvySettingsErrorDialog {

    private JPanel rootPanel;
    private JTextArea txtMessage;
    private LinkLabel lblLink;

    public JPanel getRootPanel() {        
        return rootPanel;
    }

    public void setMessage(String message) {
        txtMessage.setText(message);
    }

    public void setLinkData(final IvySettingsNotFoundException.ConfigLocation configLocation, final String configName) {

        // TODO: Currently I don't know how to open the facet setting from the code, so there is no
        //              use in showing the link...
        lblLink.setVisible(configLocation == IvySettingsNotFoundException.ConfigLocation.Project);

        lblLink.setText("Open " + configLocation + " settings for " + configName + "...");
        lblLink.setListener(new LinkListener() {
            public void linkSelected(LinkLabel linkLabel, Object o) {
                final Project project = IntellijUtils.getCurrentProject();
                if (configLocation == IvySettingsNotFoundException.ConfigLocation.Module) {
                    // TODO: Open relevant settings GUI!
                    // if only the facet config was a Configurable instance!
/*
                    final Module module = ModuleManager.getInstance(project).findModuleByName(configName);
                    final IvyIdeaFacet facetByType = FacetManager.getInstance(module).getFacetByType(IvyIdeaFacetType.ID);
                    Configurable component = IntellijUtils.getCurrentProject().getComponent(IvyIdeaFacetConfiguration.class);
                    ShowSettingsUtil.getInstance().editConfigurable(project, component);
*/
                } else {
                    Configurable component = project.getComponent(IvyIdeaProjectSettingsComponent.class);
                    ShowSettingsUtil.getInstance().editConfigurable(project, component);
                }
            }
        }, null);
    }
}
