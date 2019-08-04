/*
 * Copyright 2017, 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common;

import java.io.PrintWriter;
import java.util.function.Consumer;

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
     * Print a closure at the current indentation level and increment the level.
     *
     * @param pName         The name of the closure, possibly null.
     * @param pBodyAction   An action that when passed this instance will print the closure's body.
     */
    public void printClosure(String pName, Consumer<GradlePrettyPrinter> pBodyAction)
    {
        if (pName != null)
            printIndentedLine(pName);

        printIndentedLine("{");

        fIndentationLevel++;
        pBodyAction.accept(this);
        fIndentationLevel--;

        printIndentedLine("}");
    }


    /**
     * Print a line with an assignment of a string attribute. The line is printed at the current
     * indentation level. If the value is null, nothing is printed.
     *
     * @param pName     The name of the attribute.
     * @param pValue    The value of the attribute, possibly null.
     */
    public void printAttribute(String pName, String pValue)
    {
        if (pValue != null)
        {
            printIndentation();
            fWriter.print(pName);
            fWriter.print(" = ");
            fWriter.println(PrettyPrintable.quote(pValue));
        }
    }


    /**
     * Print a line with an assignment of a boolean attribute. The line is printed at the current
     * indentation level.
     *
     * @param pName     The name of the attribute.
     * @param pValue    The value of the attribute.
     */
    public void printAttribute(String pName, boolean pValue)
    {
        printIndentation();
        fWriter.print(pName);
        fWriter.print(" = ");
        fWriter.println(pValue);
    }


    /**
     * Print a line containing a method call with a string argument. The string will be put inside
     * single quotes, and not surrounded by parentheses, i.e. the call will be on the format
     *<pre>
     * name 'arg'
     *</pre>
     * The line is printed at the current indentation level. If the argument is null, nothing is
     * printed.
     *
     * @param pName             The name of the method.
     * @param pArgument         The argument to the method, possibly null.
     * @param pQuote            If true, the string argument will be put inside quotes.
     * @param pWithParentheses  If true, the string argument will be enclosed in parentheses.
     */
    public void printMethodCall(String pName, String pArgument, boolean pQuote, boolean pWithParentheses)
    {
        if (pArgument != null)
        {
            if (pQuote)
                pArgument = PrettyPrintable.quote(pArgument);

            printIndentation();
            fWriter.print(pName);

            if (pWithParentheses)
            {
                fWriter.print('(');
                fWriter.print(pArgument);
                fWriter.println(')');
            }
            else
            {
                fWriter.print(' ');
                fWriter.println(pArgument);
            }
        }
    }


    /**
     * Print a line at the current indentation level. The line consists of a single string.
     *
     * @param pLine The line to print.
     */
    public void printIndentedLine(String pLine)
    {
        printIndentation();
        fWriter.println(pLine);
    }


    /**
     * Print the current indentation. Each indentation level is represented by two spaces.
     */
    private void printIndentation()
    {
        for (int i=0; i<fIndentationLevel; i++)
            fWriter.print("  ");
    }
}
