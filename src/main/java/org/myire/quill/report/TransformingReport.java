/*
 * Copyright 2015, 2018, 2019, 2024 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.File;
import java.util.function.BiFunction;

import org.gradle.api.Project;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;


/**
 * A single file report that is created by applying an XSL transformation to some XML input. The
 * result of the transformation will be written to the report's destination.
 */
public interface TransformingReport extends CompatibleSingleFileReport
{
    /**
     * Get the XSL file that will be used to transform the XML input.
     *
     * @return  The XSL file. If this file is null the implementation may use a default style sheet
     *          to perform the transformation.
     */
    @InputFile
    @Optional
    File getXslFile();

    /**
     * Set the file containing the style sheet to use when transforming the XML input.
     *
     * @param pFile The file specification, will be resolved using {@code Project.file()}.
     */
    void setXslFile(Object pFile);

    /**
     * Check if the report is up to date with respect to its XML input.
     *
     * @return  True if this report is up to date, false if not.
     */
    boolean checkUpToDate();

    /**
     * Transform the report's XML input using the XSL style sheet specified for this report and
     * write the result to this report's destination.
     */
    void transform();

    /**
     * Pass the XSL parameter for the project root directory path to a function.
     *
     * @param pProject  The project to get the root directory path from.
     * @param pFunction The function to pass the XSL parameter name and root directory path to.
     *
     * @param <T>       The return type of the function.
     *
     * @return  The result of the function.
     */
    static <T> T applyProjectRootXslParameter(Project pProject, BiFunction<String, Object, T> pFunction)
    {
        return pFunction.apply("gradle-project-root", pProject.getRootDir().getAbsolutePath());
    }
}
