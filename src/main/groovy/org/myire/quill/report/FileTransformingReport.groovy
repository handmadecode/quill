/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report

import org.gradle.api.Project

import org.myire.quill.common.Projects


/**
 * A transforming report that applies an XSL transformation on an XML file obtained lazily through a
 * closure.
 */
class FileTransformingReport extends AbstractTransformingReport
{
    private final Closure<File> fInput;


    /**
     * Create a new {@code FileTransformingReport}.
     *
     * @param pProject      The project for which the report will be produced.
     * @param pName         The report's symbolic name.
     * @param pDisplayName  The report's descriptive name.
     * @param pInput        A closure specifying the file to transform the output of.
     * @param pXslResource  The resource containing the default style sheet to apply if no XSL file
     *                      is specified.
     */
    FileTransformingReport(Project pProject,
                           String pName,
                           String pDisplayName,
                           Closure<File> pInput,
                           String pXslResource)
    {
        super(pProject, pName, pDisplayName, pXslResource, { defaultDestination(pProject, pInput, pName) });
        fInput = pInput;
    }


    @Override
    protected File getInputFile()
    {
        return fInput.call();
    }


    static private File defaultDestination(Project pProject, Closure<File> pInput, String pDefaultBaseName)
    {
        File aInputFile = pInput.call();
        if (aInputFile != null)
            return new File(aInputFile.parentFile, aInputFile.name.replace('.xml', '.html'));
        else
            return Projects.createReportDirectorySpec(pProject, pDefaultBaseName + '.html');
    }
}
