package org.clarent.ivyidea.intellij;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;

public class IvyIdeaConsoleService {

    private final ConsoleView consoleView;

    public IvyIdeaConsoleService(Project project) {
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
    }

    public ConsoleView getConsoleView() {
        return consoleView;
    }

}
