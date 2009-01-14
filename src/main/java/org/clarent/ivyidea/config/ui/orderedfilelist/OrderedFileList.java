package org.clarent.ivyidea.config.ui.orderedfilelist;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

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
    private boolean modified;

    public OrderedFileList(Project project) {
        this.project = project;

        // TODO: implement reordering functionality
        btnUp.setVisible(false);
        btnDown.setVisible(false);

        lstFileNames.setModel(new OrderedFileListModel());
        // TODO: implement multi select
        lstFileNames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstFileNames.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                btnRemove.setEnabled(lstFileNames.getSelectedIndex() > -1);
            }
        });

        btnAdd.setEnabled(false);
        btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String fileName = txtFileNameToAdd.getTextField().getText();
                ((OrderedFileListModel) lstFileNames.getModel()).add(fileName);
                txtFileNameToAdd.getTextField().setText("");
            }
        });

        btnRemove.setEnabled(false);
        btnRemove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final int selectedIndex = lstFileNames.getSelectedIndex();
                if (selectedIndex != -1) {
                    ((OrderedFileListModel) lstFileNames.getModel()).removeItemAt(selectedIndex);
                }                
            }
        });

        txtFileNameToAdd.addBrowseFolderListener("Select properties file", "", this.project, new FileChooserDescriptor(true, false, false, false, false, false));
        txtFileNameToAdd.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {

            protected void textChanged(DocumentEvent e) {
                final File file = new File(txtFileNameToAdd.getTextField().getText());
                btnAdd.setEnabled(file.exists() && !file.isDirectory());
            }
        });

        UserActivityWatcher watcher = new UserActivityWatcher();
        watcher.addUserActivityListener(new UserActivityListener() {
            public void stateChanged() {
                modified = true;
            }
        });
        watcher.register(pnlRoot);
        
    }

    public boolean isModified() {
        return modified;
    }

    public List<String> getFileNames() {
        return ((OrderedFileListModel) lstFileNames.getModel()).getAllItems();
    }

    public void setFileNames(List<String> items) {
        ((OrderedFileListModel) lstFileNames.getModel()).add(items);
    }

    public JPanel getRootPanel() {
        return pnlRoot;
    }

}
