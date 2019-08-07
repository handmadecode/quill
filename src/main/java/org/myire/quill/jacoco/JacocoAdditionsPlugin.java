/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jacoco;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.gradle.testing.jacoco.tasks.JacocoReportsContainer;
import org.gradle.util.VersionNumber;

import org.myire.quill.common.Projects;


/**
 * Gradle plugin for applying the Jacoco plugin and overriding some of the properties in the related
 * extensions and tasks.
 */
public class JacocoAdditionsPlugin implements Plugin<Project>
{
    static private final String DEFAULT_TOOL_VERSION = "0.8.4";
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

        // Configure the Test task extension added by the Jacoco plugin.
        pProject.getTasks().withType(Test.class, this::configureJacocoTestExtension);
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
            aReports.getXml().setEnabled(true);
            aReports.getHtml().setEnabled(true);
            aReports.getCsv().setEnabled(false);

            // Add the Jacoco report task to the build task's dependencies.
            Task aBuildTask = Projects.getTask(pProject, "build", Task.class);
            if (aBuildTask != null)
                aBuildTask.dependsOn(aTask);
        }
    }


    private void configureJacocoTestExtension(Test pTestTask)
    {
        Object aExtension = pTestTask.getExtensions().findByName(JacocoPluginExtension.TASK_EXTENSION_NAME);
        if (aExtension instanceof JacocoTaskExtension)
        {
            VersionNumber aGradleVersion = VersionNumber.parse(pTestTask.getProject().getGradle().getGradleVersion());
            if (aGradleVersion.compareTo(VersionNumber.version(5)) < 0)
                // Append property deprecated starting with Gradle v 5.0
                ((JacocoTaskExtension) aExtension).setAppend(false);
        }
    }
}
