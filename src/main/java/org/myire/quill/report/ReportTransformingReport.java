/*
 * Copyright 2015, 2018, 2021 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.File;
import static java.util.Objects.requireNonNull;

import groovy.lang.Closure;

import org.gradle.api.Project;
import org.gradle.api.reporting.Report;

import org.myire.quill.common.Projects;


/**
 * A transforming report that applies an XSL transformation on the output/destination of another
 * report to create its own output.
 */
public class ReportTransformingReport extends AbstractTransformingReport
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

     * @throws NullPointerException if {@code pProject} or {@code pInput} is null.
     */
    public ReportTransformingReport(
        Project pProject,
        String pName,
        String pDisplayName,
        Report pInput,
        String pXslResource)
    {
        super(pProject, pName, pDisplayName, pXslResource, new DefaultDestination(pProject, pInput, pName));
        fInput = pInput;
    }


    @Override
    public boolean reportIsRequired()
    {
        return super.reportIsRequired() && Reports.isRequired(fInput);
    }


    @Override
    protected File getInputFile()
    {
        return Reports.getOutputLocation(fInput);
    }


    /**
     * Closure for lazily evaluating the default destination.
     */
    static private class DefaultDestination extends Closure<File>
    {
        private final Project fProject;
        private final Report fInput;
        private final String fDefaultBaseName;

        DefaultDestination(Project pProject, Report pInput, String pDefaultBaseName)
        {
            super(null);
            fProject = requireNonNull(pProject);
            fInput = requireNonNull(pInput);
            fDefaultBaseName = pDefaultBaseName;
        }

        public File doCall(Object pValue)
        {
            File aInputFile = Reports.getOutputLocation(fInput);
            if (aInputFile != null)
                return new File(aInputFile.getParentFile(), aInputFile.getName().replace(".xml", ".html"));
            else
                return Projects.createReportDirectorySpec(fProject, fDefaultBaseName + ".html");
        }
    }
}
