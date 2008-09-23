package org.clarent.ivyidea.intellij.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.clarent.ivyidea.intellij.facet.ui.IvyFacetEditorTab;

/**
 * @author Guy Mahieu
 */

public class IvyFacetConfiguration implements FacetConfiguration {

    private String ivyFile;

    public String getIvyFile() {
        return ivyFile;
    }

    public void setIvyFile(String ivyFile) {
        this.ivyFile = ivyFile;
    }

    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        return new FacetEditorTab[] { new IvyFacetEditorTab(editorContext) };
    }

    public void readExternal(Element element) throws InvalidDataException {
        setIvyFile(element.getAttributeValue("ivyFile"));
    }

    public void writeExternal(Element element) throws WriteExternalException {
        if (ivyFile != null) {
            element.setAttribute("ivyFile", ivyFile);
        }
    }
}
