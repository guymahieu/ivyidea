package org.clarent.ivyidea.logging;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.apache.ivy.util.AbstractMessageLogger;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;

import static com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT;

public class ConsoleViewMessageLogger extends AbstractMessageLogger {

    private final ConsoleView consoleView;
    private final IvyLogLevel threshold;

    public ConsoleViewMessageLogger(final Project project, final ConsoleView consoleView) {
        this.consoleView = consoleView;
        threshold = IvyIdeaConfigHelper.getIvyLoggingThreshold(project);
    }

    public void log(final String msg, final int level) {
        rawlog(msg, level);
    }

    public void rawlog(final String msg, final int level) {
        rawlog(msg, IvyLogLevel.fromLevelCode(level));
    }

    public void rawlog(final String message, final IvyLogLevel logLevelForMessage) {
        if (threshold.isMoreVerboseThan(logLevelForMessage)) {
            logToConsoleView(message + "\n", logLevelForMessage.getContentType());
        }
    }

    protected void doProgress() {
        logToConsoleView(".", SYSTEM_OUTPUT);
    }

    protected void doEndProgress(final String msg) {
        logToConsoleView(msg + '\n', SYSTEM_OUTPUT);
    }

    private void logToConsoleView(final String message, final ConsoleViewContentType contentType) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                consoleView.print(message, contentType);
            }
        });
    }
}