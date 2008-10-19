package org.clarent.ivyidea.ivy;

import com.intellij.openapi.module.Module;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.plugins.parser.ModuleDescriptorParserRegistry;
import org.apache.ivy.plugins.parser.ParserSettings;
import org.clarent.ivyidea.intellij.facet.IvyFacetConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Logger;

/**
 * @author Guy Mahieu
 */

public class IvyUtil {

    private static final Logger LOGGER = Logger.getLogger(IvyUtil.class.getName());

    @Nullable
    public static ModuleDescriptor getIvyModuleDescriptor(Module intellijModule, ParserSettings settings) {
        final File ivyFile = getIvyFile(intellijModule);
        if (ivyFile != null && ivyFile.exists()) {
            return parseIvyFile(ivyFile, settings);
        } else {
            return null;
        }
    }

    @Nullable
    public static File getIvyFile(Module module) {
        return new File(IvyFacetConfiguration.getInstance(module).getIvyFile());
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
}
