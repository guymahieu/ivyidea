package org.clarent.ivyidea.config.ui.propertieseditor;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Guy Mahieu
 */
public class PropertiesTableModel extends AbstractTableModel {

    private List<Property> data;

    public PropertiesTableModel() {
        this.data = new ArrayList<Property>();
    }

    public PropertiesTableModel(Collection<Property> data) {
        this.data = new ArrayList<Property>(data);
    }

    public Property getPropertyAt(int rowIndex) {
        return data.get(rowIndex);
    }

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return 2;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String value = aValue == null ? "" : aValue.toString();
        while (rowIndex > data.size()) {
            data.add(new Property());
        }
        if (columnIndex == 0) {
            data.get(rowIndex).setKey(value);
        }
        if (columnIndex == 1) {
            data.get(rowIndex).setValue(value);
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return data.get(rowIndex).getKey();
        }
        if (columnIndex == 1) {
            return data.get(rowIndex).getValue();
        }
        throw new IllegalArgumentException("columnIndex is out of range:" + columnIndex);
    }

}