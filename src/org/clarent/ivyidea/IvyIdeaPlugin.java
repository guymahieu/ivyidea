package org.clarent.ivyidea;

import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.ExcactFileNameMatcher;
import org.clarent.ivyidea.intellij.facet.IvyFacetType;
import org.clarent.ivyidea.intellij.IvyFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaPlugin implements ApplicationComponent {

    @NonNls
    @NotNull
    public String getComponentName() {
        return "IvyIDEA.PluginApplicationComponent";
    }

    public void initComponent() {
        FacetTypeRegistry.getInstance().registerFacetType(new IvyFacetType());

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                List<FileNameMatcher> matchers = new ArrayList<FileNameMatcher>();
                matchers.add(new ExcactFileNameMatcher("ivy.xml"));
                FileTypeManager.getInstance().registerFileType(IvyFileType.IVY_FILE_TYPE, matchers);
            }
        });


    }

    public void disposeComponent() {
    }
}