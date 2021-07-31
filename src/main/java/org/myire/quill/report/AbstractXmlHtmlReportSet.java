/*
 * Copyright 2020-2021 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.File;
import java.util.Locale;

import groovy.lang.Closure;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.util.ConfigureUtil;

import org.myire.quill.common.Projects;
import org.myire.quill.common.Tasks;


/**
 * Base class for implementations of {@code XmlHtmlReportSet}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
abstract public class AbstractXmlHtmlReportSet<T extends XmlHtmlReportSet<T>> implements XmlHtmlReportSet<T>
{
    private final SingleFileReport fXmlReport;
    private final TransformingReport fHtmlReport;
    private final String fXmlReportName;
    private final String fHtmlReportName;


    /**
     * Create a new {@code AbstractXmlHtmlReports}.
     *
     * @param pTask             The task that owns this report set.
     * @param pReportSetName    The name of the report set, used for report names and file base
     *                          names.
     * @param pXslResourcePath  The path to the built-in XSL resource to use by default when
     *                          transforming the XML report to the HTML report.
     *
     * @throws NullPointerException if {@code pTask} or {@code pReportSetName} is null.
     */
    protected AbstractXmlHtmlReportSet(
        Task pTask,
        String pReportSetName,
        String pXslResourcePath)
    {
        Project aProject = pTask.getProject();

        String aLowerCaseName = pReportSetName.toLowerCase(Locale.getDefault());
        fXmlReportName = aLowerCaseName + "Xml";
        fHtmlReportName = aLowerCaseName + "Html";

        fXmlReport =
            new DefaultSingleFileReport(
                aProject,
                fXmlReportName,
                pReportSetName + " XML report",
                new DefaultXmlReportDestination(aProject, aLowerCaseName));

        fHtmlReport =
            new ReportTransformingReport(
                aProject,
                fHtmlReportName,
                pReportSetName + " HTML report",
                fXmlReport,
                pXslResourcePath);

        // Both reports are enabled by default.
        Reports.setRequired(fXmlReport, true);
        Reports.setRequired(fHtmlReport, true);
    }


    /**
     * Get the XML report. Default is a file called &quot;&lt;report-set-name&gt;.xml&quot; located
     * in a directory called &quot;&lt;report-set-name&gt;&quot; in the project's report directory
     * or, if no project report directory is defined, in a subdirectory called
     * &quot;&lt;report-set-name&gt;&quot; in the project's build directory.
     *
     * @return  The XML report, never null.
     */
    @Override
    public SingleFileReport getXml()
    {
        return fXmlReport;
    }


    /**
     * Get the HTML report. Default is a transforming report located next to the XML report.
     *
     * @return  The HTML report, never null.
     */
    @Override
    public TransformingReport getHtml()
    {
        return fHtmlReport;
    }


    @Override
    public Report getReportByName(String pReportName)
    {
        if (fXmlReportName.equalsIgnoreCase(pReportName))
            return fXmlReport;
        else if (fHtmlReportName.equalsIgnoreCase(pReportName))
            return fHtmlReport;
        else
            return null;
    }


    @Override
    public T configure(Closure pClosure)
    {
        ConfigureUtil.configureSelf(pClosure, this);
        return self();
    }


    /**
     * Add the enabled flags of the reports to a task's input properties and the destination files
     * of teh reports to the task's output files.
     *
     * @param pTask The task.
     *
     * @throws NullPointerException if {@code pTask} is null.
     */
    public void setInputsAndOutputs(Task pTask)
    {
        // If any of the reports' enabled flag is modified the task should be rerun.
        Tasks.inputProperty(pTask, "xmlReportEnabled", () -> Reports.isRequired(fXmlReport));
        Tasks.inputProperty(pTask, "htmlReportEnabled", () -> Reports.isRequired(fHtmlReport));

        // The XSL file used to create the HTML report is an optional input file.
        Tasks.optionalInputFile(pTask, fHtmlReport::getXslFile);

        // Add the destination of both reports as output files of this task.
        Tasks.outputFile(pTask, fXmlReport::getDestination);
        Tasks.outputFile(pTask, fHtmlReport::getDestination);

        // Let the HTML report decide if it is up to date.
        pTask.getOutputs().upToDateWhen(_ignore -> fHtmlReport.checkUpToDate());
    }


    /**
     * Return this instance with the correct type.
     *
     * @return  This instance.
     */
    protected abstract T self();


    /**
     * Closure for lazily evaluating the default XML report file spec.
     */
    static private class DefaultXmlReportDestination extends Closure<File>
    {
        private final Project fProject;
        private final String fBaseName;

        DefaultXmlReportDestination(Project pProject, String pBaseName)
        {
            super(null);
            fProject = pProject;
            fBaseName = pBaseName;
        }

        public File doCall(Object pValue)
        {
            return new File(Projects.createReportDirectorySpec(fProject, fBaseName), fBaseName + ".xml");
        }
    }
}
