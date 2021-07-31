/*
 * Copyright 2015, 2018, 2019, 2021 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.check

import org.gradle.api.Task
import org.gradle.api.reporting.Report

import org.myire.quill.common.Projects
import org.myire.quill.common.Tasks
import org.myire.quill.report.Reports
import org.myire.quill.report.ReportTransformingReport
import org.myire.quill.report.TransformingReport


/**
 * Abstract base class for enhancers of code quality tasks.
 *
 * @param T   The type of task this enhancer operates on.
 */
abstract class AbstractCheckTaskEnhancer<T extends Task>
{
    static final String TRANSFORMING_REPORT_NAME = 'quillHtmlReport'


    private final T fTask;


    /**
     * Create a new {@code AbstractTaskEnhancer}.
     *
     * @param pTask The task to enhance.
     */
    protected AbstractCheckTaskEnhancer(T pTask)
    {
        fTask = pTask;
    }


    /**
     * Get the task being enhanced by this enhancer.
     *
     * @return  The task under enhancement.
     */
    T getTask()
    {
        return fTask;
    }


    /**
     * Configure the task's extension with sensible defaults.
     */
    abstract void enhance();


    /**
     * Create a {@code TransformingReport} and add it to the task being enhanced.
     *<ul>
     * <li>The report will have the name &quot;<i>taskName</i>Html&quot;</li>
     * <li>The report will have the display name &quot;<i>taskName</i> HTML report&quot;</li>
     * <li>The report will be added to the task's convention properties under the name
     *     &quot;quillHtmlReport&quot;</li>
     * <li>The report's {@code checkUpToDate()} method will be added to the task's outputs
     *     up-to-date check</li>
     * <li>A call to the report's {@code transform()} method will be appended to the task's
     *     actions</li>
     *</ul>
     *
     * @param pInputReport  The report that will be transformed by the new transforming report.
     * @param pXslResource  The resource containing the default style sheet to apply if no XSL file
     *                      is specified for the new transforming report.
     */
    protected void addTransformingReport(Report pInputReport, String pXslResource)
    {
        // Create the transforming report, enable it and add it to the task's convention.
        TransformingReport aReport = new ReportTransformingReport(fTask.project,
                                                                  fTask.name + 'Html',
                                                                  fTask.name.capitalize() + ' HTML report',
                                                                  pInputReport,
                                                                  pXslResource);
        Reports.setRequired(aReport, true);
        fTask.convention.add(TRANSFORMING_REPORT_NAME, aReport);

        // The XSL file used to create the transforming report is an input to the task.
        Tasks.optionalInputFile(fTask, { -> aReport.xslFile });

        // Add a check of the report to the task's output up-to-date checks.
        fTask.outputs.upToDateWhen({ aReport.checkUpToDate() });

        // Add a task action to create the HTML report.
        fTask.doLast({ aReport.transform() });
    }


    /**
     * Extract a classpath resource to a file. If the file already exists it will be left
     * unmodified.
     *
     * @param pResource The name of the resource.
     * @param pFile     The file to extract to.
     */
    protected void extractResourceToFile(String pResource, File pFile)
    {
        if (Projects.extractResource(pResource, pFile))
            fTask.logger.debug('Copied resource {} to file {}', pResource, pFile.absolutePath);
    }
}
