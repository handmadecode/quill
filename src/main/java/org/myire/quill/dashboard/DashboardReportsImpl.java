/*
 * Copyright 2015, 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dashboard;

import java.io.File;

import groovy.lang.Closure;

import org.gradle.api.Project;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.reporting.internal.TaskReportContainer;

import org.myire.quill.common.Projects;
import org.myire.quill.report.DefaultSingleFileReport;


/**
 * Default implementation of {@code DashboardReports}.
 */
class DashboardReportsImpl extends TaskReportContainer<Report> implements DashboardReports
{
    static private final String HTML_REPORT_NAME = "html";


    /**
     * Create a new {@code DashboardReportsImpl}.
     *
     * @param pTask The task that owns this reports instance.
     */
    DashboardReportsImpl(DashboardTask pTask)
    {
        super(ConfigurableReport.class, pTask);

        // Add the HTML report.
        SingleFileReport aHtmlReport =
            add(DefaultSingleFileReport.class,
                pTask.getProject(),
                HTML_REPORT_NAME,
                "Dashboard HTML report",
                new DefaultReportDestination(pTask.getProject()));

        // The report is enabled by default.
        aHtmlReport.setEnabled(true);
    }


    /**
     * Get the dashboard HTML file report. Default is a file called &quot;dashboard.html&quot; in
     * the project report directory.
     */
    @Override
    public SingleFileReport getHtml()
    {
        return (SingleFileReport) getByName(HTML_REPORT_NAME);
    }


    /**
     * Closure for lazily evaluating the default dashboard report file spec.
     */
    static private class DefaultReportDestination extends Closure<File>
    {
        private final Project fProject;

        DefaultReportDestination(Project pProject)
        {
            super(null);
            fProject = pProject;
        }

        public File doCall(Object pValue)
        {
            return Projects.createReportDirectorySpec(fProject, "reportsDashboard.html");
        }
    }
}
