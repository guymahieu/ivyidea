/*
 * Copyright 2010 Guy Mahieu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.clarent.ivyidea.config.ui.orderedfilelist;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private boolean modified;

    public OrderedFileList(Project project) {
        this.project = project;

        // TODO: implement reordering functionality
        btnUp.setVisible(false);
        btnDown.setVisible(false);

        wireFileList();
        wireAddButton();
        wireRemoveButton();

        installActivityListener();
    }

    private void installActivityListener() {
        UserActivityWatcher watcher = new UserActivityWatcher();
        watcher.addUserActivityListener(new UserActivityListener() {
            public void stateChanged() {
                modified = true;
            }
        });
        watcher.register(pnlRoot);
    }

    private void wireFileList() {
        lstFileNames.setModel(new OrderedFileListModel());
        // TODO: implement multi select
        lstFileNames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstFileNames.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                updateRemoveButtonState();
            }
        });
        lstFileNames.getModel().addListDataListener(new ListDataListener() {
            public void intervalAdded(ListDataEvent e) {
                updateRemoveButtonState();
            }

            public void intervalRemoved(ListDataEvent e) {
                updateRemoveButtonState();
            }

            public void contentsChanged(ListDataEvent e) {
                updateRemoveButtonState();
            }
        });
    }

    private void updateRemoveButtonState() {
        btnRemove.setEnabled(isRemoveAllowed());
    }

    private boolean isRemoveAllowed() {
        return lstFileNames.getModel().getSize() > 0 &&
                lstFileNames.getSelectedIndex() > -1;
    }

    private void wireAddButton() {
        btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final FileChooserDescriptor fcDescriptor = FileChooserDescriptorFactory.createMultipleFilesNoJarsDescriptor();
                fcDescriptor.setTitle("Select properties file(s)");
                final VirtualFile[] files = FileChooser.chooseFiles(project, fcDescriptor);
                for (VirtualFile file : files) {
                    addFilenameToList(file.getPresentableUrl());
                }
            }
        });

    }

    private void wireRemoveButton() {
        btnRemove.setEnabled(false);
        btnRemove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedItemFromList();
            }
        });
    }

    private void addFilenameToList(String fileName) {
        getFileListModel().add(fileName);
        modified = true;
    }

    private void removeSelectedItemFromList() {
        final int selectedIndex = lstFileNames.getSelectedIndex();
        getFileListModel().removeItemAt(selectedIndex);
        updateListSelection(selectedIndex);
        modified = true;
    }

    private void updateListSelection(int indexToSelect) {
        if (indexToSelect >= 0) {
            if (indexToSelect < getFileListModel().getSize()) {
                lstFileNames.getSelectionModel().setSelectionInterval(indexToSelect, indexToSelect);
            } else {
                lstFileNames.getSelectionModel().setSelectionInterval(getFileListModel().getSize() - 1, getFileListModel().getSize() - 1);
            }
        }
    }

    private OrderedFileListModel getFileListModel() {
        return ((OrderedFileListModel) lstFileNames.getModel());
    }

    public boolean isModified() {
        return modified;
    }

    public List<String> getFileNames() {
        return getFileListModel().getAllItems();
    }

    public void setFileNames(List<String> items) {
        getFileListModel().setItems(items);
    }

    public JPanel getRootPanel() {
        return pnlRoot;
    }

}
