/*
 * Copyright 2015 Peter Franzen. All rights reserved.
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
 * Default implementation of {@code CpdReports}. For some reason the Groovy compiler cannot handle
 * the covariant return types of some overridden methods in {@code NamedDomainObjectSet}. The Java
 * compiler has no problems handling it, so this implementation is in Java. Note that all subclasses
 * of {@code TaskReportContainer} in the Gradle distribution (e.g. {@code FindBugsReportsImpl} and
 * {@code DefaultTestTaskReports} are Java classes.
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
        DefaultFormatChoiceReport aPrimaryReport = add(DefaultFormatChoiceReport.class,
                                                       pTask,
                                                       PRIMARY_REPORT_NAME,
                                                       "CPD primary report",
                                                       FORMAT_XML,
                                                       new DefaultPrimaryReportDestination(pTask));

        // CSV and text are also legal format for the primary report.
        // CSL with linecount per file was introduced in CPD v5.3, but there is a bug in the CPD ant
        // task that prevents the Gradle task from using it.
        aPrimaryReport.addLegalFormats(FORMAT_CSV, FORMAT_TEXT/*, FORMAT_CSV_LINECOUNT*/);

        // Add the HTML report.
        TransformingReport aHtmlReport = add(FormatChoiceReportTransformingReport.class,
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
     * Get the primary report. Default is a file called &quot;cpd.&lt;ext>&quot; where
     * &quot;&lt;ext>&quot; is the value of the report's {@code format} property. The default file
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
