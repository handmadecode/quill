/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report

import org.gradle.api.Project
import org.gradle.api.reporting.Report

import org.myire.quill.common.Projects


/**
 * A transforming report that applies an XSL transformation on the output/destination of another
 * report to create its own output.
 */
class ReportTransformingReport extends AbstractTransformingReport
{
    private final Report fInput;


    /**
     * Create a new {@code ReportTransformingReport}.
     *
     * @param pProject              The project for which the report will be produced.
     * @param pName                 The report's symbolic name.
     * @param pDisplayName          The report's descriptive name.
     * @param pInput                The report to transform the output of.
     * @param pXslResource          The resource containing the default style sheet to apply if no
     *                              XSL file is specified.
     */
    ReportTransformingReport(Project pProject,
                             String pName,
                             String pDisplayName,
                             Report pInput,
                             String pXslResource)
    {
        super(pProject, pName, pDisplayName, pXslResource, { defaultDestination(pProject, pInput, pName) });
        fInput = pInput;
    }


    @Override
    boolean isEnabled()
    {
        return super.isEnabled() && fInput.enabled;
    }


    @Override
    protected File getInputFile()
    {
        return fInput.destination;
    }


    static private File defaultDestination(Project pProject, Report pInput, String pDefaultBaseName)
    {
        File aInputFile = pInput.destination;
        if (aInputFile != null)
            return new File(aInputFile.parentFile, aInputFile.name.replace('.xml', '.html'));
        else
            return Projects.createReportDirectorySpec(pProject, pDefaultBaseName + '.html');
    }
}
