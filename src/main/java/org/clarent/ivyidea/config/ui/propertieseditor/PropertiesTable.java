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

package org.clarent.ivyidea.config.ui.propertieseditor;

import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.TableModel;

/**
 * Table to allow the user to add custom properties to inject during the ivy resolve process
 * from within IntelliJ IDEA.
 *
 * @author Guy Mahieu
 */
public class PropertiesTable extends JBTable {

    public PropertiesTable() {
        super(new PropertiesTableModel());
        initComponents();
    }

    public void setModel(@NotNull TableModel dataModel) {
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
    }

}