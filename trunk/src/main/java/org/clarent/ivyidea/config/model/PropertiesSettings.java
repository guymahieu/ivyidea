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
