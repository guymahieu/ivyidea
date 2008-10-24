package org.clarent.ivyidea.intellij.facet.ui.components;

import com.intellij.util.ui.Table;

/**
 * @author Guy Mahieu
 */

public class ConfigurationSelectionTable extends Table {

    public ConfigurationSelectionTable() {
        super(new ConfigurationSelectionTableModel());
    }

}
