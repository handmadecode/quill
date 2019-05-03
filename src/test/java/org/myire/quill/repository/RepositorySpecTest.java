/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.repository;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * Common unit tests for subclasses of {@code org.myire.quill.repository.RepositorySpec}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
abstract public class RepositorySpecTest
{
    /**
     * Create an instance of the subclass to test.
     *
     * @param pName The repository name to pass to the constructor.
     * @param pUrl  The url to pass to the constructor.
     *
     * @return  A new instance of the subclass to test.
     */
    abstract protected RepositorySpec createInstance(String pName, String pUrl);


    /**
     * The constructor should throw a {@code NullPointerException} when passed a null name argument.
     */
    @Test(expected = NullPointerException.class)
    public void ctorThrowsForNullName()
    {
        createInstance(null, "/x/y");
    }


    /**
     * {@code getName()} should return the name passed to the constructor.
     */
    @Test
    public void getNameReturnsNamePassedToCtor()
    {
        // Given
        String aName = "repo";

        // When
        RepositorySpec aSpec = createInstance(aName, "/");

        // Then
        assertEquals(aName, aSpec.getName());
    }


    /**
     * {@code getUrl()} should return the url passed to the constructor.
     */
    @Test
    public void getUrlReturnsUrlPassedToCtor()
    {
        // Given
        String aUrl = "http://repo2.mycompany.com/maven2";

        // When
        RepositorySpec aSpec = createInstance("rep", aUrl);

        // Then
        assertEquals(aUrl, aSpec.getUrl());

        // When
        aSpec = createInstance("rep", null);

        // Then
        assertNull(aSpec.getUrl());
    }


    /**
     * {@code getCredentials()} should return null by default and the last credentials passed to
     * {@code setCredentials()}.
     */
    @Test
    public void getCredentialsReturnsTheExpectedValue()
    {
        // Given
        String aUserName1 = "usr", aUserName2 = "ussr", aPassword = "qwerty";
        RepositorySpec aSpec = createInstance("r", "u");

        // Then (a repository has no credentials by default).
        assertNull(aSpec.getCredentials());

        // When
        aSpec.setCredentials(aUserName1, aPassword);

        // Then
        assertEquals(aUserName1, aSpec.getCredentials().getUserName());
        assertEquals(aPassword, aSpec.getCredentials().getPassword());

        // When
        aSpec.setCredentials(aUserName2, null);

        // Then
        assertEquals(aUserName2, aSpec.getCredentials().getUserName());
        assertNull(aSpec.getCredentials().getPassword());

        // When
        aSpec.setCredentials(null, aPassword);

        // Then (the credentials should be cleared when the user name is set to null).
        assertNull(aSpec.getCredentials());
    }
}
