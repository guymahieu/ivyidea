package org.clarent.ivyidea.exception.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.labels.LinkLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Standard dialog for showing exception feedback to users from IvyIDEA.
 * Allows you to specify a hyperlink so the user do a single click to navigate to the UI where the problem
 * can be resolved. 
 *
 * @author Guy Mahieu
 */
public class IvyIdeaExceptionDialog extends DialogWrapper {

    private JPanel rootPanel;
    private JTextArea txtMessage;
    private LinkLabel lblLink;

    /**
     * Shows a standard IvyIDEA exception dialog without a hyperlink.
     */
    public static void showModalDialog(String title, Throwable exception, Project project) {
        showModalDialog(title, exception, project, null);
    }

    /**
     * Shows a standard IvyIDEA exception dialog with a hyperlink that takes its parameters from the given linkBehavior.
     */
    public static void showModalDialog(String title, Throwable exception, Project project, LinkBehavior linkBehavior) {
        final IvyIdeaExceptionDialog dlg = new IvyIdeaExceptionDialog(project);
        dlg.setTitle(title);
        dlg.setMessageFromThrowable(exception);
        dlg.setLinkBehavior(linkBehavior);
        dlg.show();
    }

    public IvyIdeaExceptionDialog(Project project) {
        super(project, false);

        setButtonsAlignment(SwingConstants.CENTER);

        // By default we do not show a link
        lblLink.setVisible(false);

        // You have to call this or nothing is shown!
        init();
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public void setMessage(@NotNull String message) {
        txtMessage.setText(message);
        // scroll to top
        txtMessage.setSelectionStart(0);
        txtMessage.setSelectionEnd(0);
    }

    public void setMessageFromThrowable(@NotNull Throwable exception) {
        String message = exception.getMessage() + '\n';
        Throwable cause = exception.getCause();
        int maxDepth = 20;
        int currDepth = 0;
        while (currDepth++ <= maxDepth && cause != null) {
            message += "\nCaused by: " + cause.getMessage();
            cause = cause.getCause();
        }
        if (cause != null) {
            message += "\nMore causes skipped.";
        }
        setMessage(message);
    }

    public void setLinkBehavior(@Nullable LinkBehavior linkBehavior) {
        if (linkBehavior == null || linkBehavior.getLinkText() == null || linkBehavior.getLinkText().trim().length() == 0) {
            lblLink.setText("");
            lblLink.setListener(null, null);
            lblLink.setVisible(false);
        } else {
            lblLink.setText(linkBehavior.getLinkText());
            lblLink.setListener(linkBehavior.getLinkListener(), linkBehavior.getData());
            lblLink.setVisible(true);
        }
    }

    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    @Override
    protected Action[] createActions() {
        return new Action[]{getOKAction()};
    }
}
