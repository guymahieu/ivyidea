package org.clarent.ivyidea.intellij.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaToolWindow {

    public static final String ID = "IvyIDEA";

    private JPanel root;
    private JTextPane txtConsole;
    private JTabbedPane tabbedPane1;
    private JButton btnAboutIvyIDEA;

    public IvyIdeaToolWindow() {
        btnAboutIvyIDEA.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                txtConsole.setText("");
                append("IvyIDEA");
                append("");
                append("This is where logging from the resolve process will be put in upcoming versions. Stay tuned! :)");
            }
        });

    }


    public JPanel getRoot() {
        return root;
    }

    private void append(String message) {
        txtConsole.setText(txtConsole.getText() + "\n" + message);
    }
}

