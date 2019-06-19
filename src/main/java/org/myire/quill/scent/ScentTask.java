/*
 * Copyright 2016, 2018-2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.scent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import groovy.lang.Closure;

import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

import org.myire.quill.common.ExternalToolLoader;
import org.myire.quill.common.Projects;
import org.myire.quill.common.Tasks;
import org.myire.quill.report.TransformingReport;


/**
 * Task for creating source code metrics reports using the Scent library.
 */
public class ScentTask extends SourceTask implements Reporting<ScentReports>
{
    // The default version of Scent to use.
    static private final String DEFAULT_TOOL_VERSION = "2.0";

    // Fully qualified name of the ScentRunner implementation class to use.
    static private final String IMPLEMENTATION_PACKAGE = "org.myire.quill.scent.impl.";
    static private final String IMPLEMENTATION_CLASS = "ScentRunnerImpl";


    // Task properties.
    private String fToolVersion;
    private String fSourceEncoding;
    private FileCollection fScentClasspath;
    private ScentReportsImpl fReports;


    /**
     * Get the version of Scent to use. Default is &quot;2.0&quot;.
     */
    @Input
    public String getToolVersion()
    {
        return fToolVersion != null ? fToolVersion : DEFAULT_TOOL_VERSION;
    }


    public void setToolVersion(String pToolVersion)
    {
        fToolVersion = pToolVersion;
    }


    /**
     * Get the encoding of the Java source files. The platform's default encoding will be used if
     * this property isn't specified.
     */
    @Input
    @Optional
    public String getSourceEncoding()
    {
        return fSourceEncoding;
    }


    public void setSourceEncoding(String pSourceEncoding)
    {
        fSourceEncoding = pSourceEncoding;
    }


    /**
     * Get the classpath containing the Scent classes used by the task. The plugin sets this
     * property to its default value, which is the {@code scent} configuration.
     */
    @InputFiles
    public FileCollection getScentClasspath()
    {
        return fScentClasspath;
    }


    public void setScentClasspath(FileCollection pScentClasspath)
    {
        fScentClasspath = pScentClasspath;
    }


    /**
     * Get the source files to collect metrics from. Default is the main source set's Java files.
     *
     * @return  The source files.
     */
    @Override
    @InputFiles
    @SkipWhenEmpty
    public FileTree getSource()
    {
        FileTree aSource = super.getSource();
        if (aSource == null || aSource.isEmpty())
        {
            // No sources specified, use the main source set's java files as default.
            SourceSet aMainSourceSet = Projects.getSourceSet(getProject(), SourceSet.MAIN_SOURCE_SET_NAME);
            if (aMainSourceSet != null)
            {
                aSource = aMainSourceSet.getAllJava();
                setSource(aSource);
            }
        }

        return aSource;
    }


    /**
     * Get the reports produced by this task.
     *
     * @return  The reports.
     */
    @Override
    @Nested
    public ScentReports getReports()
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
    public ScentReports reports(Closure pClosure)
    {
        fReports.configure(pClosure);
        return fReports;
    }


    /**
     * Configure this task's reports.
     *
     * @param pAction   An action that configures the reports.
     *
     * @return  This task's reports.
     */
    @Override
    public ScentReports reports(Action<? super ScentReports> pAction)
    {
        pAction.execute(fReports);
        return fReports;
    }


    /**
     * Calculate code metrics for the sources and produce the enabled report(s).
     */
    @TaskAction
    public void run()
    {
        SingleFileReport aXmlReport = fReports.getXml();
        if (aXmlReport.isEnabled())
        {
            // Collect the code metrics and create the XML report.
            collectMetricsAsXml(aXmlReport.getDestination());

            // Create the HTML report if enabled.
            TransformingReport aHtmlReport = fReports.getHtml();
            if (aHtmlReport.isEnabled())
                aHtmlReport.transform();
        }
        else
            getLogger().info("Scent XML report is disabled, metrics will not be collected");
    }


    /**
     * Create the task's report container and specify the report related inputs and outputs.
     */
    void setupReports()
    {
        fReports = new ScentReportsImpl(this);

        // Only execute the task if its XML report is enabled, as the HTML report is created from
        // the XML report.
        onlyIf(ignore -> getReports().getXml().isEnabled());

        // If any of the reports' enabled flag is modified the task should be rerun.
        Tasks.inputProperty(this, "xmlReportEnabled", () -> this.getReports().getXml().isEnabled());
        Tasks.inputProperty(this, "htmlReportEnabled", () -> this.getReports().getHtml().isEnabled());

        // The XSL file used to create the HTML report is an input to the task.
        Tasks.optionalInputFile(this, () -> this.getReports().getHtml().getXslFile());

        // Add both reports' destination as output of this task.
        Tasks.outputFile(this, () -> this.getReports().getXml().getDestination());
        Tasks.outputFile(this, () -> this.getReports().getHtml().getDestination());
    }


    /**
     * Add checks of the HTML report to the task's up-to-date check of its outputs.
     */
    void addUpToDateCheck()
    {
        // Let the HTML report decide if it is up to date.
        getOutputs().upToDateWhen(ignore -> this.getReports().getHtml().checkUpToDate());
    }


    /**
     * Create the XML report file.
     *
     * @param pXmlFile  The file to write the XML to.
     */
    private void collectMetricsAsXml(File pXmlFile)
    {
        try
        {
            // Ensure the report file's directory exists.
            Projects.ensureParentExists(pXmlFile);

            // Use the charset specified in the sourceEncoding property with the platform default as
            // fallback.
            Charset aCharset =
                fSourceEncoding != null ? Charset.forName(fSourceEncoding) : Charset.defaultCharset();

            getLogger().debug(
                "Collecting scent metrics using charset '{}' into file '{}'",
                aCharset.name(),
                pXmlFile);

            // Collect the metrics and write the XML report.
            loadScentRunner().collectMetricsAsXml(getSource().getFiles(), aCharset, pXmlFile);
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
        {
            getLogger().error("Could not create an instance of '{}{}'",
                              IMPLEMENTATION_PACKAGE,
                              IMPLEMENTATION_CLASS,
                              e);
        }
        catch (IOException ioe)
        {
            getLogger().error("Could not write the metrics report to '{}'", pXmlFile, ioe);
        }
    }


    /**
     * Load the {@code ScentRunner} implementation and thereby the Scent classes specified by the
     * {@code scentClasspath} property.
     *
     * @return  A new instance of the loaded {@code ScentRunner} implementation.
     *
     * @throws ClassNotFoundException   if the implementation class or any class it refers to could
     *                                  not be found.
     * @throws InstantiationException   if the implementation class is abstract or doesn't have a
     *                                  no-args constructor (or can't be instantiated for some other
     *                                  reason).
     * @throws IllegalAccessException   if the implementation class or its no-args constructor
     *                                  isn't accessible.
     */
    private ScentRunner loadScentRunner()
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        ExternalToolLoader<ScentRunner> aLoader =
            new ExternalToolLoader<>(
                ScentRunner.class,
                IMPLEMENTATION_PACKAGE,
                IMPLEMENTATION_CLASS,
                this::getScentClasspath);

        return aLoader.createToolProxy();
    }
}
