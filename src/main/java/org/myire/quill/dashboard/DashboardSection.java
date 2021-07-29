/*
 * Copyright 2015, 2019, 2021 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dashboard;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.reporting.DirectoryReport;
import org.gradle.api.reporting.Report;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;

import org.myire.quill.common.ProjectAware;
import org.myire.quill.report.ReportBuilder;
import org.myire.quill.report.Reports;
import org.myire.quill.report.TransformingReport;


/**
 * A dashboard section produces an HTML fragment for the reports dashboard. This fragment contains a
 * summary of a report generated during a build. It may also hold a reference to a more detailed
 * report.
 *<p>
 * The summary is created by applying an XSL transformation to the underlying report's XML version.
 */
public class DashboardSection extends ProjectAware
{
    private final String fName;
    private final Report fReport;
    private final Report fDetailedReport;
    private final String fXslResource;

    private File fXslFile;


    /**
     * Create a new {@code DashboardSection}.
     *
     * @param pProject          The project that the dashboard contains report summaries for.
     * @param pName             The name of this section.
     * @param pReport           The XML report to get the section's content from.
     * @param pDetailedReport   Any detailed report the section should refer to.
     * @param pXslResource      The XSL resource to use if no XSL file is specified.
     */
    DashboardSection(
        Project pProject,
        String pName,
        Report pReport,
        Report pDetailedReport,
        String pXslResource)
    {
        super(pProject);
        fName = pName;
        fReport = pReport;
        fDetailedReport = pDetailedReport;
        fXslResource = pXslResource;
    }


    /**
     * Create a new {@code DashboardSection}.
     *
     * @param pProject          The project that the dashboard contains report summaries for.
     * @param pName             The name of this section.
     * @param pReport           The XML report to get the section's content from.
     * @param pDetailedReport   Any detailed report the section should refer to.
     * @param pXslFile          The XSL file to transform the report with.
     */
    DashboardSection(
        Project pProject,
        String pName,
        Report pReport,
        Report pDetailedReport,
        File pXslFile)
    {
        super(pProject);
        fName = pName;
        fReport = pReport;
        fDetailedReport = pDetailedReport;
        fXslResource = null;
        fXslFile = pXslFile;
    }


    /**
     * Get the name of this section.
     *
     * @return  The section's name.
     */
    @Input
    public String getName()
    {
        return fName;
    }


    /**
     * Get the underlying XML report for this section.
     *
     * @return  The XML report the section's contents are based on.
     */
    @Nested
    public Report getReport()
    {
        return fReport;
    }


    /**
     * Get the detailed report the section should refer to.
     *
     * @return  The section's detailed report, possibly null.
     */
    @Nested
    @Optional
    public Report getDetailedReport()
    {
        return fDetailedReport;
    }


    /**
     * Get the XSL file that will be used to transform the XML input.
     *
     * @return  The XSL file. If this file is null the default style sheet specified in the
     *          constructor will be used to perform the transformation, if available.
     */
    @InputFile
    @Optional
    public File getXslFile()
    {
        return fXslFile;
    }


    /**
     * Set the XSL file to use when transforming the XML input. The specified file will be resolved
     * relative to the project directory.
     *
     * @param pFile The XSL file.
     */
    public void setXslFile(Object pFile)
    {
        fXslFile = pFile != null ? getProject().file(pFile) : null;
    }


    /**
     * Write this section to a report by letting the specified report builder apply the
     * transformation. If the underlying XML report isn't enabled nothing will be output.
     *
     * @param pReportBuilder    The report builder holding the report to write this section to.
     */
    void writeTo(ReportBuilder pReportBuilder)
    {
        if (Reports.isRequired(fReport))
        {
            getProjectLogger().debug("Creating dashboard section '{}'", fName);
            transform(pReportBuilder);
        }
        else
            getProjectLogger().debug(
                "Report '{}' for dashboard section '{}' is disabled, skipping",
                fReport.getName(),
                fName);
    }


    /**
     * Transform this section's underlying XML report using the XSL file specified in the
     * {@code xslFile} property or the default XSL resource, and write the result to a
     * {@code ReportBuilder}.
     *
     * @param pReportBuilder    The report builder to write to.
     */
    private void transform(ReportBuilder pReportBuilder)
    {
        File aInputFile = Reports.getOutputLocation(fReport);
        if (aInputFile == null)
        {
            getProjectLogger().error(
                "The report for the '{}' dashboard section has no destination file",
                fName);
        }
        else if (!aInputFile.exists())
        {
            getProjectLogger().error(
                "The report file '{}' for the '{}' dashboard section does not exist, skipping",
                aInputFile.getAbsolutePath(),
                fName);
        }
        else if (!aInputFile.canRead())
        {
            getProjectLogger().error(
                "The report file '{}' for the '{}' dashboard section is not readable, skipping",
                aInputFile.getAbsolutePath(),
                fName);
        }
        else
        {
            Map<String, Object> aXslParams = createXslParameters(pReportBuilder.getDestination());
            if (fXslFile != null)
                // An XSL file has been specified, use its style sheet for the transformation.
                pReportBuilder.transform(aInputFile, fXslFile, aXslParams);
            else
                // No XSL file specified, use the default resource.
                pReportBuilder.transform(aInputFile, fXslResource, aXslParams);
        }
    }


    /**
     * Create a map with the XSL parameters for the dashboard section XSL transformation. The
     * parameters will always contain the Gradle project's root directory path. If the section has a
     * detailed report that exists, the parameters will contain the path to that report, relative to
     * the dashboard report file's path.
     *
     * @param pDashboardReportFile  The dashboard report file, used as the starting point for the
     *                              relative path to the detailed report.
     *
     * @return  A map with the parameter(s).
     */
    private Map<String, Object> createXslParameters(File pDashboardReportFile)
    {
        Map<String, Object> aParams = new HashMap<>();
        TransformingReport.applyProjectRootXslParameter(getProject(), aParams::put);

        File aDetailedReport = getDetailedReportFile();
        if (aDetailedReport != null && aDetailedReport.exists())
        {
            String aRelativePath = pDashboardReportFile.toPath().getParent().relativize(aDetailedReport.toPath()).toString();
            aParams.put("detailed-report-path", aRelativePath);
        }

        return aParams;
    }


    private File getDetailedReportFile()
    {
        if (Reports.isRequired(fDetailedReport))
        {
            if (fDetailedReport instanceof DirectoryReport)
                return ((DirectoryReport) fDetailedReport).getEntryPoint();
            else
                return Reports.getOutputLocation(fDetailedReport);
        }
        else
            return null;
    }
}
