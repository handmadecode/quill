/*
 * Copyright 2017-2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.repository;

import java.util.Collection;
import static java.util.Objects.requireNonNull;

import org.myire.quill.common.GradlePrettyPrinter;
import org.myire.quill.common.PrettyPrintable;


/**
 * Abstract base class for a specification of an artifact repository.
 */
abstract public class RepositorySpec implements PrettyPrintable
{
    static private final String CLOSURE_REPOSITORIES = "repositories";
    static private final String ATTRIBUTE_URL = "url";


    private final String fName;
    private final String fUrl;
    private CredentialsSpec fCredentials;


    /**
     * Create a new {@code RepositorySpec}.
     *
     * @param pName The repository's name.
     * @param pUrl  The repository's url.
     *
     * @throws NullPointerException if {@code pName} is null.
     */
    protected RepositorySpec(String pName, String pUrl)
    {
        fName = requireNonNull(pName);
        fUrl = pUrl;
    }


    /**
     * Get the repository's name.
     *
     * @return  The name.
     */
    public String getName()
    {
        return fName;
    }


    /**
     * Get the repository's url.
     *
     * @return  The url as a string.
     */
    public String getUrl()
    {
        return fUrl;
    }


    /**
     * Get the credentials to use when accessing the repository.
     *
     * @return  The credentials, or null if no credentials should be used.
     */
    public CredentialsSpec getCredentials()
    {
        return fCredentials;
    }


    /**
     * Set the credentials to use when accessing the repository.
     *
     * @param pUserName The user name. Pass null to clear the credentials.
     * @param pPassword The password.
     */
    public void setCredentials(String pUserName, String pPassword)
    {
        if (pUserName != null)
            fCredentials = new CredentialsSpec(pUserName, pPassword);
        else
            fCredentials = null;
    }


    /**
     * Pretty print a collection of repository specifications on Gradle repository closure format.
     *
     * @param pPrinter      The printer to print with.
     * @param pRepositories The repositories to print.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    static public void prettyPrintRepositories(
        GradlePrettyPrinter pPrinter,
        Collection<? extends RepositorySpec> pRepositories)
    {
        if (pRepositories.size() > 0)
        {
            pPrinter.printClosure(
                CLOSURE_REPOSITORIES,
                p -> pRepositories.forEach(r -> r.prettyPrint(p))
            );
        }
    }


    /**
     * Print the body of a closure containing the values of this repository specification.
     *
     * @param pPrinter  The printer to print with.
     *
     * @throws NullPointerException if {@code pPrinter} is null.
     */
    protected void printClosureBody(GradlePrettyPrinter pPrinter)
    {
        pPrinter.printMethodCall(ATTRIBUTE_URL, fUrl, true, false);

        CredentialsSpec aCredentials = getCredentials();
        if (aCredentials != null)
            aCredentials.prettyPrint(pPrinter);
    }
}
