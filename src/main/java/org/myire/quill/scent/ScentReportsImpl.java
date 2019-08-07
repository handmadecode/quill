/*
 * Copyright 2016, 2018-2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.scent;

import java.io.File;

import groovy.lang.Closure;

import org.gradle.api.Project;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.util.ConfigureUtil;

import org.myire.quill.common.Projects;
import org.myire.quill.report.DefaultSingleFileReport;
import org.myire.quill.report.ReportTransformingReport;
import org.myire.quill.report.TransformingReport;


/**
 * Default implementation of {@code ScentReports}.
 */
class ScentReportsImpl implements ScentReports
{
    static private final String XML_REPORT_NAME = "scentXml";
    static private final String HTML_REPORT_NAME = "scentHtml";
    static private final String BUILTIN_SCENT_XSL = "/org/myire/quill/rsrc/report/scent/scent.xsl";

    private final SingleFileReport fXmlReport;
    private final TransformingReport fHtmlReport;


    /**
     * Create a new {@code ScentReportsImpl}.
     *
     * @param pTask The task that owns this reports instance.
     */
    ScentReportsImpl(ScentTask pTask)
    {
        Project aProject = pTask.getProject();

        fXmlReport =
            new DefaultSingleFileReport(
                aProject,
                XML_REPORT_NAME,
                "Scent XML report",
                new DefaultXmlReportDestination(aProject));

        fHtmlReport =
            new ReportTransformingReport(
                aProject,
                HTML_REPORT_NAME,
                "Scent HTML report",
                fXmlReport,
                BUILTIN_SCENT_XSL);

        // Both reports are enabled by default.
        fXmlReport.setEnabled(true);
        fHtmlReport.setEnabled(true);
    }


    /**
     * Get the XML report. Default is a file called &quot;scent.xml&quot; located in a directory
     * called &quot;scent&quot; in the project's report directory or, if no project report directory
     * is defined, in a subdirectory called &quot;scent&quot; in the project's build directory.
     *
     * @return  The XML report.
     */
    @Override
    public SingleFileReport getXml()
    {
        return fXmlReport;
    }


    /**
     * Get the HTML report. Default is a transforming report located next to the XML report and
     * using the built-in Scent XSL.
     *
     * @return  The HTML report.
     */
    @Override
    public TransformingReport getHtml()
    {
        return fHtmlReport;
    }


    @Override
    public Report getReportByName(String pReportName)
    {
        if (XML_REPORT_NAME.equalsIgnoreCase(pReportName))
            return fXmlReport;
        else if (HTML_REPORT_NAME.equalsIgnoreCase(pReportName))
            return fHtmlReport;
        else
            return null;
    }


    @Override
    public ScentReports configure(Closure pClosure)
    {
        ConfigureUtil.configureSelf(pClosure, this);
        return this;
    }


    /**
     * Closure for lazily evaluating the default XML report file spec.
     */
    static private class DefaultXmlReportDestination extends Closure<File>
    {
        private final Project fProject;

        DefaultXmlReportDestination(Project pProject)
        {
            super(null);
            fProject = pProject;
        }

        public File doCall(Object pValue)
        {
            return new File(Projects.createReportDirectorySpec(fProject, "scent"), "scent.xml");
        }
    }
}
