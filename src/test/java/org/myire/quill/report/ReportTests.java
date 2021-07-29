/*
 * Copyright 2021 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.nio.file.Path;

import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.reporting.SingleFileReport;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Report test utilities.
 */
final class ReportTests
{
    static SingleFileReport createReportWithOutputLocation(Path pOutputLocation)
    {
        RegularFile aReportFile = mock(RegularFile.class);
        when(aReportFile.getAsFile()).thenReturn(pOutputLocation.toFile());

        RegularFileProperty aReportFileProperty = mock(RegularFileProperty.class);
        when(aReportFileProperty.getOrNull()).thenReturn(aReportFile);

        SingleFileReport aReport = mock(SingleFileReport.class);
        when(aReport.getOutputLocation()).thenReturn(aReportFileProperty);

        return aReport;
    }
}
