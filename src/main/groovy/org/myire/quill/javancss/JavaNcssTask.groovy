/*
 * Copyright 2014-2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.javancss

import org.gradle.api.Action
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.reporting.Reporting
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

import org.myire.quill.common.Projects
import org.myire.quill.report.TransformingReport


/**
 * Task for calculating source code metrics by invoking the JavaNCSS tool.
 */
public class JavaNcssTask extends SourceTask implements Reporting<JavaNcssReports>
{
    static private final String DEFAULT_TOOL_VERSION = "33.54"


    // Property accessed through getter and setter only.
    private JavaNcssReports fReports;


    /**
     * The version of JavaNCSS to use. Default is &quot;33.54&quot;.
     */
    @Input
    String toolVersion

    /**
     * Classpath containing the JavaNCSS classes used by the task.
     */
    @InputFiles
    FileCollection javancssClasspath

    /**
     * Should the total number of non commenting source statements in the input files be calculated?
     * Default is true.
     */
    @Input
    boolean ncss = true

    /**
     * Should metrics data be calculated for each package? Default is true.
     */
    @Input
    boolean packageMetrics = true

    /**
     * Should metrics data be calculated for each class/interface? Default is true.
     */
    @Input
    boolean classMetrics = true

    /**
     * Should metrics data be calculated for each method/function? Default is true.
     */
    @Input
    boolean functionMetrics = true

    /**
     * Should the build continue in case of failures? Default is true.
     */
    @Input
    boolean ignoreFailures = true


    /**
     * Create the task's report container.
     */
    void setupReports()
    {
        fReports = new JavaNcssReportsImpl(this);

        // Only execute the task if its primary report is enabled.
        onlyIf { reports.getPrimary().enabled }

        // If any of the reports' enabled flag is modified the task should be rerun.
        inputs.property('primaryReportEnabled', { -> this.reports.getPrimary().enabled })
        inputs.property('htmlReportEnabled', { -> this.reports.getHtml().enabled })

        // The XSL file used to create the HTML report is an input to the task.
        inputs.file({ -> this.reports.getHtml().xslFile })

        // Add both reports' destination as output of this task.
        outputs.file( { -> this.reports.getPrimary().destination } );
        outputs.file( { -> this.reports.getHtml().destination } );
    }


    /**
     * Get the reports produced by this task.
     *
     * @return  The reports.
     */
    @Override
    JavaNcssReports getReports()
    {
        return fReports;
    }


    /**
     * Configure this task's reports.
     *
     * @param pClosure  A closure that configures the reports.
     *
     * @return  This task's reports.
     */
    @Override
    JavaNcssReports reports(Closure pClosure)
    {
        return fReports.configure(pClosure);
    }


    @Override
    JavaNcssReports reports(Action<? super JavaNcssReports> pAction)
    {
        pAction.execute(fReports);
        return fReports;
    }


    @Override
    @InputFiles
    @SkipWhenEmpty
    FileTree getSource()
    {
        FileTree aSource = super.getSource();
        if (aSource == null || aSource.empty)
        {
            aSource = Projects.getSourceSet(project, SourceSet.MAIN_SOURCE_SET_NAME)?.allJava;
            setSource(aSource);
        }

        return aSource;
    }


    String getToolVersion()
    {
        return toolVersion ?: DEFAULT_TOOL_VERSION;
    }


    /**
     * Add checks of the reports to the task's up-to-date check of its outputs.
     */
    void addUpToDateCheck()
    {
        outputs.upToDateWhen {
            // If the primary report is written to console it is never up to date.
            return !getReports().getPrimary().destination?.name?.endsWith("console");
        }

        outputs.upToDateWhen {
            // Let the HTML report decide if it is up to date.
            return getReports().getHtml().checkUpToDate();
        }
    }


    /**
     * Get the default destination for the task's primary report.
     *
     * @return  The primary report's destination.
     */
    File defaultPrimaryDestination()
    {
        String aFormat = reports.getPrimary().format;
        return new File(Projects.createReportDirectorySpec(project, 'javancss'), 'javancss.' + aFormat);
    }

    /**
     * Calculate code metrics on the sources and produces the enabled report(s).
     */
    @TaskAction
    void run()
    {
        SingleFileReport aPrimaryReport = fReports.getPrimary();
        if (aPrimaryReport.enabled)
        {
            // Perform the code metrics calculation and create the primary report.
            new JavaNcssExec(this).execute();

            // Create the HTML report if enabled.
            TransformingReport aHtmlReport = fReports.getHtml();
            if (aHtmlReport.enabled)
                aHtmlReport.transform();
        }
        else
            logger.info('JavaNCSS primary is report disabled, skipping analysis');
    }
}
