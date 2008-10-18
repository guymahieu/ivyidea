package org.clarent.ivyidea.tempstuff;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;

/**
 * @author Guy Mahieu
 */

public class IvyFileMonitor extends VirtualFileAdapter {

    Project project;

    public IvyFileMonitor(Project project) {
        this.project = project;
    }

    public void contentsChanged(VirtualFileEvent event) {
//        final VirtualFile changedFile = event.getFile();
        // TODO: link virtualfile to module, check if it is the ivy file from the facet config and refresh if so...
    }
}
