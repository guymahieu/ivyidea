package org.clarent.ivyidea.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.psi.PsiFile;
import org.clarent.ivyidea.intellij.IvyFileType;
import org.clarent.ivyidea.intellij.ui.IvyIdeaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaFacetType extends FacetType<IvyIdeaFacet, IvyIdeaFacetConfiguration> {

    public static final FacetTypeId<IvyIdeaFacet> ID = new FacetTypeId<IvyIdeaFacet>("IvyIDEA");

    public IvyIdeaFacetType() {
        super(ID, "IvyIDEA", "IvyIDEA");
    }

    public IvyIdeaFacetConfiguration createDefaultConfiguration() {
        return new IvyIdeaFacetConfiguration();
    }

    public IvyIdeaFacet createFacet(@NotNull Module module, String name, @NotNull IvyIdeaFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
        return new IvyIdeaFacet(this, module, name, configuration, underlyingFacet);
    }

    public javax.swing.Icon getIcon() {
        return IvyIdeaIcons.MAIN_ICON_SMALL;
    }

    public void registerDetectors(FacetDetectorRegistry<IvyIdeaFacetConfiguration> ivyIdeaDetectorRegistry) {
        VirtualFileFilter virtualFileFilter = new VirtualFileFilter() {
            public boolean accept(VirtualFile file) {
                return "ivy.xml".equals(file.getName());
            }
        };
        ivyIdeaDetectorRegistry.registerUniversalDetector(IvyFileType.IVY_FILE_TYPE, virtualFileFilter, new FacetDetector<VirtualFile, IvyIdeaFacetConfiguration>() {
            public IvyIdeaFacetConfiguration detectFacet(VirtualFile source, Collection<IvyIdeaFacetConfiguration> existentFacetConfigurations) {
                return configureDetectedFacet(source, existentFacetConfigurations);
            }
        });

        ivyIdeaDetectorRegistry.registerOnTheFlyDetector(IvyFileType.IVY_FILE_TYPE, virtualFileFilter, new Condition<PsiFile>() {
            public boolean value(PsiFile psiFile) {
                return true;
            }
        }, new FacetDetector<PsiFile, IvyIdeaFacetConfiguration>() {
            public IvyIdeaFacetConfiguration detectFacet(PsiFile source, Collection<IvyIdeaFacetConfiguration> existentFacetConfigurations) {
                return configureDetectedFacet(source.getVirtualFile(), existentFacetConfigurations);
            }
        });

        ivyIdeaDetectorRegistry.registerDetectorForWizard(IvyFileType.IVY_FILE_TYPE, virtualFileFilter, new FacetDetector<VirtualFile, IvyIdeaFacetConfiguration>() {

            public IvyIdeaFacetConfiguration detectFacet(VirtualFile source, Collection<IvyIdeaFacetConfiguration> existentFacetConfigurations) {
                return configureDetectedFacet(source, existentFacetConfigurations);
            }
        });

    }

    protected IvyIdeaFacetConfiguration configureDetectedFacet(VirtualFile ivyFile, Collection<IvyIdeaFacetConfiguration> existingFacetConfigurations) {
        if (existingFacetConfigurations.isEmpty()) {
            final IvyIdeaFacetConfiguration defaultConfiguration = createDefaultConfiguration();
            defaultConfiguration.setIvyFile(ivyFile.getPath());
            return defaultConfiguration;
        } else {
            // TODO: only use file that is the closest to the iml file!
            //              http://code.google.com/p/ivyidea/issues/detail?id=1
            return existingFacetConfigurations.iterator().next();
        }
    }
}
