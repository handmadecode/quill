/*
 * Copyright 2015, 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report

import org.gradle.api.Project
import org.gradle.api.reporting.Report


/**
 * A report that lazily evaluates its destination through a Closure to a default value if no
 * destination has been specified when {@code getDestination()} is called.
 */
class DefaultDestinationReport extends SimpleConfigurableReport
{
    private final Closure<File> fDefaultDestination;


    /**
     * Create a new {@code DefaultDestinationReport}.
     *
     * @param pProject              The project for which the report will be produced.
     * @param pName                 The report's symbolic name.
     * @param pDisplayName          The report's descriptive name.
     * @param pOutputType           The type of output the report produces.
     * @param pFileResolver         The resolver to use when resolving file locations.
     * @param pDefaultDestination   A closure that will return the report's default destination when
     *                              called.
     */
    DefaultDestinationReport(Project pProject,
                             String pName,
                             String pDisplayName,
                             Report.OutputType pOutputType,
                             Closure<File> pDefaultDestination)
    {
        super(pProject, pName, pDisplayName, pOutputType);
        fDefaultDestination = pDefaultDestination;
    }


    @Override
    File getDestination()
    {
        File aDestination = super.getDestination();
        if (aDestination == null)
        {
            aDestination = fDefaultDestination.call();
            // Gradle v4 adds setDestination(File) to ConfigurableReport, but adding that method
            // to this class (or its superclass) doesn't work with Gradle's dynamic property
            // handling in versions prior to 2.9.
            // To handle both situations the File is cast to Object, which avoids calling the
            // unimplemented setDestination(File) in v4 and trigger a java.lang.AbstractMethodError.
            setDestination((Object) aDestination);
        }

        return aDestination;
    }
}
