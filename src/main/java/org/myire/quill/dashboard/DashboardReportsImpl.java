/*
 * Copyright 2015, 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dashboard;

import java.io.File;

import groovy.lang.Closure;

import org.gradle.api.Project;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.util.ConfigureUtil;

import org.myire.quill.common.Projects;
import org.myire.quill.report.DefaultSingleFileReport;


/**
 * Default implementation of {@code DashboardReports}.
 */
class DashboardReportsImpl implements DashboardReports
{
    static private final String HTML_REPORT_NAME = "html";


    private final SingleFileReport fHtmlReport;


    /**
     * Create a new {@code DashboardReportsImpl}.
     *
     * @param pTask The task that owns this reports instance.
     */
    DashboardReportsImpl(DashboardTask pTask)
    {
        fHtmlReport =
            new DefaultSingleFileReport(
                pTask.getProject(),
                HTML_REPORT_NAME,
                "Dashboard HTML report",
                new DefaultReportDestination(pTask.getProject()));

        // The report is enabled by default.
        fHtmlReport.setEnabled(true);
    }


    /**
     * Get the dashboard HTML file report. Default is a file called &quot;dashboard.html&quot; in
     * the project report directory.
     */
    @Override
    public SingleFileReport getHtml()
    {
        return fHtmlReport;
    }


    @Override
    public Report getReportByName(String pReportName)
    {
        return HTML_REPORT_NAME.equalsIgnoreCase(pReportName) ? fHtmlReport : null;
    }


    @Override
    public DashboardReports configure(Closure pClosure)
    {
        ConfigureUtil.configureSelf(pClosure, this);
        return this;
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
