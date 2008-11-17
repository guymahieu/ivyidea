package org.clarent.ivyidea.intellij.ui;

import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.facet.ui.ProjectSettingsContext;
import com.intellij.lang.refactoring.InlineHandler;
import org.clarent.ivyidea.config.exception.IvySettingsNotFoundException;

import javax.swing.*;
import java.awt.event.*;

public class IvySettingsNotFoundDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea txtMessage;
    private LinkLabel lblLink;

    public IvySettingsNotFoundDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
// add your code here
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
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

    public static void main(String[] args) {
        IvySettingsNotFoundDialog dialog = new IvySettingsNotFoundDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
