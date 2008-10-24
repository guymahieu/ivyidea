package org.clarent.ivyidea.intellij.facet.ui.components;

import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.ui.BooleanTableCellRenderer;
import com.intellij.util.ui.Table;
import org.apache.ivy.core.module.descriptor.Configuration;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Guy Mahieu
 */

public class ConfigurationSelectionTable extends Table {

    public ConfigurationSelectionTable() {
        super(new ConfigurationSelectionTableModel());
        initComponents();
    }

    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        initComponents();
    }

    public Set<Configuration> getSelectedConfigurations() {
        return ((ConfigurationSelectionTableModel) getModel()).getSelectedConfigurations();
    }

    private void initComponents() {
        getColumnModel().getColumn(0).setCellRenderer(new BooleanTableCellRenderer());
        getColumnModel().getColumn(0).setCellEditor(new BooleanTableCellEditor());

        // Register custom renderer to draw deprecated configs in 'strikethrough'
        getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {

            private Font regularFont;
            private Font strikethroughFont;

            // implements javax.swing.table.TableCellRenderer
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final ConfigurationSelectionTableModel tableModel = (ConfigurationSelectionTableModel) table.getModel();
                final Component rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (regularFont == null) {
                    regularFont = rendererComponent.getFont();
                }
                if (tableModel.getConfigurationAt(row).getDeprecated() != null) {
                    if (strikethroughFont == null) {
                        final HashMap<TextAttribute, Object> attribs = new HashMap<TextAttribute, Object>();
                        attribs.put(TextAttribute.STRIKETHROUGH, Boolean.TRUE);
                        strikethroughFont = regularFont.deriveFont(attribs);
                    }
                    rendererComponent.setFont(strikethroughFont);
                } else {
                    rendererComponent.setFont(regularFont);
                }
                return rendererComponent;
            }
        });
    }

}
