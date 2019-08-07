/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common;

/**
 * A {@code PrettyPrintable} can print itself on a format suitable for Gradle build scripts using a
 * {@link GradlePrettyPrinter}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
@FunctionalInterface
public interface PrettyPrintable
{
    /**
     * Pretty print this instance with a {@code GradlePrettyPrinter} on a format suitable for build
     * scripts.
     *
     * @param pPrinter  The printer to print with.
     *
     * @throws NullPointerException if {@code pPrinter} is null.
     */
    void prettyPrint(GradlePrettyPrinter pPrinter);


    /**
     * Enclose a value in quotes and escape any quotes in the value.
     *
     * @param pValue    The value quote.
     *
     * @return  The quoted value.
     */
    static String quote(String pValue)
    {
        return '\'' + pValue.replace("'", "\\'") + '\'';
    }
}
