/*
 * Copyright 2016 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.scent

import java.nio.charset.Charset

import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.reporting.Reporting
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

import org.myire.quill.common.ClassLoaders
import org.myire.quill.common.Projects
import org.myire.quill.report.TransformingReport


/**
 * Task for creating source code metrics reports using the Scent library.
 */
class ScentTask extends SourceTask implements Reporting<ScentReports>
{
    // The default version of Scent to use.
    static private final String DEFAULT_TOOL_VERSION = "0.9"


    // Property accessed through getter and setter only.
    private ScentReportsImpl fReports;


    /**
     * The version of Scent to use. Default is &quot;0.9&quot;.
     */
    @Input
    String toolVersion

    /**
     * Classpath containing the Scent classes used by the task. The plugin sets this property to
     * its default value, which is the {@code scent} configuration.
     */
    @InputFiles
    FileCollection scentClasspath

    /**
     * The encoding of the Java source files. The platform's default encoding will be used if this
     * property isn't specified.
     */
    @Input
    @Optional
    String sourceEncoding


    /**
     * Create the task's report container and specify the report related inputs and outputs.
     */
    void setupReports()
    {
        fReports = new ScentReportsImpl(this);

        // Only execute the task if its XML report is enabled, as the HTML report is created from
        // the XML report.
        onlyIf { reports.getXml().enabled }

        // If any of the reports' enabled flag is modified the task should be rerun.
        inputs.property('xmlReportEnabled', { -> this.reports.getXml().enabled })
        inputs.property('htmlReportEnabled', { -> this.reports.getHtml().enabled })

        // The XSL file used to create the HTML report is an input to the task.
        inputs.file({ -> this.reports.getHtml().xslFile })

        // Add both reports' destination as output of this task.
        outputs.file( { -> this.reports.getXml().destination } );
        outputs.file( { -> this.reports.getHtml().destination } );
    }


    /**
     * Get the reports produced by this task.
     *
     * @return  The reports.
     */
    @Override
    ScentReports getReports()
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
    ScentReports reports(Closure pClosure)
    {
        return fReports.configure(pClosure);
    }


    @Override
    @InputFiles
    @SkipWhenEmpty
    FileTree getSource()
    {
        FileTree aSource = super.getSource();
        if (aSource == null || aSource.empty)
        {
            // No sources specified, use the main source set as default.
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
     * Add checks of the HTML report to the task's up-to-date check of its outputs.
     */
    void addUpToDateCheck()
    {
        outputs.upToDateWhen {
            // Let the HTML report decide if it is up to date.
            return getReports().getHtml().checkUpToDate();
        }
    }


    /**
     * Get the default destination for the task's XML report.
     *
     * @return  The XML report's destination.
     */
    File defaultXmlDestination()
    {
        return new File(Projects.createReportDirectorySpec(project, 'scent'), 'scent.xml');
    }


    /**
     * Calculate code metrics for the sources and produce the enabled report(s).
     */
    @TaskAction
    void run()
    {
        SingleFileReport aXmlReport = fReports.getXml();
        if (aXmlReport.enabled)
        {
            // Inject the scent classpath into the current class loader.
            ClassLoaders.inject(getClass().getClassLoader(), scentClasspath);

            // Collect the code metrics and create the XML report.
            createXmlReportFile(collectMetricsAsXml(loadScentRunner()), aXmlReport.destination);

            // Create the HTML report if enabled.
            TransformingReport aHtmlReport = fReports.getHtml();
            if (aHtmlReport.enabled)
                aHtmlReport.transform();
        }
        else
            logger.info('Scent XML report is disabled, metrics will not be collected');
    }


    /**
     * Collect source code metrics for the Java files specified in the {@code source} property and
     * return the result as an XML node.
     *
     * @param pScentRunner  The {@code ScentRunner} instance to delegate the collecting of metrics
     *                      to.
     *
     * @return  A {@code Node} with the collected metrics, never null.
     */
    private Node collectMetricsAsXml(Object pScentRunner)
    {
        // Use the charset specified in the sourceEncoding property with the platform default as
        // fallback.
        Charset aCharset = sourceEncoding ? Charset.forName(sourceEncoding) : Charset.defaultCharset();
        logger.debug('Collecting scent metrics using charset \'{}\'', aCharset.name());
        return pScentRunner.collectMetricsAsXml(source.files, aCharset);
    }


    /**
     * Create the XML report file.
     *
     * @param pXml      The file's contents.
     * @param pXmlFile  The file to write the XML to.
     */
    private void createXmlReportFile(Node pXml, File pXmlFile)
    {
        logger.debug('Creating Scent XML report \'{}\'', pXmlFile);
        pXmlFile.parentFile?.mkdirs();
        new XmlNodePrinter(new PrintWriter(pXmlFile)).print(pXml);
    }


    /**
     * Load the Scent runner delegate. Since the Scent classes are injected into the class loader
     * when this task is executed, those classes cannot be referenced already when this class is
     * loaded. To overcome this the ScentRunner class is loaded by name and instantiated separately
     * when the classes are available.
     *
     * @return  A new {@code ScentRunner} instance, never null.
     */
    private Object loadScentRunner()
    {
        return getClass().getClassLoader().loadClass('org.myire.quill.scent.ScentRunner').newInstance();
    }
}
