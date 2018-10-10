/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.internal.AbstractTask
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.Reporting

import org.myire.quill.common.Projects


/**
 * A task that holds additional reports for other tasks that for some reason cannot be added to
 * those tasks' report containers. The reason for this is most likely that these tasks have
 * immutable report containers. By adding those additional reports to this task they can still be
 * picked up by the {@code build-dashboard} plugin.
 */
class AdditionalReportsTask extends AbstractTask implements Reporting<ReportContainer>
{
    static private final String TASK_NAME = "additionalReports";


    // Property accessed through getter and setter only.
    private MutableReportContainer fReports;


    /**
     * Get the {@code AdditionalReportsTask} from a project, creating it if it doesn't exist.
     *
     * @param pProject  The project to get the task from.
     *
     * @return  The project's {@code AdditionalReportsTask}.
     */
    static public AdditionalReportsTask maybeCreate(Project pProject)
    {
        AdditionalReportsTask aTask = Projects.getTask(pProject, TASK_NAME, AdditionalReportsTask.class);
        if (aTask == null)
        {
            aTask = pProject.tasks.create(TASK_NAME, AdditionalReportsTask.class)
            aTask.description = 'Placeholder task for reports logically produced by other tasks';
            aTask.createReports();
        }

        return aTask;
    }


    /**
     * Create the task's report container.
     */
    void createReports()
    {
        fReports = new MutableReportContainer(this);
    }


    @Override
    ReportContainer getReports()
    {
        return fReports;
    }


    @Override
    ReportContainer reports(Closure pClosure)
    {
        return fReports.configure(pClosure);
    }


    @Override
    ReportContainer reports(Action<? super ReportContainer> pAction)
    {
        pAction.execute(fReports);
        return fReports;
    }
}
