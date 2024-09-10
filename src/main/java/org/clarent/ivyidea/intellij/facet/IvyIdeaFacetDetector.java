/*
 * Copyright 2010 Maarten Coene
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

package org.clarent.ivyidea.intellij.facet;

import com.intellij.facet.FacetType;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.util.indexing.FileContent;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Maarten Coene
 */

public class IvyIdeaFacetDetector extends FacetBasedFrameworkDetector<IvyIdeaFacet, IvyIdeaFacetConfiguration> {

    public IvyIdeaFacetDetector() {
        super("IvyIDEA");
    }

    public FacetType<IvyIdeaFacet, IvyIdeaFacetConfiguration> getFacetType() {
        return IvyIdeaFacetType.getInstance();
    }

    @NotNull
    public FileType getFileType() {
        return FileTypeManager.getInstance().getFileTypeByExtension("xml");
    }

    @NotNull
    public ElementPattern<FileContent> createSuitableFilePattern() {
        return FileContentPattern.fileContent().withName("ivy.xml").xmlWithRootTag("ivy-module");
    }

    @Override
    protected IvyIdeaFacetConfiguration createConfiguration(Collection<? extends VirtualFile> files) {
        final IvyIdeaFacetConfiguration result = super.createConfiguration(files);

        if (!files.isEmpty()) {
            VirtualFile ivyFile = files.iterator().next();
            result.setIvyFile(ivyFile.getPath());
        }

        return result;
    }
}
