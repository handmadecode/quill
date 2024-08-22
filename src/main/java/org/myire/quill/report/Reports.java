/*
 * Copyright 2021, 2024 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.reporting.Report;
import org.myire.quill.common.Invocations;


/**
 * Report related utility methods.
 */
public final class Reports
{
    // The method Report::getOutputLocation changed return type from Provider to Property in Gradle
    // version 8. Invoke that method through a MethodHandle to allow code compiled against Gradle
    // versions < 8 to run with Gradle versions >= 8.
    static private final MethodHandle GET_OUTPUT_LOCATION_PROVIDER =
        Invocations.lookupVirtualMethod(
            MethodHandles.lookup(),
            Report.class,
            "getOutputLocation",
            MethodType.methodType(Provider.class));
    static private final MethodHandle GET_OUTPUT_LOCATION_PROPERTY =
        Invocations.lookupVirtualMethod(
            MethodHandles.lookup(),
            Report.class,
            "getOutputLocation",
            MethodType.methodType(Property.class));


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

        try
        {
            Object aInvokeResult = null;
            if (GET_OUTPUT_LOCATION_PROVIDER != null)
                aInvokeResult = GET_OUTPUT_LOCATION_PROVIDER.invoke(pReport);
            else if (GET_OUTPUT_LOCATION_PROPERTY != null)
                aInvokeResult = GET_OUTPUT_LOCATION_PROPERTY.invoke(pReport);

            if (aInvokeResult instanceof Provider<?>)
            {
                Object aProvided = ((Provider<?>) aInvokeResult).getOrNull();
                return aProvided instanceof FileSystemLocation ? ((FileSystemLocation) aProvided).getAsFile() : null;
            }
        }
        catch (Throwable t)
        {
            System.err.println("Failed to invoke Report::getOutputLocation: " + t.getMessage());
        }

        return null;
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
