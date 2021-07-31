/*
 * Copyright 2015, 2019, 2021 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cpd;

import java.io.File;

import groovy.lang.Closure;

import org.gradle.api.reporting.Report;
import org.gradle.util.ConfigureUtil;

import org.myire.quill.report.DefaultFormatChoiceReport;
import org.myire.quill.report.FormatChoiceReport;
import org.myire.quill.report.FormatChoiceReportTransformingReport;
import org.myire.quill.report.Reports;
import org.myire.quill.report.TransformingReport;


/**
 * Default implementation of {@code CpdReports}.
 */
public class CpdReportsImpl implements CpdReports
{
    static private final String PRIMARY_REPORT_NAME = "cpdPrimary";
    static private final String HTML_REPORT_NAME = "cpdHtml";
    static private final String BUILTIN_CPD_XSL = "/org/myire/quill/rsrc/report/cpd/cpd.xsl";

    private final DefaultFormatChoiceReport fPrimaryReport;
    private final TransformingReport fHtmlReport;


    /**
     * Create a new {@code CpdReportsImpl}.
     *
     * @param pTask The task that owns this reports instance.
     */
    CpdReportsImpl(CpdTask pTask)
    {
        fPrimaryReport =
            new DefaultFormatChoiceReport(
                pTask.getProject(),
                PRIMARY_REPORT_NAME,
                "CPD primary report",
                FORMAT_XML,
                new DefaultPrimaryReportDestination(pTask));

        // CSV, text, and Visual Studio are also legal format for the primary report.
        fPrimaryReport.addLegalFormats(FORMAT_CSV, FORMAT_TEXT, FORMAT_CSV_LINECOUNT, FORMAT_VS);

        fHtmlReport =
            new FormatChoiceReportTransformingReport(
                pTask.getProject(),
                HTML_REPORT_NAME,
                "CPD HTML report",
                fPrimaryReport,
                BUILTIN_CPD_XSL);

        // Both reports are enabled by default.
        Reports.setRequired(fPrimaryReport, true);
        Reports.setRequired(fHtmlReport, true);
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
        return fPrimaryReport;
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
        return fHtmlReport;
    }


    @Override
    public Report getReportByName(String pReportName)
    {
        if (PRIMARY_REPORT_NAME.equalsIgnoreCase(pReportName))
            return fPrimaryReport;
        else if (HTML_REPORT_NAME.equalsIgnoreCase(pReportName))
            return fHtmlReport;
        else
            return null;
    }


    @Override
    public CpdReports configure(Closure pClosure)
    {
        ConfigureUtil.configureSelf(pClosure, this);
        return this;
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
