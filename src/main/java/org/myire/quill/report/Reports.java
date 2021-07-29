/*
 * Copyright 2021 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.File;

import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.reporting.Report;


/**
 * Report related utility methods.
 */
public final class Reports
{
    private Reports()
    {
        // Don't allow instantiations of utility classes.
    }


    /**
     * Get the value of a report's {@code outputLocation} property.
     *
     * @param pReport   The report.
     *
     * @return  The value of the {@code outputLocation} property. If the property isn't set, or if
     *          {@code pReport} is null, null is returned.
     */
    static public File getOutputLocation(Report pReport)
    {
        if (pReport == null)
            return null;

        FileSystemLocation aFileSystemLocation = pReport.getOutputLocation().getOrNull();
        return aFileSystemLocation != null ? aFileSystemLocation.getAsFile() : null;
    }


    /**
     * Get the value of a report's {@code required} property.
     *
     * @param pReport   The report.
     *
     * @return  The value of the {@code required} property. If the property isn't set, false is
     *          returned. False is also returned if {@code pReport} is null.
     */
    static public boolean isRequired(Report pReport)
    {
        if (pReport != null)
            return pReport.getRequired().getOrElse(Boolean.FALSE).booleanValue();
        else
            return false;
    }


    /**
     * Set the value of a report's {@code required} property.
     *
     * @param pReport       The report.
     * @param pIsRequired   The value to set.
     */
    static public void setRequired(Report pReport, boolean pIsRequired)
    {
        if (pReport != null)
            pReport.getRequired().set(Boolean.valueOf(pIsRequired));
    }
}
