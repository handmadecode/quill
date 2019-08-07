/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.repository;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.myire.quill.common.PrettyPrintableTests.assertPrintAction;


/**
 * Unit tests for static methods in {@code RepositorySpec}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class RepositorySpecStaticTest
{
    /**
     * {@code prettyPrintRepositories()} should print a closure with all repository specifications
     * passed to the method.
     */
    @Test
    public void prettyPrintRepositoriesPrintsTheExpectedClosure()
    {
        // Given
        List<RepositorySpec> aSpecs = new ArrayList<>();

        String aUrl1= "http://repo.acme.com/m1";
        aSpecs.add(new MavenRepositorySpec("mvn", aUrl1));

        String aUrl2= "http://repo.acme.com/m2";
        String aUserName = "the.user", aPassword = "pwd2";
        MavenRepositorySpec aSpec = new MavenRepositorySpec("mvn", aUrl2);
        aSpec.setCredentials(aUserName, aPassword);
        aSpecs.add(aSpec);

        // Then
        String[] aExpected = {
            "repositories",
            "{",
            "  maven",
            "  {",
            "    url '" + aUrl1 + '\'',
            "  }",
            "  maven",
            "  {",
            "    url '" + aUrl2 + '\'',
            "    credentials",
            "    {",
            "      username = '" + aUserName + '\'',
            "      password = '" + aPassword + '\'',
            "    }",
            "  }",
            "}"
        };

        assertPrintAction(p -> RepositorySpec.prettyPrintRepositories(p, aSpecs), aExpected);
    }
}
