/*
 * Copyright 2020 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;

import org.gradle.api.reporting.SingleFileReport;


/**
 * A writer of XML file reports.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class XmlReportWriter extends ReportWriter
{
    static private final char[] PROLOG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>".toCharArray();

    static private final FragmentWriter<?> NULL_FRAGMENT_WRITER = _x -> {};


    /**
     * Create a new {@code XmlReportWriter}.
     *
     * @param pReport       The report to write.
     * @param pOpenOptions  Any options to specify when opening the report's file.
     *
     * @throws IOException  if opening the report's destination file fails.
     * @throws NullPointerException if any of the parameters is null.
     */
    public XmlReportWriter(SingleFileReport pReport, OpenOption... pOpenOptions) throws IOException
    {
        super(pReport, StandardCharsets.UTF_8, pOpenOptions);
    }


    /**
     * Write the XML prolog to the underlying stream.
     *
     * @throws IOException if writing to the report file fails.
     */
    public void writeProlog() throws IOException
    {
        write(PROLOG);
        writeLineBreak();
    }


    /**
     * Write an element with attributes and a body.
     *
     * @param pElementName      The name of the element.
     * @param pAttributeWriter  A writer of the element's attributes.
     * @param pBodyWriter       A writer of the element's body.
     * @param pElement          The element's data.
     *
     * @param <T> The type holding the element data.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pElementName} or one of the writers is null.
     */
    public <T> void writeElement(
        String pElementName,
        FragmentWriter<T> pAttributeWriter,
        FragmentWriter<T> pBodyWriter,
        T pElement) throws IOException
    {
        writeElementStart(pElementName, pAttributeWriter, pElement);
        increaseIndentationLevel();
        pBodyWriter.write(pElement);
        decreaseIndentationLevel();
        writeElementEnd(pElementName);
    }


    /**
     * Write an element with a body and no attributes.
     *
     * @param pElementName  The name of the element.
     * @param pBodyWriter   A writer of the element's body.
     * @param pBody         The body's data.
     *
     * @param <T> The type holding the body data.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pElementName} or {@code pBodyWriter} is null.
     */
    public <T> void writeElement(
        String pElementName,
        FragmentWriter<T> pBodyWriter,
        T pBody) throws IOException
    {
        writeElementStart(pElementName, NULL_FRAGMENT_WRITER, null);
        increaseIndentationLevel();
        pBodyWriter.write(pBody);
        decreaseIndentationLevel();
        writeElementEnd(pElementName);
    }


    /**
     * Write an element with attributes and no body on a single line.
     *
     * @param pElementName      The name of the element.
     * @param pAttributeWriter  A writer of the element's attributes.
     * @param pAttributes       The attributes' data.
     *
     * @param <T> The type holding the attribute data.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pElementName} or {@code pAttributeWriter} is null.
     */
    public <T> void writeEmptyElement(
        String pElementName,
        FragmentWriter<T> pAttributeWriter,
        T pAttributes) throws IOException
    {
        writeIndentation();
        write('<');
        write(pElementName);
        pAttributeWriter.write(pAttributes);
        write('/');
        write('>');
        writeLineBreak();
    }


    /**
     * Write an element without attributes and body on a single line.
     *
     * @param pElementName  The name of the element.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pElementName} is null.
     */
    public void writeEmptyElement(String pElementName) throws IOException
    {
        writeEmptyElement(pElementName, NULL_FRAGMENT_WRITER, null);
    }


    /**
     * Write the opening tag of an element without attributes on a separate line.
     *
     * @param pElementName      The name of the element.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if any of the parameters is null.
     */
    public void writeElementStart(String pElementName) throws IOException
    {
        writeElementStart(pElementName, NULL_FRAGMENT_WRITER, null);
    }


    /**
     * Write the opening tag of an element with attributes on a separate line.
     *
     * @param pElementName      The name of the element.
     * @param pAttributeWriter  A writer of the element's attributes.
     * @param pAttributes       The attributes' data.
     *
     * @param <T> The type holding the attribute data.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pElementName} or {@code pAttributeWriter} is null.
     */
    public <T> void writeElementStart(
        String pElementName,
        FragmentWriter<T> pAttributeWriter,
        T pAttributes) throws IOException
    {
        writeIndentation();
        write('<');
        write(pElementName);
        pAttributeWriter.write(pAttributes);
        write('>');
        writeLineBreak();
    }


    /**
     * Write the closing tag of an element on a separate line.
     *
     * @param pElementName  The name of the element.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pName} is null.
     */
    public void writeElementEnd(String pElementName) throws IOException
    {
        writeIndentation();
        write('<');
        write('/');
        write(pElementName);
        write('>');
        writeLineBreak();
    }


    /**
     * Write a sequence of elements as child elements to a sequence root element, which in its turn
     * is a child element to the current element.
     *<p>
     * Example:
     *<pre>
     * &lt;sequence-root&gt;
     *   &lt;sequence-child/&gt;
     *   &lt;sequence-child/&gt;
     * &lt;/sequence-root&gt;
     *</pre>
     *
     * @param pRootElementName      The name of the sequence root element.
     * @param pChildElementName     The name of the sequence child elements.
     * @param pChildAttributeWriter A writer of the attributes of each child element.
     * @param pChildBodyWriter      A writer of the body of each child element.
     * @param pChildren             The instances with the values for the child elements.
     *
     * @param <T>   The type containing child element data.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if any of the reference parameters is null.
     */
    public <T> void writeSequence(
        String pRootElementName,
        String pChildElementName,
        FragmentWriter<T> pChildAttributeWriter,
        FragmentWriter<T> pChildBodyWriter,
        Iterable<T> pChildren) throws IOException
    {
        // Write the opening root element tag.
        writeElementStart(pRootElementName);

        // Write each child element as an indented element, delegating the actual writing to the
        // writers passed as parameters.
        increaseIndentationLevel();
        for (T aChild : pChildren)
            writeElement(pChildElementName, pChildAttributeWriter, pChildBodyWriter, aChild);
        decreaseIndentationLevel();

        // Write the closing root element tag.
        writeElementEnd(pRootElementName);
    }


    /**
     * Write an attribute of the current element.
     *
     * @param pName     The attribute's name.
     * @param pValue    The attribute's value.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if any of the parameters is null.
     */
    public void writeAttribute(String pName, String pValue) throws IOException
    {
        write(' ');
        write(pName);
        write('=');
        write('"');
        writeEscaped(pValue);
        write('"');
    }


    /**
     * Write a numeric attribute of the current element.
     *
     * @param pName     The attribute's name.
     * @param pValue    The attribute's value.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pName} is null.
     */
    public void writeAttribute(String pName, long pValue) throws IOException
    {
        write(' ');
        write(pName);
        write('=');
        write('"');
        write(String.valueOf(pValue));
        write('"');
    }


    /**
     * Write an attribute value, escaping any special XML characters such as '&lt;'.
     *
     * @param pValue    The attribute value.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pValue} is null.
     */
    public void writeEscaped(String pValue) throws IOException
    {
        int aLength = pValue.length();
        for (int i=0; i<aLength; i++)
        {
            char aChar = pValue.charAt(i);
            if (aChar == '<')
                write("&lt;");
            else if (aChar == '"')
                write("&quot;");
            else if (aChar == '&')
                write("&amp;");
            else
                write(aChar);
        }
    }


    /**
     * A writer of an XML report fragment.
     *
     * @param <T>   The type holding the fragment data.
     */
    @FunctionalInterface
    public interface FragmentWriter<T>
    {
        /**
         * Write a report fragment.
         *
         * @param pFragment The fragment's data.
         *
         * @throws IOException if writing to the report file fails.
         */
        void write(T pFragment) throws IOException;
    }
}
