/*
 * Copyright 2020, 2024 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import org.gradle.api.tasks.Nested;
import org.gradle.util.Configurable;


/**
 * A {@code ReportSet} containing an XML report and an HTML report. The latter is created by
 * applying an XSL transformation to the former.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public interface XmlHtmlReportSet<T extends XmlHtmlReportSet<T>> extends ReportSet, Configurable<T>
{
    /**
     * Get the XML file report.
     *
     * @return The XML file report.
     */
    @Nested
    CompatibleSingleFileReport getXml();

    /**
     * Get the HTML file report. This report is produced by applying an XSL transformation on the
     * XML report.
     *
     * @return The HTML file report.
     */
    @Nested
    TransformingReport getHtml();
}
