/*
 * Copyright 2015, 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.junit

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

import org.myire.quill.common.Projects


/**
 * Gradle plugin for adding enhancements to the JUnit test task.
 */
class JUnitAdditionsPlugin implements Plugin<Project>
{
    static final String SUMMARY_REPORT_NAME = 'junitSummaryReport'


    @Override
    void apply(Project pProject)
    {
        Test aTestTask = Projects.getTask(pProject, "test", Test.class)
        if (aTestTask != null)
        {
            // Create a JUnit summary report, enable it and add it to the task's convention.
            JUnitSummaryReport aReport = new JUnitSummaryReport(aTestTask);
            aReport.enabled = true;
            aTestTask.convention.add(SUMMARY_REPORT_NAME, aReport);

            // Add the report to the test task's update check to allow triggering the tests if the
            // summary report is out-of-date.
            aTestTask.outputs.upToDateWhen({ aReport.checkUpToDate() });

            // Add a task action to create the summary report.
            aTestTask.doLast({ aReport.createReport() });
        }
    }
}
