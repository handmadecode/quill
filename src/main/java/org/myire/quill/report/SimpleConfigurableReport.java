/*
 * Copyright 2018, 2020 Peter Franzen. All rights reserved.
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
public class SimpleConfigurableReport extends ProjectAware implements ConfigurableReport
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
        return fRequiredProperty.get().booleanValue();
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
}
