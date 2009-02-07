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

package org.clarent.ivyidea.exception;

/**
 * Typically thrown when no ivy settings file can be found during the resolve process.
 *
 * @author Guy Mahieu
 */

public class IvySettingsNotFoundException extends IvyIdeaException {

    public enum ConfigLocation {
        Project,
        Module
    }

    private ConfigLocation configLocation;
    private String configName;

    public IvySettingsNotFoundException(String message, ConfigLocation configLocation, String configName) {
        super(message);
        this.configLocation = configLocation;
        this.configName = configName;
    }

    public ConfigLocation getConfigLocation() {
        return configLocation;
    }

    public String getConfigName() {
        return configName;
    }
}

