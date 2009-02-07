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

package org.clarent.ivyidea.resolve.dependency;

import com.intellij.openapi.roots.OrderRootType;
import org.apache.ivy.core.module.descriptor.Artifact;

import java.io.File;

/**
 * @author Guy Mahieu
 */

public class ExternalSourceDependency extends ExternalDependency {

    public ExternalSourceDependency(Artifact artifact, File externalArtifact) {
        super(artifact, externalArtifact);
    }

    protected String getTypeName() {
        return "sources";
    }

    public OrderRootType getType() {
        return OrderRootType.SOURCES;
    }
}