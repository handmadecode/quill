/*
 * Copyright 2015, 2018, 2021 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import org.gradle.api.Project;


/**
 * A {@code ReportTransformingReport} that transforms a {@code FormatChoiceReport}. The latter's
 * format must be XML for the transformation to take place.
 */
public class FormatChoiceReportTransformingReport extends ReportTransformingReport
{
    private final FormatChoiceReport fFormatChoiceReport;


    /**
     * Create a new {@code FormatChoiceReportTransformingReport}.
     *
     * @param pProject      The project for which the report will be produced.
     * @param pName         The report's symbolic name.
     * @param pDisplayName  The report's descriptive name.
     * @param pInput        The report to transform the output of.
     * @param pXslResource  The resource containing the default style sheet to apply if no XSL file
     *                      is specified.
     *
     * @throws NullPointerException if {@code pProject} or {@code pInput} is null.
     */
    public FormatChoiceReportTransformingReport(
        Project pProject,
        String pName,
        String pDisplayName,
        FormatChoiceReport pInput,
        String pXslResource)
    {
        super(pProject, pName, pDisplayName, pInput, pXslResource);
        fFormatChoiceReport = pInput;
    }


    @Override
    public boolean reportIsRequired()
    {
        // The format choice report must be required and on the XML format for this report to be
        // required.
        return
            super.reportIsRequired()
            &&
            Reports.isRequired(fFormatChoiceReport)
            &&
            "xml".equals(fFormatChoiceReport.getFormat());
    }
}
