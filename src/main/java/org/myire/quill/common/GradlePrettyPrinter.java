/*
 * Copyright 2017, 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common;

import java.io.PrintWriter;
import static java.util.Objects.requireNonNull;


/**
 * Base class for Gradle build script pretty printing. All printing is delegated to a
 * {@code PrintWriter}.
 */
public class GradlePrettyPrinter
{
    private final PrintWriter fWriter;

    private int fIndentationLevel;


    /**
     * Create a new {@code GradlePrettyPrinter}.
     *
     * @param pWriter   The writer to delegate the printing to.
     *
     * @throws NullPointerException if {@code pWriter} is null.
     */
    public GradlePrettyPrinter(PrintWriter pWriter)
    {
        fWriter = requireNonNull(pWriter);
    }


    /**
     * Print the start of a closure at the current indentation level and increment the level.
     *
     * @param pName The name of the closure, possibly null.
     */
    public void printClosureStart(String pName)
    {
        if (pName != null)
            printIndentedLine(pName);
        printIndentedLine("{");
        fIndentationLevel++;
    }


    /**
     * Decrement the indentation level and print the end of a closure at the new level.
     */
    public void printClosureEnd()
    {
        fIndentationLevel--;
        printIndentedLine("}");
    }


    /**
     * Print a closure on a single line at the current indentation level.
     *
     * @param pName     The name of the closure, possibly null.
     * @param pKey      The closure's key.
     * @param pValue    The closure's value.
     */
    public void printSingleLineClosure(String pName, String pKey, String pValue)
    {
        printIndentation();
        if (pName != null)
        {
            fWriter.print(pName);
            fWriter.print(' ');
        }

        fWriter.print('{');
        fWriter.print(pKey);
        fWriter.print(' ');
        fWriter.print(pValue);
        fWriter.println('}');
    }


    /**
     * Print a line containing a key and a value at the current indentation level.
     *
     * @param pKey      The key to print.
     * @param pValue    The value to print.
     */
    public void printKeyValue(String pKey, String pValue)
    {
        printIndentation();
        fWriter.print(pKey);
        fWriter.print(' ');
        fWriter.println(pValue);
    }


    /**
     * Print a line containing a name followed by an argument string inside parentheses at the
     * current indentation level.
     *
     * @param pName The name to print.
     * @param pArgs The argument string to print inside parentheses.
     */
    public void printNameAndArgs(String pName, String pArgs)
    {
        printIndentation();
        fWriter.print(pName);
        fWriter.print('(');
        fWriter.print(pArgs);
        fWriter.println(')');
    }


    /**
     * Print a line at the current indentation level. The line consists of a single string.
     *
     * @param pString   The string to print.
     */
    private void printIndentedLine(String pString)
    {
        printIndentation();
        fWriter.println(pString);
    }


    /**
     * Print the current indentation. Each indentation level is represented by two spaces.
     */
    private void printIndentation()
    {
        for (int i=0; i<fIndentationLevel; i++)
            fWriter.print("  ");
    }


    /**
     * Enclose a value in quotes and escape any quotes in the value.
     *
     * @param pValue    The value quote.
     *
     * @return  The quoted value.
     */
    static public String quote(String pValue)
    {
        return '\'' + pValue.replace("'", "\\'") + '\'';
    }
}
