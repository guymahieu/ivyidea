package org.clarent.ivyidea.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.clarent.ivyidea.intellij.externalsystem.IvyIdeaExternalSystem;
import org.jetbrains.annotations.NotNull;

public class IvyIdeaStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        IvyIdeaExternalSystem.init(project);
    }
}
