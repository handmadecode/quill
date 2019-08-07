/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cpd;

import java.io.File;
import java.io.IOException;
import java.util.Collection;


/**
 * A {@code CpdRunner} performs copy-paste analysis on a collection of files using the CPD tool, and
 * writes a report with the result of the analysis.
 */
public interface CpdRunner
{
    /**
     * Perform copy-paste analysis on a collection of files and write a report with the result.
     *
     * @param pFiles        The files to analyze.
     * @param pReportFile   The file to write the report to.
     * @param pReportFormat The format of the report, see {@link CpdReports}.
     * @param pParameters   The parameters to configure CPD with.
     *
     * @throws IOException  if accessing the files to analyze fails, or if the report cannot be
     *                      written.
     */
    void runCpd(
        Collection<File> pFiles,
        File pReportFile,
        String pReportFormat,
        CpdParameters pParameters) throws IOException;
}
