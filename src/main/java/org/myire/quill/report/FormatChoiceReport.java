/*
 * Copyright 2015, 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;


/**
 * A {@code SingleFileReport} that produces one of several possible formats.
 */
public interface FormatChoiceReport extends SingleFileReport
{
    /**
     * Get the selected format of the report.
     *
     * @return  The format.
     */
    @Input
    @Optional
    String getFormat();

    /**
     * Set the selected format of the report.
     *
     * @param pFormat   The selected format.
     */
    void setFormat(String pFormat);
}
