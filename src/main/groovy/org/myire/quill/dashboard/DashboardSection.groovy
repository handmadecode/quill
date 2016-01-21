/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dashboard

import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.reporting.Report
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional

import org.myire.quill.report.ReportBuilder


/**
 * A dashboard section produces a HTML fragment for the report dashboard. This fragment contains a
 * summary of a report generated during a build. It may also hold a  reference to the full report.
 *<p>
 * The summary is created by applying an XSL transformation to the underlying report's XML version.
 */
class DashboardSection
{
    private final DashboardTask fTask;
    private final String fXslResource

    // Property accessed through getter and setter only.
    private File fXslFile


    /**
     * The name of this section.
     */
    final String name

    /**
     * The underlying XML report for this section.
     */
    final Report report

    /**
     * Any detailed report the section should refer to.
     */
    final Report detailedReport


    /**
     * Create a new {@code DashboardSection}.
     *
     * @param pTask             The task that creates the report this section is part of.
     * @param pName             The name of this section.
     * @param pReport           The XML report to get the section's content from.
     * @param pDetailedReport   Any detailed report the section should refer to.
     * @param pXslResource      The XSL resource to use if no XSL file is specified.
     */
    DashboardSection(DashboardTask pTask,
                     String pName,
                     Report pReport,
                     Report pDetailedReport,
                     String pXslResource)
    {
        fTask = pTask;
        name = pName;
        report = pReport;
        detailedReport = pDetailedReport;
        fXslResource = pXslResource;
    }


    /**
     * Create a new {@code DashboardSection}.
     *
     * @param pTask             The task that creates the report this section is part of.
     * @param pName             The name of this section.
     * @param pReport           The XML report to get the section's content from.
     * @param pDetailedReport   Any detailed report the section should refer to.
     * @param pXslFile          The XSL file to transform the report with.
     */
    DashboardSection(DashboardTask pTask,
                     String pName,
                     Report pReport,
                     Report pDetailedReport,
                     File pXslFile)
    {
        fTask = pTask;
        name = pName;
        report = pReport;
        detailedReport = pDetailedReport;
        fXslResource = null;
        fXslFile = pXslFile;
    }


    /**
     * Get the XSL file that will be used to transform the XML input.
     *
     * @return  The XSL file. If this file is null the default style sheet specified in the
     *          constructor will be used to perform the transformation, if available.
     */
    @InputFile
    @Optional
    File getXslFile()
    {
        return fXslFile;
    }


    /**
     * Set the XSL file to use when transforming the XML input. The specified file will be resolved
     * relative to the project directory.
     *
     * @param pFile The XSL file.
     */
    void setXslFile(Object pFile)
    {
        fXslFile = pFile ? fTask.project.file(pFile) : null;
    }


    /**
     * Write this section to a report by letting the specified report builder apply the
     * transformation. If the underlying XML report isn't enabled nothing will be output.
     *
     * @param pReportBuilder    The report builder holding the report to write this section to.
     */
    void writeTo(ReportBuilder pReportBuilder)
    {
        if (report.enabled)
        {
            fTask.logger.debug('Creating dashboard section \'{}\'', name);
            transform(pReportBuilder);
        }
        else
            fTask.logger.debug('Report \'{}\' for dashboard section \'{}\' is disabled, skipping',
                               report.name,
                               name);
    }


    /**
     * Transform this section's underlying XML report using the XSL file specified in the
     * {@code xslFile} property or the default XSL resource,  and write the result to a
     * {@code ReportBuilder}.
     *
     * @param pReportBuilder    The report builder to write to.
     */
    private void transform(ReportBuilder pReportBuilder)
    {
        File aInputFile = report.destination;
        if (aInputFile == null)
        {
            fTask.logger.error('The report for the  \'{}\' dashboard section has no destination file', name);
        }
        else if (!aInputFile.canRead())
        {
            fTask.logger.error('The report file \'{}\' for the \'{}\' dashboard section is not accessible',
                               aInputFile.absolutePath,
                               name);
        }
        else
        {
            Map<String, Object> aTransformerParams = createDetailedReportParameter(pReportBuilder.destination);
            if (fXslFile != null)
                // An XSL file has been specified, use its style sheet for the transformation.
                pReportBuilder.transform(aInputFile, fXslFile, aTransformerParams);
            else
                // No XSL file specified, use the default resource.
                pReportBuilder.transform(aInputFile, fXslResource, aTransformerParams);
        }
    }


    /**
     * Create a map with the XSL parameter for the path to the detailed report. The value of the
     * parameter will be the path to the detailed report's destination relative to the dashboard
     * report file's path.
     *
     * @param pDashboardReportFile  The dashboard report file, used as the starting point for the
     *                              relative path to the detailed report.
     *
     * @return  A map with the parameter, or null if there is no detailed report.
     */
    private Map<String, Object> createDetailedReportParameter(File pDashboardReportFile)
    {
        File aDetailedReport = getDetailedReportFile();
        if (aDetailedReport?.exists())
        {
            String aRelativePath = pDashboardReportFile.toPath().getParent().relativize(aDetailedReport.toPath()).toString();
            return ['detailed-report-path' : aRelativePath];
        }
        else
            return null;
    }


    private File getDetailedReportFile()
    {
       if (detailedReport == null || ! detailedReport.enabled)
           return null;

       if (detailedReport instanceof DirectoryReport)
           return ((DirectoryReport) detailedReport).entryPoint;
        else
           return detailedReport?.destination;
    }
}
