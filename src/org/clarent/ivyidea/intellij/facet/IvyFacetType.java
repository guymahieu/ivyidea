package org.clarent.ivyidea.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.clarent.ivyidea.intellij.IvyFileType;

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
        
        ivyIdeaDetectorRegistry.registerUniversalDetector(new IvyFileType(), virtualFileFilter, new IvyFacetDetector());

/*
        VirtualFileFilter virtualFileFilter = new VirtualFileFilter() {
            public boolean accept(VirtualFile file) {
                List<String> headersToDetect = new ArrayList<String>(Arrays.asList(DETECTION_HEADERS));
                BufferedReader bufferedReader = null;
                try {
                    InputStream inputStream = file.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    bufferedReader = new BufferedReader(inputStreamReader);

                    while (bufferedReader.ready() && headersToDetect.size() > 0) {
                        String line = bufferedReader.readLine();
                        for (Iterator<String> headersToDetectIterator = headersToDetect.iterator();
                             headersToDetectIterator.hasNext();) {
                            String headertoDeteect = headersToDetectIterator.next();
                            if (line.startsWith(headertoDeteect)) {
                                headersToDetectIterator.remove();
                                break;
                            }
                        }
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
                finally {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                return headersToDetect.size() == 0;
            }
        };
        FacetDetector<VirtualFile, IvyFacetConfiguration> detector =
                new FacetDetector<VirtualFile, IvyFacetConfiguration>() {
                    public IvyFacetConfiguration detectFacet(VirtualFile source, Collection<IvyFacetConfiguration> existentFacetConfigurations) {
                        if (!existentFacetConfigurations.isEmpty()) {
                            return existentFacetConfigurations.iterator().next();
                        }
                        return createDefaultConfiguration();
                    }
                };


        ivyIdeaDetectorRegistry.registerUniversalDetector(ManifestApplicationComponent.MANIFEST,
                virtualFileFilter, detector);*/
    }

}
