/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

package org.myire.quill.report;

import java.io.File;

import groovy.lang.Closure;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.Report;
import org.gradle.util.ConfigureUtil;

import org.myire.quill.common.ProjectAware;


/**
 * Implementation of {@code org.gradle.api.reporting.ConfigurableReport}.
 */
public class SimpleConfigurableReport extends ProjectAware implements ConfigurableReport
{
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
     *
     * @throws NullPointerException if {@code pProject} is null.
     */
    public SimpleConfigurableReport(
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
        return fDestination != null ? getProject().file(fDestination) : null;
    }


    @Override
    @SuppressWarnings("deprecation") // Must implement even if deprecated
    public void setDestination(Object pDestination)
    {
        fDestination = pDestination;
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


    @Override
    public boolean isEnabled()
    {
        return fEnabled;
    }


    @Override
    public void setEnabled(boolean pEnabled)
    {
        fEnabled = pEnabled;
    }


    @Override
    public void setEnabled(Provider<Boolean> pProvider)
    {
        fEnabled = pProvider.get().booleanValue();
    }


    @Override
    public Report configure(Closure pConfigureClosure)
    {
        return ConfigureUtil.configureSelf(pConfigureClosure, this);
    }


    @Override
    public String toString()
    {
        return "Report " + getName();
    }
}
