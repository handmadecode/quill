/*
 * Copyright 2019-2021 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jacoco;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.gradle.testing.jacoco.tasks.JacocoReportsContainer;

import org.myire.quill.common.Projects;
import org.myire.quill.report.Reports;


/**
 * Gradle plugin for applying the Jacoco plugin and overriding some of the properties in the related
 * extensions and tasks.
 */
public class JacocoAdditionsPlugin implements Plugin<Project>
{
    static private final String DEFAULT_TOOL_VERSION = "0.8.6";
    static private final String JACOCO_TEST_REPORT_TASK_NAME = "jacocoTestReport";


    @Override
    public void apply(Project pProject)
    {
        // Make sure the Java plugin is applied so that Jacoco adds a test report task.
        pProject.getPlugins().apply(JavaPlugin.class);

        // Apply the Jacoco plugin.
        pProject.getPlugins().apply(JacocoPlugin.class);

        // Configure the Jacoco extension.
        configureExtension(pProject);

        // Configure the Jacoco report task.
        configureReportTask(pProject);
    }


    private void configureExtension(Project pProject)
    {
        JacocoPluginExtension aExtension =
            Projects.getExtension(pProject, JacocoPluginExtension.TASK_EXTENSION_NAME, JacocoPluginExtension.class);
        if (aExtension != null)
            aExtension.setToolVersion(DEFAULT_TOOL_VERSION);
    }


    private void configureReportTask(Project pProject)
    {
        JacocoReport aTask = Projects.getTask(pProject, JACOCO_TEST_REPORT_TASK_NAME, JacocoReport.class);
        if (aTask != null)
        {
            JacocoReportsContainer aReports = aTask.getReports();
            Reports.setRequired(aReports.getXml(), true);
            Reports.setRequired(aReports.getHtml(), true);
            Reports.setRequired(aReports.getCsv(), false);

            // Add the Jacoco report task to the build task's dependencies.
            Task aBuildTask = Projects.getTask(pProject, "build", Task.class);
            if (aBuildTask != null)
                aBuildTask.dependsOn(aTask);
        }
    }
}
