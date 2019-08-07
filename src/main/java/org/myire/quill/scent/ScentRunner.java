/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.scent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;


/**
 * A {@code ScentRunner} collects source code metrics from a collection of files using the Scent
 * library, and writes an XML report with the collected metrics.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public interface ScentRunner
{
    /**
     * Collect source code metrics from a collection of files and write an XML report.
     *
     * @param pFiles        The files and/or directories to collect Java file metrics from.
     * @param pCharset      The charset the Java files are encoded in.
     * @param pReportFile   The file to write the XML report to.
     *
     * @throws IOException  if writing the report file fails.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    void collectMetricsAsXml(
        Collection<File> pFiles,
        Charset pCharset,
        File pReportFile) throws IOException;
}
