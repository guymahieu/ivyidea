package org.clarent.ivyidea.intellij.task;

import javax.swing.Icon;

import com.intellij.util.concurrency.Semaphore;
import org.clarent.ivyidea.intellij.ui.IvyIdeaIcons;
import org.clarent.ivyidea.resolve.ResolveActionHelper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Key;

/**
 * "Before Run" Task that resolves the Ivy dependencies for the current Project.
 */
public class IvyIdeaBeforeRunResolveForProjectTaskProvider extends BeforeRunTaskProvider<IvyIdeaBeforeRunResolveForProjectTaskProvider.Task> implements DumbAware
{
    public static final Key<Task> ID = Key.create( "IvyIdeaResolve" );

    @Override
    public Key<Task> getId()
    {
        return ID;
    }

    @Nls ( capitalization = Nls.Capitalization.Title )
    @Override
    public String getName()
    {
        return "Resolve Ivy Dependencies for Project";
    }

    @Override
    public @Nullable Icon getIcon()
    {
        return IvyIdeaIcons.MAIN_ICON_SMALL;
    }

    @Nullable
    @Override
    public Task createTask( @NotNull RunConfiguration runConfiguration )
    {
        return new Task();
    }

    @Override
    public boolean executeTask( @NotNull DataContext dataContext, @NotNull RunConfiguration runConfiguration, @NotNull ExecutionEnvironment executionEnvironment, @NotNull Task ivyIdeaBeforeRunTask )
    {
        final Semaphore resolveDone = new Semaphore();
        resolveDone.down();

        ResolveActionHelper.resolveForProject(runConfiguration.getProject(), null, resolveDone);

        resolveDone.waitFor();
        return true;
    }

    public static class Task extends BeforeRunTask<Task> implements DumbAware
    {
        protected Task()
        {
            super( IvyIdeaBeforeRunResolveForProjectTaskProvider.ID );
        }
    }
}
