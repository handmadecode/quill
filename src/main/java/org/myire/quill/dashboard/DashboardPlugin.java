/*
 * Copyright 2015, 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dashboard;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaBasePlugin;

import org.myire.quill.common.Projects;


/**
 * Gradle plugin for adding a build reports dashboard task to a project.
 */
public class DashboardPlugin implements Plugin<Project>
{
    @Override
    public void apply(Project pProject)
    {
        // Make sure the Java base plugin is applied. This will create the build task, and if that
        // task is available when the reportsDashboard task is created, the former will configured
        // to be finalized by the latter, which will trigger the execution of reportsDashboard when
        // the build task has finished.
        pProject.getPlugins().apply(JavaBasePlugin.class);

        // Create the reportsDashboard task.
        DashboardTask aTask = pProject.getTasks().create(DashboardTask.TASK_NAME, DashboardTask.class);
        aTask.init();
        aTask.setDescription("Creates an HTML report with a summary of the build reports");

        // Trigger the dashboard task when the build task has executed.
        Task aBuildTask = Projects.getTask(pProject, "build", Task.class);
        if (aBuildTask != null)
            aBuildTask.finalizedBy(aTask);
    }
}
