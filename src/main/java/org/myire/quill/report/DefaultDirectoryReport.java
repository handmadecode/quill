/*
 * Copyright 2015, 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.File;

import groovy.lang.Closure;

import org.gradle.api.Project;
import org.gradle.api.reporting.DirectoryReport;
import org.gradle.api.reporting.Report;


/**
 * A {@code DirectoryReport} with a lazily evaluated default destination.
 */
public class DefaultDirectoryReport extends DefaultDestinationReport implements DirectoryReport
{
    private final String fEntryPointRelativePath;


    /**
     * Create a new {@code DefaultDirectoryReport}.
     *
     * @param pProject              The project for which the report will be produced.
     * @param pName                 The report's symbolic name.
     * @param pDisplayName          The report's descriptive name.
     * @param pEntryPointRelativePath
     *                              The report's entry point relative to its destination. Pass null
     *                              to use the destination as entry point.
     * @param pDefaultDestination   A closure that will return the report's default directory
     *                              destination when called.
     *
     * @throws NullPointerException if {@code pProject} or {@code pDefaultDestination} is null.
     */
    public DefaultDirectoryReport(
        Project pProject,
        String pName,
        String pDisplayName,
        String pEntryPointRelativePath,
        Closure<File> pDefaultDestination)
    {
        super(pProject,
              pName,
              pDisplayName,
              Report.OutputType.DIRECTORY,
              pDefaultDestination);
        fEntryPointRelativePath = pEntryPointRelativePath;
    }


    @Override
    public File getEntryPoint()
    {
        if  (fEntryPointRelativePath != null)
            return new File(getDestination(), fEntryPointRelativePath);
        else
            return getDestination();
    }
}
