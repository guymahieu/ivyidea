package org.clarent.ivyidea.tempstuff;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: guy
 * Date: 27-apr-2008
 * Time: 19:22:03
 * To change this template use File | Settings | File Templates.
 */
public class EclipseLibKeeperModuleComponent implements ModuleComponent {

    public static final String ECLIPSE_GROUP_LIB_PATH = "ivy/eclipse";

    private Module module;

    public EclipseLibKeeperModuleComponent(Module module) {
        this.module = module;
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    public void moduleAdded() {
        String path = module.getModuleFilePath();
        System.out.println("path = " + path);
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "LIBKEEPER";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }
}
