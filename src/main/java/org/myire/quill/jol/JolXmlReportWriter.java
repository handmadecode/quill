/*
 * Copyright 2020 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jol;

import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.gradle.api.reporting.SingleFileReport;

import org.myire.quill.report.XmlReportWriter;


/**
 * A write of Jol analysis result XML report files.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
class JolXmlReportWriter extends XmlReportWriter
{
    static private final String ELEMENT_REPORT = "jol-report";
    static private final String ELEMENT_PACKAGES = "packages";
    static private final String ELEMENT_PACKAGE = "package";
    static private final String ELEMENT_CLASS = "class";
    static private final String ELEMENT_FIELD = "field";


    /**
     * Create a new {@code JolXmlReportWriter}.
     *
     * @param pReport   The Jol XMl report.
     *
     * @throws IOException  if opening the report's destination file fails.
     * @throws NullPointerException if any of the parameters is null.
     */
    JolXmlReportWriter(SingleFileReport pReport) throws IOException
    {
        super(pReport, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }


    /**
     * Write a {@code JolResult} to the destination file of the report specified in the constructor.
     *
     * @param pResult   The result to write.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pResult} is null.
     */
    void writeReport(JolResult pResult) throws IOException
    {
        writeProlog();
        writeElement(ELEMENT_REPORT, this::writeReportAttributes, this::writePackages, pResult);
    }


    /**
     * Write the attributes of the root element.
     *
     * @param pResult   The instance holding the values for the attributes.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pResult} is null.
     */
    private void writeReportAttributes(JolResult pResult) throws IOException
    {
        LocalDateTime aTimestamp = LocalDateTime.now().withNano(0);
        writeAttribute("date", DateTimeFormatter.ISO_DATE.format(aTimestamp));
        writeAttribute("time", DateTimeFormatter.ISO_LOCAL_TIME.format(aTimestamp));

        writeAttribute("version", pResult.getVersion());
        writeAttribute("description", pResult.getDescription());

        writeAttribute("total-internal-gap-size", pResult.getInternalAlignmentGapSize());
        writeAttribute("total-external-gap-size", pResult.getExternalAlignmentGapSize());
    }


    /**
     * Write the sequence of package elements.
     *
     * @param pResult   The instance holding the values for the elements.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pResult} is null.
     */
    private void writePackages(JolResult pResult) throws IOException
    {
        if (pResult.getNumPackages() > 0)
        {
            writeSequence(
                ELEMENT_PACKAGES,
                ELEMENT_PACKAGE,
                this::writePackageAttributes,
                this::writePackageBody,
                pResult.getPackages());
        }
        else
        {
            writeEmptyElement(ELEMENT_PACKAGES);
        }
    }


    /**
     * Write the attributes of a package element.
     *
     * @param pPackageLayout    The instance holding the values for the attributes.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pResult} is null.
     */
    private void writePackageAttributes(JolResult.PackageLayout pPackageLayout) throws IOException
    {
        writeAttribute("name", pPackageLayout.getName());
        writeAttribute("total-internal-gap-size", pPackageLayout.getInternalAlignmentGapSize());
        writeAttribute("total-external-gap-size", pPackageLayout.getExternalAlignmentGapSize());
    }


    /**
     * Write the body of a package element. The body contains one class element for each analyzed
     * class in the package.
     *
     * @param pPackageLayout    The instance holding the values for the element's body.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pResult} is null.
     */
    private void writePackageBody(JolResult.PackageLayout pPackageLayout) throws IOException
    {
        for (JolResult.ClassLayout aClassLayout : pPackageLayout.getClasses())
        {
            if (aClassLayout.getNumFields() > 0)
                writeElement(ELEMENT_CLASS, this::writeClassAttributes, this::writeClassBody, aClassLayout);
            else
                writeEmptyElement(ELEMENT_CLASS, this::writeClassAttributes, aClassLayout);
        }
    }


    /**
     * Write the attributes of a class element.
     *
     * @param pClassLayout  The instance holding the values for the attributes.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pResult} is null.
     */
    private void writeClassAttributes(JolResult.ClassLayout pClassLayout) throws IOException
    {
        writeAttribute("name", pClassLayout.getFullClassName());
        writeAttribute("header-size", pClassLayout.getHeaderSize());
        writeAttribute("instance-size", pClassLayout.getInstanceSize());
        writeAttribute("internal-gaps", pClassLayout.getInternalAlignmentGapSize());
        writeAttribute("external-gaps", pClassLayout.getExternalAlignmentGapSize());
    }


    /**
     * Write the body of a class element. The body contains one field element for each field in the
     * class.
     *
     * @param pClassLayout  The instance holding the values for the element's body.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pResult} is null.
     */
    private void writeClassBody(JolResult.ClassLayout pClassLayout) throws IOException
    {
        for (JolResult.FieldLayout aFieldLayout : pClassLayout.getFields())
            writeEmptyElement(ELEMENT_FIELD, this::writeFieldAttributes, aFieldLayout);
    }


    /**
     * Write the attributes of a field element.
     *
     * @param pFieldLayout  The instance holding the values for the attributes.
     *
     * @throws IOException if writing to the report file fails.
     * @throws NullPointerException if {@code pResult} is null.
     */
    private void writeFieldAttributes(JolResult.FieldLayout pFieldLayout) throws IOException
    {
        writeAttribute("name", pFieldLayout.getName());
        writeAttribute("type", pFieldLayout.getType());
        writeAttribute("offset", pFieldLayout.getOffset());
        writeAttribute("size", pFieldLayout.getSize());
    }
}
