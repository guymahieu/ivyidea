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
 * Thrown when there was a problem while reading (accessing/parsing/...)
 * the ivy settings file for a module. 
 *
 * @author Guy Mahieu
 */
public class IvySettingsFileReadException extends IvyIdeaException {

    private String fileName;
    private String moduleName;

    public IvySettingsFileReadException(String fileName, String moduleName, Throwable cause) {
        super(cause);
        this.fileName = fileName;
        this.moduleName = moduleName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getModuleName() {
        return moduleName;
    }

    @Override
    public String getMessage() {
        return "An error occured while reading the ivy settings for module " + moduleName + " from " + fileName;
    }
}
