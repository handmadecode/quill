/*
 * Copyright 2015, 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cpd;

import java.io.File;

import groovy.lang.Closure;

import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.internal.TaskReportContainer;

import org.myire.quill.report.DefaultFormatChoiceReport;
import org.myire.quill.report.FormatChoiceReport;
import org.myire.quill.report.FormatChoiceReportTransformingReport;
import org.myire.quill.report.TransformingReport;


/**
 * Default implementation of {@code CpdReports}.
 */
public class CpdReportsImpl extends TaskReportContainer<Report> implements CpdReports
{
    static private final String PRIMARY_REPORT_NAME = "cpdPrimary";
    static private final String HTML_REPORT_NAME = "cpdHtml";
    static private final String BUILTIN_CPD_XSL = "/org/myire/quill/rsrc/report/cpd/cpd.xsl";


    /**
     * Create a new {@code CpdReportsImpl}.
     *
     * @param pTask The task that owns this reports instance.
     */
    CpdReportsImpl(CpdTask pTask)
    {
        super(ConfigurableReport.class, pTask);

        // Add the primary report.
        DefaultFormatChoiceReport aPrimaryReport =
            add(
                DefaultFormatChoiceReport.class,
                pTask.getProject(),
                PRIMARY_REPORT_NAME,
                "CPD primary report",
                FORMAT_XML,
                new DefaultPrimaryReportDestination(pTask));

        // CSV, text, and Visual Studio are also legal format for the primary report.
        aPrimaryReport.addLegalFormats(FORMAT_CSV, FORMAT_TEXT, FORMAT_CSV_LINECOUNT, FORMAT_VS);

        // Add the HTML report.
        TransformingReport aHtmlReport =
            add(
                FormatChoiceReportTransformingReport.class,
                pTask.getProject(),
                HTML_REPORT_NAME,
                "CPD HTML report",
                aPrimaryReport,
                BUILTIN_CPD_XSL);

        // Both reports are enabled by default.
        aPrimaryReport.setEnabled(true);
        aHtmlReport.setEnabled(true);
    }


    /**
     * Get the primary report. Default is a file called &quot;cpd.&lt;ext&gt;&quot; where
     * &quot;&lt;ext&gt;&quot; is the value of the report's {@code format} property. The default file
     * is located in a directory called &quot;cpd&quot; in the project's report directory or, if no
     * project report directory is defined, in a subdirectory called &quot;cpd&quot; in the
     * project's build directory.
     *
     * @return  The primary report.
     */
    @Override
    public FormatChoiceReport getPrimary()
    {
        return (FormatChoiceReport) getByName(PRIMARY_REPORT_NAME);
    }


    /**
     * Get the HTML report. Default is a transforming report located next to the primary report and
     * using the built-in CPD XSL.
     *
     * @return  The HTML report.
     */
    @Override
    public TransformingReport getHtml()
    {
        return (TransformingReport) getByName(HTML_REPORT_NAME);
    }


    /**
     * Closure for lazily evaluating the default primary report file spec.
     */
    static private class DefaultPrimaryReportDestination extends Closure<File>
    {
        private final CpdTask fTask;

        DefaultPrimaryReportDestination(CpdTask pTask)
        {
            super(null);
            fTask = pTask;
        }

        public File doCall(Object pValue)
        {
            return fTask.defaultPrimaryDestination();
        }
    }
}
