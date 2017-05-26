/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.maven

import org.myire.quill.dependency.DependencySpec
import org.myire.quill.repository.MavenRepositorySpec


/**
 * An {@code EffectivePomLoader} loads the effective pom from a Maven pom file and retrieves (parts
 * of) its contents.
 */
interface EffectivePomLoader
{
    /**
     * Specify the pom file to load and any settings file to apply when loading the effective pom.
     *
     * @param pPomFile      The pom file.
     * @param pSettingsFile Any settings file to use. If null the default Maven settings will be
     *                      used.
     *
     * @throws NullPointerException if {@code pPomFile} is null.
     */
    void init(File pPomFile, File pSettingsFile);

    /**
     * Get the dependencies from the effective pom, possibly loading it first.
     *
     * @return  A collection of {@code DependencySpec}, one for each dependency in the effective
     *          pom. The returned collection will never be null but may be empty. The Maven scope
     *          names will be used as configuration names in the specs.
     */
    Collection<DependencySpec> getDependencies();

    /**
     * Get the repositories from the effective pom, possibly loading it first.
     *
     * @return  A collection of {@code MavenArtifactRepository}, one for each repository in the pom.
     *          The returned collection will never be null but may be empty.
     */
    Collection<MavenRepositorySpec> getRepositories();

    /**
     * Get the group ID from the effective pom, possibly loading it first.
     *
     * @return  The project model's group ID.
     */
    String getGroupId();

    /**
     * Get the artifact ID from the effective pom, possibly loading it first.
     *
     * @return  The project model's artifact ID.
     */
    String getArtifactId();

    /**
     * Get the version string from the effective pom, possibly loading it first.
     *
     * @return  The project model's version string.
     */
    String getVersion();
}
