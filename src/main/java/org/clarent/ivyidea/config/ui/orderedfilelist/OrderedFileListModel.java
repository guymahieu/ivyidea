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
