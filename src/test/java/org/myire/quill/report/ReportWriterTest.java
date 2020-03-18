/*
 * Copyright 2020 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.gradle.api.reporting.SingleFileReport;

import org.myire.quill.test.FileBasedTest;


/**
 * JUnit tests for {@code ReportWriter}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class ReportWriterTest extends FileBasedTest
{
    @Test(expected = NullPointerException.class)
    public void constructorThrowsForNullReport() throws IOException
    {
        new ReportWriter(null, StandardCharsets.UTF_8);
    }


    @Test(expected = NullPointerException.class)
    public void constructorThrowsForNullCharset() throws IOException
    {
        new ReportWriter(mock(SingleFileReport.class), null);
    }


    @Test(expected = NullPointerException.class)
    public void constructorThrowsForNullOpenOptions() throws IOException
    {
        OpenOption[] aOptions = null;
        new ReportWriter(mock(SingleFileReport.class), StandardCharsets.UTF_8, aOptions);
    }


    @Test
    public void indentationIsWrittenToFile() throws IOException
    {
        // Given
        Path aReportFile = createTemporaryFile("indentationIsWrittenToFile", ".txt");
        ReportWriter aReportWriter = createReportWriter(aReportFile);

        // When
        aReportWriter.writeIndentation();
        aReportWriter.writeLineBreak();
        aReportWriter.increaseIndentationLevel();
        aReportWriter.writeIndentation();
        aReportWriter.writeLineBreak();
        aReportWriter.decreaseIndentationLevel();
        aReportWriter.writeIndentation();
        aReportWriter.writeLineBreak();
        aReportWriter.close();

        // Then
        List<String> aLines = Files.readAllLines(aReportFile);
        assertEquals(3, aLines.size());
        assertEquals("", aLines.get(0));
        assertEquals("  ", aLines.get(1));
        assertEquals("", aLines.get(2));
    }


    @Test
    public void singleCharacterIsWrittenToFile() throws IOException
    {
        // Given
        String aText = "single char text";
        Path aReportFile = createTemporaryFile("singleCharacterIsWrittenToFile", ".txt");
        ReportWriter aReportWriter = createReportWriter(aReportFile);

        // When
        for (int i=0; i<aText.length(); i++)
            aReportWriter.write(aText.charAt(i));
        aReportWriter.close();

        // Then
        List<String> aLines = Files.readAllLines(aReportFile);
        assertEquals(1, aLines.size());
        assertEquals(aText, aLines.get(0));
    }


    @Test
    public void charArrayIsWrittenToFile() throws IOException
    {
        // Given
        String aText = "array text";
        Path aReportFile = createTemporaryFile("charArrayIsWrittenToFile", ".txt");
        ReportWriter aReportWriter = createReportWriter(aReportFile);

        // When
        aReportWriter.write(aText.toCharArray());
        aReportWriter.close();

        // Then
        List<String> aLines = Files.readAllLines(aReportFile);
        assertEquals(1, aLines.size());
        assertEquals(aText, aLines.get(0));
    }


    @Test
    public void stringIsWrittenToFile() throws IOException
    {
        // Given
        String aText = "report text";
        Path aReportFile = createTemporaryFile("charArrayIsWrittenToFile", ".txt");
        ReportWriter aReportWriter = createReportWriter(aReportFile);

        // When
        aReportWriter.write(aText);
        aReportWriter.close();

        // Then
        List<String> aLines = Files.readAllLines(aReportFile);
        assertEquals(1, aLines.size());
        assertEquals(aText, aLines.get(0));
    }


    static private ReportWriter createReportWriter(Path pReportFile) throws IOException
    {
        SingleFileReport aReport = mock(SingleFileReport.class);
        when(aReport.getDestination()).thenReturn(pReportFile.toFile());
        return new ReportWriter(aReport, StandardCharsets.US_ASCII);
    }
}
