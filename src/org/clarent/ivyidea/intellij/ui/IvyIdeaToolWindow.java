package org.clarent.ivyidea.intellij.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaToolWindow extends JPanel {

    public static final String ID = "IvyIDEA";

    private JTextPane txtConsole;
    private JButton btnAboutIvyIDEA;
    private JTabbedPane tabbedPane1;
    private JPanel root;
    private static final String DEFAULT_CONSOLE_TXT = "-=(IvyIDEA Console)=-\n\nFor now this is just a raw view of the ivy output during the resolve process.\n\n";

    public IvyIdeaToolWindow() {
        this.setLayout(new BorderLayout());
        this.add(BorderLayout.CENTER, root);
        txtConsole.setText(DEFAULT_CONSOLE_TXT);

        btnAboutIvyIDEA.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                txtConsole.setText(DEFAULT_CONSOLE_TXT);
            }
        });

    }

    public JPanel getRoot() {
        return this;
    }

    public void clear() {
        txtConsole.setText("");
    }

    public void append(final String message) {
        txtConsole.setText(txtConsole.getText() + "\n" + message);
    }
}

