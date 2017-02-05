/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.repository

import org.gradle.api.Action
import org.gradle.api.artifacts.repositories.PasswordCredentials


/**
 * Specification of a username and password.
 */
class CredentialsSpec implements Action<PasswordCredentials>
{
    final String username;
    final String password;


    /**
     * Create a new {@code CredentialsSpec}.
     *
     * @param pUserName The user name.
     * @param pPassword The password.
     */
    CredentialsSpec(String pUserName, String pPassword)
    {
        username = pUserName;
        password = pPassword;
    }


    /**
     * Apply the values of this specification to a {@code PasswordCredentials}.
     *
     * @param pCredentials  The instance to set the values of.
     */
    @Override
    void execute(PasswordCredentials pCredentials)
    {
        pCredentials.username = username;
        pCredentials.password = password;
    }
}
