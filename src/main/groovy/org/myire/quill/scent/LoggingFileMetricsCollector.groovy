/*
 * Copyright 2016 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.scent

import java.nio.charset.Charset
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import org.myire.scent.collect.JavaMetricsCollector
import org.myire.scent.file.JavaFileMetricsCollector
import org.myire.scent.metrics.PackageMetrics


/**
 * Extension of {@code JavaFileMetricsCollector} that logs progress and errors through Gradle
 * logging.
 */
class LoggingFileMetricsCollector extends JavaFileMetricsCollector
{
    static private final String ERROR_LOG_TEMPLATE = "Failed to collect source code metrics from %s: %s";

    private final Logger fLogger = Logging.getLogger(LoggingFileMetricsCollector.class);


    /**
     * Create a new {@code LoggingFileMetricsCollector}.
     *
     * @param pFileEncoding The charset the Java files are encoded with.
     */
    LoggingFileMetricsCollector(Charset pFileEncoding)
    {
        super(new JavaMetricsCollector(), pFileEncoding);
    }


    /**
     * Log that a directory is about to be scanned for Java files to collect metrics from.
     *
     * @param pDirectory    The path to the directory.
     * @param pAttributes   The directory's attributes.
     *
     * @return  Always {@code FileVisitResult.CONTINUE}.
     */
    @Override
    public FileVisitResult preVisitDirectory(Path pDirectory, BasicFileAttributes pAttributes)
    {
        fLogger.debug('Collecting source code metrics for Java files in ' + pDirectory);
        return FileVisitResult.CONTINUE;
    }


    /**
     * Visit a file by collecting source code metrics from it if it is a Java file. If an error
     * occurs when accessing the file it will be logged, and the file processing will be continued.
     *
     * @param pFile         The path to the file.
     * @param pAttributes   The file's attributes.
     *
     * @return  Always {@code FileVisitResult.CONTINUE}.
     */
    @Override
    public FileVisitResult visitFile(Path pFile, BasicFileAttributes pAttributes)
    {
        try
        {
            return super.visitFile(pFile, pAttributes);
        }
        catch (IOException e)
        {
            fLogger.error(String.format(ERROR_LOG_TEMPLATE, pFile, e.message));
            return FileVisitResult.CONTINUE;
        }
    }


    /**
     * Log an exception thrown when opening a file or when reading its attributes.
     *
     * @param pFile         The path to the file.
     * @param pException    The exception.
     *
     * @return  Always {@code FileVisitResult.CONTINUE}.
     */
    @Override
    public FileVisitResult visitFileFailed(Path pFile, IOException pException)
    {
        fLogger.error(String.format(ERROR_LOG_TEMPLATE, pFile, pException.message));
        return FileVisitResult.CONTINUE;
    }


    /**
     * Log any exception thrown when iterating over the items in a directory.
     *
     * @param pFile         The path to the file.
     * @param pException    The exception, or null if the directory was visited successfully.
     *
     * @return  Always {@code FileVisitResult.CONTINUE}.
     */
    @Override
    public FileVisitResult postVisitDirectory(Path pDirectory, IOException pException)
    {
        if (pException != null)
            fLogger.error(String.format(ERROR_LOG_TEMPLATE, pDirectory, pException.message));

        return FileVisitResult.CONTINUE;
    }


    /**
     * Get the metrics collected by this instance.
     *
     * @return  The collected metrics, never null.
     */
    Iterable<PackageMetrics> getCollectedMetrics()
    {
        return getDelegate().collectedMetrics;
    }
}
