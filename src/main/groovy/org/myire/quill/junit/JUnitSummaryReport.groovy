/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.junit

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.testing.Test

import org.myire.quill.common.Projects
import org.myire.quill.report.DefaultSingleFileReport


/**
 * A report that aggregates Junit XML reports into a single report.
 */
class JUnitSummaryReport extends DefaultSingleFileReport
{
    // By default the test report files to aggregate are on the form "TEST-xxxxx.xml".
    static private final String DEFAULT_FILE_NAME_PATTERN = '^TEST\\-.*\\.xml$';


    File junitReportDirectory;
    String fileNamePattern;


    private final Test fTask;


    /**
     * Create a new {@code JUnitSummaryReport}.
     *
     * @param pTask The task this report is part of.
     */
    JUnitSummaryReport(Test pTask)
    {
        super(pTask.project, 'junitSummary', 'JUnit XML summary report', { defaultReportFile(pTask.project) });
        fTask = pTask;
    }


    /**
     * Get the directory where to look for the JUnit XML reports to aggregate.
     *
     * @return  The JUnit report directory.
     */
    @InputDirectory
    @SkipWhenEmpty
    File getJunitReportDirectory()
    {
        if (junitReportDirectory != null)
            return junitReportDirectory;
        else
            return fTask.reports.getJunitXml()?.destination;
    }


    /**
     * Get a regular expression with the pattern that the names of the report files to aggregate
     * must match. The default pattern is &quot;^TEST\\-.*\\.xml$&quot;.
     *
     * @return  The JUnit report file name pattern.
     */
    @Input
    String getFileNamePattern()
    {
        if (fileNamePattern != null)
            return fileNamePattern;
        else
            return DEFAULT_FILE_NAME_PATTERN;
    }


    /**
     * Check if the report is up to date.
     *
     * @return  True if this report is up tp date, false if not.
     */
    boolean checkUpToDate()
    {
        if (!enabled)
            // A disabled report is always up-to-date.
            return true;

        File aReportFile = getDestination();
        if (aReportFile == null)
            // No report file should be created and it is thereby always up-to-date.
            return true;

        if (!aReportFile.exists())
            // The report file doesn't exist and is thus out-of-date.
            return false;

        // The summary report is up-to-date if it was modified after or at the same time as the HTML
        // report.
        File aHtmlIndex = fTask.reports.getHtml()?.entryPoint;
        return aHtmlIndex == null || aReportFile.lastModified() >= aHtmlIndex.lastModified();
    }


    /**
     * Create the JUnit summary report.
     */
    void createReport()
    {
        JUnitReportAggregator aAggregator = new JUnitReportAggregator();
        aAggregator.aggregate(getJunitReportDirectory(), getFileNamePattern());
        aAggregator.writeXmlFile(getDestination());
    }


    static private File defaultReportFile(Project pProject)
    {
        File aDirectory = Projects.createReportDirectorySpec(pProject, 'junit');
        return new File(aDirectory, 'junitSummary.xml');
    }
}
