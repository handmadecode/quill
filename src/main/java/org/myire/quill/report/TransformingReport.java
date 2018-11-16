/*
 * Copyright 2015, 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.File;

import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;


/**
 * A single file report that is created by applying an XSL transformation to some XML input. The
 * result of the transformation will be written to the report's destination.
 */
public interface TransformingReport extends SingleFileReport
{
    /**
     * Get the XSL file that will be used to transform the XML input.
     *
     * @return  The XSL file. If this file is null the implementation may use a default style sheet
     *          to perform the transformation.
     */
    @Input
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
}
