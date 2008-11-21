package org.clarent.ivyidea.exception.ui;

import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.labels.LinkLabel;

import javax.swing.*;

/**
 * @author Guy Mahieu
 */

public class IvyIdeaExceptionDialog {

    private JPanel rootPanel;
    private JTextArea txtMessage;
    private LinkLabel lblLink;

    public static void showModalDialog(String title, Throwable exception, Project project) {
        showModalDialog(title, exception, project, null);
    }

    public static void showModalDialog(String title, Throwable exception, Project project, LinkBehavior linkBehavior) {
        final DialogBuilder builder = new DialogBuilder(project);
        final IvyIdeaExceptionDialog dlg = new IvyIdeaExceptionDialog();
        dlg.buildMessageFromThrowable(exception);
        dlg.setLinkBehavior(linkBehavior);
        builder.setCenterPanel(dlg.getRootPanel());
        builder.setOkActionEnabled(true);
        builder.setOkOperation(null);
        builder.setTitle(title);
        builder.showModal(false);
    }

    public IvyIdeaExceptionDialog() {
        // by default do not show a link
        lblLink.setVisible(false);
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public void setMessage(String message) {
        txtMessage.setText(message);
        // scroll to top
        txtMessage.setSelectionStart(0);
        txtMessage.setSelectionEnd(0);
    }

    public void buildMessageFromThrowable(Throwable exception) {
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

    public void setLinkBehavior(LinkBehavior linkBehavior) {
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

}
