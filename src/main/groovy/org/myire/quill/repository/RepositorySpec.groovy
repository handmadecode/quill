/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.repository

/**
 * Base class for artifact repository specifications.
 */
class RepositorySpec
{
    final String name;
    final String url;
    CredentialsSpec credentials;


    /**
     * Create a new {@code RepositorySpec}.
     *
     * @param pName The repository's name.
     * @param pUrl  The repository's url.
     */
    RepositorySpec(String pName, String pUrl)
    {
        name = pName;
        url = pUrl;
    }


    /**
     * Set the credentials to use when accessing the repository.
     *
     * @param pUserName The user name.
     * @param pPassword The password.
     */
    void setCredentials(String pUserName, String pPassword)
    {
        credentials = new CredentialsSpec(pUserName, pPassword);
    }
}
