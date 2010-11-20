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

package org.clarent.ivyidea.ivy;

import com.intellij.openapi.module.Module;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.ModuleDescriptorParserRegistry;
import org.apache.ivy.plugins.parser.ParserSettings;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public class IvyUtil {

    private static final Logger LOGGER = Logger.getLogger(IvyUtil.class.getName());

    @Nullable
    public static ModuleDescriptor getIvyModuleDescriptor(Module intellijModule, ParserSettings settings) {
        final File ivyFile = getIvyFile(intellijModule);
        if (ivyFile.exists()) {
            return parseIvyFile(ivyFile, settings);
        } else {
            return null;
        }
    }

    /**
     * Returnes the ivy file for the given module.
     *
     * @param module the IntelliJ module for which you want to lookup the ivy file
     * @return the File representing the ivy xml file for the given module
     * @throws RuntimeException if the given module does not have an IvyIDEA facet configured.
     */
    @NotNull
    public static File getIvyFile(Module module) {
        final IvyIdeaFacetConfiguration configuration = IvyIdeaFacetConfiguration.getInstance(module);
        if (configuration == null) {
            throw new RuntimeException("Internal error: No IvyIDEA facet configured for module " + module.getName() + ", but an attempt was made to use it as such.");
        }
        return new File(configuration.getIvyFile());
    }

    /**
     * Parses the given ivyFile into a ModuleDescriptor using the given settings.
     *
     * @param ivyFile  the ivy file to parse
     * @param settings the settings for the parser
     * @return the ModuleDescriptor object representing the ivy file.
     */
    public static ModuleDescriptor parseIvyFile(@NotNull File ivyFile, ParserSettings settings) {
        try {
            LOGGER.info("Parsing ivy file " + ivyFile.getAbsolutePath());
            return ModuleDescriptorParserRegistry.getInstance().parseDescriptor(settings, ivyFile.toURI().toURL(), false);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gives a set of configurations defined in the given ivyFileName.
     * Will never throw an exception, if something goes wrong, null is returned
     *
     * @param ivyFileName the name of the ivy file to parse
     * @param ivySettings the settings to use; defaults will be used if this is null
     * @return a set of configurations, null if anything went wrong parsing the ivy file
     *
     * @throws java.text.ParseException if there was an error parsing the ivy file; if the file
     *          does not exist or is a directory, no exception will be thrown
     */
    @Nullable
    public static Set<Configuration> loadConfigurations(@NotNull String ivyFileName, @Nullable IvySettings ivySettings) throws ParseException {
        try {
            final File file = new File(ivyFileName);
            if (file.exists() && !file.isDirectory()) {
                if (ivySettings == null) {
                    ivySettings = new IvySettings();
                }
                ivySettings.setValidate(false);
                final ModuleDescriptor md = parseIvyFile(file, ivySettings);
                Set<Configuration> result = new TreeSet<Configuration>(new Comparator<Configuration>() {
                    public int compare(Configuration o1, Configuration o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                });
                result.addAll(Arrays.asList(md.getConfigurations()));
                return result;
            } else {
                return null;
            }
        } catch (RuntimeException e) {
            // Not able to parse module descriptor; no problem here...
            LOGGER.info("Error while parsing ivy file during attempt to load configurations from it: " + e);
            if (e.getCause() instanceof ParseException) {
                throw (ParseException) e.getCause();
            }
            return null;
        }
    }
}
