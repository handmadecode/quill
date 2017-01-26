/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.repository

import org.gradle.api.Action
import org.gradle.api.artifacts.repositories.MavenArtifactRepository


/**
 * Specification of a Maven artifact repository.
 */
class MavenRepositorySpec extends RepositorySpec implements Action<MavenArtifactRepository>
{
    /**
     * Create a new {@code RepositorySpec}.
     *
     * @param pName The repository's name.
     * @param pUrl  The repository's url.
     */
    MavenRepositorySpec(String pName, String pUrl)
    {
        super(pName, pUrl)
    }


    /**
     * Apply the values of this specification to a {@code MavenArtifactRepository}.
     *
     * @param pRepository   The instance to set the values of.
     */
    @Override
    void execute(MavenArtifactRepository pRepository)
    {
        pRepository.name = name;
        pRepository.url = url;
        if (credentials != null)
            pRepository.credentials(credentials);
    }
}
