/*
 * Copyright 2015, 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import groovy.lang.Closure;

import org.gradle.api.Project;


/**
 * A {@code FormatChoiceReport} with a default destination.
 */
public class DefaultFormatChoiceReport extends DefaultSingleFileReport implements FormatChoiceReport
{
    private final Set<String> fLegalFormats = new HashSet<>();
    private String fFormat;


    /**
     * Create a new {@code DefaultFormatChoiceReport}.
     *
     * @param pProject              The project for which the report will be produced.
     * @param pName                 The report's symbolic name.
     * @param pDisplayName          The report's display name.
     * @param pDefaultFormat        The report's default format.
     * @param pDefaultDestination   A closure that will return the report's default file destination
     *                              when called.
     *
     * @throws NullPointerException if {@code pProject} or {@code pDefaultDestination} is null.
     */
    public DefaultFormatChoiceReport(
        Project pProject,
        String pName,
        String pDisplayName,
        String pDefaultFormat,
        Closure<File> pDefaultDestination)
    {
        super(pProject, pName, pDisplayName, pDefaultDestination);
        fLegalFormats.add(pDefaultFormat);
        fFormat = pDefaultFormat;
    }


    /**
     * Add a sequence of formats accepted by this report.
     *
     * @param pFormats  The legal formats to add.
     */
    public void addLegalFormats(String... pFormats)
    {
        Collections.addAll(fLegalFormats, pFormats);
    }


    @Override
    public String getFormat()
    {
        return fFormat;
    }


    @Override
    public void setFormat(String pFormat)
    {
        if (fLegalFormats.contains(pFormat))
            fFormat = pFormat;
        else
            getProjectLogger().warn(
                "Report '{}' does not support format '{}', keeping '{}'",
                getName(),
                pFormat,
                fFormat);
    }
}
