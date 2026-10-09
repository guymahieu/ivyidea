package org.clarent.ivyidea.intellij.externalsystem;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.externalSystem.autoimport.*;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.ivy.IvyUtil;
import org.clarent.ivyidea.resolve.ResolveActionHelper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class IvyIdeaExternalSystem implements ExternalSystemProjectAware {
    private static final Logger LOGGER = Logger.getLogger(IvyIdeaExternalSystem.class.getName());
    private static final ProjectSystemId SYSTEM_ID = new ProjectSystemId("IvyIdeaSystem", "IvyIDEA");
    private static final Map<Project, IvyIdeaExternalSystem> externalSystems = new IdentityHashMap<>();

    public static void init(Project project) {
        if (!externalSystems.containsKey(project)) {
            LOGGER.info("init for project '" + project.getName() + "'");
            IvyIdeaExternalSystem externalSystem = new IvyIdeaExternalSystem(project);
            ExternalSystemProjectTracker tracker = ExternalSystemProjectTracker.getInstance(project);
            tracker.register(externalSystem, project);

            // For some reason, firing these events is needed during startup.
            // otherwise IntelliJ does not show the reload button after manually editing the ivy.xml file(s).
            IvyIdeaListener listener = project.getMessageBus().syncPublisher(IvyIdeaListener.TOPIC);
            listener.resolveStarted();
            listener.resolveFinished();

            externalSystems.put(project, externalSystem);
        } else {
            LOGGER.info("skip repeated init for project '" + project.getName() + "'");
        }
    }

    private final Project project;
    private final ExternalSystemProjectId externalSystemProjectId;

    public IvyIdeaExternalSystem(@NotNull Project project) {
        this.project = project;
        String basePath = project.getBasePath();
        this.externalSystemProjectId = new ExternalSystemProjectId(SYSTEM_ID, basePath == null ? "" : basePath);
    }

    @NotNull
    @Override
    public ExternalSystemProjectId getProjectId() {
        return externalSystemProjectId;
    }

    @NotNull
    @Override
    public Set<String> getSettingsFiles() {
        Set<String> settingsFiles = new HashSet<>();

        for (Module module : IntellijUtils.getAllModulesWithIvyIdeaFacet(project)) {
            File ivyFile = IvyUtil.getIvyFile(module);
            if (ivyFile != null && ivyFile.isFile()) {
                settingsFiles.add(FileUtilRt.toSystemIndependentName(ivyFile.getAbsolutePath()));
            }

            try {
                String ivySettings = IvyIdeaConfigHelper.getIvySettingsFile(module);
                if (ivySettings != null) {
                    File ivySettingsFile = new File(ivySettings);
                    if (ivySettingsFile.isFile()) { // "lazy" check to ignore URLs
                        settingsFiles.add(FileUtilRt.toSystemIndependentName(ivySettingsFile.getAbsolutePath()));
                    }
                }
            } catch (IvySettingsNotFoundException ignored) {
            }
        }

        LOGGER.info("getSettingsFiles found " + settingsFiles.size() + " files for project '" + project.getName() + "'");
        return settingsFiles;
    }

    @Override
    public void subscribe(@NotNull ExternalSystemProjectListener externalSystemProjectListener, @NotNull Disposable disposable) {
        LOGGER.info("subscribing to external system");
        project.getMessageBus().connect(disposable).subscribe(IvyIdeaListener.TOPIC, new IvyIdeaListener() {
            @Override
            public void resolveStarted() {
                LOGGER.info("on resolveStarted");
                externalSystemProjectListener.onProjectReloadStart();
            }

            @Override
            public void resolveFinished() {
                LOGGER.info("on resolveFinished");
                externalSystemProjectListener.onProjectReloadFinish(ExternalSystemRefreshStatus.SUCCESS);
            }
        });
    }

    @Override
    public void reloadProject(@NotNull ExternalSystemProjectReloadContext reloadCtx) {
        LOGGER.info("reloadProject()");
        ResolveActionHelper.resolveForProject(project, null, null);
    }
}