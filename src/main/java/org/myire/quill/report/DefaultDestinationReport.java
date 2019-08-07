/*
 * Copyright 2015, 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.File;
import static java.util.Objects.requireNonNull;

import groovy.lang.Closure;

import org.gradle.api.Project;
import org.gradle.api.reporting.Report;


/**
 * A report that lazily evaluates its destination through a Closure to a default value if no
 * destination has been specified when {@code getDestination()} is called.
 */
public class DefaultDestinationReport extends SimpleConfigurableReport
{
    private final Closure<File> fDefaultDestination;


    /**
     * Create a new {@code DefaultDestinationReport}.
     *
     * @param pProject              The project for which the report will be produced.
     * @param pName                 The report's symbolic name.
     * @param pDisplayName          The report's descriptive name.
     * @param pOutputType           The type of output the report produces.
     * @param pDefaultDestination   A closure that will return the report's default destination when
     *                              called.
     *
     * @throws NullPointerException if {@code pProject} or {@code pDefaultDestination} is null.
     */
    public DefaultDestinationReport(
        Project pProject,
        String pName,
        String pDisplayName,
        Report.OutputType pOutputType,
        Closure<File> pDefaultDestination)
    {
        super(pProject, pName, pDisplayName, pOutputType);
        fDefaultDestination = requireNonNull(pDefaultDestination);
    }


    @Override
    public File getDestination()
    {
        File aDestination = super.getDestination();
        if (aDestination == null)
        {
            aDestination = fDefaultDestination.call();
            setDestination(aDestination);
        }

        return aDestination;
    }
}
