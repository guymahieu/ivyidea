package org.clarent.ivyidea.intellij.ui;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.IvyIdeaProjectSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Guy Mahieu
 */

@State(
        name = IvyIdeaProjectSettingsComponent.COMPONENT_NAME,
        storages = {@Storage(id = "IvyIDEA", file = "$PROJECT_FILE$")}
)
public class IvyIdeaProjectSettingsComponent implements ProjectComponent, Configurable, PersistentStateComponent<IvyIdeaProjectSettings> {

    public static final String COMPONENT_NAME = "IvyIDEA.ProjectSettings";

    private boolean modified;
    private TextFieldWithBrowseButton txtIvySettingsFile;
    private JPanel projectSettingsPanel;
    private JCheckBox chkValidateIvyFiles;
    private IvyIdeaProjectSettings internalState;

    public IvyIdeaProjectSettingsComponent() {
        UserActivityWatcher watcher = new UserActivityWatcher();
        watcher.addUserActivityListener(new UserActivityListener() {

            public void stateChanged() {
                modified = true;
            }
        });
        watcher.register(projectSettingsPanel);

        txtIvySettingsFile.addBrowseFolderListener("Select ivy settings file", "", IntellijUtils.getCurrentProject(), new FileChooserDescriptor(true, false, false, false, false, false));
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
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
        if (internalState == null) {
            internalState = new IvyIdeaProjectSettings();
        }
        internalState.setIvySettingsFile(txtIvySettingsFile.getText());
        internalState.setValidateIvyFiles(chkValidateIvyFiles.isSelected());
    }

    public void reset() {
        if (internalState != null) {
            txtIvySettingsFile.setText(internalState.getIvySettingsFile());
            chkValidateIvyFiles.setSelected(internalState.isValidateIvyFiles());
        } else {
            txtIvySettingsFile.setText("");
            chkValidateIvyFiles.setSelected(false);
        }
    }

    public void disposeUIResources() {
    }

    public IvyIdeaProjectSettings getState() {
        if (internalState == null) {
            internalState = new IvyIdeaProjectSettings();
        }
        return internalState;
    }

    public void loadState(IvyIdeaProjectSettings state) {
        if (state == null) {
            state = new IvyIdeaProjectSettings();
        }
        this.internalState = state;
    }
}
