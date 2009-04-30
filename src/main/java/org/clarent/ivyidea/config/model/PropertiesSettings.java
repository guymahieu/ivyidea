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

package org.clarent.ivyidea.config.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Guy Mahieu
 */
public class PropertiesSettings {
    
    private List<String> propertyFiles = new ArrayList<String>();

    public static PropertiesSettings copyDataFrom(PropertiesSettings propertiesSettings) {
        PropertiesSettings result = new PropertiesSettings();
        result.propertyFiles = new ArrayList<String>(propertiesSettings.propertyFiles);
        return result;
    }

    public List<String> getPropertyFiles() {
        return propertyFiles;
    }

    public void setPropertyFiles(List<String> propertyFiles) {
        this.propertyFiles = propertyFiles;
    }

}
