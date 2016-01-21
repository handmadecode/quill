/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report

import org.gradle.api.Project


/**
 * Abstract base class for {@code TransformingReport} implementations that use an XSL style sheet
 * stored in a resource if no XSL file has been explicitly specified.
 */
abstract class AbstractTransformingReport extends DefaultSingleFileReport implements TransformingReport
{
    static final String HTML_RESOURCE_REPORT_CSS = '/org/myire/quill/rsrc/report/report.css'


    private final Project fProject;
    private final String fXslResource;

    private File fXslFile;


    /**
     * Create a new {@code AbstractTransformingReport}.
     *
     * @param pProject              The project for which the report will be produced.
     * @param pName                 The report's symbolic name.
     * @param pDisplayName          The report's descriptive name.
     * @param pXslResource          The resource containing the default style sheet to apply if no
     *                              XSL file is specified.
     * @param pDefaultDestination   A closure that will return the report's default file destination
     *                              when called.
     */
    protected AbstractTransformingReport(Project pProject,
                                         String pName,
                                         String pDisplayName,
                                         String pXslResource,
                                         Closure<File> pDefaultDestination)
    {
        super(pProject, pName, pDisplayName, pDefaultDestination);
        fProject = pProject;
        fXslResource = pXslResource;
    }


    @Override
    File getXslFile()
    {
        return fXslFile;
    }


    @Override
    void setXslFile(Object pFile)
    {
        fXslFile = pFile ? fProject.file(pFile) : null;
    }


    @Override
    void transform()
    {
        if (!enabled)
            return;

        File aInputFile = getInputFile();
        if (aInputFile != null)
        {
            if (aInputFile.canRead())
                // Create the report by applying the XSL transformation.
                transformFile(aInputFile);
            else
                fProject.logger.error('The XML input file for the {} report is not readable', getDisplayName());
        }
        else
            fProject.logger.error('The XML input file for the {} report does not exist', getDisplayName());
    }


    @Override
    boolean checkUpToDate()
    {
        if (!enabled)
            // A disabled report is always up to date.
            return true;

        File aInputFile = getInputFile();
        if (aInputFile == null || !aInputFile.exists())
            // If the input file does not exist it cannot be transformed, and the HTML report can
            // therefore not be created, and is thus not out-of-date.
            return true;

        File aDestination = getDestination();
        return aDestination.exists() && aDestination.lastModified() >= aInputFile.lastModified();
    }


    /**
     * Get the XML input file to transform.
     *
     * @return  The XML input file.
     */
    abstract protected File getInputFile();


    /**
     * Apply the transformation to an XML file and write the result to this report's destination.
     *
     * @param pXmlFile  The XML file to transform.
     */
    private void transformFile(File pXmlFile)
    {
        ReportBuilder aReportBuilder = new ReportBuilder(getDestination());
        if (fXslFile != null)
        {
            // An XSL file has been specified, use only its style sheet for the transformation.
            aReportBuilder.transform(pXmlFile, fXslFile);
        }
        else
        {
            // No XSL file specified, use the default resource. All default XSL resources expect to
            // be embedded in the <body> tag and have the default CSS availablex.
            aReportBuilder.write('<html><head><title>');
            aReportBuilder.write(getDisplayName());
            aReportBuilder.write('</title><style type="text/css">');
            aReportBuilder.copy(HTML_RESOURCE_REPORT_CSS);
            aReportBuilder.write('</style></head><body>');
            aReportBuilder.transform(pXmlFile, fXslResource);
            aReportBuilder.write('</body></html>');
        }

        aReportBuilder.close();
    }
}
