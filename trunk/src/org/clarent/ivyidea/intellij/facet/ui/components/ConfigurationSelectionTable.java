package org.clarent.ivyidea.intellij.facet.ui.components;

import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.ui.BooleanTableCellRenderer;
import com.intellij.util.ui.Table;
import org.apache.ivy.core.module.descriptor.Configuration;

import javax.swing.table.TableModel;
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
    }

}
