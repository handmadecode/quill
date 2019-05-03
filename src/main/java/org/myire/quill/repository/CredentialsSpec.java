/*
 * Copyright 2017-2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.repository;

import static java.util.Objects.requireNonNull;

import org.myire.quill.common.GradlePrettyPrinter;
import org.myire.quill.common.PrettyPrintable;


/**
 * Specification of a username and password.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class CredentialsSpec implements PrettyPrintable
{
    static private final String CLOSURE_CREDENTIALS ="credentials";
    static private final String ATTRIBUTE_USER_NAME ="username";
    static private final String ATTRIBUTE_PASSWORD ="password";

    private final String fUserName;
    private final String fPassword;


    /**
     * Create a new {@code CredentialsSpec}.
     *
     * @param pUserName The user name.
     * @param pPassword The password.
     *
     * @throws NullPointerException if {@code pUserName} is null.
     */
    public CredentialsSpec(String pUserName, String pPassword)
    {
        fUserName = requireNonNull(pUserName);
        fPassword = pPassword;
    }


    public String getUserName()
    {
        return fUserName;
    }


    public String getPassword()
    {
        return fPassword;
    }


    @Override
    public void prettyPrint(GradlePrettyPrinter pPrinter)
    {
        pPrinter.printClosure(CLOSURE_CREDENTIALS, this::printClosureBody);
    }


    /**
     * Print the body of a closure containing the values of this credentials specification.
     *
     * @param pPrinter  The printer to print with.
     *
     * @throws NullPointerException if {@code pPrinter} is null.
     */
    private void printClosureBody(GradlePrettyPrinter pPrinter)
    {
        pPrinter.printAttribute(ATTRIBUTE_USER_NAME, getUserName());
        pPrinter.printAttribute(ATTRIBUTE_PASSWORD, getPassword());
    }
}
