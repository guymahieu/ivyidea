package org.clarent.ivyidea.intellij.facet.ui.components.orderedfilelist;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import javax.swing.*;
import java.awt.*;

/**
 * Provides a list to which files can be added and ordered.
 *
 * @author Guy Mahieu
 */
public class OrderedFileListComponent extends JPanel {

    private TextFieldWithBrowseButton txtFileToAdd;    

    private JScrollPane scrollPane;
    private JList lstFiles;

    private JButton btnAdd;
    private JButton btnRemove;
    private JButton btnUp;
    private JButton btnDown;

    public OrderedFileListComponent() {
        final GridBagLayout bagLayout = new GridBagLayout();
        this.setLayout(bagLayout);

//        bagLayout.setConstraints();
    }
}
