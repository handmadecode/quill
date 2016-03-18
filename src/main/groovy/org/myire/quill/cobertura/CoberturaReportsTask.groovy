/*
 * Copyright 2014-2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cobertura

import org.gradle.api.file.FileCollection
import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.reporting.Reporting
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction


/**
 * Task that creates a summary XML report and/or a detailed HTML report from a Cobertura coverage
 * analysis.
 *<p>
 * Note that all properties are read-only; they are configured in the task's context.
 *<p>
 * The properties of this task are to a large extent identical to the ones of the
 * {@code Cobertura-Report} Ant task, see the
 * <a href="https://github.com/cobertura/cobertura/wiki/Ant-Task-Reference">reference</> for more
 * information.
 */
class CoberturaReportsTask extends AbstractCoberturaTask implements Reporting<CoberturaReports>
{
    // Properties accessed through getter and setter only.
    private CoberturaReportsImpl fReports


    void init(CoberturaExtension pExtension, CoberturaContext pContext)
    {
        super.init(pExtension, pContext);
        fReports = new CoberturaReportsImpl(this);

        // If any of the reports' enabled flag is modified the task should be rerun.
        inputs.property('xmlReportEnabled', { -> this.reports.getXml().enabled })
        inputs.property('htmlReportEnabled', { -> this.reports.getHtml().enabled })

        // Add both reports' destination as output of this task.
        outputs.file( { -> this.reports.getXml().destination } );
        outputs.dir( { -> this.reports.getHtml().destination } );

        // Only execute if the data file exists and at least one of the reports is enabled.
        onlyIf { getDataFile().exists() };
        onlyIf { this.reports.getXml().enabled || this.reports.getHtml().enabled };
    }


    /**
     * Get the reports produced by this task.
     *
     * @return  The reports.
     */
    @Override
    CoberturaReports getReports()
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
    CoberturaReports reports(Closure pClosure)
    {
        return fReports.configure(pClosure);
    }


    /**
     * Get the file containing meta data about the instrumented classes and the test execution.
     *
     * @return  The execution data file.
     */
    @InputFile
    File getDataFile()
    {
        return context.executionDataFile;
    }


    /**
     * Get the directories containing the sources of the analyzed classes.
     *
     * @return  The source file directories.
     */
    @InputFiles
    FileCollection getSourceDirs()
    {
        return context.sourceDirs;
    }


    /**
     * Get the encoding to use when reading the source files. The platform's default encoding will
     * be used if this property is null.
     */
    @Input
    @Optional
    String getSourceEncoding()
    {
        return context.sourceEncoding;
    }


    /**
     * Create the Cobertura reports specified by the {@code reports} property.
     */
    @TaskAction
    void createReports()
    {
        SingleFileReport aXmlReport = getReports().xml;
        DirectoryReport aHtmlReport = getReports().html;
        if (aXmlReport.enabled || aHtmlReport.enabled)
        {
            // Define the Ant task from the Cobertura classes.
            ant.taskdef(name: 'coberturaReport',
                        classname: 'net.sourceforge.cobertura.ant.ReportTask',
                        classpath: getCoberturaClassPath().asPath);

            if (aXmlReport.enabled)
                createXmlReport(aXmlReport.destination);

            if (aHtmlReport.enabled)
                createCoberturaReport(aHtmlReport.destination, 'html');
        }
        else
            // Neither report type specified, nothing to do.
            logger.warn('No report type enabled for Cobertura, no reports will be created');
    }


    /**
     * Create the XML summary report.
     */
    private void createXmlReport(File pXmlReportFile)
    {
        File aReportDirectory = pXmlReportFile.parentFile;
        createCoberturaReport(aReportDirectory, 'xml');

        // Cobertura doesn't allow the file name to be specified, it always uses the name
        // 'coverage.xml'. If another file name was specified the created file must be renamed.
        File aCreatedFile = new File(aReportDirectory, 'coverage.xml');
        if (pXmlReportFile != aCreatedFile)
        {
            logger.debug('Renaming {} to {}', aCreatedFile.absolutePath, pXmlReportFile.absolutePath);
            aCreatedFile.renameTo(pXmlReportFile);
        }
    }


    /**
     * Create a Cobertura report.
     *
     * @param pDirectory    The directory to create the report in.
     * @param pFormat       The format of the report, either 'xml' or 'html'.
     */
    private void createCoberturaReport(File pDirectory, String pFormat)
    {
        logger.debug("Creating Cobertura {} report in {}", pFormat, pDirectory.absolutePath);

        // Create the argument list for the ant task.
        def aArguments = [format: pFormat,
                          datafile: getDataFile(),
                          destdir: pDirectory];

        String aSourceEncoding = getSourceEncoding();
        if (aSourceEncoding != null)
            aArguments['encoding'] = aSourceEncoding;

        // Execute the ant report task.
        ant.coberturaReport(aArguments)
        {
            // Specify each source directory as an Ant file set.
            getSourceDirs()?.each {
                project.fileTree(it).addToAntBuilder(ant, "fileset", FileCollection.AntType.FileSet);
            }
        }
    }
}
