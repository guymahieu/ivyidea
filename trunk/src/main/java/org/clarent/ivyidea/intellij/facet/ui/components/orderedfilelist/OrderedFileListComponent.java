package org.clarent.ivyidea.intellij.facet.ui.components.orderedfilelist;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import javax.swing.*;

/**
 * Provides a list to which files can be added and ordered.
 *
 * @author Guy Mahieu
 */
public class OrderedFileListComponent extends JComponent {

    private TextFieldWithBrowseButton txtFileToAdd;    

    private JScrollPane scrollPane;
    private JList lstFiles;

    private JButton btnAdd;
    private JButton btnRemove;
    private JButton btnUp;
    private JButton btnDown;
    
}
