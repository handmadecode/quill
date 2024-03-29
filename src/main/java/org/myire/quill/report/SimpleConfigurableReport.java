/*
 * Copyright 2018, 2020-2021 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

package org.myire.quill.report;

import java.io.File;

import groovy.lang.Closure;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.Report;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.util.ConfigureUtil;

import org.myire.quill.common.ProjectAware;


/**
 * Implementation of {@code org.gradle.api.reporting.ConfigurableReport}.
 */
abstract public class SimpleConfigurableReport extends ProjectAware implements ConfigurableReport
{
    private final String fName;
    private final String fDisplayName;
    private final Report.OutputType fOutputType;

    private final Property<Boolean> fRequiredProperty;
    private Object fDestination;


    /**
     * Create a new {@code SimpleReport}.
     *
     * @param pProject      The project for which the report will be produced.
     * @param pName         The report's symbolic name.
     * @param pDisplayName  The report's descriptive name.
     * @param pOutputType   The type of output the report produces.
     *
     * @throws NullPointerException if {@code pProject} is null.
     */
    protected SimpleConfigurableReport(
            Project pProject,
            String pName,
            String pDisplayName,
            Report.OutputType pOutputType)
    {
        super(pProject);

        fName = pName;
        fDisplayName = pDisplayName;
        fOutputType = pOutputType;

        fRequiredProperty = pProject.getObjects().property(Boolean.class);
        fRequiredProperty.set(Boolean.FALSE);
    }


    @Override
    public String getName()
    {
        return fName;
    }


    @Override
    public String getDisplayName()
    {
        return fDisplayName;
    }


    @Override
    public Report.OutputType getOutputType()
    {
        return fOutputType;
    }


    @Override
    public File getDestination()
    {
        return resolveDestination();
    }


    @Override
    public void setDestination(File pFile)
    {
        fDestination = pFile;
    }


    @Override
    public void setDestination(Provider<File> pProvider)
    {
        fDestination = pProvider;
    }


    /**
     * Set the value of the {@code outputLocation} property.
     *
     * @param pLocation The property's new value.
     */
    public void setOutputLocation(Object pLocation)
    {
        useDestination(pLocation);
    }


    @Override
    public boolean isEnabled()
    {
        return reportIsRequired();
    }


    @Override
    public void setEnabled(boolean pEnabled)
    {
        fRequiredProperty.set(Boolean.valueOf(pEnabled));
    }


    @Override
    public void setEnabled(Provider<Boolean> pProvider)
    {
        fRequiredProperty.set(pProvider);
    }


    @Override
    public Report configure(Closure pConfigureClosure)
    {
        return ConfigureUtil.configureSelf(pConfigureClosure, this);
    }


    // 6.1 compatibility.
    @Input
    public Property<Boolean> getRequired()
    {
        return fRequiredProperty;
    }


    /**
     * Set the value of the {@code required} property.
     *
     * @param pValue    The property's new value.
     */
    public void setRequired(boolean pValue)
    {
        fRequiredProperty.set(Boolean.valueOf(pValue));
    }


    // Override to add @Internal annotation
    @Override
    @Internal
    public Project getProject()
    {
        return super.getProject();
    }


    // Override to add @Internal annotation
    @Override
    @Internal
    public Logger getProjectLogger()
    {
        return super.getProjectLogger();
    }


    @Override
    public String toString()
    {
        return "Report " + getName();
    }


    /**
     * Check if this report is required (enabled) and should be created.
     *
     * @return  True if this report is required, false if not.
     */
    protected boolean reportIsRequired()
    {
        return fRequiredProperty.getOrElse(Boolean.FALSE).booleanValue();
    }


    /**
     * Resolve the destination specified by the latest call to {@link #setDestination(File)},
     * {@link #setDestination(Provider)}, or {@link #useDestination(Object)}.
     *
     * @return  The destination file resolved with the project directory as base directory. If no
     *          destination has been specified, null is returned.
     */
    protected File resolveDestination()
    {
        return fDestination != null ? getProject().file(fDestination) : null;
    }


    /**
     * Set the value of the report's destination without calling {@link #setDestination(File)}. That
     * method was deprecated in Gradle 7.1.
     *
     * @param pDestination  The report's destination, possibly null.
     */
    protected void useDestination(Object pDestination)
    {
        fDestination = pDestination;
    }
}
