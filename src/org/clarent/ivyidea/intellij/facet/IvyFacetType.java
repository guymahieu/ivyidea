package org.clarent.ivyidea.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.clarent.ivyidea.intellij.IvyFileType;

import java.util.Collection;

/**
 * @author Guy Mahieu
 */

public class IvyFacetType extends FacetType<IvyFacet, IvyFacetConfiguration> {

    public static final FacetTypeId<IvyFacet> ID = new FacetTypeId<IvyFacet>("IvyIDEA");

    public IvyFacetType() {
        super(ID, "IvyIDEA", "IvyIDEA");
    }

    public IvyFacetConfiguration createDefaultConfiguration() {
        return new IvyFacetConfiguration();
    }

    public IvyFacet createFacet(@NotNull Module module, String name, @NotNull IvyFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
        return new IvyFacet(this, module, name, configuration, underlyingFacet);
    }

    public javax.swing.Icon getIcon() {
        // TODO: icon !
        return null;
    }

    public void registerDetectors(FacetDetectorRegistry<IvyFacetConfiguration> ivyIdeaDetectorRegistry) {
        VirtualFileFilter virtualFileFilter = new VirtualFileFilter() {
            public boolean accept(VirtualFile file) {
                return "ivy.xml".equals(file.getName());
            }
        };
        ivyIdeaDetectorRegistry.registerUniversalDetector(IvyFileType.IVY_FILE_TYPE, virtualFileFilter, new FacetDetector<VirtualFile, IvyFacetConfiguration>() {

            public IvyFacetConfiguration detectFacet(VirtualFile source, Collection<IvyFacetConfiguration> existentFacetConfigurations) {
/*
                final String path = source.getPath();
                System.out.println("path = " + path);
*/
                // TODO: Detect facet!
                return null;
            }

        });

    }
}
