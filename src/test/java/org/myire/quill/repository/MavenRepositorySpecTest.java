/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.repository;

import org.junit.Test;

import static org.myire.quill.common.PrettyPrintableTests.assertPrettyPrint;


/**
 * Unit tests for {@code org.myire.quill.repository.MavenRepositorySpec}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class MavenRepositorySpecTest extends RepositorySpecTest
{
    @Override
    protected RepositorySpec createInstance(String pName, String pUrl)
    {
        return new MavenRepositorySpec(pName, pUrl);
    }


    /**
     * {@code prettyPrint()} should print a closure with the url only if the repository has no
     * credentials.
     */
    @Test
    public void prettyPrintPrintsUrlOnlyIfCredentialsAreNull()
    {
        // Given
        String aUrl= "/path/to/maven";
        MavenRepositorySpec aSpec = new MavenRepositorySpec("mvn", aUrl);

        // Then
        String[] aExpected = {
            "maven",
            "{",
            "  url '" + aUrl + "'",
            "}"
        };
        assertPrettyPrint(aSpec, aExpected);
    }


    /**
     * {@code prettyPrint()} should print a closure with the url and the credentials if the latter are
     * non-null.
     */
    @Test
    public void prettyPrintPrintsCredentials()
    {
        // Given
        String aUrl= "http://repo.acme.com/m2";
        String aUserName = "the.user", aPassword = "pwd2";
        MavenRepositorySpec aSpec = new MavenRepositorySpec("mvn", aUrl);
        aSpec.setCredentials(aUserName, aPassword);

        // Then
        String[] aExpected = {
            "maven",
            "{",
            "  url '" + aUrl + '\'',
            "  credentials",
            "  {",
            "    username = '" + aUserName + '\'',
            "    password = '" + aPassword + '\'',
            "  }",
            "}"
        };

        // Then
        assertPrettyPrint(aSpec, aExpected);
    }
}
