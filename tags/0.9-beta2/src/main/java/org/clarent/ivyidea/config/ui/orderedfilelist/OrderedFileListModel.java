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

package org.clarent.ivyidea.config.ui.orderedfilelist;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Guy Mahieu
*/
class OrderedFileListModel extends AbstractListModel {

    private List<String> items = new ArrayList<String>();

    public List<String> getAllItems() {
        return new ArrayList<String>(items);
    }

    public void setItems(List<String> itemsToSet) {
        clear();
        add(itemsToSet);
    }

    private void add(List<String> itemsToAdd) {
        items.addAll(itemsToAdd);
        fireIntervalAdded(this, items.size() - itemsToAdd.size(), items.size());
    }

    void add(String item) {
        items.add(item);
        fireIntervalAdded(this, items.size(), items.size());
    }

    void removeItemAt(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            fireIntervalRemoved(this, index, index);
        }
    }

    void clear() {
        int nrOfItemsBeforeClear = items.size();
        items.clear();
        fireContentsChanged(this, 0, nrOfItemsBeforeClear);
    }

    public int getSize() {
        return items.size();
    }       

    String getItemAt(int index) {
        return items.get(index);
    }

    public Object getElementAt(int index) {
        return getItemAt(index);
    }
}
