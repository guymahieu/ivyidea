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

package org.clarent.ivyidea.intellij.compatibility;

import com.intellij.openapi.application.ApplicationInfo;

import java.util.logging.Logger;

/**
 * Abstracts changed api's between intellij 7 and 8 to allow the plugin
 * to be used in both versions.
 *
 * @author Guy Mahieu
 */
public class IntellijCompatibilityService {

    private static Logger LOGGER = Logger.getLogger("IvyIDEA-Compatibility");

    private static IntellijCompatibilityMethods compatibilityMethods;

    static {
        final String majorVersion = ApplicationInfo.getInstance().getMajorVersion();
        LOGGER.info("Detected intellij major version " + majorVersion + "; activating the appropriate compatibility mode.");
        int numericMajorVersion = getNumericMajorVersion();
        if (numericMajorVersion > 0) {
            // Numeric comparison...
            if (numericMajorVersion >= 8) {
                compatibilityMethods = new Intellij8Methods();
            } else if (numericMajorVersion > 0) {
                compatibilityMethods = new Intellij7Methods();
            }
        } else {
            // literal string comparison...
            if ("8".equals(majorVersion)) {
                LOGGER.info("Compatibility mode set to IntelliJ 8.0");
                compatibilityMethods = new Intellij8Methods();
            } else {
                LOGGER.info("Compatibility mode set to IntelliJ 7.0");
                compatibilityMethods = new Intellij7Methods();
            }
        }
    }

    public static IntellijCompatibilityMethods getCompatibilityMethods() {
        return compatibilityMethods;
    }

    private static int getNumericMajorVersion() {
        final String version = ApplicationInfo.getInstance().getMajorVersion();
        try {
            return Integer.parseInt(version);
        } catch (NumberFormatException e) {
            LOGGER.info("Non-numeric major version encountered:" + version);
            return -1;
        }
    }

}
