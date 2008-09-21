package org.clarent.ivyidea.tempstuff;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class TestProjectComponent implements ProjectComponent {

    public TestProjectComponent(Project project) {

    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    public void initComponent() {
        
    }

    public void disposeComponent() {
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "TEST";
    }


}
