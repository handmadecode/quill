/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

package org.myire.quill.report

import org.gradle.api.Project
import org.gradle.api.reporting.ConfigurableReport
import org.gradle.api.reporting.Report
import org.gradle.util.ConfigureUtil

import org.myire.quill.common.ProjectAware;


/**
 * Implementation of {@code org.gradle.api.reporting.ConfigurableReport}.
 */
class SimpleConfigurableReport extends ProjectAware implements ConfigurableReport
{
    static private final boolean USE_CONFIGURE_SELF = configureSelfAvailable();


    private final String fName;
    private final String fDisplayName;
    private final Report.OutputType fOutputType;

    private Object fDestination;
    private boolean fEnabled;


    /**
     * Create a new {@code SimpleReport}.
     *
     * @param pProject      The project for which the report will be produced.
     * @param pName         The report's symbolic name.
     * @param pDisplayName  The report's descriptive name.
     * @param pOutputType   The type of output the report produces.
     */
    SimpleConfigurableReport(
            Project pProject,
            String pName,
            String pDisplayName,
            Report.OutputType pOutputType)
    {
        super(pProject);
        fName = pName;
        fDisplayName = pDisplayName;
        fOutputType = pOutputType;
    }


    String getName()
    {
        return fName;
    }


    String getDisplayName()
    {
        return fDisplayName;
    }


    Report.OutputType getOutputType()
    {
        return fOutputType;
    }


    File getDestination()
    {
        return fDestination != null ? project.file(fDestination) : null;
    }


    void setDestination(Object pDestination)
    {
        fDestination = pDestination;
    }


    boolean isEnabled()
    {
        return fEnabled;
    }


    void setEnabled(boolean pEnabled)
    {
        fEnabled = pEnabled;
    }


    Report configure(Closure pConfigureClosure)
    {
        if (USE_CONFIGURE_SELF)
            return ConfigureUtil.configureSelf(pConfigureClosure, this);
        else
            return ConfigureUtil.configure(pConfigureClosure, this, false);
    }


    String toString()
    {
        return "Report " + getName();
    }


    /**
     * Check if the {@code configureSelf} method is available in the {@code ConfigureUtil} class.
     * This method, introduced in Gradle 2.14, is, if available, the preferred way to configure
     * an entity with a closure.
     *
     * @return  True if the method is available, false if not.
     */
    static private boolean configureSelfAvailable()
    {
        try
        {
            ConfigureUtil.class.getMethod("configureSelf", Closure.class, Object.class) != null;
            return true;
        }
        catch (ReflectiveOperationException e)
        {
            return false;
        }
    }
}
