/*
 * Copyright 2009 Guy Mahieu
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

package org.clarent.ivyidea.intellij.facet.ui.components;

import org.apache.ivy.core.module.descriptor.Configuration;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 * @author Guy Mahieu
 */
public class ConfigurationSelectionTableModel extends AbstractTableModel {

    private List<Configuration> data;
    private Set<Integer> selectedIndexes;

    public ConfigurationSelectionTableModel() {
        this.data = Collections.emptyList();
        this.selectedIndexes = Collections.emptySet();
    }

    public ConfigurationSelectionTableModel(Collection<Configuration> data) {
        this.data = new ArrayList<Configuration>(data);
        this.selectedIndexes = new HashSet<Integer>();
    }

    public ConfigurationSelectionTableModel(Collection<Configuration> data, Collection<String> selectedConfigNames) {
        this.data = new ArrayList<Configuration>(data);
        this.selectedIndexes = buildSelectedIndexes(this.data, selectedConfigNames);
    }

    public Set<Configuration> getSelectedConfigurations() {
        Set<Configuration> result = new HashSet<Configuration>();
        for (Integer selectedIndex : selectedIndexes) {
            result.add(data.get(selectedIndex));
        }
        return result;
    }

    public Configuration getConfigurationAt(int rowIndex) {
        return data.get(rowIndex);
    }

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return 3;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0 && aValue instanceof Boolean) {
            boolean checked = (Boolean) aValue;
            if (checked) {
                selectedIndexes.add(rowIndex);
            } else {
                selectedIndexes.remove(rowIndex);
            }
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        final Configuration configuration = data.get(rowIndex);
        if (columnIndex == 0) {
            return selectedIndexes.contains(rowIndex);
        }
        if (columnIndex == 1) {
            return configuration.getName();
        }
        if (columnIndex == 2) {
            return configuration.getDescription();
        }
        return null;
    }

    private static Set<Integer> buildSelectedIndexes(@NotNull List<Configuration> configurations, @NotNull Collection<String> selectedConfigNames) {
        final HashSet<Integer> result = new HashSet<Integer>();
        for (Configuration configuration : configurations) {
            if (selectedConfigNames.contains(configuration.getName())) {
                result.add(configurations.indexOf(configuration));
            }
        }
        return result;
    }

}
