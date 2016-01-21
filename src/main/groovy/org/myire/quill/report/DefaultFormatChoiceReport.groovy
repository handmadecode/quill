/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report

import org.gradle.api.Task


/**
 * A {@code FormatChoiceReport} with a default destination.
 */
class DefaultFormatChoiceReport extends DefaultSingleFileReport implements FormatChoiceReport
{
    private final Task fTask;
    private final Set<String> fLegalFormats = new HashSet<>();
    private String fFormat


    /**
     * Create a new {@code DefaultFormatChoiceReport}.
     *
     * @param pTask                 The task for which the report will be produced.
     * @param pName                 The report's symbolic name.
     * @param pDisplayName          The report's display name.
     * @param pDefaultFormat        The report's default format.
     * @param pDefaultDestination   A closure that will return the report's default file destination
     *                              when called.
     */
    DefaultFormatChoiceReport(Task pTask,
                              String pName,
                              String pDisplayName,
                              String pDefaultFormat,
                              Closure<File> pDefaultDestination)
    {
        super(pTask.project, pName, pDisplayName, pDefaultDestination);
        fTask = pTask;
        fLegalFormats.add(pDefaultFormat);
        fFormat = pDefaultFormat;
    }


    /**
     * Add a sequence of formats accepted by this report.
     *
     * @param pFormats  The legal formats to add.
     */
    void addLegalFormats(String... pFormats)
    {
        for (String aFormat : pFormats)
            fLegalFormats.add(aFormat);
    }


    @Override
    String getFormat()
    {
        return fFormat;
    }


    @Override
    void setFormat(String pFormat)
    {
        if (fLegalFormats.contains(pFormat))
            fFormat = pFormat;
        else
            fTask.logger.warn('Report \'{}\' in task \'{}\' does not support format \'{}\', keeping \'{}\'',
                              name,
                              fTask.name,
                              pFormat,
                              fFormat)
    }
}
