/*
 * Copyright 2010 Guy Mahieu
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

package org.clarent.ivyidea.exception;

/**
 * Thrown when there was a problem while reading (accessing/parsing/...)
 * the ivy xml file for a module. 
 *
 * @author Guy Mahieu
 */
public class IvyFileReadException extends IvyIdeaException {

    private String fileName;
    private String moduleName;

    public IvyFileReadException(String fileName, String moduleName, Throwable cause) {
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
        return "Exception while reading ivy file " + fileName + " for module " + moduleName;
    }
}

