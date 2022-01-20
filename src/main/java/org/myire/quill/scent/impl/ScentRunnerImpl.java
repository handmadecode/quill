/*
 * Copyright 2016, 2018, 2022 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.scent.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Collection;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import org.myire.scent.collect.JavaLanguageLevel;
import org.myire.scent.collect.JavaMetricsCollector;
import org.myire.scent.report.MetricsReportMetaData;
import org.myire.scent.report.XmlReportWriter;

import org.myire.quill.scent.ScentRunner;


/**
 * Delegate class for running Scent and converting the collected metrics to XML. This class
 * encapsulates all dependencies on the Scent library, making it possible to easily control when
 * those classes are first referenced and loaded.
 */
public class ScentRunnerImpl implements ScentRunner
{
    private final Logger fLogger = Logging.getLogger(ScentRunnerImpl.class);


    /**
     * Scan the directories and files in a collection of Java files, collect source code metrics
     * for them, and create an XML report from the collected metrics.
     *
     * @param pFiles            The files and/or directories to collect Java file metrics from.
     * @param pLanguageLevel    The Java language level to use when parsing the source files. Pass
     *                          {@code 0} to use the default language level.
     * @param pEnableLanguagePreviews
     *                          If true, language feature previews at the specified language level
     *                          will be enabled.
     * @param pCharset          The charset the Java files are encoded with.
     * @param pReportFile       The file to write the XML report to.
     *
     * @throws IOException  if writing the report file fails.
     *
     * @throws NullPointerException if any of the parameters is null.
     *
     */
    @Override
    public void collectMetricsAsXml(
        Collection<File> pFiles,
        Charset pCharset,
        int pLanguageLevel,
        boolean pEnableLanguagePreviews,
        File pReportFile) throws IOException
    {
        JavaLanguageLevel aLanguageLevel =
            pLanguageLevel == 0 ?
                JavaLanguageLevel.getDefault():
                    JavaLanguageLevel.forNumericValue(pLanguageLevel);
        if (aLanguageLevel == null)
        {
            aLanguageLevel = JavaLanguageLevel.getDefault();
            fLogger.error(
                "Java language level " +
                pLanguageLevel +
                " is not supported, using default level " +
                aLanguageLevel.getNumericValue());
        }

        JavaMetricsCollector aCollector = new JavaMetricsCollector(aLanguageLevel, pEnableLanguagePreviews);

        // Visit each file/directory in the collection and pass the Java files to the
        // JavaMetricsCollector.
        CollectingFileVisitor aVisitor = new CollectingFileVisitor(aCollector, pCharset);
        for (File aJavaFile : pFiles)
        {
            try
            {
                Files.walkFileTree(aJavaFile.toPath(), aVisitor);
            }
            catch (IOException e)
            {
                // Log and continue with the next file.
                fLogger.error("Error when collecting metrics from " + aJavaFile + ": " + e.getMessage());
            }
        }

        // Write the XML report.
        try (FileOutputStream aStream = new FileOutputStream(pReportFile))
        {
            // Get the current timestamp and the version string of the Scent library.
            MetricsReportMetaData aReportMetaData = new MetricsReportMetaData(
                LocalDateTime.now().withNano(0),
                org.myire.scent.Main.class.getPackage().getImplementationVersion());

            // Write the collected metrics as an XML report.
            XmlReportWriter aReportWriter = new XmlReportWriter(aStream);
            aReportWriter.writeReport(aCollector.getCollectedMetrics(), aReportMetaData);
        }
    }
}
