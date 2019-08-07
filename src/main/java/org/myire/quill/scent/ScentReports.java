/*
 * Copyright 2016, 2018-2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.scent;

import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.Nested;
import org.gradle.util.Configurable;

import org.myire.quill.report.ReportSet;
import org.myire.quill.report.TransformingReport;


/**
 * The reports produced by a {@code ScentTask}.
 */
public interface ScentReports extends ReportSet, Configurable<ScentReports>
{
    /**
     * Get the XML file report.
     *
     * @return The XML file report.
     */
    @Nested
    SingleFileReport getXml();

    /**
     * Get the HTML file report. This report is produced by applying an XSL transformation on the
     * XML report.
     *
     * @return The HTML file report.
     */
    @Nested
    TransformingReport getHtml();
}
