package org.clarent.ivyidea.intellij.ui;

import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;

import javax.swing.*;

import org.clarent.ivyidea.config.exception.IvySettingsNotFoundException;

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

    public void setLinkData(IvySettingsNotFoundException.ConfigLocation configLocation, String configName) {
        lblLink.setText("Open " + configLocation + " settings for " + configName + "...");
        lblLink.setListener(new LinkListener() {
            public void linkSelected(LinkLabel linkLabel, Object o) {
                // TODO: Open relevant settings GUI!
            }
        }, null);
    }
}
