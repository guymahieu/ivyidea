package org.clarent.ivyidea.config.ui.orderedfilelist;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.io.File;

/**
 * @author Guy Mahieu
 */
public class OrderedFileList {

    private final Project project;
    
    private JPanel pnlRoot;
    private JButton btnUp;
    private JButton btnRemove;
    private JButton btnDown;
    private JButton btnAdd;
    private JList lstFileNames;
    private TextFieldWithBrowseButton txtFileNameToAdd;

    public OrderedFileList(Project project) {
        this.project = project;

        btnAdd.setEnabled(false);
        txtFileNameToAdd.addBrowseFolderListener("Select properties file", "", project, new FileChooserDescriptor(true, false, false, false, false, false));
        txtFileNameToAdd.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {

            protected void textChanged(DocumentEvent e) {
                final File file = new File(txtFileNameToAdd.getTextField().getText());
                btnAdd.setEnabled(file.exists() && !file.isDirectory());
            }
        });
    }

    public JPanel getRootPanel() {
        return pnlRoot;
    }
}
