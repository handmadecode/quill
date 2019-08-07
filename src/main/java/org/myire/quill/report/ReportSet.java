/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import org.gradle.api.reporting.Report;


/**
 * A {@code ReportSet} provides access to a set of reports by their names.
 */
public interface ReportSet
{
    /**
     * Get a report by its name.
     *
     * @param pReportName   The report's name.
     *
     * @return  The report with the specified name, or null if the set contains no report with that
     *          name.
     */
    Report getReportByName(String pReportName);
}
