package org.clarent.ivyidea.intellij.ui;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ComponentConfig;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.ui.UserActivityWatcher;
import com.intellij.ui.UserActivityListener;
import com.intellij.ide.DataManager;

import javax.swing.*;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.clarent.ivyidea.intellij.IvyFileType;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaProjectSettingsComponent implements ProjectComponent, Configurable {

    private boolean modified;

    private TextFieldWithBrowseButton txtIvySettingsFile;
    private JPanel projectSettingsPanel;

    public IvyIdeaProjectSettingsComponent() {
        UserActivityWatcher watcher = new UserActivityWatcher();
        watcher.addUserActivityListener(new UserActivityListener() {

            public void stateChanged() {
                modified = true;
            }
        });
        watcher.register(projectSettingsPanel);

        final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        descriptor.setNewFileType(IvyFileType.IVY_FILE_TYPE);
        DataContext dataContext = DataManager.getInstance().getDataContext();
        Project project = DataKeys.PROJECT.getData(dataContext);
        txtIvySettingsFile.addBrowseFolderListener("Select ivy file", "", project, descriptor);
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "IvyIDEA.ProjectSettings";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @Nls
    public String getDisplayName() {
        return "IvyIDEA";
    }

    @Nullable
    public Icon getIcon() {
        return IvyIdeaIcons.MAIN_ICON;
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        return projectSettingsPanel;
    }

    public boolean isModified() {
        return modified;
    }

    public void apply() throws ConfigurationException {
    }

    public void reset() {
    }

    public void disposeUIResources() {
    }
}
