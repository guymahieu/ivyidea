package org.clarent.ivyidea.ivy;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Guy Mahieu
 */

public class IntellijProjectIvyInfo {

    private Map<Module, ModuleRevisionId> intellijModuleRevisionInfo = new HashMap<Module, ModuleRevisionId>();

    public IntellijProjectIvyInfo(Project project) {
        final Module[] modules = ModuleManager.getInstance(project).getModules();
        final IvyHelper ivyHelper = new IvyHelper();
        for (Module module : modules) {
            final ModuleDescriptor ivyModuleDescriptor = ivyHelper.getIvyModuleDescriptor(module);
            intellijModuleRevisionInfo.put(module, ivyModuleDescriptor.getModuleRevisionId());
        }
    }

    public ModuleRevisionId getRevisionId(Module module) {
        return intellijModuleRevisionInfo.get(module);
    }


}
