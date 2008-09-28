package org.clarent.ivyidea.ivy;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.settings.IvySettings;
import org.clarent.ivyidea.intellij.IntellijUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Guy Mahieu
 */

public class IntellijProjectIvyInfo {

    private Map<Module, ModuleRevisionId> intellijModuleRevisionInfo = new HashMap<Module, ModuleRevisionId>();

    public IntellijProjectIvyInfo(Project project) {
        final IvyManager ivyManager = new IvyManager();
        for (Module module : IntellijUtils.getAllModules(project)) {
            final IvySettings ivySettings = ivyManager.getIvy(module).getSettings();
            final ModuleDescriptor ivyModuleDescriptor = IvyUtil.getIvyModuleDescriptor(module, ivySettings);
            if (ivyModuleDescriptor != null) {
                intellijModuleRevisionInfo.put(module, ivyModuleDescriptor.getModuleRevisionId());
            }
        }
    }

    public ModuleRevisionId getRevisionId(Module module) {
        return intellijModuleRevisionInfo.get(module);
    }


}
