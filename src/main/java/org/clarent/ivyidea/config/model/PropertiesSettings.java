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

package org.clarent.ivyidea.config.model;

import java.util.*;

/**
 * @author Guy Mahieu
 */
public class PropertiesSettings {
    
    private List<String> propertyFiles = Collections.emptyList();
    private SortedProperties additionalProperties = new SortedProperties();

    public List<String> getPropertyFiles() {
        return propertyFiles;
    }

    public void setPropertyFiles(List<String> propertyFiles) {
        this.propertyFiles = propertyFiles;
    }

    public SortedProperties getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(SortedProperties additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public static class SortedProperties implements Iterable<String> {

        private List<String> sortedKeys = new ArrayList<String>();
        private Map<String, String> data = new HashMap<String, String>();

        public void add(String key, String value) {
            if (sortedKeys.contains(key)) {
                sortedKeys.remove(key);
            }
            sortedKeys.add(key);
            data.put(key, value);
        }

        public void remove(String key) {
            sortedKeys.remove(key);
            data.remove(key);
        }

        public Iterator<String> keySet() {
            return sortedKeys.iterator();
        }

        public String getValue(String key) {
            return data.get(key);
        }

        public Iterator<String> iterator() {
            return keySet();
        }
    }
}
