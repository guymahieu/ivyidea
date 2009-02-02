package org.clarent.ivyidea.config.ui.orderedfilelist;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Guy Mahieu
*/
class OrderedFileListModel extends AbstractListModel {

    private List<String> items = new ArrayList<String>();

    public List<String> getAllItems() {
        return Collections.unmodifiableList(items);
    }

    public void add(List<String> items) {
        this.items.addAll(items);
        fireContentsChanged(this, 0, items.size());
    }

    public void add(String item) {
        this.items.add(item);
        fireContentsChanged(this, 0, items.size());
    }

    public void removeItemAt(int index) {
        this.items.remove(index);
        fireContentsChanged(this, 0, items.size());
    }

    public int getSize() {
        return items.size();
    }       

    public String getItemAt(int index) {
        return items.get(index);
    }

    public Object getElementAt(int index) {
        return getItemAt(index);
    }
}
