/*
 * Copyright 2017-2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.repository;

import org.myire.quill.common.GradlePrettyPrinter;


/**
 * Specification of a Maven artifact repository.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class MavenRepositorySpec extends RepositorySpec
{
    static private final String CLOSURE_MAVEN = "maven";


    /**
     * Create a new {@code MavenRepositorySpec}.
     *
     * @param pName The repository's name.
     * @param pUrl  The repository's url.
     *
     * @throws NullPointerException if {@code pName} is null.
     */
    public MavenRepositorySpec(String pName, String pUrl)
    {
        super(pName, pUrl);
    }


    @Override
    public void prettyPrint(GradlePrettyPrinter pPrinter)
    {
        pPrinter.printClosure(CLOSURE_MAVEN, this::printClosureBody);
    }
}
