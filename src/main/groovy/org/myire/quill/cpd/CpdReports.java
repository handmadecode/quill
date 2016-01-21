/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cpd;

import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.ReportContainer;

import org.myire.quill.report.FormatChoiceReport;
import org.myire.quill.report.TransformingReport;


/**
 * The reports produced by a {@code CpdTask}.
 */
public interface CpdReports extends ReportContainer<Report>
{
    // The supported primary report formats.
    String FORMAT_XML = "xml";
    String FORMAT_CSV = "csv";
    String FORMAT_TEXT = "text";
    String FORMAT_CSV_LINECOUNT = "csv_with_linecount_per_file";


    /**
     * Get the primary report, which is a single file report on either the XML, CSV, or text format.
     *
     * @return The primary report.
     */
    FormatChoiceReport getPrimary();

    /**
     * Get the HTML file report. This report is produced by applying an XSL transformation on the
     * primary report, given that the latter is on the XML format.
     *
     * @return The HTML file report.
     */
    TransformingReport getHtml();
}
