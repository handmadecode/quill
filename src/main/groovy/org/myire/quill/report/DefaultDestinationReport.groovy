/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report

import org.gradle.api.internal.file.FileResolver
import org.gradle.api.reporting.Report
import org.gradle.api.reporting.internal.SimpleReport


/**
 * A report that lazily evaluates its destination through a Closure to a default value if no
 * destination has been specified when {@code getDestination()} is called.
 */
class DefaultDestinationReport extends SimpleReport
{
    private final Closure<File> fDefaultDestination;


    /**
     * Create a new {@code DefaultDestinationReport}.
     *
     * @param pName                 The report's symbolic name.
     * @param pDisplayName          The report's descriptive name.
     * @param pOutputType           The type of output the report produces.
     * @param pFileResolver         The resolver to use when resolving file locations.
     * @param pDefaultDestination   A closure that will return the report's default destination when
     *                              called.
     */
    DefaultDestinationReport(String pName,
                             String pDisplayName,
                             Report.OutputType pOutputType,
                             FileResolver pFileResolver,
                             Closure<File> pDefaultDestination)
    {
        super(pName, pDisplayName, pOutputType, pFileResolver);
        fDefaultDestination = pDefaultDestination;
    }


    @Override
    File getDestination()
    {
        File aDestination = super.getDestination();
        if (aDestination == null)
        {
            aDestination = fDefaultDestination.call();
            setDestination(aDestination);
        }

        return aDestination;
    }


    // Override to widen access scope from protected to public.
    @Override
    void setDestination(Object pDestination)
    {
        super.setDestination(pDestination)
    }
}
