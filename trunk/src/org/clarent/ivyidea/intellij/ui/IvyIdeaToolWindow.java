package org.clarent.ivyidea.intellij.ui;

import javax.swing.*;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaToolWindow {

    public static final String ID = "IvyIDEA";

    private JPanel root;
    private JTextPane txtConsole;
    private JButton clearButton;

    public JPanel getRoot() {
        return root;
    }

    private void append(String message) {
        txtConsole.setText(txtConsole.getText() + "\n" + message);
    }
}

