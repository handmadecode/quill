/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;


/**
 * Utility method for unit tests of {@code org.myire.quill.common.PrettyPrintable} implementations.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public final class PrettyPrintableTests
{
    /**
     * Private constructor to disallow instantiations of utility method class.
     */
    private PrettyPrintableTests()
    {
        // Empty default ctor, defined to override access scope.
    }


    /**
     * Invoke the {@code prettyPrint} method on a {@code PrettyPrintable} and assert that what is
     * printed is equal to a sequence of text lines.
     *
     * @param pPrintable        The instance to print.
     * @param pExpectedLines    The lines that {@code pPrintable} is expected to print.
     *
     * @throws AssertionError   if the printed lines differ from the expected lines.
     */
    static public void assertPrettyPrint(PrettyPrintable pPrintable, String... pExpectedLines)
    {
        assertPrintAction(pPrintable::prettyPrint, pExpectedLines);
    }


    /**
     * Pass a {@code GradlePrettyPrinter} to a test action and assert that what is printed is equal
     * to a sequence of text lines.
     *
     * @param pTestAction       The test action.
     * @param pExpectedLines    The lines that {@code pPrintable} is expected to print.
     *
     * @throws AssertionError   if the printed lines differ from the expected lines.
     */
    static public void assertPrintAction(
        Consumer<GradlePrettyPrinter> pTestAction,
        String... pExpectedLines)
    {
        // Let the PrettyPrintable print itself to a StringWriter wrapped inside a
        // GradlePrettyPrinter.
        StringWriter aWriter = new StringWriter();
        pTestAction.accept(new GradlePrettyPrinter(new PrintWriter(aWriter)));

        // Assert that the printed result is equals to the expected lines.
        assertEquals(toLines(pExpectedLines), aWriter.toString());
    }


    /**
     * Concatenate a sequence of strings, separating them with line separators.
     *
     * @param pLines    The lines to concatenate.
     *
     * @return  A string with the concatenated lines.
     */
    static private String toLines(String... pLines)
    {
        StringBuilder aBuffer = new StringBuilder(16 * pLines.length);
        for (String aLine : pLines)
            aBuffer.append(aLine).append(System.lineSeparator());

        return aBuffer.toString();
    }
}
