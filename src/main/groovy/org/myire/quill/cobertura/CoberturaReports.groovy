/*
 * Copyright 2015, 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cobertura

import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.Nested
import org.gradle.util.Configurable

import org.myire.quill.report.ReportSet


/**
 * The reports produced by a {@code CoberturaReportTask}.
 */
interface CoberturaReports extends ReportSet, Configurable<CoberturaReports>
{
    /**
     * Get the XML file report.
     *
     * @return  The XML file report.
     */
    @Nested
    SingleFileReport getXml();

    /**
     * Get the directory containing the HTML report.
     *
     * @return  The HTML directory report.
     */
    @Nested
    DirectoryReport getHtml();
}
