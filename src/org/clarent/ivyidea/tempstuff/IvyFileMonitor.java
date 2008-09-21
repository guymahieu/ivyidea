package org.clarent.ivyidea.tempstuff;

import com.intellij.openapi.vfs.*;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import org.clarent.ivyidea.config.PostIvyPluginConfiguration;
import org.clarent.ivyidea.DependencyUpdater;

/**
 * @author Guy Mahieu
 */

public class IvyFileMonitor extends VirtualFileAdapter {

    Project project;

    public IvyFileMonitor(Project project) {
        this.project = project;
    }

    public void contentsChanged(VirtualFileEvent event) {
        final String ivyFile = PostIvyPluginConfiguration.getCurrent().getIvyModuleDescriptorFileName();
        final VirtualFile changedFile = event.getFile();
        if (ivyFile.equals(changedFile)) {
            DependencyUpdater.setupLibraries(ModuleUtil.findModuleForFile(changedFile, project));            
        }
    }
}
