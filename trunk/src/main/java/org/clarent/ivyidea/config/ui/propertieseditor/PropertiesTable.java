/*
 *    Copyright 2009 Guy Mahieu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 * Copyright (c) , Your Corporation. All Rights Reserved.
 */

package org.clarent.ivyidea.config.ui.propertieseditor;

import com.intellij.util.ui.Table;

import javax.swing.table.TableModel;

/**
 * Table to allow the user to add custom properties to inject during the ivy resolve process
 * from within IntelliJ IDEA.
 *
 * @author Guy Mahieu
 */
public class PropertiesTable extends Table {

    public PropertiesTable() {
        super(new PropertiesTableModel());
        initComponents();
    }

    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        initComponents();
    }

    private void initComponents() {
        setRowSelectionAllowed(false);
        setColumnSelectionAllowed(false);

        setAutoResizeMode(AUTO_RESIZE_OFF);
        getColumnModel().getColumn(0).setPreferredWidth(150);
        getColumnModel().getColumn(1).setPreferredWidth(150);

        getColumnModel().getColumn(0).setHeaderValue("Name");
        getColumnModel().getColumn(1).setHeaderValue("Value");

        // Register custom renderer to draw deprecated configs in 'strikethrough'
//        getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
//
//            private Font regularFont;
//            private Font strikethroughFont;
//
//            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//                final ConfigurationSelectionTableModel tableModel = (ConfigurationSelectionTableModel) table.getModel();
//                final Component rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//                if (regularFont == null) {
//                    regularFont = rendererComponent.getFont();
//                }
////                final int modelIndex = table.convertRowIndexToModel(row); // JDK 1.6 - if table sorting is enabled
//                final Configuration configuration = tableModel.getConfigurationAt(row);
//                if (configuration.getDeprecated() != null) {
//                    if (strikethroughFont == null) {
//                        final HashMap<TextAttribute, Object> attribs = new HashMap<TextAttribute, Object>();
//                        attribs.put(TextAttribute.STRIKETHROUGH, Boolean.TRUE);
//                        strikethroughFont = regularFont.deriveFont(attribs);
//                    }
//                    setToolTipText("Depracated: " + configuration.getDeprecated());
//                    rendererComponent.setFont(strikethroughFont);
//                } else {
//                    setToolTipText(null);
//                    rendererComponent.setFont(regularFont);
//                }
//                rendererComponent.setEnabled(table.isEnabled());
//                return rendererComponent;
//            }
//        });
//
//        // Render description disabled if table is disabled
//        getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
//            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//                final Component rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//                rendererComponent.setEnabled(table.isEnabled());
//                return rendererComponent;
//            }
//        });

    }

}