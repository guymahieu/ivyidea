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

package org.clarent.ivyidea.intellij;

import com.intellij.ide.caches.CachesInvalidator;
import com.intellij.openapi.util.io.FileUtil;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;

/**
 * Clears the IvyIDEA workspace cache when the user invalidates the caches of the IDE.
 */
public class IvyIdeaCachesInvalidator extends CachesInvalidator {

    @Override
    public String getDescription() {
        return "Clear IvyIDEA workspace cache";
    }

    @Override
    public Boolean optionalCheckboxDefaultValue() {
        return Boolean.FALSE;
    }

    @Override
    public void invalidateCaches() {
        FileUtil.delete(IvyIdeaConfigHelper.getWorkspaceCacheDir());
    }
}
