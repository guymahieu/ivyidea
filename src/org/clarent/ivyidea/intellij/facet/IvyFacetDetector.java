package org.clarent.ivyidea.intellij.facet;

import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Collection;

/**
 * @author Guy Mahieu
 */

public class IvyFacetDetector extends FacetDetector<VirtualFile, IvyFacetConfiguration> {

    public IvyFacetConfiguration detectFacet(VirtualFile source, Collection<IvyFacetConfiguration> existentFacetConfigurations) {
        return null;
    }
}
