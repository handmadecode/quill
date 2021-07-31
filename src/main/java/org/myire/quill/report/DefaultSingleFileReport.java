/*
 * Copyright 2015, 2018, 2020-2021 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.File;

import groovy.lang.Closure;

import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.OutputFile;

import org.myire.quill.common.Providers;


/**
 * A {@code SingleFileReport} with a lazily evaluated default destination.
 */
public class DefaultSingleFileReport extends DefaultDestinationReport implements SingleFileReport
{
    private RegularFileProperty fOutputLocation;


    /**
     * Create a new {@code DefaultSingleFileReport}.
     *
     * @param pProject              The project for which the report will be produced.
     * @param pName                 The report's symbolic name.
     * @param pDisplayName          The report's descriptive name.
     * @param pDefaultDestination   A closure that will return the report's default file destination
     *                              when called.
     *
     * @throws NullPointerException if {@code pProject} or {@code pDefaultDestination} is null.
     */
    public DefaultSingleFileReport(
        Project pProject,
        String pName,
        String pDisplayName,
        Closure<File> pDefaultDestination)
    {
        super(pProject,
              pName,
              pDisplayName,
              Report.OutputType.FILE,
              pDefaultDestination);
    }


    // 6.1 compatibility
    @OutputFile
    public RegularFileProperty getOutputLocation()
    {
        if (fOutputLocation == null)
        {
            fOutputLocation = Providers.createFileProperty(getProject());
            fOutputLocation.set(resolveDestination());
        }

        return fOutputLocation;
    }
}
