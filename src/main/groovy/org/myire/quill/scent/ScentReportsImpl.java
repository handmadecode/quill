/*
 * Copyright 2016 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.scent;

import java.io.File;

import groovy.lang.Closure;

import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.reporting.internal.TaskReportContainer;

import org.myire.quill.report.DefaultSingleFileReport;
import org.myire.quill.report.ReportTransformingReport;
import org.myire.quill.report.TransformingReport;


/**
 * Default implementation of {@code ScentReports}. See {@link org.myire.quill.cpd.CpdReportsImpl}
 * for a discussion on why the implementation is in Java rather than Groovy.
 */
public class ScentReportsImpl extends TaskReportContainer<Report> implements ScentReports
{
    static private final String XML_REPORT_NAME = "scentXml";
    static private final String HTML_REPORT_NAME = "scentHtml";
    static private final String BUILTIN_SCENT_XSL = "/org/myire/quill/rsrc/report/scent/scent.xsl";


    /**
     * Create a new {@code ScentReportsImpl}.
     *
     * @param pTask The task that owns this reports instance.
     */
    ScentReportsImpl(ScentTask pTask)
    {
        super(ConfigurableReport.class, pTask);

        // Add the XML report.
        SingleFileReport aXmlReport = add(DefaultSingleFileReport.class,
                                          pTask.getProject(),
                                          XML_REPORT_NAME,
                                          "Scent XML report",
                                          new DefaultXmlReportDestination(pTask));

        // Add the HTML report.
        TransformingReport aHtmlReport = add(ReportTransformingReport.class,
                                             pTask.getProject(),
                                             HTML_REPORT_NAME,
                                             "Scent HTML report",
                                             aXmlReport,
                                             BUILTIN_SCENT_XSL);

        // Both reports are enabled by default.
        aXmlReport.setEnabled(true);
        aHtmlReport.setEnabled(true);
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
        return (SingleFileReport) getByName(XML_REPORT_NAME);
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
        return (TransformingReport) getByName(HTML_REPORT_NAME);
    }


    /**
     * Closure for lazily evaluating the default XML report file spec.
     */
    static private class DefaultXmlReportDestination extends Closure<File>
    {
        private final ScentTask fTask;

        DefaultXmlReportDestination(ScentTask pTask)
        {
            super(null);
            fTask = pTask;
        }

        public File doCall(Object pValue)
        {
            return fTask.defaultXmlDestination();
        }
    }
}
