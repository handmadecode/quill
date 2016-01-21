/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dashboard

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import org.myire.quill.common.Projects


/**
 * Gradle plugin for adding a dashboard report task to a project.
 */
class DashboardPlugin implements Plugin<Project>
{
    static final String DASHBOARD_TASK_NAME = 'reportsDashboard'


    private Project fProject


    @Override
    void apply(Project pProject)
    {
        fProject = pProject;
        createDashboardReportTask();
    }


    private DashboardTask createDashboardReportTask()
    {
        DashboardTask aTask = fProject.tasks.create(DASHBOARD_TASK_NAME, DashboardTask.class);
        aTask.init();
        aTask.description = 'Creates an HTML report with a summary of the build reports'

        // Trigger the dashboard task when the build task has executed.
        Projects.getTask(fProject, 'build', Task.class)?.finalizedBy(aTask);

        return aTask;
    }
}
