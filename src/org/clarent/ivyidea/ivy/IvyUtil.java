package org.clarent.ivyidea.ivy;

import com.intellij.openapi.module.Module;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.ModuleDescriptorParserRegistry;
import org.apache.ivy.plugins.parser.ParserSettings;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacetConfiguration;
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

    @NotNull
    public static File getIvyFile(Module module) {
        return new File(IvyIdeaFacetConfiguration.getInstance(module).getIvyFile());
    }

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
     * @return a set of configurations, null if anything went wrong parsing the ivy file
     */
    @Nullable
    public static Set<Configuration> loadConfigurations(String ivyFileName, IvySettings ivySettings) {
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
            }
        } catch (RuntimeException e) {
            // Not able to parse module descriptor; no problem here...
            LOGGER.info("Error while parsing ivy file during attempt to load configurations from it: " + e);
        }
        return null;
    }
}
