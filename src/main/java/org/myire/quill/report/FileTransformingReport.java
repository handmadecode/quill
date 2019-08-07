/*
 * Copyright 2015, 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.File;

import groovy.lang.Closure;

import org.gradle.api.Project;

import org.myire.quill.common.Projects;


/**
 * A transforming report that applies an XSL transformation on an XML file obtained lazily through a
 * closure.
 */
public class FileTransformingReport extends AbstractTransformingReport
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
     *
     * @throws NullPointerException if {@code pProject} or {@code pDefaultDestination} is null.
     */
    public FileTransformingReport(
        Project pProject,
        String pName,
        String pDisplayName,
        String pXslResource,
        Closure<File> pInput)
    {
        super(pProject, pName, pDisplayName, pXslResource, new DefaultDestination(pProject, pInput, pName));
        fInput = pInput;
    }


    @Override
    protected File getInputFile()
    {
        return fInput.call();
    }


    /**
     * Closure for lazily evaluating the default destination.
     */
    static private class DefaultDestination extends Closure<File>
    {
        private final Project fProject;
        private final Closure<File> fInput;
        private final String fDefaultBaseName;

        DefaultDestination(Project pProject, Closure<File> pInput, String pDefaultBaseName)
        {
            super(null);
            fProject = pProject;
            fInput = pInput;
            fDefaultBaseName = pDefaultBaseName;
        }

        public File doCall(Object pValue)
        {
            File aInputFile = fInput.call();
            if (aInputFile != null)
                return new File(aInputFile.getParentFile(), aInputFile.getName().replace(".xml", ".html"));
            else
                return Projects.createReportDirectorySpec(fProject, fDefaultBaseName + ".html");
        }
    }
}
