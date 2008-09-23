package org.clarent.ivyidea.ivy;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.settings.IvySettings;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Guy Mahieu
 */

public class IntellijProjectIvyInfo {

    private Map<Module, ModuleRevisionId> intellijModuleRevisionInfo = new HashMap<Module, ModuleRevisionId>();

    public IntellijProjectIvyInfo(Project project) {
        final Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            // TODO: Keep ivy instances per settings file so we don't configure for every module!!!
            final IvySettings ivySettings = new IvyWrapper(module).getIvy().getSettings();
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
