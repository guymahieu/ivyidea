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

package org.clarent.ivyidea.intellij.compatibility;

import com.intellij.openapi.application.ApplicationInfo;

import java.util.logging.Logger;

/**
 * Abstracts changed api's between intellij versions to allow the plugin to be used in all these versions.
 *
 * @author Guy Mahieu
 */
public class IntellijCompatibilityService {

    private static Logger LOGGER = Logger.getLogger("IvyIDEA-Compatibility");

    private static IntellijCompatibilityMethods compatibilityMethods;

    static {
        final String majorVersion = ApplicationInfo.getInstance().getMajorVersion();
        LOGGER.info("Detected intellij major version " + majorVersion + "; activating the appropriate compatibility mode.");

        try {
            int numericMajorVersion = Integer.parseInt(majorVersion);
            if (numericMajorVersion >= 12) {
                compatibilityMethods = new Intellij12Methods();
            } else if (numericMajorVersion >= 8) {
                compatibilityMethods = new Intellij8Methods();
            } else {
                compatibilityMethods = new Intellij7Methods();
            }
        } catch (NumberFormatException e) {
            LOGGER.info("Non-numeric major version encountered:" + majorVersion);
            compatibilityMethods = new Intellij12Methods();
        }
    }

    public static IntellijCompatibilityMethods getCompatibilityMethods() {
        return compatibilityMethods;
    }

}
