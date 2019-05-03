/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.repository;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static org.myire.quill.common.PrettyPrintableTests.assertPrettyPrint;


/**
 * Unit tests for {@code org.myire.quill.repository.CredentialsSpec}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class CredentialsSpecTest
{
    /**
     * The constructor should throw a {@code NullPointerException} when passed a null name argument.
     */
    @Test(expected = NullPointerException.class)
    public void ctorThrowsForNullName()
    {
        new CredentialsSpec(null, "pwd");
    }


    /**
     * {@code getUserName()} should return the name passed to the constructor.
     */
    @Test
    public void getUserNameReturnsNamePassedToCtor()
    {
        // Given
        String aName = "user1";

        // When
        CredentialsSpec aSpec = new CredentialsSpec(aName, null);

        // Then
        assertEquals(aName, aSpec.getUserName());
    }


    /**
     * {@code getPassword()} should return the password passed to the constructor.
     */
    @Test
    public void getPasswordReturnsPasswordPassedToCtor()
    {
        // Given
        String aPassword = "123";

        // When
        CredentialsSpec aSpec = new CredentialsSpec("u", aPassword);

        // Then
        assertEquals(aPassword, aSpec.getPassword());

        // When
        aSpec = new CredentialsSpec("u", null);

        // Then
        assertNull(aSpec.getPassword());
    }


    /**
     * {@code prettyPrint()} should print a closure with the user name and password.
     */
    @Test
    public void prettyPrintPrintsUserNameAndPassword()
    {
        // Given
        String aUserName = "user 1", aPassword = "the quick fox";
        CredentialsSpec aSpec = new CredentialsSpec(aUserName, aPassword);

        // Then
        String[] aExpected = {
            "credentials",
            "{",
            "  username = '" + aUserName + '\'',
            "  password = '" + aPassword + '\'',
            "}"
        };

        // Then
        assertPrettyPrint(aSpec, aExpected);
    }


    /**
     * {@code prettyPrint()} should print a closure with the user name only if the password is null.
     */
    @Test
    public void prettyPrintOmitsNullPassword()
    {
        // Given
        String aUserName = "a user";
        CredentialsSpec aSpec = new CredentialsSpec(aUserName, null);

        String[] aExpected = {
            "credentials",
            "{",
            "  username = '" + aUserName + '\'',
            "}"
        };

        // Then
        assertPrettyPrint(aSpec, aExpected);
    }
}
