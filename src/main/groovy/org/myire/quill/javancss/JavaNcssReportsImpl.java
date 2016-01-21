/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.javancss;

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
 * Default implementation of {@code JavaNcssReports}. For some reason the Groovy compiler cannot
 * handle the covariant return types of some overridden methods in {@code NamedDomainObjectSet}. The
 * Java compiler has no problems handling it, so this implementation is in Java. Note that all
 * subclasses of {@code TaskReportContainer} in the Gradle distribution (e.g.
 * {@code JDependReportsImpl} and {@code CodeNarcReportsImpl} are Java classes.
 */
public class JavaNcssReportsImpl extends TaskReportContainer<Report> implements JavaNcssReports
{
    static private final String PRIMARY_REPORT_NAME = "javancssPrimary";
    static private final String HTML_REPORT_NAME = "javancssHtml";
    static private final String BUILTIN_JAVANCSS_XSL = "/org/myire/quill/rsrc/report/javancss/javancss.xsl";


    /**
     * Create a new {@code JavaNcssReportsImpl}.
     *
     * @param pTask The task that owns this reports instance.
     */
    JavaNcssReportsImpl(JavaNcssTask pTask)
    {
        super(ConfigurableReport.class, pTask);

        // Add the primary report, which has XML as its default format.
        DefaultFormatChoiceReport aPrimaryReport = add(DefaultFormatChoiceReport.class,
                                                       pTask,
                                                       PRIMARY_REPORT_NAME,
                                                       "JavaNCSS primary report",
                                                       "xml",
                                                       new DefaultPrimaryReportDestination(pTask));

        // Text is also a legal format for the primary report.
        aPrimaryReport.addLegalFormats("text");

        // Add the HTML report.
        TransformingReport aHtmlReport = add(FormatChoiceReportTransformingReport.class,
                                             pTask.getProject(),
                                             HTML_REPORT_NAME,
                                             "JavaNCSS HTML report",
                                             aPrimaryReport,
                                             BUILTIN_JAVANCSS_XSL);

        // Both reports are enabled by default.
        aPrimaryReport.setEnabled(true);
        aHtmlReport.setEnabled(true);
    }


    /**
     * Get the primary report. Default is a file called &quot;javancss.&lt;ext>&quot; where
     * &quot;&lt;ext>&quot; is the value of the report's {@code format} property. The default file
     * is located in a directory called &quot;javancss&quot; in the project's report directory or,
     * if no project report directory is defined, in a subdirectory called &quot;javancss&quot; in
     * the project's build directory.
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
     * using the built-in JavaNCSS XSL.
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
        private final JavaNcssTask fTask;

        DefaultPrimaryReportDestination(JavaNcssTask pTask)
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
