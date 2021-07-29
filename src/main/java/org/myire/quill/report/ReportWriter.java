/*
 * Copyright 2020 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;

import org.gradle.api.reporting.SingleFileReport;


/**
 * A writer for the destination file of a {@code SingleFileReport}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class ReportWriter implements Closeable
{
    static private final char[] LINE_BREAK = System.getProperty("line.separator").toCharArray();
    static private final char[] INDENTATION = {' ', ' '};


    private final Writer fWriter;
    private int fIndentationLevel;


    /**
     * Create a new {@code ReportWriter}.
     *
     * @param pReport       The report to write.
     * @param pCharset      The character set to encode the report with.
     * @param pOpenOptions  Any options to specify when opening the report's file.
     *
     * @throws IOException  if opening the report's destination file fails.
     * @throws NullPointerException if any of the parameters is null.
     */
    public ReportWriter(
        SingleFileReport pReport,
        Charset pCharset,
        OpenOption... pOpenOptions) throws IOException
    {
        fWriter =
            new OutputStreamWriter(
                Files.newOutputStream(Reports.getOutputLocation(pReport).toPath(), pOpenOptions),
                pCharset);
    }


    /**
     * Increase the indentation level used by {@link #writeIndentation()}.
     */
    public void increaseIndentationLevel()
    {
        fIndentationLevel++;
    }


    /**
     * Decrease the indentation level used by {@link #writeIndentation()}.
     */
    public void decreaseIndentationLevel()
    {
        fIndentationLevel--;
    }


    /**
     * Write the current indentation to the report file. Each indentation level is represented  by
     * two spaces.
     *
     * @throws IOException if writing to the report file fails.
     */
    public void writeIndentation() throws IOException
    {
        for (int i=0; i<fIndentationLevel; i++)
            write(INDENTATION);
    }


    /**
     * Write the platform's line separator to the report file.
     *
     * @throws IOException if writing to the report file fails.
     */
    public void writeLineBreak() throws IOException
    {
        write(LINE_BREAK);
    }


    /**
     * Write a single character to the report file.
     *
     * @param pChar The character to write.
     *
     * @throws IOException if writing to the report file fails.
     */
    public void write(char pChar) throws IOException
    {
        fWriter.write(pChar);
    }


    /**
     * Write all characters in an array to the report file.
     *
     * @param pChars    The characters to write.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pChars} is null.
     */
    public void write(char[] pChars) throws IOException
    {
        fWriter.write(pChars);
    }


    /**
     * Write all characters in a string to the report file.
     *
     * @param pChars    The characters to write.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pChars} is null.
     */
    public void write(String pChars) throws IOException
    {
        fWriter.write(pChars);
    }


    /**
     * Flush any buffered writes to the report file.
     *
     * @throws IOException if flushing to the report file fails.
     */
    public void flush() throws IOException
    {
        fWriter.flush();
    }


    @Override
    public void close() throws IOException
    {
        fWriter.close();
    }
}
