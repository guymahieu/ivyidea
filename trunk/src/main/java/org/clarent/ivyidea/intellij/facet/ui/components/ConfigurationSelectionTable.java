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
 * Table to allow the user to configure the configurations that need to be resolved
 * from within IntelliJ IDEA.
 *
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
        setRowSelectionAllowed(false);
        setColumnSelectionAllowed(false);

        setAutoResizeMode(AUTO_RESIZE_OFF);
        getColumnModel().getColumn(0).setPreferredWidth(30);
        getColumnModel().getColumn(1).setPreferredWidth(120);
        getColumnModel().getColumn(2).setPreferredWidth(400);

        getColumnModel().getColumn(0).setHeaderValue("");
        getColumnModel().getColumn(1).setHeaderValue("Name");
        getColumnModel().getColumn(2).setHeaderValue("Description");

        // Render checkbox disabled if table is disabled
        getColumnModel().getColumn(0).setCellRenderer(new BooleanTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                rendererComponent.setEnabled(table.isEnabled());
                return rendererComponent;
            }
        });
        getColumnModel().getColumn(0).setCellEditor(new BooleanTableCellEditor());

        // Register custom renderer to draw deprecated configs in 'strikethrough'
        getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {

            private Font regularFont;
            private Font strikethroughFont;

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final ConfigurationSelectionTableModel tableModel = (ConfigurationSelectionTableModel) table.getModel();
                final Component rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (regularFont == null) {
                    regularFont = rendererComponent.getFont();
                }
//                final int modelIndex = table.convertRowIndexToModel(row); // JDK 1.6 - if table sorting is enabled
                final Configuration configuration = tableModel.getConfigurationAt(row);
                if (configuration.getDeprecated() != null) {
                    if (strikethroughFont == null) {
                        final HashMap<TextAttribute, Object> attribs = new HashMap<TextAttribute, Object>();
                        attribs.put(TextAttribute.STRIKETHROUGH, Boolean.TRUE);
                        strikethroughFont = regularFont.deriveFont(attribs);
                    }
                    setToolTipText("Depracated: " + configuration.getDeprecated());
                    rendererComponent.setFont(strikethroughFont);
                } else {
                    setToolTipText(null);
                    rendererComponent.setFont(regularFont);
                }
                rendererComponent.setEnabled(table.isEnabled());
                return rendererComponent;
            }
        });

        // Render description disabled if table is disabled
        getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                rendererComponent.setEnabled(table.isEnabled());
                return rendererComponent;
            }
        });

    }

}
