/*
 * Copyright 2020 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.gradle.api.reporting.SingleFileReport;

import org.myire.quill.test.FileBasedTest;


/**
 * JUnit tests for {@code XmlReportWriter}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class XmlReportWriterTest extends FileBasedTest
{
    @Test(expected = NullPointerException.class)
    public void constructorThrowsForNullReport() throws IOException
    {
        new XmlReportWriter(null);
    }


    @Test(expected = NullPointerException.class)
    public void constructorThrowsForNullOpenOptions() throws IOException
    {
        OpenOption[] aOptions = null;
        new XmlReportWriter(mock(SingleFileReport.class), aOptions);
    }


    @Test
    public void prologIsWrittenToFile() throws IOException
    {
        // Given
        Path aReportFile = createTemporaryFile("prologIsWrittenToFile", ".xml");
        XmlReportWriter aReportWriter = createXmlReportWriter(aReportFile);

        // When
        aReportWriter.writeProlog();
        aReportWriter.close();

        // Then
        List<String> aLines = Files.readAllLines(aReportFile);
        assertEquals(1, aLines.size());
        assertTrue(aLines.get(0).startsWith("<?xml version="));
    }


    @Test
    public void elementWithAttributesAndBodyIsWrittenToFile() throws IOException
    {
        // Given
        Path aReportFile = createTemporaryFile("elementWithAttributesAndBodyIsWrittenToFile", ".xml");
        XmlReportWriter aReportWriter = createXmlReportWriter(aReportFile);

        // When
        aReportWriter.writeElement(
            "parent",
            _s -> aReportWriter.writeAttribute("a", "b"),
            aReportWriter::writeEmptyElement,
            "child");
        aReportWriter.close();

        // Then
        List<String> aLines = Files.readAllLines(aReportFile);
        assertEquals(3, aLines.size());
        assertEquals("<parent a=\"b\">", aLines.get(0));
        assertEquals("  <child/>", aLines.get(1));
        assertEquals("</parent>", aLines.get(2));
    }


    @Test
    public void elementWithAttributesOnlyIsWrittenToFile() throws IOException
    {
        // Given
        Path aReportFile = createTemporaryFile("elementWithAttributesOnlyIsWrittenToFile", ".xml");
        XmlReportWriter aReportWriter = createXmlReportWriter(aReportFile);

        // When
        aReportWriter.writeEmptyElement(
            "empty",
            _a -> aReportWriter.writeAttribute(_a, "b"),
            "attr");
        aReportWriter.close();

        // Then
        List<String> aLines = Files.readAllLines(aReportFile);
        assertEquals(1, aLines.size());
        assertEquals("<empty attr=\"b\"/>", aLines.get(0));
    }


    @Test
    public void elementWithBodyOnlyIsWrittenToFile() throws IOException
    {
        // Given
        Path aReportFile = createTemporaryFile("elementWithBodyOnlyIsWrittenToFile", ".xml");
        XmlReportWriter aReportWriter = createXmlReportWriter(aReportFile);

        // When
        aReportWriter.writeElement(
            "parent",
            aReportWriter::writeEmptyElement,
            "child");
        aReportWriter.close();

        // Then
        List<String> aLines = Files.readAllLines(aReportFile);
        assertEquals(3, aLines.size());
        assertEquals("<parent>", aLines.get(0));
        assertEquals("  <child/>", aLines.get(1));
        assertEquals("</parent>", aLines.get(2));
    }


    @Test
    public void sequenceIsWrittenToFile() throws IOException
    {
        // Given
        Path aReportFile = createTemporaryFile("sequenceIsWrittenToFile", ".xml");
        XmlReportWriter aReportWriter = createXmlReportWriter(aReportFile);

        // When
        aReportWriter.writeSequence(
            "parent",
            "child",
            _v -> aReportWriter.writeAttribute("attr", _v),
            aReportWriter::writeEmptyElement,
            Arrays.asList("first", "second", "third"));
        aReportWriter.close();

        // Then
        List<String> aLines = Files.readAllLines(aReportFile);
        assertEquals(11, aLines.size());
        assertEquals("<parent>", aLines.get(0));
        assertEquals("  <child attr=\"first\">", aLines.get(1));
        assertEquals("    <first/>", aLines.get(2));
        assertEquals("  </child>", aLines.get(3));
        assertEquals("  <child attr=\"second\">", aLines.get(4));
        assertEquals("    <second/>", aLines.get(5));
        assertEquals("  </child>", aLines.get(6));
        assertEquals("  <child attr=\"third\">", aLines.get(7));
        assertEquals("    <third/>", aLines.get(8));
        assertEquals("  </child>", aLines.get(9));
        assertEquals("</parent>", aLines.get(10));
    }


    static private XmlReportWriter createXmlReportWriter(Path pReportFile) throws IOException
    {
        SingleFileReport aReport = mock(SingleFileReport.class);
        when(aReport.getDestination()).thenReturn(pReportFile.toFile());
        return new XmlReportWriter(aReport);
    }
}
